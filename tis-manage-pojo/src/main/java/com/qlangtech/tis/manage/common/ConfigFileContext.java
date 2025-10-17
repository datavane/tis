/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.manage.common;

import com.google.common.collect.Lists;
import com.qlangtech.tis.lang.ErrorValue;
import com.qlangtech.tis.lang.TisException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.qlangtech.tis.lang.TisException.ErrorCode.HTTP_CONNECT_FAILD;

/**
 * 推送 信息到zk上
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2011-12-23
 */
public class ConfigFileContext {

    public static final String KEY_HEAD_FILE_MD5 = "filemd5";

    public static final String KEY_HEAD_LAST_UPDATE = "lastupdate";
    public static final String KEY_HEAD_FILE_NOT_EXIST = "notexist";

    public static final String KEY_HEAD_FILE_SIZE = "content-size";

    public static final String KEY_HEAD_FILE_DOWNLOAD = "download";

    public static final String KEY_HEAD_FILES = "dirlist";

//    public static int getPort(RunEnvironment runEnvir) {
//        return (runEnvir == RunEnvironment.DAILY ? 8080 : 7001);
//    }

    private static final int DEFAULT_MAX_CONNECT_RETRY_COUNT = 1;

    private static final Logger logger = LoggerFactory.getLogger(ConfigFileContext.class);

    public static void main(String[] arg) throws Exception {
    }

    public static byte[] getBytesContent(Integer bizid, Integer appid, Short groupIndex, Short runtimeEnvironment, final PropteryGetter getter, String terminatorRepository, final String md5ValidateCode) throws MalformedURLException, IOException {
        URL url = new URL(terminatorRepository + "/download/publish/" + bizid + "/" + appid + "/group" + groupIndex + "/r" + runtimeEnvironment + "/" + getter.getFileName());
        return processContent(url, new PostFormStreamProcess<byte[]>() {

            @Override
            public ContentType getContentType() {
                return null;
            }

            @Override
            public byte[] p(int status, InputStream stream, Map<String, List<String>> getHeaderFields) {
                final String remoteMd5 = md5ValidateCode;
                List<String> md5 = getHeaderFields.get(KEY_HEAD_FILE_MD5);
                Optional<String> filemd5 = md5.stream().findFirst();
                if (!filemd5.isPresent()) {
                    throw new IllegalStateException("head key:" + KEY_HEAD_FILE_MD5 + " is not exist in response header");
                }
                if (!StringUtils.equalsIgnoreCase(remoteMd5, filemd5.get())) {
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

    public static <T> T processContent(URL url, StreamProcess<T> process, int retryCount) {
        return processContent(url, process, HTTPMethod.GET, null, /* content */        retryCount);
    }

    public static <T> T processContent(URL url, StreamProcess<T> process, HTTPMethod method, byte[] content, final int maxRetry) {
        InputStream reader = null;
        int retryCount = 0;
        while (true) {
            try {
                HttpURLConnection conn = getNetInputStream(url, process, method, content);
                try {
                    reader = conn.getInputStream();
                } catch (IOException e) {
                    InputStream errStream = null;
                    try {
                        errStream = conn.getErrorStream();
                        process.error(conn.getResponseCode(), errStream, e);
                        return null;
                    } finally {
                        IOUtils.closeQuietly(errStream);
                    }
                }
                return process.p(conn, reader);
            } catch (Exception e) {
                if (++retryCount >= maxRetry) {
                    throw TisException.create(ErrorValue.create(HTTP_CONNECT_FAILD, Collections.emptyMap())
                            , "maxRetry:" + maxRetry + ",url:" + url.toString(), e);
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

    private static HttpURLConnection getNetInputStream(URL url, StreamProcess streamProcess, HTTPMethod method, byte[] content) throws IOException {
        List<Header> heads = streamProcess.getHeaders();

        if (HttpUtils.mockConnMaker != null) {
            HttpURLConnection conn = HttpUtils.mockConnMaker.create(url, heads, method, content);
            if (conn != null) {
                return conn;
            }
        }
        HttpURLConnection conn = null;
        HttpURLConnection.setFollowRedirects(false);
        conn = (HttpURLConnection) url.openConnection();
        streamProcess.preSet(conn);
        // 设置15秒超时
        conn.setConnectTimeout(15 * 1000);
        conn.setReadTimeout((int) streamProcess.getSocketReadTimeout().toMillis());
        conn.setRequestMethod(method.name());
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
        for (Header h : heads) {
            conn.addRequestProperty(h.getKey(), h.getValue());
        }
        if (content != null) {
            conn.setDoOutput(true);
            OutputStream output = conn.getOutputStream();
            IOUtils.write(content, output);
            output.flush();
        }
        conn.connect();
        if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IllegalStateException("ERROR_CODE=" + conn.getResponseCode() + "  request faild, revsion center apply url :" + url);
        }
        return conn;
    }

    public static class StreamErrorProcess {
        public void error(int status, InputStream errstream, IOException e) throws Exception {
            logger.error("error code:" + status + "\n" + ((errstream != null) ? IOUtils.toString(errstream, TisUTF8.get()) : "errstream is null"));
            throw new Exception(e);
        }
    }

    public abstract static class StreamProcess<T> extends StreamErrorProcess {

        public static String HEADER_KEY_GET_FILE_META = "get_file_meta";

        protected static final List<Header> HEADER_TEXT_HTML = Lists.newArrayList(new Header("content-type", "text/html"));

        protected static final List<Header> HEADER_GET_META;


        static {
            List<Header> tmpList = Lists.newArrayList(HEADER_TEXT_HTML);
            tmpList.add(new Header(HEADER_KEY_GET_FILE_META, String.valueOf(true)));
            HEADER_GET_META = Collections.unmodifiableList(tmpList);
        }

        protected static void setAuthorization(HttpURLConnection conn, String userName, String password) {
            String userpass = userName + ":" + StringUtils.trimToEmpty(password);
            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
            conn.setRequestProperty("Authorization", basicAuth);
        }

        /**
         * 可以做在提交之前 先设置用户名密码之类的
         *
         * @param conn
         * @throws IOException
         */
        public void preSet(HttpURLConnection conn) throws IOException {
//            URL url = new URL(“location address”);
//            URLConnection uc = url.openConnection();
//            String userpass = username + ":" + password;
//            String basicAuth = "Basic " + new String(Base64.getEncoder().encode(userpass.getBytes()));
//            uc.setRequestProperty ("Authorization", basicAuth);
//            InputStream in = uc.getInputStream();
        }

        public T p(HttpURLConnection conn, InputStream stream) throws IOException {
            return p(conn.getResponseCode(), stream, conn.getHeaderFields());
        }

        /**
         * @param status
         * @param stream
         * @param
         * @return
         */
        public abstract T p(int status, InputStream stream, Map<String, List<String>> headerFields) throws IOException;


        public List<Header> getHeaders() {
            return HEADER_TEXT_HTML;
        }

        public static final Duration dftSocketReadTimeout = Duration.ofSeconds(15);

        /**
         * socket read 超时时间 <br>
         * 单位:秒
         *
         * @return
         */
        public Duration getSocketReadTimeout() {
            return dftSocketReadTimeout;
        }
    }

    public enum HTTPMethod {

        GET,
        POST,
        HEAD,
        OPTIONS,
        PUT,
        DELETE,
        TRACE
    }

    // //////////////////////////////////////////////////////////////////////////////////

    /**
     * 发送json请求给远端服务器
     *
     * @param url
     * @param content
     * @param process
     * @return
     */
    public static <T> T processContent(URL url, String content, PostFormStreamProcess<T> process) {
        return processContent(url, content.getBytes(Charset.defaultCharset()), process);
    }

    public static <T> T processContent(URL url, byte[] content, PostFormStreamProcess<T> process) {
        return processContent(url, process, HTTPMethod.POST, content, 1);
    }

    static <T> T processDeleteContent(URL url, String content, PostFormStreamProcess<T> process) {
        return processDeleteContent(url, content.getBytes(Charset.defaultCharset()), process);
    }

    static <T> T processDeleteContent(URL url, byte[] content, PostFormStreamProcess<T> process) {
        return processContent(url, process, HTTPMethod.DELETE, content, 1);
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
