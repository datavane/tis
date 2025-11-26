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

package com.qlangtech.tis.plugin.alert.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.plugin.alert.AlertChannel;
import com.qlangtech.tis.plugin.alert.AlertTemplate;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP回调告警渠道
 * 通过HTTP POST请求将告警信息发送到自定义的回调URL
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class HttpCallbackAlertChannel extends AlertChannel {

    private static final Logger logger = LoggerFactory.getLogger(HttpCallbackAlertChannel.class);

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String callbackUrl;

    @FormField(ordinal = 2, type = FormFieldType.TEXTAREA, validate = {})
    public String headers;

    @FormField(ordinal = 3, type = FormFieldType.ENUM, validate = {})
    public Boolean sendAsJson = true;

    @Override
    public void send(AlertTemplate alertTemplate) {
        if (alertTemplate == null) {
            throw new IllegalArgumentException("alertTemplate can not be null");
        }

        try {
            //String requestBody;
            List<HttpUtils.PostParam> postParams = Lists.newArrayList();
            if (this.sendAsJson != null && this.sendAsJson) {
                // 发送JSON格式的AlertTemplate对象
                //  requestBody = JSON.toJSONString(alertTemplate);
                alertTemplate.visitAllProp((key, val) -> {
                    postParams.add(new HttpUtils.PostParam(key, val));
                });
            } else {
                // 发送渲染后的模板内容
                String renderedContent = renderTemplate(alertTemplate);
                //Map<String, String> bodyMap = new HashMap<>();
                postParams.add(new HttpUtils.PostParam("content", renderedContent));
                postParams.add(new HttpUtils.PostParam("jobName", alertTemplate.getJobName()));
                postParams.add(new HttpUtils.PostParam("status", alertTemplate.getStatus()));
                //  requestBody = JSON.toJSONString(bodyMap);
            }
            List<ConfigFileContext.Header> headers = Lists.newArrayList();
            // 添加自定义请求头
            if (StringUtils.isNotEmpty(this.headers)) {
                String[] headerPairs = this.headers.split("\n");
                for (String headerPair : headerPairs) {
                    String[] kv = headerPair.trim().split(":", 2);
                    if (kv.length == 2) {
                        // conn.setRequestProperty(kv[0].trim(), kv[1].trim());
                        headers.add(new ConfigFileContext.Header(kv[0].trim(), kv[1].trim()));
                    }
                }
            }
            // 发送HTTP请求
            sendHttpRequest(this.callbackUrl, postParams, headers);

            logger.info("HTTP callback alert sent successfully via channel [{}] to [{}]",
                    this.name, this.callbackUrl);

        } catch (Exception e) {
            logger.error("Failed to send HTTP callback alert via channel [{}]", this.name, e);
            throw new RuntimeException("Failed to send HTTP callback alert: " + e.getMessage(), e);
        }
    }

//    /**
//     * 发送HTTP POST请求
//     */
//    private void sendHttpRequest(String urlString, String jsonBody) throws Exception {
//        URL url = new URL(urlString);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        try {
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//
//            // 添加自定义请求头
//            if (StringUtils.isNotEmpty(this.headers)) {
//                String[] headerPairs = this.headers.split("\n");
//                for (String headerPair : headerPairs) {
//                    String[] kv = headerPair.trim().split(":", 2);
//                    if (kv.length == 2) {
//                        conn.setRequestProperty(kv[0].trim(), kv[1].trim());
//                    }
//                }
//            }
//
//            conn.setDoOutput(true);
//
//            try (OutputStream os = conn.getOutputStream()) {
//                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
//                os.write(input, 0, input.length);
//            }
//
//            int responseCode = conn.getResponseCode();
//            if (responseCode < 200 || responseCode >= 300) {
//                throw new RuntimeException("HTTP callback returned error code: " + responseCode);
//            }
//
//        } finally {
//            conn.disconnect();
//        }
//    }

    /**
     * 加载默认的HTTP回调告警模板
     */
    public static String loadDefaultTpl() {
//        try {
//            return org.apache.commons.io.IOUtils.toString(
//                HttpCallbackAlertChannel.class.getResourceAsStream("httpcallback-alert-template.vm"),
//                java.nio.charset.StandardCharsets.UTF_8);
//        } catch (java.io.IOException e) {
//            throw new RuntimeException("Failed to load default httpcallback template", e);
//        }

        return com.qlangtech.tis.extension.impl.IOUtils.loadResourceFromClasspath(
                HttpCallbackAlertChannel.class, "httpcallback-alert-template.vm");
    }

    @TISExtension
    public static class DefaultDescriptor extends AlertChannelDescDesc<HttpCallbackAlertChannel> {

        @Override
        public EndType getEndType() {
            return EndType.Http;
        }

        @Override
        protected String verifySuccessMessage(HttpCallbackAlertChannel alertChannel) {
            return "已经成功调用 Http URL：" + alertChannel.callbackUrl;
        }
    }
}
