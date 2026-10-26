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

package com.qlangtech.tis.plugin;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.Descriptor.ParseDescribable;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.ValueChangePipe;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-12-07 18:28
 **/
@SuppressWarnings("all")
public interface IPluginStore<T extends Describable> extends IRepositoryResource, IPluginStoreSave<T> {

    /**
     * 不需要持久化的pluginStore，例如JobTrigger
     *
     * @param <T>
     * @return
     * @see com.qlangtech.tis.plugin.trigger.JobTrigger
     */
    public static <T extends Describable<T>> IPluginStore<T> noSaveStore(UploadPluginMeta pluginMeta) {
        return new AdapterPluginStore<>() {
            @Override
            public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional<Context> context,
                                               List<ParseDescribable<T>> dlist, boolean update) {
                dlist.forEach((plugin) -> {
                    plugin.getSubFormInstances().forEach((p) -> {
                        if (!(p instanceof ManipuldateProcessor)) {
                            throw new IllegalStateException("instance of " + p.getClass().getName() + " must be type "
                                    + "of " + ManipuldateProcessor.class.getSimpleName());
                        }
                        ((ManipuldateProcessor) p).manipuldateProcess(pluginContext, pluginMeta, context);
                    });
                });
                return new SetPluginsResult(true, false);
            }
        };
    }

    public static class AdapterPluginStore<T extends Describable<T>> implements IPluginStore<T> {
        @Override
        public T getPlugin() {
            return null;
        }

        @Override
        public List<T> getPlugins() {
            return Collections.emptyList();
        }

        @Override
        public void cleanPlugins() {

        }

        @Override
        public List<Descriptor<T>> allDescriptor() {
            return Collections.emptyList();
        }

        @Override
        public T find(String name, boolean throwNotFoundErr) {
            return null;
        }

        @Override
        public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional<Context> context,
                                           List<ParseDescribable<T>> dlist, boolean update) {

            throw new UnsupportedOperationException();
        }

        @Override
        public void copyConfigFromRemote() {

        }

        @Override
        public long getWriteLastModifyTimeStamp() {
            return 0;
        }

        @Override
        public XmlFile getTargetFile() {
            return null;
        }
    }

    public T getPlugin();

    public List<T> getPlugins();

    public void cleanPlugins();

    public List<Descriptor<T>> allDescriptor();

    default <TT extends T> IPluginStore<TT> unsaveCast() {
        return (IPluginStore<TT>) this;
    }

    public default T find(String name) {
        return find(name, true);
    }

    public T find(String name, boolean throwNotFoundErr);

    interface Recyclable {
        // 是否已经是脏数据了，已经在PluginStore中被替换了
        boolean isDirty();
    }

    interface RecyclableController extends Recyclable {
        /**
         * 标记已经失效
         */
        void signDirty();
    }

    interface AfterPluginSaved {
        /**
         * Plugin 保存执行回调执行
         */
        void afterSaved(IPluginContext pluginContext, Optional<Context> context);
    }

    interface AfterPluginDeleted {
        /**
         * 插件被删除之后执行
         *
         * @param pluginContext
         * @param context
         */
        void afterDeleted(IPluginContext pluginContext, Optional<Context> context);
    }

    interface BeforePluginSaved {
        /**
         * Plugin 保存执行前回调执行
         */
        void beforeSaved(IPluginContext pluginContext, Optional<Context> context);
    }

    /**
     * 不需要持久化的plugin进行提交处理
     */
    interface ManipuldateProcessor {
        /**
         * 执行处理
         */
        void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta, Optional<Context> context);
    }


    /**
     * @see FormFieldType#MULTI_DESCRIBLE_PLUGIN
     */
    interface MultiDescribleElement extends ManipuldateProcessor, IdentityName {

        public default void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                               Optional<Context> context) {

        }
    }

    /**
     * {@code MULTI_DESCRIBLE_PLUGIN} 类型字段的元素 Descriptor 基类。
     *
     * <p>当插件类的某个字段被 {@code @FormField(type = FormFieldType.MULTI_DESCRIBLE_PLUGIN)} 标注时，
     * 该字段的类型必须是 {@code List<T>}，其中 {@code T} 实现 {@link MultiDescribleElement} 接口，
     * 且 {@code T} 的 Descriptor 必须继承本类。
     *
     * <h3>用途</h3>
     * 用于在前端渲染结构化的子表单列表，每行是一个完整的 {@code Describable} 实例，支持：
     * <ul>
     *   <li>List 视图（{@link ViewStyle#List}）或 Table 视图（{@link ViewStyle#Table}）
     *   <li>行拖拽排序（{@link #isEnableRowDragDrop()}）
     *   <li>可枚举集合模式（{@link #getEnumableSetCfg()}）——用户从预定义候选池中勾选，而非手动逐行填写
     * </ul>
     *
     * <h3>典型使用场景</h3>
     * <pre>{@code
     * // 宿主插件类
     * public class ActionParameters {
     *     @FormField(type = FormFieldType.MULTI_DESCRIBLE_PLUGIN,
     *                desClazz = ActionParameterConfig.class)
     *     public List<ActionParameterConfig> parameters;
     * }
     *
     * // 元素类
     * public class ActionParameterConfig implements MultiDescribleElement {
     *     @FormField(identity = true)
     *     public String parameterId;
     *
     *     @TISExtension
     *     public static class DftDescriptor extends MultiDescribleElementDescriptor<ActionParameterConfig> {
     *         // 实现抽象方法...
     *     }
     * }
     * }</pre>
     *
     * <h3>前后端交互流程</h3>
     * <ol>
     *   <li>用户点击"添加"按钮 → 前端调用 {@code addMultiChildPluginWithEnumableSet()}
     *   <li>前端发起 {@code plugin_action} 请求，{@code type=get_desc} → 后端 {@link #httpProcess} 响应
     *   <li>如果是可枚举集合，后端返回 {@link MultiDescribleElementSetSelector} 的 descriptor
     *   <li>用户在 selector 中勾选目标项 → 前端再次请求 {@code type=generate_target_instance}
     *   <li>后端将勾选的 {@link MultiDescribleElement} 序列化后返回 → 前端追加到 {@code List<T>} 字段中
     * </ol>
     *
     * @param <T> 元素类型，必须同时实现 {@link Describable} 和 {@link MultiDescribleElement}
     * @see MultiDescribleElementSetSelector
     * @see ActionParameterConfig 完整实现示例
     * @see com.qlangtech.tis.manage.common.UserProfile.TestChild 测试示例
     */
    abstract class MultiDescribleElementDescriptor<T extends Describable<T>> extends Descriptor<T> implements DescriptorUseableShortComment,
            IEndTypeGetter {
        private static final MultiDescribleElementSetSelector.DefaultDesc multiDescribleElementSetDesc =
                new MultiDescribleElementSetSelector.DefaultDesc();

        private enum ActionType {
            GetDesc("get_desc"), GenerateTargetInstance("generate_target_instance");
            private final String token;

            private static ActionType parse(String token) {

                for (ActionType actionType : ActionType.values()) {
                    if (actionType.token.equals(token)) {
                        return actionType;
                    }
                }

                throw new IllegalStateException("illegal token:" + token);
            }

            private ActionType(String token) {
                this.token = token;
            }
        }


        /**
         * 相关字段键管理器，用于在可枚举集合模式下构建字段白名单。
         *
         * <p>当用户点击"添加"按钮打开 {@link MultiDescribleElementSetSelector} 时，
         * 需要先对宿主表单进行部分校验（而非全量校验），只校验那些与候选集合生成逻辑相关的字段。
         * 本类负责收集这些"相关字段"的键名，供 {@link AttrValMap.RelevantFieldsContextAttrValMap} 使用。
         *
         * <h3>两阶段字段收集</h3>
         * <ol>
         *   <li><b>校验阶段</b>（{@code apply()}）：收集与目标字段有级联关系的字段
         *       → 通过 {@link #toFieldValiateRelevantKeysCreator} 实现，默认调用
         *       {@code descriptor.getValueChangeFromFieldKeys(toPropertyKey)} 获取级联主字段
         *   <li><b>实例化阶段</b>（{@code addToFieldNewInstanceRelevantKeys()}）：追加目标字段本身
         *       → 通过 {@link #toFieldNewInstanceRelevantKeysCreator} 实现，默认返回 {@code [toPropertyKey]}
         * </ol>
         *
         * <h3>为什么分两阶段？</h3>
         * <pre>
         * 假设宿主表单有字段 A、B、C，其中 B 依赖 A（A 改变时 B 的候选项会刷新），目标字段是 C：
         *   1. 校验时：需要校验 A、B（影响候选集合的生成）
         *   2. 实例化时：需要读取 A、B、C（C 用作 getEnumableCandidateSet 的参数）
         * 分两次调用是因为校验在前、实例化在后，且校验失败则不继续。
         * </pre>
         *
         * <h3>使用示例</h3>
         * <pre>{@code
         * // 在 httpProcess 中：
         * RelevantFieldKeys relevantFieldKeysCreator = createRelevantFieldKeysCreator("parameters");
         * AttrValMap.RelevantFieldsContextAttrValMap<?> relevantFieldsContext =
         *     AttrValMap.createRelevantFieldsContext(paramGetter, context, postContent, relevantFieldKeysCreator);
         * // 此时 relevantFieldKeys 可能包含: ["actionType"]（级联主字段）
         *
         * if (!relevantFieldsContext.validate().isValid()) {
         *     return;  // 只校验相关字段，不涉及无关的必填项
         * }
         *
         * relevantFieldKeysCreator.addToFieldNewInstanceRelevantKeys();
         * // 此时 relevantFieldKeys 变成: ["actionType", "parameters"]
         *
         * ParseDescribable<?> describable = relevantFieldsContext.createDescribable();
         * // 实例化时会读取 actionType 和 parameters 的值
         * }</pre>
         *
         * @see AttrValMap.RelevantFieldsContextAttrValMap
         * @see #httpProcess
         */
        public static class RelevantFieldKeys implements Function<Descriptor, List<String>> {
            /**
             * 累积的相关字段键列表，会被 {@link AttrValMap#createRelevantFieldsContext} 作为白名单使用。
             */
            final List<String> relevantFieldKeys = Lists.newArrayList();
            /**
             * 目标字段的 key（即 {@code MULTI_DESCRIBLE_PLUGIN} 字段在宿主类中的属性名）。
             */
            final String toPropertyKey;

            public RelevantFieldKeys(String toPropertyKey) {
                this.toPropertyKey = toPropertyKey;
            }

            /**
             * 第一阶段：收集校验阶段需要的字段（级联主字段等）。
             *
             * @param descriptor 宿主插件的 Descriptor
             * @return 收集到的字段键列表
             */
            @Override
            public final List<String> apply(Descriptor descriptor) {
                relevantFieldKeys.addAll(toFieldValiateRelevantKeysCreator(descriptor));
                return relevantFieldKeys;
            }

            /**
             * 子类可覆盖以定制校验阶段的字段白名单。
             * 默认实现：查询与 {@code toPropertyKey} 有级联关系的主字段。
             *
             * @param descriptor 宿主插件的 Descriptor
             * @return 需要在校验阶段读取的字段键列表
             */
            protected List<String> toFieldValiateRelevantKeysCreator(
                    Descriptor descriptor) {
                return descriptor.getValueChangeFromFieldKeys(toPropertyKey);
            }

            /**
             * 第二阶段：追加实例化阶段需要的字段（通常是目标字段本身）。
             * 必须在 {@code validate()} 成功后、{@code createDescribable()} 之前调用。
             */
            public final void addToFieldNewInstanceRelevantKeys() {
                this.relevantFieldKeys.addAll(toFieldNewInstanceRelevantKeysCreator());
            }

            /**
             * 子类可覆盖以定制实例化阶段追加的字段。
             * 默认实现：返回目标字段本身。
             *
             * @return 需要在实例化阶段追加的字段键列表
             */
            protected List<String> toFieldNewInstanceRelevantKeysCreator() {
                return Lists.newArrayList(this.toPropertyKey);
            }
        }

        /**
         * 处理 {@code MULTI_DESCRIBLE_PLUGIN} 字段的 HTTP 请求，支持两种操作类型。
         *
         * <p>前端通过 {@code coredefine:plugin_action:description_process} 调用本方法，
         * 请求参数中的 {@code type} 决定具体行为：
         *
         * <h3>1. {@code get_desc} —— 获取选择器描述符</h3>
         * 用户点击"添加"按钮时触发，用于渲染 {@link MultiDescribleElementSetSelector}（可枚举集合的勾选界面）。
         *
         * <p><b>请求参数</b>：
         * <ul>
         *   <li>{@code type=get_desc}
         *   <li>{@code property} - 目标字段名（如 {@code "parameters"}）
         *   <li>{@code impl} - 元素的 Descriptor 实现类全限定名
         *   <li>POST body - 宿主表单当前填写的值（用于生成候选集合）
         * </ul>
         *
         * <p><b>处理流程</b>：
         * <ol>
         *   <li>构建相关字段白名单（{@link #createRelevantFieldKeysCreator}）
         *   <li>对宿主表单的相关字段进行部分校验（{@link AttrValMap.RelevantFieldsContextAttrValMap#validate}）
         *   <li>实例化宿主对象（{@link AttrValMap.RelevantFieldsContextAttrValMap#createDescribable}）
         *   <li>调用 {@link #getEnumableCandidateSet} 生成候选列表
         *   <li>将候选列表放入 {@code context}，并返回 {@link MultiDescribleElementSetSelector} 的 descriptor JSON
         * </ol>
         *
         * <p><b>响应</b>：{@code MultiDescribleElementSetSelector.DefaultDesc} 的 descriptor JSON，
         * 前端据此渲染勾选界面。
         *
         * <h3>2. {@code generate_target_instance} —— 生成目标实例</h3>
         * 用户在 selector 中勾选完毕、点击"确定"后触发，将勾选的元素序列化返回给前端。
         *
         * <p><b>请求参数</b>：
         * <ul>
         *   <li>{@code type=generate_target_instance}
         *   <li>{@code property} - 目标字段名
         *   <li>{@code impl} - 元素的 Descriptor 实现类全限定名
         *   <li>POST body 结构：
         *       <pre>{@code
         *       {
         *         "host": { ... },  // 宿主表单当前值
         *         "MultiDescribleElementSetSelector": {
         *           "target": [...]  // 用户勾选的项（MultiDescribleElement 实例）
         *         }
         *       }
         *       }</pre>
         * </ul>
         *
         * <p><b>处理流程</b>：
         * <ol>
         *   <li>实例化宿主对象（同 {@code get_desc}）
         *   <li>调用 {@link #getEnumableCandidateSet} 生成候选列表
         *   <li>从 {@code context} 中读取用户勾选的 {@link MultiDescribleElementSetSelector#target}
         *   <li>过滤出被勾选的元素，调用 {@link ValueChangePipe#renderValueSerialize2Json} 序列化
         *   <li>将序列化结果放入 {@code bizResult}
         * </ol>
         *
         * <p><b>响应</b>：{@code bizResult} 中包含序列化后的元素数组，格式与
         * {@link DescribableJSON#getItemJson()} 一致：
         * <pre>{@code
         * [
         *   {"impl": "com.example.Foo", "vals": {...}},
         *   {"impl": "com.example.Bar", "vals": {...}}
         * ]
         * }</pre>
         * 前端接收后追加到 {@code List<T>} 字段中。
         *
         * <h3>相关字段白名单机制</h3>
         * 由于用户可能在宿主表单的其它字段尚未填写完整时就点击"添加"按钮，
         * 此时不能对整个表单做全量校验（会误报必填项错误）。因此使用
         * {@link AttrValMap.RelevantFieldsContextAttrValMap} 只校验和实例化与本次操作相关的字段：
         * <ul>
         *   <li>级联主字段（如候选集合依赖的上级选择器）
         *   <li>目标字段本身（用作 {@link #getEnumableCandidateSet} 的参数）
         * </ul>
         * 未进白名单的字段在实例化时会被赋予 {@code null}，不影响候选集合的生成。
         *
         * @param paramGetter 请求参数读取器（包含 query 和 POST body）
         * @param pluginContext 插件上下文（用于读取 JSON POST body）
         * @param context 请求上下文（用于在两次调用间传递 {@code MultiDescribleElementSetSelector} 实例）
         * @throws Exception 校验失败或业务异常
         * @see MultiDescribleElementSetSelector
         * @see ActionParameterConfig.DftDescriptor#getEnumableCandidateSet 实现示例
         * @see com.qlangtech.tis.coredefine.module.action.PluginAction#doDescriptionProcess 路由入口
         */
        @Override
        public final void httpProcess(IControlMsgHandler paramGetter, IPluginContext pluginContext, Context context) throws Exception {
            //            if (context.get(UploadPluginMeta.KEY_PLUGIN_META) == null) {
            //                List<UploadPluginMeta> metas = pluginContext.getPluginMeta(false);
            //                for (UploadPluginMeta meta : metas) {
            //                    context.put(UploadPluginMeta.KEY_PLUGIN_META, meta);
            //                    break;
            //                }
            //            }
            ActionType actionType = ActionType.parse(paramGetter.getString("type"));
            JSONObject postContent = pluginContext.getJSONPostContent();
            final String toPropertyKey = paramGetter.getString("property");
            if (StringUtils.isEmpty(toPropertyKey)) {
                throw new IllegalArgumentException("key property can not be empty");
            }
            // 该集合会被 createRelevantFieldsContext 按引用用作属性集的过滤依据（见其 javadoc），
            // 所以这里一直持有它，在 validate() 之后往里追加 toPropertyKey 即可让 createDescribable() 读到
            //final List<String> relevantFieldKeys = Lists.newArrayList();

            RelevantFieldKeys relevantFieldKeysCreator = createRelevantFieldKeysCreator(toPropertyKey);

            // 只对「与本级联相关的字段」做校验和实例化，避免误判表单中其它尚未填写的属性
            AttrValMap.RelevantFieldsContextAttrValMap<?> relevantFieldsContext =
                    AttrValMap.createRelevantFieldsContext(paramGetter, context, switch (actionType) {
                        case GetDesc -> postContent;
                        case GenerateTargetInstance -> postContent.getJSONObject("host");
                    }, relevantFieldKeysCreator);

            switch (actionType) {
                case GetDesc -> {
                    // 走带作用域的校验，保证错误信息能定位到具体 item
                    if (!relevantFieldsContext.validate().isValid()) {
                        return;
                    }
                    // 创建实例的时候要将toPropertyKey对应的属性设置上值
                    // relevantFieldKeys.add(toPropertyKey);
                    relevantFieldKeysCreator.addToFieldNewInstanceRelevantKeys();
                    ParseDescribable<?> describable = relevantFieldsContext.createDescribable();
                    context.put(MultiDescribleElementSetSelector.class.getName(), getEnumableCandidateSet(describable));
                    paramGetter.setBizResult(context, DescriptorsJSON.desc(multiDescribleElementSetDesc));
                }
                case GenerateTargetInstance -> {
                    // 创建实例的时候要将toPropertyKey对应的属性设置上值
                    // relevantFieldKeys.add(toPropertyKey);
                    relevantFieldKeysCreator.addToFieldNewInstanceRelevantKeys();
                    ParseDescribable<?> describable = relevantFieldsContext.createDescribable();
                    List<Pair<Option, MultiDescribleElement>> enumableCandidateSet =
                            getEnumableCandidateSet(describable);
                    MultiDescribleElementSetSelector multiDescribleElementSetSelector =
                            Objects.requireNonNull((MultiDescribleElementSetSelector) context.get(MultiDescribleElementSetSelector.class.getName())
                                    , "key:" + MultiDescribleElementSetSelector.class.getName()
                                            + " relevant instance can not be null");
                    // 被选中的
                    Set<String> selected =
                            multiDescribleElementSetSelector.target.stream().map(IdentityName::identityValue).collect(Collectors.toSet());


                    paramGetter.setBizResult(context,
                            ValueChangePipe.renderValueSerialize2Json(enumableCandidateSet.stream()
                                    .filter((p) -> selected.contains(p.getValue().identityValue()))
                                    .map(Pair::getValue).toList()));
                }
                default -> throw new IllegalStateException("illegal actionType:" + actionType);
            }


        }

        /**
         * 创建相关字段键管理器，子类可覆盖以定制字段白名单逻辑。
         *
         * <p>默认实现返回标准的 {@link RelevantFieldKeys}，其行为是：
         * <ul>
         *   <li>校验阶段：收集与 {@code toPropertyKey} 有 {@code valueChangePipe} 关系的主字段
         *   <li>实例化阶段：追加 {@code toPropertyKey} 本身
         * </ul>
         *
         * <h3>何时需要覆盖？</h3>
         * 当候选集合的生成逻辑依赖宿主表单中的其它字段（非级联关系），且这些字段不在
         * {@code getValueChangeFromFieldKeys} 返回的列表中时，需要子类覆盖以扩展白名单。
         *
         * <p>例如：候选集合依赖 {@code domain} 字段，但 {@code domain} 与目标字段无级联关系，
         * 则需要手动追加：
         * <pre>{@code
         * @Override
         * protected RelevantFieldKeys createRelevantFieldKeysCreator(String toPropertyKey) {
         *     return new RelevantFieldKeys(toPropertyKey) {
         *         @Override
         *         protected List<String> toFieldValiateRelevantKeysCreator(Descriptor descriptor) {
         *             List<String> keys = super.toFieldValiateRelevantKeysCreator(descriptor);
         *             keys.add("domain");  // 手动追加依赖字段
         *             return keys;
         *         }
         *     };
         * }
         * }</pre>
         *
         * @param toPropertyKey 目标字段的 key
         * @return 字段键管理器实例
         * @see RelevantFieldKeys
         */
        protected RelevantFieldKeys createRelevantFieldKeysCreator(String toPropertyKey) {
            return new RelevantFieldKeys(toPropertyKey);
        }

        /**
         * 获取可枚举候选集合，供 {@link MultiDescribleElementSetSelector} 渲染勾选界面。
         *
         * <p>当 {@link #getEnumableSetCfg()} 返回 {@code EnumableSetCfg(true)} 时，
         * 用户点击"添加"按钮不再是手动填写新行，而是从预定义的候选池中勾选。
         * 本方法负责生成该候选池。
         *
         * <h3>参数 describable 的含义</h3>
         * {@code describable} 是 {@link #httpProcess} 根据当前宿主表单的填写值实例化出的宿主对象（部分字段）。
         * 候选集合的生成逻辑通常需要依赖宿主表单的某些字段，例如：
         * <ul>
         *   <li>{@code ActionParameterConfig} 的候选集合依赖 {@code ActionType}（不同 Action 类型有不同的参数模板）
         *   <li>{@code PropertyMappingConfig} 的候选集合依赖上级选择器的 {@code sourceObject}（源对象不同，属性列表不同）
         * </ul>
         * 因此本方法接收的 {@code describable} 是一个"部分实例化"的宿主对象：
         * 只有进入相关字段白名单的字段才有值，其余字段为 {@code null}。
         *
         * <h3>返回值格式</h3>
         * 返回 {@code Pair<Option, MultiDescribleElement>} 列表，其中：
         * <ul>
         *   <li>{@code Option} - 勾选项的显示信息（{@code name}、{@code value}，可选 {@code subTitle}）
         *   <li>{@code MultiDescribleElement} - 被勾选后要插入目标列表的实例
         * </ul>
         *
         * <p>{@code Option.value} 必须与 {@code MultiDescribleElement.identityValue()} 一致，
         * 用于在 {@code generate_target_instance} 阶段过滤出被勾选的项。
         *
         * <h3>实现示例</h3>
         * <pre>{@code
         * @Override
         * protected List<Pair<Option, MultiDescribleElement>> getEnumableCandidateSet(ParseDescribable<?> describable) {
         *     // 从 describable 中读取依赖字段
         *     ActionParameters hostInstance = (ActionParameters) describable.instance;
         *     ActionType actionType = hostInstance.getActionType();
         *
         *     // 根据 actionType 生成候选参数列表
         *     List<ActionParameterConfig> templates = actionType.getParameterTemplates();
         *
         *     // 转换为 Pair<Option, MultiDescribleElement>
         *     return templates.stream()
         *         .map(param -> Pair.of(
         *             new Option(param.displayName, param.parameterId),
         *             (MultiDescribleElement) param
         *         ))
         *         .toList();
         * }
         * }</pre>
         *
         * <h3>默认实现</h3>
         * 返回空列表，表示不支持可枚举模式（用户必须手动逐行填写）。
         * 子类若要启用可枚举模式，必须同时覆盖本方法和 {@link #getEnumableSetCfg()}。
         *
         * @param describable 宿主对象的部分实例（只有相关字段有值），用于生成候选集合
         * @return 候选项列表，每项是 {@code (显示信息, 元素实例)} 对
         * @see MultiDescribleElementSetSelector
         * @see #getEnumableSetCfg()
         * @see ActionParameterConfig.DftDescriptor#getEnumableCandidateSet 完整实现示例
         */
        protected List<Pair<Option, MultiDescribleElement>> getEnumableCandidateSet(ParseDescribable<?> describable) {
            return Lists.newArrayList();
        }



        /**
         * 提取该 Descriptor 的属性配置，用于前端渲染 {@code MULTI_DESCRIBLE_PLUGIN} 字段。
         *
         * <p>前端通过 {@code DescriptorsJSON} 获取 descriptor 元数据时，会调用本方法收集显示风格、
         * 列配置、拖拽排序等子表单渲染所需的全部配置项。这些配置最终出现在前端
         * {@code MultiChildViewDescriptor} 对象中。
         *
         * <h3>追加的属性</h3>
         * 除了父类 {@code Descriptor#getExtractProps} 提供的基础属性外，本方法额外追加：
         * <table>
         *   <tr><th>属性名</th><th>类型</th><th>来源方法</th><th>说明</th></tr>
         *   <tr><td>{@code viewStyle}</td><td>String</td><td>{@link #viewStyle()}</td>
         *       <td>显示风格：{@code "List"} 或 {@code "Table"}</td></tr>
         *   <tr><td>{@code colsCfg}</td><td>List</td><td>{@link #colsConfig()}</td>
         *       <td>列配置（Table 视图必填，List 视图可选）</td></tr>
         *   <tr><td>{@code maxHeight}</td><td>int</td><td>{@link #maxHeight()}</td>
         *       <td>列表/表格最大高度（px），超出后滚动</td></tr>
         *   <tr><td>{@code enumableSet}</td><td>boolean</td><td>{@link #getEnumableSetCfg()}</td>
         *       <td>是否为可枚举集合（true = 勾选模式，false = 手动添加模式）</td></tr>
         *   <tr><td>{@code enumableSetItemDependencyDepth}</td><td>int</td><td>{@link EnumableSetCfg#itemDependencyDepth}</td>
         *       <td>候选集合依赖的宿主实例深度（0 = 直接宿主，1 = 父级宿主）</td></tr>
         *   <tr><td>{@code enableRowDragDrop}</td><td>boolean</td><td>{@link #isEnableRowDragDrop()}</td>
         *       <td>是否启用行拖拽排序（仅 Table 视图）</td></tr>
         *   <tr><td>{@code rowDragDropOrderByField}</td><td>String</td><td>{@link RowDragDropCfg#orderByFieldName}</td>
         *       <td>拖拽后写入哪个字段（如 {@code "order"}）</td></tr>
         * </table>
         *
         * <h3>Table 视图强制约束</h3>
         * 当 {@code viewStyle() == ViewStyle.Table} 时，{@code colsConfig()} 返回的列配置中
         * <b>至少有一列必须被标记为 {@code clickable}</b>（通过 {@code ColConfig#setClickable()}），
         * 否则本方法抛出 {@code IllegalStateException}。
         *
         * <p>这是因为 Table 视图中用户需要点击某列才能打开详情面板编辑该行，
         * 缺少 clickable 列会导致用户无法修改已添加的条目。
         *
         * @param forAIPromote 是否用于 AI 提示（保留参数，本方法未使用）
         * @return 包含所有渲染配置的 Map，供前端 {@code MultiChildViewDescriptor} 使用
         * @throws IllegalStateException 当 Table 视图缺少 clickable 列时
         * @see #viewStyle()
         * @see #colsConfig()
         * @see #getEnumableSetCfg()
         */
        @Override
        public final Map<String, Object> getExtractProps(boolean forAIPromote) {
            Map<String, Object> props = super.getExtractProps(forAIPromote);
            props.put("viewStyle", this.viewStyle().name());

            List<ColConfig> colsCfg = this.colsConfig();
            if (this.viewStyle() == ViewStyle.Table) {
                boolean hasClickable = false;
                for (ColConfig cc : colsCfg) {
                    // clickable 是 Boolean 且缺省为 null，此处需空安全比较，否则未标记 clickable 的列会先抛 NPE
                    if (Boolean.TRUE.equals(cc.getClickable())) {
                        hasClickable = true;
                        break;
                    }
                }
                if (!hasClickable) {
                    throw new IllegalStateException(colsCfg.stream()
                            .map((col) -> col.titleKey).collect(Collectors.joining(","))
                            + " must contain a clickable col");
                }
                RowDragDropCfg rowDragDropCfg = this.isEnableRowDragDrop();
                props.put("enableRowDragDrop", rowDragDropCfg.enable);
                if (rowDragDropCfg.enable) {
                    props.put("rowDragDropOrderByField", rowDragDropCfg.orderByFieldName);
                }
            }

            props.put("colsCfg", colsCfg);
            props.put("maxHeight", this.maxHeight());
            EnumableSetCfg esc = null;
            if ((esc = this.getEnumableSetCfg()).isEnumableSet()) {
                props.put("enumableSet", true);
                props.put("enumableSetItemDependencyDepth", esc.itemDependencyDepth);
            }

            return props;
        }

        /**
         * 是否启用行拖拽排序，以及拖拽后的顺序值写入哪个字段。
         *
         * <p>仅当 {@link #viewStyle()} 返回 {@code ViewStyle.Table} 时生效。
         * 启用后，前端会通过 {@code @angular/cdk/drag-drop} 渲染拖拽手柄，
         * 用户拖动行后，前端会自动更新 {@code orderByFieldName} 指定字段的值。
         *
         * <h3>使用场景</h3>
         * 当元素类包含一个显式的 {@code order} 或 {@code priority} 字段用于存储排序位置时，
         * 应启用拖拽排序：
         * <pre>{@code
         * public class ActionParameterConfig {
         *     @FormField(ordinal = 2, type = FormFieldType.INT_NUMBER)
         *     public Integer order;  // 存储该参数在列表中的显示顺序
         *
         *     @TISExtension
         *     public static class DftDescriptor extends MultiDescribleElementDescriptor<ActionParameterConfig> {
         *         @Override
         *         protected RowDragDropCfg isEnableRowDragDrop() {
         *             return new RowDragDropCfg(true, "order");
         *         }
         *     }
         * }
         * }</pre>
         *
         * <h3>前端行为</h3>
         * <ul>
         *   <li>拖拽后，前端会重新计算所有行的 {@code order} 值（通常是 1, 2, 3, ...）
         *   <li>用户保存宿主表单时，新的 {@code order} 值会随其它字段一起提交
         *   <li>下次加载时，后端应按 {@code order} 字段升序返回列表
         * </ul>
         *
         * <h3>默认实现</h3>
         * 返回 {@code new RowDragDropCfg(false, null)}，表示不启用拖拽排序。
         *
         * @return 拖拽配置对象
         * @see RowDragDropCfg
         * @see ActionParameterConfig.DftDescriptor#isEnableRowDragDrop 实现示例
         */
        protected RowDragDropCfg isEnableRowDragDrop() {
            return new RowDragDropCfg(false, null);
        }

        /**
         * 列表/表格的最大高度（单位：px），超出后显示垂直滚动条。
         *
         * <p>该值会通过 {@link #getExtractProps} 传递给前端，最终作用于：
         * <ul>
         *   <li><b>List 视图</b>：{@code <nz-list>} 的 {@code [nzScroll]="{y: maxHeight + 'px'}"}
         *   <li><b>Table 视图</b>：{@code <nz-table>} 的 {@code [nzScroll]="{y: maxHeight + 'px'}"}
         * </ul>
         *
         * <h3>推荐值</h3>
         * <ul>
         *   <li>少量固定项（≤3 行）：100 ~ 150px
         *   <li>中等数量（5 ~ 10 行）：200 ~ 300px
         *   <li>大量动态数据（≥10 行）：400 ~ 500px
         * </ul>
         *
         * <p>设置过小会导致频繁滚动；设置过大会让表单过长，影响整体布局。
         *
         * <h3>默认实现</h3>
         * 返回 {@code 100}（适合 2 ~ 3 行的紧凑列表）。子类应根据实际预期数据量覆盖。
         *
         * @return 最大高度（px）
         * @see #getExtractProps
         */
        public int maxHeight() {
            return 100;
        }

        /**
         * 配置该字段是否为可枚举集合，以及候选集合依赖的宿主实例层级。
         *
         * <h3>可枚举集合 vs 开放集合</h3>
         * <ul>
         *   <li><b>可枚举集合</b>（{@code EnumableSetCfg(true)}）：元素来自有限的预定义候选池，
         *       用户通过 {@link MultiDescribleElementSetSelector} 勾选添加，不能自由输入。
         *       适用于"从已有模板中选择"的场景（如 Action 参数从类型模板中选）。
         *   <li><b>开放集合</b>（{@code EnumableSetCfg(false)}）：用户点击"添加"后直接填写新行，
         *       可以无限创建。适用于"自由定义"的场景（如配置任意数量的映射规则）。
         * </ul>
         *
         * <h3>itemDependencyDepth 参数详解</h3>
         * 当候选集合的生成逻辑需要依赖宿主表单的某个实例时，通过 {@code itemDependencyDepth} 指定层级：
         * <ul>
         *   <li><b>0</b>（默认）：依赖当前字段的直接宿主实例。
         *       例如 {@code ActionParameters.parameters} 依赖 {@code ActionParameters} 实例本身。
         *   <li><b>1</b>：依赖宿主的父级实例。
         *       例如嵌套结构 {@code A.b.c}，字段 {@code c} 的候选集合依赖 {@code A} 实例（跳过中间的 {@code b}）。
         *   <li><b>更大值</b>：依次向上追溯更老的祖先实例。
         * </ul>
         *
         * <h3>前端如何使用 itemDependencyDepth</h3>
         * <pre>{@code
         * // 前端 item-prop-val.component.ts:1310
         * let parentDepth = multiChildViewDescriptor.enumableSetItemDependencyDepth;
         * let parentItem = parentDepth > 0 ? this.parentItems[parentDepth - 1] : this.item;
         * // 将 parentItem 作为 POST body 的 "host" 发给后端
         * }</pre>
         * 后端在 {@link #httpProcess} 中接收到这个 {@code host}，用它实例化宿主对象，
         * 再调用 {@link #getEnumableCandidateSet} 生成候选集合。
         *
         * <h3>典型用例</h3>
         * <pre>{@code
         * // 场景1：参数列表依赖 ActionType
         * public class ActionParameters {
         *     @FormField(ordinal = 0)
         *     public ActionType actionType;  // 决定有哪些参数模板
         *
         *     @FormField(ordinal = 1, type = FormFieldType.MULTI_DESCRIBLE_PLUGIN)
         *     public List<ActionParameterConfig> parameters;  // 从 actionType 的模板中选
         * }
         * // ActionParameterConfig.DftDescriptor:
         * public EnumableSetCfg getEnumableSetCfg() {
         *     return new EnumableSetCfg(true, 0);  // 依赖直接宿主 ActionParameters
         * }
         *
         * // 场景2：属性映射依赖更上层的对象选择器
         * public class ObjectActionType {
         *     @FormField(ordinal = 0)
         *     public ObjectReferenceConfig sourceObject;  // 决定有哪些属性
         *
         *     @FormField(ordinal = 1)
         *     public RuleSetConfig ruleSet;  // 中间层
         *         // RuleSetConfig 内部有：
         *         @FormField(type = FormFieldType.MULTI_DESCRIBLE_PLUGIN)
         *         public List<PropertyMappingConfig> propertyMappings;  // 从 sourceObject 的属性中选
         * }
         * // PropertyMappingConfig.DftDescriptor:
         * public EnumableSetCfg getEnumableSetCfg() {
         *     return new EnumableSetCfg(true, 1);  // 跳过 RuleSetConfig，依赖 ObjectActionType
         * }
         * }</pre>
         *
         * <h3>启用可枚举集合的完整清单</h3>
         * 子类若要启用可枚举集合，需要：
         * <ol>
         *   <li>覆盖本方法，返回 {@code new EnumableSetCfg(true, depth)}
         *   <li>覆盖 {@link #getEnumableCandidateSet}，返回候选列表
         *   <li>（可选）覆盖 {@link #createRelevantFieldKeysCreator}，追加依赖字段到白名单
         * </ol>
         *
         * <h3>默认实现</h3>
         * 返回 {@code new EnumableSetCfg(false)}，表示开放集合（用户手动逐行添加）。
         *
         * @return 可枚举集合配置对象
         * @see EnumableSetCfg
         * @see #getEnumableCandidateSet
         * @see ActionParameterConfig.DftDescriptor#getEnumableSetCfg 实现示例
         */
        public EnumableSetCfg getEnumableSetCfg() {
            return new EnumableSetCfg(false);
        }

        /**
         * 行拖拽排序配置，封装启用状态和目标字段名。
         *
         * @see #isEnableRowDragDrop()
         */
        public static class RowDragDropCfg {
            /**
             * 是否启用行拖拽排序。
             */
            private final boolean enable;
            /**
             * 拖拽后的新顺序值写入哪个字段（如 {@code "order"}、{@code "priority"}）。
             * 该字段必须是元素类上的 {@code @FormField} 字段，类型通常为 {@code Integer}。
             */
            private final String orderByFieldName;

            /**
             * 构造行拖拽配置。
             *
             * @param enable 是否启用拖拽
             * @param orderByFieldName 顺序字段名（启用时不得为空）
             * @throws IllegalStateException 当 {@code enable=true} 但 {@code orderByFieldName} 为空时
             */
            public RowDragDropCfg(boolean enable, String orderByFieldName) {
                this.enable = enable;
                if (enable && StringUtils.isEmpty(orderByFieldName)) {
                    throw new IllegalStateException("param orderByFieldName can not be empty");
                }
                this.orderByFieldName = orderByFieldName;
            }
        }

        /**
         * 可枚举集合配置，封装是否可枚举以及依赖实例的层级深度。
         *
         * @see #getEnumableSetCfg()
         */
        public static class EnumableSetCfg {
            /**
             * 集合是否为可枚举（true = 从候选池勾选，false = 用户手动添加）。
             */
            private final boolean enumableSet;
            /**
             * 候选集合生成逻辑依赖的宿主实例层级：
             * <ul>
             *   <li>0 = 当前字段的直接宿主实例
             *   <li>1 = 宿主的父级实例
             *   <li>2+ = 更上层的祖先实例
             * </ul>
             * 前端会根据此值从 {@code parentItems} 数组中取相应层级的实例，
             * 作为 POST body 的 {@code "host"} 发给后端。
             */
            private final int itemDependencyDepth;

            /**
             * 构造可枚举集合配置（依赖层级默认为 0）。
             *
             * @param enumableSet 是否为可枚举集合
             */
            public EnumableSetCfg(boolean enumableSet) {
                this(enumableSet, 0);
            }

            /**
             * 构造可枚举集合配置（指定依赖层级）。
             *
             * @param enumableSet         是否为可枚举集合
             * @param itemDependencyDepth 依赖的宿主实例层级（0 = 直接宿主，1 = 父级宿主，以此类推）
             */
            public EnumableSetCfg(boolean enumableSet, int itemDependencyDepth) {
                this.enumableSet = enumableSet;
                this.itemDependencyDepth = itemDependencyDepth;
            }

            public boolean isEnumableSet() {
                return enumableSet;
            }

            public int getItemDependencyDepth() {
                return itemDependencyDepth;
            }
        }

        /**
         * 指定前端渲染风格：List（列表）或 Table（表格）。
         *
         * <h3>两种风格的差异</h3>
         * <table>
         *   <tr><th>特性</th><th>List 视图</th><th>Table 视图</th></tr>
         *   <tr><td>组件</td><td>{@code <nz-list>}</td><td>{@code <nz-table>}</td></tr>
         *   <tr><td>显示密度</td><td>松散（每行占据较多垂直空间）</td><td>紧凑（表格行紧密排列）</td></tr>
         *   <tr><td>列配置</td><td>可选（{@link #colsConfig()} 可返回空列表）</td>
         *       <td>必填（至少包含一个 clickable 列）</td></tr>
         *   <tr><td>拖拽排序</td><td>不支持</td><td>支持（{@link #isEnableRowDragDrop()}）</td></tr>
         *   <tr><td>适用场景</td><td>字段少、需要完整显示每项</td><td>字段多、需要快速浏览大量数据</td></tr>
         * </table>
         *
         * <h3>如何选择？</h3>
         * <ul>
         *   <li><b>List</b>：元素类字段 ≤ 3 个，或字段内容较长（如描述文本、多行配置）
         *   <li><b>Table</b>：元素类字段 ≥ 4 个，且多为短文本或枚举，需要快速对比多行数据
         * </ul>
         *
         * <h3>实现示例</h3>
         * <pre>{@code
         * // List 视图（默认）
         * public ViewStyle viewStyle() {
         *     return ViewStyle.List;
         * }
         *
         * // Table 视图
         * public ViewStyle viewStyle() {
         *     return ViewStyle.Table;
         * }
         *
         * @Override
         * public List<ColConfig> colsConfig() {
         *     return Lists.newArrayList(
         *         new ColConfig("parameterId", 15).setClickable(),  // 必须有一列 clickable
         *         new ColConfig("displayName", 15),
         *         new ColConfig("dataType")
         *     );
         * }
         * }</pre>
         *
         * <h3>默认实现</h3>
         * 返回 {@code ViewStyle.List}，适合大多数场景。
         *
         * @return 显示风格枚举
         * @see ViewStyle
         * @see #colsConfig()
         */
        public ViewStyle viewStyle() {
            return ViewStyle.List;
        }

        /**
         * 配置 Table 视图的列定义，List 视图可选。
         *
         * <p>当 {@link #viewStyle()} 返回 {@code ViewStyle.Table} 时，必须覆盖本方法返回非空列表，
         * 且至少有一列被标记为 {@code clickable}（通过 {@code ColConfig#setClickable()}），
         * 否则 {@link #getExtractProps} 会抛出异常。
         *
         * <h3>列配置项</h3>
         * 每个 {@link ColConfig} 包含：
         * <ul>
         *   <li><b>titleKey</b>（必填）：对应元素类中 {@code @FormField} 标注的字段名。
         *       前端会自动从该字段的 descriptor 中提取 {@code label} 作为列标题。
         *   <li><b>width</b>（可选）：列宽百分比（如 {@code 15} 表示占表格宽度的 15%）。
         *       不指定则由前端自动分配剩余空间。
         *   <li><b>clickable</b>（可选）：标记为 {@code true} 的列在前端会渲染为链接，
         *       用户点击后打开详情面板编辑该行。<b>Table 视图至少需要一个 clickable 列</b>。
         * </ul>
         *
         * <h3>实现示例</h3>
         * <pre>{@code
         * @Override
         * public List<ColConfig> colsConfig() {
         *     return Lists.newArrayList(
         *         new ColConfig("parameterId", 15).setClickable(),  // 第1列：参数ID，占15%，可点击
         *         new ColConfig("displayName", 15),                 // 第2列：显示名，占15%
         *         new ColConfig("dataType")                         // 第3列：数据类型，自动宽度
         *     );
         * }
         * }</pre>
         *
         * <h3>列顺序与字段顺序</h3>
         * {@code colsConfig()} 返回的列顺序<b>不必</b>与元素类中 {@code @FormField(ordinal=...)} 的顺序一致。
         * 你可以只选择部分字段显示在表格中（其余字段只在详情面板出现），也可以自由调整列的左右顺序。
         *
         * <h3>List 视图下的行为</h3>
         * 当 {@code viewStyle() == ViewStyle.List} 时，{@code colsConfig()} 的返回值会被忽略，
         * List 视图会显示元素的所有字段（由元素 descriptor 决定）。因此 List 视图下本方法可返回空列表。
         *
         * <h3>默认实现</h3>
         * 返回空列表，仅适用于 List 视图。Table 视图必须覆盖。
         *
         * @return 列配置列表（Table 视图必填，List 视图可选）
         * @see ColConfig
         * @see #viewStyle()
         * @see ActionParameterConfig.DftDescriptor#colsConfig 完整实现示例
         */
        public List<ColConfig> colsConfig() {
            return Collections.emptyList();
        }

        /**
         * Table 视图的列配置，定义列的显示属性。
         *
         * @see #colsConfig()
         */
        public static class ColConfig {
            /**
             * 列对应的字段名，必须是元素类上 {@code @FormField} 标注的字段名。
             * 前端会从该字段的 descriptor 中提取 {@code label} 作为列标题。
             */
            private final String titleKey;
            /**
             * 该列是否可点击（点击后打开详情面板编辑该行）。
             * Table 视图至少需要一列被标记为 {@code clickable=true}。
             * {@code null} 表示未设置（等价于 {@code false}）。
             */
            private Boolean clickable = null;
            /**
             * 列宽百分比（如 {@code 15} 表示占表格总宽度的 15%）。
             * {@code null} 表示由前端自动分配剩余空间。
             */
            private final Integer width;

            /**
             * 构造列配置（自动宽度）。
             *
             * @param titleKey 字段名
             */
            public ColConfig(String titleKey) {
                this(titleKey, null);
            }

            /**
             * 构造列配置（指定宽度）。
             *
             * @param titleKey 字段名
             * @param width 列宽百分比（可为 {@code null}）
             */
            public ColConfig(String titleKey, Integer width) {
                this.titleKey = titleKey;
                this.width = width;
            }

            public Boolean getClickable() {
                return clickable;
            }

            /**
             * 标记本列为可点击（链式调用）。
             *
             * @return 当前 {@code ColConfig} 实例（支持链式调用）
             */
            public ColConfig setClickable() {
                this.clickable = true;
                return this;
            }

            public String getTitleKey() {
                return titleKey;
            }

            public Integer getWidth() {
                return width;
            }
        }

        /**
         * 前端渲染风格枚举。
         *
         * @see #viewStyle()
         */
        public enum ViewStyle {
            /**
             * 列表视图（{@code <nz-list>}）：松散布局，适合字段少或内容长的场景。
             */
            List,
            /**
             * 表格视图（{@code <nz-table>}）：紧凑布局，适合字段多、数据量大、需要快速浏览的场景。
             */
            Table
        }
    }


    interface AfterPluginVerified {
        /**
         * Plugin 验证成功执行回调执行
         */
        void afterVerified(IPluginStoreSave pluginStore);
    }
}
