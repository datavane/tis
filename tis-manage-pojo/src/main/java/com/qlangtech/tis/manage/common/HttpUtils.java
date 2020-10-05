/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.qlangtech.tis.ajax.AjaxResult;
import com.qlangtech.tis.manage.common.ConfigFileContext.HTTPMethod;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2014年11月4日上午11:24:38
 */
public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public interface IMsgProcess {

        public void err(String content);
    }

    // =================================================================
    // >> 测试环境使用
    public static void addMockGlobalParametersConfig() {
        addMockApply(1, "global_parameters_config_action", "global_params.json");
    }

    public static void addMockApply(int order, String testStr, String classpath) {
        addMockApply(order, testStr, classpath, HttpUtils.class);
    }

    public static void addMockApply(int order, String testStr, String classpath, Class<?> clazz) {
        addMockApply(testStr, new ClasspathRes(order, classpath, clazz));
    }

    public static void addMockApply(String testStr, IClasspathRes classpathRes) {
        if (HttpUtils.mockConnMaker == null) {
            HttpUtils.mockConnMaker = new DefaultMockConnectionMaker();
        }
        HttpUtils.mockConnMaker.resourceStore.put(testStr, classpathRes);
    }

    public static DefaultMockConnectionMaker mockConnMaker;

    public static <T> T processContent(String urlString, StreamProcess<T> process) {
        try {
            URL url = new URL(urlString);
            return ConfigFileContext.processContent(url, process);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T get(URL url, StreamProcess<T> process) {
        return ConfigFileContext.processContent(url, process);
    }

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
        return process(url, params, process, HTTPMethod.POST);
    }

    public static <T> T process(URL url, List<PostParam> params, PostFormStreamProcess<T> process, HTTPMethod httpMethod) {
        return ConfigFileContext.processContent(url, process, httpMethod, process.getContentType().serializeParams(params), 1);
    }

    public static <T> T post(String urlString, List<PostParam> params, PostFormStreamProcess<T> process) {
        try {
            URL url = new URL(urlString);
            return post(url, params, process);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T delete(String urlString, List<PostParam> params, PostFormStreamProcess<T> process) {
        try {
            return process(new URL(urlString), params, process, HTTPMethod.DELETE);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T put(String urlString, List<PostParam> params, PostFormStreamProcess<T> process) {
        try {
            return process(new URL(urlString), params, process, HTTPMethod.PUT);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> AjaxResult<T> soapRemote(final String url, Class<T> clazz) {
        return soapRemote(url, Collections.emptyList(), clazz, true);
    }

    public static <T> AjaxResult<T> soapRemote(final String url, List<PostParam> params, Class<T> clazz) {
        return soapRemote(url, params, clazz, true);
    }

    /**
     * 向console节点发送http请求，并且按照console节点返回信息的格式反序列化
     *
     * @param url
     * @param params
     * @param clazz
     * @param errorShortCircuit
     * @return
     */
    public static <T> AjaxResult<T> soapRemote(final String url, List<PostParam> params, Class<T> clazz, boolean errorShortCircuit) {
        return HttpUtils.post(url, params, new PostFormStreamProcess<AjaxResult<T>>() {

            @Override
            public ContentType getContentType() {
                return ContentType.Application_x_www_form_urlencoded;
            }

            @Override
            public AjaxResult<T> p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                AjaxResult<T> r = new AjaxResult<>();
                JSONObject result = null;
                try {
                    result = JSON.parseObject(IOUtils.toString(stream, TisUTF8.get()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                JSONArray errors = null;
                JSONArray msgs = null;
                r.setSuccess(result.getBoolean(IAjaxResult.KEY_SUCCESS));
                if (!r.isSuccess()) {
                    errors = result.getJSONArray(IAjaxResult.KEY_ERROR_MSG);
                    r.setErrormsg(errors.stream().map((rr) -> {
                        return (String) rr;
                    }).collect(Collectors.toList()));
                    if (errorShortCircuit) {
                        throw new IllegalStateException(Joiner.on("\n").join(r.getErrormsg()));
                    }
                }
                msgs = result.getJSONArray(IAjaxResult.KEY_MSG);
                r.setMsg(msgs.stream().map((m) -> (String) m).collect(Collectors.toList()));
                if (clazz == Void.class) {
                    return r;
                }
                if (!result.containsKey(IAjaxResult.KEY_BIZRESULT)) {
                    return null;
                }
                r.setBizresult(result.getObject(IAjaxResult.KEY_BIZRESULT, clazz));
                return r;
            }
        });
    }

    public static class PostParam {

        private final String key;

        private final Object value;

        public PostParam(String key, Object value) {
            super();
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return String.valueOf(this.value);
        }
    }

    public static void main(String[] args) throws Exception {
        URL url = new URL("http://build-task-soa_daily_10-1-7-71:8090/druid/indexer/v1/action");
        url.openStream();
    }

    public static class DefaultMockConnectionMaker implements MockConnectionMaker {

        private final Map<String, IClasspathRes> /**
         * url test
         */
                resourceStore = Maps.newHashMap();

        @Override
        public MockHttpURLConnection create(URL url, List<ConfigFileContext.Header> heads, HTTPMethod method, byte[] content) {
            for (Map.Entry<String, IClasspathRes> entry : resourceStore.entrySet()) {
                if (StringUtils.indexOf(url.toString(), entry.getKey()) > -1) {
                    return createConnection(heads, method, content, entry.getValue());
                }
            }
            return null;
        }

        protected MockHttpURLConnection createConnection(List<ConfigFileContext.Header> heads
                , HTTPMethod method, byte[] content, IClasspathRes cpRes) {
            return new MockHttpURLConnection(cpRes.getResourceAsStream());
        }
    }

    public interface IClasspathRes {
        InputStream getResourceAsStream();
    }

    public static class ClasspathRes implements IClasspathRes {

        public final int order;

        private final String classpath;

        private final Class<?> relClass;

        public ClasspathRes(int order, String classpath, Class<?> relClass) {
            this.classpath = classpath;
            this.relClass = relClass;
            this.order = order;
        }

        public InputStream getResourceAsStream() {
            return getResourceAsStream(classpath);
        }

        public InputStream getResourceAsStream(String cpResName) {
            return this.relClass.getResourceAsStream(cpResName);
        }
    }
}
