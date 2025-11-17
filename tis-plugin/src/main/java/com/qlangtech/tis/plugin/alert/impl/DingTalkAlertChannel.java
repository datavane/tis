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
import com.qlangtech.tis.plugin.alert.AlertTemplate;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.alert.AlertChannel;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 钉钉告警渠道
 * 使用钉钉机器人Webhook发送群消息
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class DingTalkAlertChannel extends AlertChannel {

    private static final Logger logger = LoggerFactory.getLogger(DingTalkAlertChannel.class);

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String accessToken;

    @FormField(ordinal = 2, type = FormFieldType.INPUTTEXT, validate = {})
    public String secret;

    @FormField(ordinal = 3, type = FormFieldType.ENUM, validate = {})
    public Boolean atAll = false;

    @FormField(ordinal = 4, type = FormFieldType.INPUTTEXT, validate = {})
    public String atMobiles;

    @Override
    public void send(AlertTemplate alertTemplate) {
        if (alertTemplate == null) {
            throw new IllegalArgumentException("alertTemplate can not be null");
        }

        try {
            // 渲染消息内容
            String messageContent = renderTemplate(alertTemplate);

            // 构建钉钉消息体
            JSONObject message = new JSONObject();
            message.put("msgtype", "markdown");

            JSONObject markdown = new JSONObject();
            markdown.put("title", alertTemplate.getTitle() != null ? alertTemplate.getTitle() : "TIS Flink Job Alert");
            markdown.put("text", messageContent);
            message.put("markdown", markdown);

            // 构建@信息
            JSONObject at = new JSONObject();
            at.put("isAtAll", this.atAll != null && this.atAll);

            if (StringUtils.isNotEmpty(this.atMobiles)) {
                String[] mobiles = this.atMobiles.split(",");
                at.put("atMobiles", mobiles);
            }
            message.put("at", at);

            // 计算签名并构建URL
            String webhookUrl = buildWebhookUrl();

            // 发送HTTP请求
            sendHttpRequest(webhookUrl, message.toJSONString());

            logger.info("DingTalk alert sent successfully via channel [{}]", this.name);

        } catch (Exception e) {
            logger.error("Failed to send DingTalk alert via channel [{}]", this.name, e);
            throw new RuntimeException("Failed to send DingTalk alert: " + e.getMessage(), e);
        }
    }

    /**
     * 构建带签名的Webhook URL
     */
    private String buildWebhookUrl() throws Exception {
        String baseUrl = "https://oapi.dingtalk.com/robot/send?access_token=" + this.accessToken;

        // 如果配置了secret,需要计算签名
        if (StringUtils.isNotEmpty(this.secret)) {
            long timestamp = System.currentTimeMillis();
            String stringToSign = timestamp + "\n" + this.secret;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));

            String sign = URLEncoder.encode(Base64.encodeBase64String(signData), "UTF-8");

            return baseUrl + "&timestamp=" + timestamp + "&sign=" + sign;
        }

        return baseUrl;
    }

    /**
     * 发送HTTP POST请求
     */
    private void sendHttpRequest(String urlString, String jsonBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("DingTalk API returned error code: " + responseCode);
            }

        } finally {
            conn.disconnect();
        }
    }

    /**
     * 加载默认的钉钉告警模板
     */
    public static String loadDefaultTpl() {
        try {
            return org.apache.commons.io.IOUtils.toString(
                DingTalkAlertChannel.class.getResourceAsStream("dingtalk-alert-template.vm"),
                java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load default dingtalk template", e);
        }
    }

    @TISExtension
    public static class DefaultDescriptor extends AlertChannelDescDesc {
        @Override
        public String getDisplayName() {
            return "DingTalk";
        }
    }
}
