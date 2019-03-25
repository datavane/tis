/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.Lists;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/*
 * 推送 信息到zk上
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ConfigFileContext {

	public static int getPort(RunEnvironment runEnvir) {
		return (runEnvir == RunEnvironment.DAILY ? 8080 : 7001);
	}

	private static final int DEFAULT_MAX_CONNECT_RETRY_COUNT = 1;

	private static final Logger logger = LoggerFactory.getLogger(ConfigFileContext.class);

	public static void main(String[] arg) throws Exception {
	}

	public static byte[] getBytesContent(Integer bizid, Integer appid, Short groupIndex, Short runtimeEnvironment,
			final PropteryGetter getter, String terminatorRepository, final String md5ValidateCode)
			throws MalformedURLException, IOException {
		URL url = new URL(terminatorRepository + "/download/publish/" + bizid + "/" + appid + "/group" + groupIndex
				+ "/r" + runtimeEnvironment + "/" + getter.getFileName());
		return processContent(url, new StreamProcess<byte[]>() {

			@Override
			public byte[] p(int status, InputStream stream, String filemd5) {
				final String remoteMd5 = md5ValidateCode;
				if (!StringUtils.equalsIgnoreCase(remoteMd5, filemd5)) {
					throw new IllegalStateException("filemd5:" + filemd5 + " remoteMd5:" + remoteMd5);
				}
				try {
					return IOUtils.toByteArray(stream);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

	public abstract static class ContentProcess {

		public abstract void execute(PropteryGetter getter, byte[] content) throws Exception;
	}

	public static <T> T processContent(URL url, StreamProcess<T> process) {
		return processContent(url, process, DEFAULT_MAX_CONNECT_RETRY_COUNT);
	}

	public static <T> T processContent(URL url, StreamProcess<T> process, final int maxRetry) {
		InputStream reader = null;
		int retryCount = 0;
		while (true) {
			try {
				HttpURLConnection conn = getNetInputStream(url, process.getHeaders(), process);
				try {
					reader = conn.getInputStream();
				} catch (IOException e) {
					InputStream errStream = null;
					try {
						errStream = conn.getErrorStream();
						return process.p(conn.getResponseCode(), errStream, conn.getHeaderField("filemd5"));
					} finally {
						IOUtils.closeQuietly(errStream);
					}
				}
				return process.p(conn.getResponseCode(), reader, conn.getHeaderField("filemd5"));
			} catch (Exception e) {
				if (++retryCount >= maxRetry) {
					throw new RuntimeException("maxRetry:" + maxRetry + ",url:" + url.toString(), e);
				} else {
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e1) {
						throw new RuntimeException(e1);
					}
					logger.warn(e.getMessage(), e);
				}
			} finally {
				try {
					reader.close();
				} catch (Throwable e) {
				}
			}
		}
	}

	private static <T> HttpURLConnection getNetInputStream(URL url, List<Header> heads, StreamProcess<T> process)
			throws MalformedURLException, IOException {
		HttpURLConnection conn = null;
		HttpURLConnection.setFollowRedirects(false);
		conn = (HttpURLConnection) url.openConnection();
		// 设置15秒超时
		conn.setConnectTimeout(15 * 1000);
		conn.setReadTimeout(15 * 1000);
		// conn.setRequestMethod("GET");

		conn.setRequestMethod(process.getHttpMethod());

		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
		for (Header h : heads) {
			conn.addRequestProperty(h.getKey(), h.getValue());
		}
		conn.connect();
		if (// conn.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR||
		conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
			throw new IllegalStateException(
					"ERROR_CODE=" + conn.getResponseCode() + "  request faild, revsion center apply url :" + url);
		}
		return conn;
	}

	public abstract static class StreamProcess<T> {

		protected static final List<Header> HEADER_TEXT_HTML = Lists
				.newArrayList(new Header("content-type", "text/html"));

		// "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE"
		public static final String HTTP_METHOD_GET = "GET";
		public static final String HTTP_METHOD_POST = "POST";
		public static final String HTTP_METHOD_HEAD = "HEAD";

		public static final String HTTP_METHOD_PUT = "PUT";

		public String getHttpMethod() {
			return HTTP_METHOD_GET;
		}

		public abstract T p(int status, InputStream stream, String md5);

		public List<Header> getHeaders() {
			return HEADER_TEXT_HTML;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////
	/**
	 * 发送json请求给远端服务器
	 *
	 * @param url
	 * @param o
	 * @param process
	 * @return
	 */
	public static <T> T processContent(URL url, String content, PostFormStreamProcess<T> process) {
		return processContent(url, content.getBytes(Charset.defaultCharset()), process);
	}

	public static <T> T processContent(URL url, byte[] content, PostFormStreamProcess<T> process) {
		InputStream reader = null;
		HttpURLConnection conn = null;
		try {
			// conn.setRequestProperty("content-type", contentType);
			conn = sendContentBody(url, content, process.getHeaders(), process);
			reader = conn.getInputStream();
			return process.p(conn.getResponseCode(), reader, conn.getHeaderField("filemd5"));
		} catch (Exception e) {
			throw new RuntimeException("url:" + url.toString(), e);
		} finally {
			try {
				reader.close();
			} catch (Throwable e) {
			}
			try {
				conn.disconnect();
			} catch (Throwable e) {
			}
		}
	}

	private static // String
	<T> HttpURLConnection sendContentBody(// String
			URL url, // String
			byte[] content, // String
			List<Header> headers, PostFormStreamProcess<T> process) throws // contentType)
	MalformedURLException, IOException {
		HttpURLConnection conn;
		conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(15 * 1000);
		conn.setReadTimeout(30 * 1000);
		conn.setRequestMethod(process.getHttpMethod());
		conn.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
		// conn.setRequestProperty("content-type", contentType);
		for (Header h : headers) {
			conn.setRequestProperty(h.getKey(), h.getValue());
		}
		conn.setDoOutput(true);
		OutputStream output = conn.getOutputStream();
		// String content = json.toString();
		IOUtils.write(content, output);
		output.flush();
		conn.connect();
		// try {
		if ((conn.getResponseCode() != HttpURLConnection.HTTP_OK
				|| conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED)
				&& conn.getResponseCode() != HttpURLConnection.HTTP_NOT_MODIFIED) {
			try (InputStream result = conn.getErrorStream()) {
				if (result != null) {
					throw new IOException("code:" + conn.getResponseCode() + "request faild, apply url :" + url + "\n"
							+ IOUtils.toString(result, "utf8"));
				}
			}
		}
		return conn;
	}

	public static class Header {

		private final String key;

		private final String value;

		public Header(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public String getValue() {
			return value;
		}
	}
}
