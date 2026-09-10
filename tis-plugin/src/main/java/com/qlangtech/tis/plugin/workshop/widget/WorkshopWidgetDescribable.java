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
package com.qlangtech.tis.plugin.workshop.widget;

import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.util.List;

/**
 * 内置 Widget 抽象基类：携带所有 Widget 共有的表单字段
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/9
 */
public abstract class WorkshopWidgetDescribable implements IWorkshopWidget {

    /**
     * Widget 标题（画布与配置面板通用）
     */
    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 0, advance = false, validate = {Validator.require})
    public String title;

    /**
     * 输入变量绑定（变量 id 列表，多选；options 由 Descriptor 动态供给）
     */
    @FormField(type = FormFieldType.MULTI_SELECTABLE, ordinal = 1, advance = false, validate = {Validator.require})
    public List<String> inputVariables;

    /**
     * 输出变量绑定
     */
    @FormField(type = FormFieldType.MULTI_SELECTABLE, ordinal = 2, advance = false, validate = {Validator.require})
    public List<String> outputVariables;

    /**
     * 高级配置折叠区：自定义 CSS class 等
     */
    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 99, advance = true, validate = {Validator.require})
    public String cssClass;
}