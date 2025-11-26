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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 企业微信告警渠道
 * 使用企业微信机器人Webhook发送群消息
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/17
 */
public class WeComAlertChannel extends AlertChannel {

    private static final Logger logger = LoggerFactory.getLogger(WeComAlertChannel.class);

    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String webhookUrl;

    @FormField(ordinal = 2, type = FormFieldType.INPUTTEXT, validate = {})
    public String mentionedList;

    @FormField(ordinal = 3, type = FormFieldType.INPUTTEXT, validate = {})
    public String mentionedMobileList;

    @Override
    public void send(AlertTemplate alertTemplate) {
        if (alertTemplate == null) {
            throw new IllegalArgumentException("alertTemplate can not be null");
        }

        try {
            // 渲染消息内容
            String messageContent = renderTemplate(alertTemplate);

            // 构建企业微信消息体
            List<HttpUtils.PostParam> postParams = Lists.newArrayList();
            //JSONObject message = new JSONObject();
            postParams.add(new HttpUtils.PostParam("msgtype", "markdown"));

            JSONObject markdown = new JSONObject();
            markdown.put("content", messageContent);
            postParams.add(new HttpUtils.PostParam("markdown", markdown));

            // 添加@提醒列表
            if (StringUtils.isNotEmpty(this.mentionedList) || StringUtils.isNotEmpty(this.mentionedMobileList)) {
                if (StringUtils.isNotEmpty(this.mentionedList)) {
                    String[] userIds = this.mentionedList.split(",");
                    markdown.put("mentioned_list", userIds);
                }

                if (StringUtils.isNotEmpty(this.mentionedMobileList)) {
                    String[] mobiles = this.mentionedMobileList.split(",");
                    markdown.put("mentioned_mobile_list", mobiles);
                }
            }

            // 发送HTTP请求
            sendHttpRequest(this.webhookUrl, postParams);

            logger.info("WeCom alert sent successfully via channel [{}]", this.name);

        } catch (Exception e) {
            logger.error("Failed to send WeCom alert via channel [{}]", this.name, e);
            throw new RuntimeException("Failed to send WeCom alert: " + e.getMessage(), e);
        }
    }


    /**
     * 加载默认的企业微信告警模板
     */
    public static String loadDefaultTpl() {

        return IOUtils.loadResourceFromClasspath(WeComAlertChannel.class, "wecom-alert-template.vm");
    }

    @TISExtension
    public static class DefaultDescriptor extends AlertChannelDescDesc<WeComAlertChannel> {

        @Override
        public EndType getEndType() {
            return EndType.WeCom;
        }

        @Override
        protected String verifySuccessMessage(WeComAlertChannel alertChannel) {
            return "";
        }
    }
}
