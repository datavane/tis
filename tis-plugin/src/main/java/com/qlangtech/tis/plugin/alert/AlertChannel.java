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

package com.qlangtech.tis.plugin.alert;

import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * 定义告警渠道，例如：微信，email，weChat，lark，dingtalk等
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/16
 */
public abstract class AlertChannel extends ParamsConfig {
    public static final String KEY_CATEGORY = "AlertChannel";
    private static final Logger logger = LoggerFactory.getLogger(AlertChannel.class);
    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String name;


    @FormField(type = FormFieldType.TEXTAREA, advance = false, ordinal = 999, validate = {Validator.require})
    public String alertTpl;

    @Override
    public AlertChannel createConfigInstance() {
        return this;
    }

    /**
     * 发送报警消息
     *
     * @param alertTemplate 报警数据模型
     */
    public abstract void send(AlertTemplate alertTemplate);

    protected void sendHttpRequest(String urlString, List<HttpUtils.PostParam> postParams) {
        this.sendHttpRequest(urlString, postParams, Collections.emptyList());
    }

    /**
     * 发送HTTP POST请求
     */
    protected void sendHttpRequest(String urlString, List<HttpUtils.PostParam> postParams, List<ConfigFileContext.Header> appendHeaders) {
        if (CollectionUtils.isEmpty(postParams)) {
            throw new IllegalArgumentException("param postParams can not be empty");
        }
        try {
            URL url = new URL(urlString);

            HttpUtils.post(url, postParams, new PostFormStreamProcess<Void>() {
                @Override
                public ContentType getContentType() {
                    return ContentType.JSON;
                }

                @Override
                public Void p(int status, InputStream stream, Map headerFields) throws IOException {
//                super.p()
//                return response;
                    logger.info(org.apache.commons.io.IOUtils.toString(stream, TisUTF8.get()));
                    return null;
                }

                @Override
                public List<ConfigFileContext.Header> getHeaders() {
                    if (CollectionUtils.isEmpty(appendHeaders)) {
                        return super.getHeaders();
                    }
                    List<ConfigFileContext.Header> headers = new ArrayList<>(super.getHeaders());
                    //headers.add(new ConfigFileContext.Header("Authorization", "Bearer " + getApiKey()));
                    headers.addAll(appendHeaders);
                    return headers;
                }
            });

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        HttpUtils.post()
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
//                throw new RuntimeException("DingTalk API returned error code: " + responseCode);
//            }
//
//        } finally {
//            conn.disconnect();
//        }


//        List<HttpUtils.PostParam> postParams = new ArrayList<>();
//        postParams.add(new HttpUtils.PostParam("model", getModel()));


    }

    /**
     * @deprecated 保留旧方法以兼容旧代码
     */
    @Deprecated
    public void sendAlert() {
        // 默认空实现,子类可以覆盖
    }


    /**
     * 使用Velocity模板渲染报警消息
     *
     * @param alertTemplate 报警数据模型
     * @return 渲染后的消息内容
     */
    protected String renderTemplate(AlertTemplate alertTemplate) {
        if (org.apache.commons.lang.StringUtils.isEmpty(this.alertTpl)) {
            throw new IllegalStateException("alertTpl can not be empty");
        }

        // 创建Velocity上下文
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();


        alertTemplate.visitAllProp(context::put);

//        // 将AlertTemplate的所有字段添加到上下文中
//        // 使用反射获取所有字段值
//        java.lang.reflect.Field[] fields = AlertTemplate.class.getDeclaredFields();
//        for (java.lang.reflect.Field field : fields) {
//            try {
//                field.setAccessible(true);
//                Object value = field.get(alertTemplate);
//                if (value != null) {
//                    context.put(field.getName(), value);
//                }
//            } catch (IllegalAccessException e) {
//                // 忽略无法访问的字段
//            }
//        }

        // 使用DataXCfgGenerator.evaluateTemplate()进行渲染
        return com.qlangtech.tis.datax.impl.DataXCfgGenerator.evaluateTemplate(context, this.alertTpl);
    }

    @Override
    public final String identityValue() {
        return this.name;
    }

    @Override
    protected final Class<AlertChannelDescDesc> getBasicParamsConfigDescriptorClass() {
        return AlertChannelDescDesc.class;
    }

    public static abstract class AlertChannelDescDesc extends BasicParamsConfigDescriptor implements IEndTypeGetter {

        public AlertChannelDescDesc() {
            super(KEY_CATEGORY);
        }

        @Override
        public final String getDisplayName() {
            return String.valueOf(this.getEndType())+"_Chan";
        }
    }
}
