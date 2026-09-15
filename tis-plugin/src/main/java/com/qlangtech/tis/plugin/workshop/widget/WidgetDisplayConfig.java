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
import com.qlangtech.tis.plugin.workshop.model.ConditionalVisibility;

import java.io.Serializable;

/**
 * Widget 实例级的显示配置：尺寸、优化策略、条件可见性。
 * <p>
 * 落在抽象层（tis-plugin）而非某个具体插件，因为它是 Widget 基类
 * {@link WorkshopWidget} 的公共字段。
 *
 * <h3>与前端形状的对齐</h3>
 * 字段形状以后端向<em>前端</em>对齐为准：
 * <ul>
 *   <li>{@link #width} / {@link #height} —— 由原来的自由文本 {@code String}（"100%"、"300px"）
 *       改为结构化的 {@link SizingMode} 多态（自适应 / 固定像素 / flex 比例）。
 *       自由文本要靠前端解析字符串，既无法校验也表达不了「按比例分配」。</li>
 *   <li>{@link #displayOptimization} —— 由原来的 {@code lazyLoading} + {@code virtualScroll}
 *       两个 Boolean 合并而来。两个布尔有 4 种组合，但真正有意义的只有 3 种，
 *       且原来的组合<em>表达不了</em>前端的 {@code never-unmount}（永不卸载）。</li>
 *   <li>{@link #conditionalVisibility} —— 与 {@code WorkshopSection} 共用，原样保留。</li>
 * </ul>
 * 注意：尺寸与展示优化目前<b>尚无消费方</b>（前端 widgets.model 里声明了对应类型但未读取），
 * 这里是先把契约定下来。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/13
 */
public class WidgetDisplayConfig implements Describable<WidgetDisplayConfig>, Serializable {

    private static final long serialVersionUID = 1L;

    /** 宽度策略 */
    @FormField(ordinal = 0)
    public SizingMode width;

    /** 高度策略 */
    @FormField(ordinal = 1)
    public SizingMode height;

    /** 条件可见性：引用变量控制该 Widget 的显示隐藏 */
    @FormField(ordinal = 2)
    public ConditionalVisibility conditionalVisibility;

    /** 挂载 / 渲染优化策略 */
    @FormField(ordinal = 3, type = FormFieldType.ENUM)
    public DisplayOptimization displayOptimization = DisplayOptimization.dft;

    /**
     * 展示优化策略。
     * <p>
     * Java 枚举常量名无法直接写成前端的字面量（{@code default} 是保留字、kebab-case 也不是合法标识符），
     * 因此用 {@link #key} 显式声明与前端 {@code WidgetDisplay.displayOptimization} 的映射。
     */
    public enum DisplayOptimization {
        /** 默认：进入视口即挂载，离开后卸载 */
        dft("default", "默认"),
        /** 提前挂载：即便不在视口也预先挂载，切换更快 */
        eagerMount("eager-mount", "提前挂载"),
        /** 永不卸载：挂载后常驻，保留内部状态（如滚动位置） */
        neverUnmount("never-unmount", "永不卸载");

        /** 与前端字面量一致的 key */
        public final String key;
        public final String label;

        DisplayOptimization(String key, String label) {
            this.key = key;
            this.label = label;
        }
    }

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<WidgetDisplayConfig> {
        @Override
        public String getDisplayName() {
            return "Widget Display Config";
        }
    }
}
