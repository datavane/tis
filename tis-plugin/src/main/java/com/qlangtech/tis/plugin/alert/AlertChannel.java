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
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * 定义告警渠道，例如：微信，email，weChat，lark，dingtalk等
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/16
 */
public abstract class AlertChannel extends ParamsConfig {
    public static final String KEY_CATEGORY = "AlertChannel";
    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String name;


    @FormField(type = FormFieldType.TEXTAREA, ordinal = 999, validate = {Validator.require})
    public String alertTpl;

    @Override
    public AlertChannel createConfigInstance() {
        return this;
    }

    /**
     * 发送报警消息
     * @param alertTemplate 报警数据模型
     */
    public abstract void send(AlertTemplate alertTemplate);

    /**
     * @deprecated 保留旧方法以兼容旧代码
     */
    @Deprecated
    public void sendAlert() {
        // 默认空实现,子类可以覆盖
    }

    /**
     * 使用Velocity模板渲染报警消息
     * @param alertTemplate 报警数据模型
     * @return 渲染后的消息内容
     */
    protected String renderTemplate(AlertTemplate alertTemplate) {
        if (org.apache.commons.lang.StringUtils.isEmpty(this.alertTpl)) {
            throw new IllegalStateException("alertTpl can not be empty");
        }

        // 创建Velocity上下文
        org.apache.velocity.VelocityContext context = new org.apache.velocity.VelocityContext();

        // 将AlertTemplate的所有字段添加到上下文中
        // 使用反射获取所有字段值
        java.lang.reflect.Field[] fields = AlertTemplate.class.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            try {
                field.setAccessible(true);
                Object value = field.get(alertTemplate);
                if (value != null) {
                    context.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }

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

    public static abstract class AlertChannelDescDesc extends BasicParamsConfigDescriptor {

        public AlertChannelDescDesc() {
            super(KEY_CATEGORY);
        }
    }
}
