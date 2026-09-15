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

import java.io.Serializable;

/**
 * Widget 尺寸策略（宽度 / 高度通用）—— 自适应 / 绝对像素 / flex 比例三选一。
 * <p>
 * 为什么是三个子类、而不是「一个 {@code SizingMode} 类 + 一个 mode 枚举 + 三个可空载荷字段」：
 * 后者允许出现 mode=FLEX 却填了 pixels 这类<b>自相矛盾</b>的状态，编译器无从约束；
 * 而「固定取值集合 + 每种取值有各自的载荷」正是 TIS 中 Describable 多态要解决的问题。
 * 这与 {@code VariableDefinitionConfig}、{@code OverlayTypeConfig} 的处理方式一致。
 * <p>
 * 三个子类的短 key 与前端 {@code SizingMode} 判别联合的 {@code type} 字面量对齐：
 * {@code auto} / {@code absolute} / {@code flex}（见各子类 {@code shortComment()}）。
 * <p>
 * 注意：前端 {@code widgets.model.ts} 的 {@code SizingMode} 目前<b>尚无消费方</b>，
 * 本类型是先把契约定下来。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/13
 */
public abstract class SizingMode implements Describable<SizingMode>, Serializable {

    private static final long serialVersionUID = 1L;

    public abstract static class BasicDescriptor extends Descriptor<SizingMode> {
    }
}
