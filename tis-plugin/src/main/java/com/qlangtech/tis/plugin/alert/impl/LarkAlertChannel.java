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

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.plugin.alert.AlertChannel;
import com.qlangtech.tis.plugin.alert.AlertTemplate;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 飞书(Lark)告警渠道
 * 使用飞书机器人Webhook发送群消息
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class LarkAlertChannel extends AlertChannel {

    private static final Logger logger = LoggerFactory.getLogger(LarkAlertChannel.class);

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require, Validator.url})
    public String webhookUrl;

    @FormField(ordinal = 2, type = FormFieldType.PASSWORD, validate = {Validator.require})
    public String secret;

    @FormField(ordinal = 3, type = FormFieldType.ENUM, validate = {Validator.require})
    public Boolean atAll = false;

    @Override
    public void send(AlertTemplate alertTemplate) {
        if (alertTemplate == null) {
            throw new IllegalArgumentException("alertTemplate can not be null");
        }

        try {
            // 渲染消息内容
            String messageContent = renderTemplate(alertTemplate);

            // 构建飞书消息体
            List<HttpUtils.PostParam> postParams = Lists.newArrayList();
            //JSONObject message = new JSONObject();
            postParams.add(new HttpUtils.PostParam("msg_type", "interactive"));

            // 构建卡片内容
            JSONObject card = new JSONObject();

            // 标题
            JSONObject header = new JSONObject();
            JSONObject title = new JSONObject();
            title.put("tag", "plain_text");
            title.put("content", alertTemplate.getTitle() != null ? alertTemplate.getTitle() : "TIS Flink Job Alert");
            header.put("title", title);
            header.put("template", "red");  // 使用红色模板
            card.put("header", header);

            // 内容
            JSONObject content = new JSONObject();
            content.put("tag", "div");
            JSONObject text = new JSONObject();
            text.put("tag", "lark_md");
            text.put("content", messageContent);
            content.put("text", text);

            card.put("elements", new Object[]{content});

            postParams.add(new HttpUtils.PostParam("card", card));

            // 如果配置了secret,需要计算签名
            if (StringUtils.isNotEmpty(this.secret)) {
                long timestamp = System.currentTimeMillis() / 1000;
                String sign = generateSign(timestamp);
                postParams.add(new HttpUtils.PostParam("timestamp", String.valueOf(timestamp)));
                postParams.add(new HttpUtils.PostParam("sign", sign));
            }

            // 发送HTTP请求
            sendHttpRequest(this.webhookUrl, postParams);

            logger.info("Lark alert sent successfully via channel [{}]", this.name);

        } catch (Exception e) {
            logger.error("Failed to send Lark alert via channel [{}]", this.name, e);
            throw new RuntimeException("Failed to send Lark alert: " + e.getMessage(), e);
        }
    }

    /**
     * 生成签名
     */
    private String generateSign(long timestamp) throws Exception {
        String stringToSign = timestamp + "\n" + this.secret;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(stringToSign.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] signData = mac.doFinal(new byte[]{});

        return Base64.encodeBase64String(signData);
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
//            conn.setDoOutput(true);
//
//            try (OutputStream os = conn.getOutputStream()) {
//                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
//                os.write(input, 0, input.length);
//            }
//
//            int responseCode = conn.getResponseCode();
//            if (responseCode != HttpURLConnection.HTTP_OK) {
//                throw new RuntimeException("Lark API returned error code: " + responseCode);
//            }
//
//        } finally {
//            conn.disconnect();
//        }
//    }

    /**
     * 加载默认的飞书告警模板
     */
    public static String loadDefaultTpl() {
        return IOUtils.loadResourceFromClasspath(LarkAlertChannel.class, "lark-alert-template.vm");
    }

    @TISExtension
    public static class DefaultDescriptor extends AlertChannelDescDesc<LarkAlertChannel> {

        @Override
        public EndType getEndType() {
            return EndType.Lark;
        }

        @Override
        protected String verifySuccessMessage(LarkAlertChannel alertChannel) {
            return "已经成功发送消息到飞书";
        }
    }
}
