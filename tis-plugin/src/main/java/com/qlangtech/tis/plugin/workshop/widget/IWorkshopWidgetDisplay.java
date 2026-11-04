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

/**
 * Widget 显示配置扩展点 —— 每个 Widget 至多一份，独立于 Widget 自身落盘
 * （{@code widgets/{widgetId}_display.xml}）。
 *
 * <p>抽成独立扩展点的理由：
 * <ul>
 *   <li>编辑 Display 时不必重写整个 Widget 表单；</li>
 *   <li>「显示配置」的形态随 Widget 种类而变（有内容区的 Widget 需要高度与挂载策略，
 *       单行表单控件不需要），由多个 impl 表达比在 Widget 继承树里分叉更直接；</li>
 *   <li>Widget 侧不再需要 Full / Compact 两条中间抽象基类。</li>
 * </ul>
 *
 * <p>所有具体 impl 的 identity 字段都必须是「所属 widget 的 id」，落盘时由服务端按
 * widget id 生成文件名，不依赖前端传入。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/30
 */
public interface IWorkshopWidgetDisplay extends Describable<IWorkshopWidgetDisplay> {
}
