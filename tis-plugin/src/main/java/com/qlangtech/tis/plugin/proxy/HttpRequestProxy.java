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
package com.qlangtech.tis.plugin.proxy;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.manage.common.IURLConnectionSender;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.realtime.utils.NetUtils;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.util.IPluginContext;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/12/12
 */
public class HttpRequestProxy extends ParamsConfig implements IURLConnectionSender, IPluginStore.AfterPluginSaved,
        IPluginStore.AfterPluginDeleted {

    public static final String ConfigType = "Http-Proxy";
    public static final String FIELD_PROXY_PORT = "proxyPort";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {Validator.require,
            Validator.identity})
    public String name;

    @FormField(type = FormFieldType.ENUM, ordinal = 1, validate = {Validator.require})
    public Boolean enable;

    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 2, validate = {Validator.require, Validator.hostWithoutPort})
    public String proxyHost;

    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 3, validate = {Validator.require, Validator.integer})
    public Integer proxyPort;

    @FormField(ordinal = 4, validate = {Validator.require, Validator.integer})
    public HttpRequestProxyAuth auth;

    private transient ProxySelector _proxy;

    private ProxySelector getProxy() {
        if (this._proxy == null) {
            _proxy = ProxySelector.of(new InetSocketAddress(this.proxyHost, this.proxyPort));// new Proxy(Proxy.Type
            // .HTTP, new InetSocketAddress(proxyHost, proxyPort));
        }
        return _proxy;
    }

    @Override
    public HttpRequestProxy createConfigInstance() {
        return this;
    }

    @Override
    public String identityValue() {
        return name;
    }

    @Override
    public void afterSaved(IPluginContext pluginContext, Optional<Context> context) {
        IURLConnectionSender.sender[0] = enable ? this : disableHttpProxy();
        this._proxy = null;
    }

    @Override
    public HttpClient.Builder setHttpClientBuilder(HttpClient.Builder clientBuilder, boolean skipProxy) {
        if (skipProxy) {
            return clientBuilder;
        }
        clientBuilder.proxy(this.getProxy());
        auth.setHttpClientBuilder(clientBuilder);
        return clientBuilder;
    }

    /**
     * 插件被删除之后
     *
     * @param pluginContext
     * @param context
     */
    @Override
    public void afterDeleted(IPluginContext pluginContext, Optional<Context> context) {
        disableHttpProxy();
    }

    private static IURLConnectionSender disableHttpProxy() {
        IURLConnectionSender.sender[0] = new IURLConnectionSender() {
            @Override
            public HttpClient.Builder setHttpClientBuilder(HttpClient.Builder clientBuilder, boolean skipProxy) {
                return clientBuilder;
            }
        };
        return IURLConnectionSender.sender[0];
    }

    @TISExtension
    public static class DefaultHttpRequestProxyDescriptor extends BasicParamsConfigDescriptor implements IEndTypeGetter {
        public DefaultHttpRequestProxyDescriptor() {
            super(ConfigType);
        }

        @Override
        public EndType getEndType() {
            return EndType.HttpProxy;
        }

        public boolean validateProxyHost(IFieldErrorHandler msgHandler, Context context, String fieldName,
                                         String value) {

            if (!NetUtils.isReachable(value)) {
                msgHandler.addFieldError(context, fieldName, "无法连接");
                return false;
            }

            return true;
        }


        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.verify(msgHandler, context, postFormVals);
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            HttpRequestProxy proxy = postFormVals.newInstance();

            if (!NetUtils.isPortAvailable(proxy.proxyHost, proxy.proxyPort)) {
                msgHandler.addFieldError(context, FIELD_PROXY_PORT, "端口不可用");
                return false;
            }

            // Test URL to verify proxy connection
            final String testUrl = "http://www.baidu.com";

            try {
                // Create HttpClient with proxy configuration
                HttpClient.Builder clientBuilder = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10));

                // Configure proxy and authentication
                proxy.setHttpClientBuilder(clientBuilder, false);

                HttpClient client = clientBuilder.build();

                // Create HTTP request
                HttpRequest request =
                        HttpRequest.newBuilder().uri(URI.create(testUrl)).timeout(Duration.ofSeconds(10)).GET().build();

                // Send request to test proxy connection
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Check if response is successful (2xx status code)
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    msgHandler.addErrorMessage(context,
                            "Proxy connection test failed with HTTP status code: " + response.statusCode());
                    return false;
                }

                return true;
            } catch (java.net.ConnectException e) {
                msgHandler.addErrorMessage(context,
                        "Failed to connect to proxy server at " + proxy.proxyHost + ":" + proxy.proxyPort + ". Please"
                                + " check if the proxy server is running and the host/port are correct");
                return false;
            } catch (java.net.SocketTimeoutException e) {
                msgHandler.addErrorMessage(context, "Connection to proxy server timed out. Please check if the proxy "
                        + "server is accessible");
                return false;
            } catch (java.net.UnknownHostException e) {
                msgHandler.addErrorMessage(context,
                        "Unknown proxy host: " + proxy.proxyHost + ". Please verify the " + "proxy hostname");
                return false;
            } catch (java.io.IOException e) {
                msgHandler.addErrorMessage(context, "Proxy connection test failed: " + e.getMessage());
                return false;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                msgHandler.addErrorMessage(context, "Proxy connection test was interrupted");
                return false;
            } catch (Exception e) {
                msgHandler.addErrorMessage(context, "Unexpected error during proxy verification: " + e.getMessage());
                return false;
            }
        }
    }
}
