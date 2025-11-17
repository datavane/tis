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

import com.qlangtech.tis.plugin.alert.AlertChannel;
import com.qlangtech.tis.plugin.alert.AlertTemplate;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Email告警渠道
 * 使用Apache Commons Email发送邮件
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class EmailAlertChannel extends AlertChannel {

    private static final Logger logger = LoggerFactory.getLogger(EmailAlertChannel.class);

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String smtpHost;

    @FormField(ordinal = 2, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer smtpPort;

    @FormField(ordinal = 3, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String userName;

    @FormField(ordinal = 4, type = FormFieldType.PASSWORD, validate = {Validator.require})
    public String password;

    @FormField(ordinal = 5, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String from;

    @FormField(ordinal = 6, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String to;

    @FormField(ordinal = 7, type = FormFieldType.ENUM, validate = {})
    public Boolean ssl = false;

    @Override
    public void send(AlertTemplate alertTemplate) {
        if (alertTemplate == null) {
            throw new IllegalArgumentException("alertTemplate can not be null");
        }

        try {
            // 渲染邮件内容
            String emailContent = renderTemplate(alertTemplate);

            // 创建邮件对象
            HtmlEmail email = new HtmlEmail();
            email.setHostName(this.smtpHost);
            email.setSmtpPort(this.smtpPort);
            email.setAuthentication(this.userName, this.password);
            email.setSSLOnConnect(this.ssl != null && this.ssl);
            email.setCharset("UTF-8");

            // 设置发件人
            email.setFrom(this.from);

            // 设置收件人(支持多个,用逗号分隔)
            String[] recipients = this.to.split(",");
            for (String recipient : recipients) {
                String trimmed = recipient.trim();
                if (StringUtils.isNotEmpty(trimmed)) {
                    email.addTo(trimmed);
                }
            }

            // 设置邮件主题和内容
            String subject = alertTemplate.getSubject();
            if (StringUtils.isEmpty(subject)) {
                subject = alertTemplate.getTitle();
            }
            if (StringUtils.isEmpty(subject)) {
                subject = "TIS Flink Job Alert";
            }

            email.setSubject(subject);
            email.setHtmlMsg(emailContent);

            // 发送邮件
            String messageId = email.send();
            logger.info("Email alert sent successfully via channel [{}], messageId: {}",
                       this.name, messageId);

        } catch (EmailException e) {
            logger.error("Failed to send email alert via channel [{}]", this.name, e);
            throw new RuntimeException("Failed to send email alert: " + e.getMessage(), e);
        }
    }

    /**
     * 加载默认的Email告警模板
     */
    public static String loadDefaultTpl() {
        try {
            return IOUtils.toString(
                EmailAlertChannel.class.getResourceAsStream("email-alert-template.vm"),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load default email template", e);
        }
    }

    @TISExtension
    public static class DefaultDescriptor extends AlertChannelDescDesc {
        @Override
        public String getDisplayName() {
            return "Email";
        }
    }
}
