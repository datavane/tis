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

import java.io.Serializable;
import java.util.UUID;

/**
 * Workshop Widget 抽象基类 —— 既是插件扩展点的实例载体，也是布局实体的字段宿主。
 *
 * <h3>为什么只有一个类</h3>
 * 原先存在两个平行概念：抽象层的 {@code WorkshopWidgetDescribable}（定位为「运行时行为」）
 * 与 ontology 插件里的 {@code WorkshopWidget} 实体（定位为「布局 / 定位」），二者靠
 * {@code setupConfig} 字段做组合，而 {@code setupConfig} 声明的是 {@code WidgetSetupConfig} ——
 * 一个只有 3 个零引用子类的类型，因此该字段恒为 {@code null}，组合从未真正成立。
 * <p>
 * TIS 的插件本来就是充血模型，「运行时行为」与「布局」不需要拆成两个类型：具体 Widget 子类
 * 通过 {@code @FormField} 声明自己的配置字段，通用的布局字段由本基类提供。
 * 因此删除了 {@code WorkshopWidgetDescribable}（本类即其更名版本）、ontology 侧的
 * {@code WorkshopWidget} 实体、以及 {@code WidgetSetupConfig} 及其三个子类。
 *
 * <h3>类型身份</h3>
 * 「我是哪一种 Widget」由<b>子类的 Java 类型</b>承担，<b>没有</b>与之平行的类型枚举或
 * 类型字段。原先的 {@code WidgetType} 枚举（20 个常量，中文标签与各子类描述符
 * {@code shortComment()} 逐字重复）属于重复分类体系，已删除。
 * 面向用户的短 key（{@code object-table}、{@code chart-xy} …）由各子类描述符经
 * {@code WorkShopWidgetType} + {@code getExtractProps()} 下发。
 * <p>
 * 实例 JSON 上具体类以扁平 {@code impl} 键（FQCN）出现，前端读
 * {@code setupConfig.impl} 即得类型，无需额外字段。
 *
 * <h3>字段序位</h3>
 * 基类占用 {@code 0}~{@code 5} 区间与 {@code 99}；各子类字段从 {@code 10} 起编排，避免 ordinal 撞车
 * —— TIS 的表单排序是对 {@code formField.ordinal()} 做稳定排序，而字段集合来自 HashMap，
 * 相同 ordinal 的相对顺序不确定。{@code 2}、{@code 3} 原为变量绑定字段所占用，两字段已删除
 * （见下），序位留空无害，不必重排。
 *
 * <h3>变量绑定字段为何不在基类</h3>
 * 基类原先声明 {@code List<String> inputVariables} / {@code outputVariables}，作为
 * 「角色 → 变量」的绑定容器。该设计不成立：其一，它把变量<b>角色</b>（objectSet / dataSource /
 * activeObject …）压成了一个无角色的字符串列表，而每个 Widget 的角色各不相同，基类无法表达；
 * 其二，{@code MULTI_SELECTABLE} 需要 {@code enum} 或 {@code elementCreator} 才能渲染，
 * 这两字段两样都没有，导致前端 {@code buildMultiSelectedAttr} 在取不到 {@code eprops.enum}
 * 时直接抛错 —— <b>20 个 Widget 的表单全部无法渲染</b>；其三，全工程没有任何代码读写它们
 * （无依赖图、无执行引擎、无序列化、无校验）。
 * 因此删除，改由各子类声明自己的<b>类型化</b>绑定字段
 * （如 {@code ObjectTableWidget.objectSetVar}、{@code ObjectListWidget.activeObjectVar}），
 * 用 {@code FormFieldType.SELECTABLE} + {@code WidgetOptionHelper} 按变量类型供给选项。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/13
 */
public abstract class WorkshopWidget implements IWorkshopWidget, Serializable {

    private static final long serialVersionUID = 1L;

    /** 模块内唯一的组件名（供 Section 编排与调试引用） */
    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 0, advance = false, validate = {Validator.require})
    public String name;

    /** Widget 标题（画布与配置面板通用，面向用户展示） */
    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 1, advance = false, validate = {Validator.require})
    public String title;

    /** 实例级显示配置（尺寸 / 优化策略 / 条件可见性） */
    @FormField(ordinal = 4)
    public WidgetDisplayConfig displayConfig;

    /** 同一 Section 内的显示顺序 */
    @FormField(type = FormFieldType.INT_NUMBER, ordinal = 5)
    public Integer sortOrder = 0;

    /**
     * 高级配置折叠区：自定义 CSS class（原 ontology 实体 metadata.customId 的职责并入此处）
     */
    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 99, advance = true)
    public String cssClass;

    private String id;

    public WorkshopWidget() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
