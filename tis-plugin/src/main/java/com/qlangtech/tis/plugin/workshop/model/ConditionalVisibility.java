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
package com.qlangtech.tis.plugin.workshop.model;

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;

import java.io.Serializable;

/**
 * 条件可见性配置：引用一个 Workshop 变量，按变量值的真假控制宿主（Section / Widget）的显示隐藏。
 * <p>
 * 位于抽象层而非某个具体插件中，因为它被 <b>Widget 基类</b>
 * （{@link com.qlangtech.tis.plugin.workshop.widget.WidgetDisplayConfig}）与
 * {@code WorkshopSection} 共同复用，而 Widget 基类必须落在 tis-plugin
 * （Groovy 脚本 Widget 也在本模块）。
 * <p>
 * 与之相对，{@code VariableBasedVisibility} 支持 equals / notEmpty / greaterThan 等
 * <b>比较表达式</b>，目前仅 Overlay 使用；两者能力不同，尚未收敛。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/13
 */
public class ConditionalVisibility implements Describable<ConditionalVisibility>, Serializable {

    private static final long serialVersionUID = 1L;

    /** 被引用的变量名（模块内唯一，大小写不敏感） */
    @FormField(ordinal = 0, type = FormFieldType.INPUTTEXT)
    public String variableId;

    /** 变量值为假时是否隐藏；false 表示变量为假时才显示 */
    @FormField(ordinal = 1, type = FormFieldType.ENUM)
    public Boolean hideWhenFalse = true;

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<ConditionalVisibility> {
        @Override
        public String getDisplayName() {
            return "Conditional Visibility";
        }
    }
}
