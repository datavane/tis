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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.noggit.JSONParser;
import org.noggit.ObjectBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HttpUtils {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

	@SuppressWarnings("all")
	public static ProcessResponse processResponse(InputStream stream, IMsgProcess msgProcess) {
		ProcessResponse result = new ProcessResponse();
		Object resp = null;
		String respBody = null;
		try {
			respBody = IOUtils.toString(stream, "utf8");
			resp = ObjectBuilder.getVal(new JSONParser(respBody));
			result.respBody = respBody;
			result.result = resp;
		} catch (Exception pe) {
			throw new RuntimeException("Expected JSON response from server but received: " + respBody
					+ "\nTypically, this indicates a problem with the Solr server; check the Solr server logs for more information.");
		}
		Map<String, Object> json = null;
		if (resp != null && resp instanceof Map) {
			json = (Map<String, Object>) resp;
		} else {
			throw new RuntimeException("Expected JSON object in response but received " + resp);
		}

		Long statusCode = asLong("/responseHeader/status", json);
		if (statusCode == -1) {
			// addErrorMessage(context, "Unable to determine outcome of GET
			// request! Response: " + json);
			msgProcess.equals("Unable to determine outcome of GET request! Response: " + json);
			result.success = false;
			return result;
		} else if (statusCode != 0) {
			String errMsg = asString("/error/msg", json);
			if (errMsg == null)
				errMsg = String.valueOf(json);
			// addErrorMessage(context, errMsg);
			logger.error(errMsg);
			msgProcess.err(errMsg);
			result.success = false;
			return result;
		} else {
			// make sure no "failure" object in there either
			Object failureObj = json.get("failure");
			if (failureObj != null) {
				if (failureObj instanceof Map) {
					Object err = ((Map) failureObj).get("");
					if (err != null) {
						// addErrorMessage(context, err.toString());
						logger.error(err.toString());
						msgProcess.err(err.toString());
					} // throw new SolrServerException(err.toString());
				}
				// addErrorMessage(context, failureObj.toString());
				logger.error(failureObj.toString());
				msgProcess.err(failureObj.toString());
				result.success = false;
				return result;
			}
		}

		result.success = true;
		return result;
	}

	public static class ProcessResponse {
		public boolean success;
		public Object result;
		public String respBody;
	}

	public interface IMsgProcess {
		public void err(String content);

	}

	/**
	 * Helper function for reading a String value from a JSON Object tree.
	 */
	private static String asString(String jsonPath, Map<String, Object> json) {
		return pathAs(String.class, jsonPath, json);
	}

	/**
	 * Helper function for reading a Long value from a JSON Object tree.
	 */
	private static Long asLong(String jsonPath, Map<String, Object> json) {
		return pathAs(Long.class, jsonPath, json);
	}

	@SuppressWarnings("unchecked")
	private static <T> T pathAs(Class<T> clazz, String jsonPath, Map<String, Object> json) {
		T val = null;
		Object obj = atPath(jsonPath, json);
		if (obj != null) {
			if (clazz.isAssignableFrom(obj.getClass())) {
				val = (T) obj;
			} else {
				// no ok if it's not null and of a different type
				throw new IllegalStateException("Expected a " + clazz.getName() + " at path " + jsonPath + " but found "
						+ obj + " instead! " + json);
			}
		} // it's ok if it is null
		return val;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object atPath(String jsonPath, Map<String, Object> json) {
		if ("/".equals(jsonPath))
			return json;

		if (!jsonPath.startsWith("/"))
			throw new IllegalArgumentException("Invalid JSON path: " + jsonPath + "! Must start with a /");

		Map<String, Object> parent = json;
		Object result = null;
		String[] path = jsonPath.split("(?<![\\\\])/"); // Break on all slashes
														// _not_ preceeded by a
														// backslash
		for (int p = 1; p < path.length; p++) {
			String part = path[p];

			if (part.startsWith("\\")) {
				part = part.substring(1);
			}

			Object child = parent.get(part);
			if (child == null)
				break;

			if (p == path.length - 1) {
				// success - found the node at the desired path
				result = child;
			} else {
				if (child instanceof Map) {
					// keep walking the path down to the desired node
					parent = (Map) child;
				} else {
					// early termination - hit a leaf before the requested node
					break;
				}
			}
		}
		return result;
	}

	// ========================================================================================
	public static <T> T processContent(URL url, StreamProcess<T> process) {
		return ConfigFileContext.processContent(url, process);
	}

	public static <T> T processContent(URL url, StreamProcess<T> process, int maxRetry) {
		return ConfigFileContext.processContent(url, process, maxRetry);
	}

	public static <T> T post(URL url, byte[] content, PostFormStreamProcess<T> process) {
		return ConfigFileContext.processContent(url, content, process);
	}

	public static <T> T post(URL url, String content, PostFormStreamProcess<T> process) {
		return ConfigFileContext.processContent(url, content, process);
	}

	public static <T> T post(URL url, List<PostParam> params, PostFormStreamProcess<T> process) {
		try {
			StringBuffer content = new StringBuffer();
			for (PostParam p : params) {
				content.append(p.key).append("=").append(URLEncoder.encode(p.value, "utf8")).append("&");
			}
			return ConfigFileContext.processContent(url, content.toString(), process);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static class PostParam {

		private final String key;

		private final String value;

		public PostParam(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}
}
