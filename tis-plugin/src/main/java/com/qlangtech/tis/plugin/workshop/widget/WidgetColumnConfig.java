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

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

/**
 * 列/行的结构化配置，供 @SubForm 驱动子表单渲染。
 * 用于 ObjectTable、PropertyList 等 Widget 的列/行定义，
 * 不再让用户手写 JSON 数组字符串。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/9
 */
public class WidgetColumnConfig implements Describable<WidgetColumnConfig> {

    /** 数据属性名（对应对象上的 property key），作为 SubForm 表行的唯一标识 */
    @FormField(identity = true, ordinal = 0, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String prop;

    /** 列/行在 UI 上的显示标签 */
    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String label;

    /** 列宽（像素），仅 ObjectTable 使用 */
    @FormField(ordinal = 2, type = FormFieldType.INT_NUMBER)
    public Integer width;

    /** 格式化方式 */
    @FormField(ordinal = 3, type = FormFieldType.ENUM, advance = true)
    public ColumnFormat format;

    /** 文本对齐方式 */
    @FormField(ordinal = 4, type = FormFieldType.ENUM, advance = true)
    public ColumnAlign align;

    /** 是否可排序 */
    @FormField(ordinal = 5, type = FormFieldType.ENUM, advance = true)
    public Boolean sortable;

    /** 单元格格式化方式 */
    public enum ColumnFormat {
        text("纯文本"), number("数值"), date("日期"), boolean_("布尔");
        public final String label;
        ColumnFormat(String label) { this.label = label; }
    }

    /** 文本对齐方式 */
    public enum ColumnAlign {
        left("左对齐"), center("居中"), right("右对齐");
        public final String label;
        ColumnAlign(String label) { this.label = label; }
    }

    @TISExtension
    public static class DftDescriptor extends Descriptor<WidgetColumnConfig> {
    }
}