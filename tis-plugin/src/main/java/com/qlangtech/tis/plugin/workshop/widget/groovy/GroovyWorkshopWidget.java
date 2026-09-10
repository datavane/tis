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
package com.qlangtech.tis.plugin.workshop.widget.groovy;

import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.workshop.widget.WorkshopWidgetDescribable;

/**
 * Groovy 脚本驱动的 Widget：用户通过 .groovy 脚本动态定义 Widget 行为与字段。
 * <p>
 * scriptPath 指向 classpath 中的 .groovy 文件，脚本可声明 displayName、renderConfig 以及
 * 自定义表单字段，由 GroovyFieldScriptBridge 加载并注入表单渲染层。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/10
 */
public class GroovyWorkshopWidget extends WorkshopWidgetDescribable {

    /**
     * 相对 classpath 的 .groovy 脚本文件路径（identity 主键）
     */
    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String scriptPath;

    /**
     * 渲染提示符，告知前端采用何种 UI 方案渲染该 Widget
     */
    @FormField(ordinal = 1, type = FormFieldType.ENUM, advance = false)
    public RenderHint renderHint;

    /**
     * Widget 渲染方式枚举
     */
    public enum RenderHint implements DescriptorUseableShortComment {
        generic_form("通用表单"),
        markdown("Markdown"),
        custom_html("自定义 HTML");

        public final String label;

        RenderHint(String label) {
            this.label = label;
        }

        @Override
        public String shortComment() {
            return this.label;
        }
    }

    @TISExtension
    public static class DftDescriptor extends GroovyWidgetDescriptor {

        public DftDescriptor() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "Groovy Widget";
        }
    }
}