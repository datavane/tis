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
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.Descriptor.ParseDescribable;
import com.qlangtech.tis.extension.Descriptor.PluginValidateResult;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.ValueChangePipe;
import com.qlangtech.tis.extension.impl.AdapterPluginFormProperties;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.runtime.module.misc.FormVaildateType;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.IUploadPluginMeta;
import com.qlangtech.tis.util.UploadPluginMeta;
import com.qlangtech.tis.util.impl.AttrVals;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-12-07 18:28
 **/
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
         * Plugin 保存执行千回调执行
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
     *
     * @see MultiDescribleElementSetSelector
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

        @Override
        public final void httpProcess(IControlMsgHandler paramGetter, IPluginContext pluginContext, Context context) throws Exception {
            if (context.get(UploadPluginMeta.KEY_PLUGIN_META) == null) {
                List<UploadPluginMeta> metas = pluginContext.getPluginMeta(false);
                for (UploadPluginMeta meta : metas) {
                    context.put(UploadPluginMeta.KEY_PLUGIN_META, meta);
                    break;
                }
            }
            ActionType actionType = ActionType.parse(paramGetter.getString("type"));
            JSONObject postContent = pluginContext.getJSONPostContent();
            switch (actionType) {
                case GetDesc -> {
                    final String toPropertyKey = paramGetter.getString("property");
                    if (StringUtils.isEmpty(toPropertyKey)) {
                        throw new IllegalArgumentException("key property can not be empty");
                    }
                    AttrValMap valMap = AttrValMap.parseDescribableMap(Optional.empty(), postContent);
                    final List<java.lang.String> fromFieldKeys =
                            Lists.newArrayList(valMap.descriptor.getValueChangeFromFieldKeys(toPropertyKey));

                    Optional<PluginFormProperties> propertyTypes =
                            Optional.of(new AdapterPluginFormProperties(valMap.descriptor.getPluginFormPropertyTypes()) {
                                @Override
                                public Set<Map.Entry<String, IPropertyType>> getKVTuples() {
                                    return super.getKVTuples().stream() //
                                            .filter((e) -> fromFieldKeys.contains(e.getKey())).collect(Collectors.toSet());
                                }
                            });
                    // 走带作用域的校验，保证错误信息能定位到具体 item
                    PluginValidateResult validate = valMap.validateWithScope(paramGetter, context, propertyTypes, 0, 0,
                            FormVaildateType.VERIFY);
                    if (!validate.isValid()) {
                        return;
                    }
                    // 创建实例的时候要将toPropertyKey对应的属性设置上值
                    fromFieldKeys.add(toPropertyKey);
                    ParseDescribable<?> describable = valMap.createDescribable(paramGetter, context, propertyTypes);
                    context.put(MultiDescribleElementSetSelector.class.getName(), getEnumableCandidateSet(describable));
                    paramGetter.setBizResult(context, DescriptorsJSON.desc(multiDescribleElementSetDesc));
                }
                case GenerateTargetInstance -> {
                    AttrValMap valMap = AttrValMap.parseDescribableMap(Optional.empty(), postContent.getJSONObject(
                            "host"));
                    ParseDescribable<?> describable = valMap.createDescribable(paramGetter, context);
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
         * 取得目标可选集合在 MultiDescribleElementSetSelector 插件中作为候选列表使用
         *
         * @param describable
         * @return
         * @see MultiDescribleElementSetSelector
         */
        protected List<Pair<Option, MultiDescribleElement>> getEnumableCandidateSet(ParseDescribable<?> describable) {
            return Lists.newArrayList();
        }

        //        @Override
        //        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals
        //        postFormVals) {
        //            postFormVals.newInstance()
        //            return super.validateAll(msgHandler, context, postFormVals);
        //        }

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
            }

            props.put("colsCfg", colsCfg);
            props.put("maxHeight", this.maxHeight());
            if (this.isEnumableSet()) {
                props.put("enumableSet", true);
            }

            return props;
        }

        /**
         * 控制<nz-list/> 和<nz-table/>最大高度，单位：px;
         *
         * @return
         */
        public int maxHeight() {
            return 100;
        }

        /**
         * 集合是否是可枚举的，集合元素是有限的不是开放的（可以无限添加的）
         *
         * @return
         */
        public boolean isEnumableSet() {
            return false;
        }

        /**
         * 取得前端显示风格
         *
         * @return
         */
        public ViewStyle viewStyle() {
            return ViewStyle.List;
        }

        /**
         * 如果当 viewStyle() 返回为Table 则colsConfig 必须要配置
         *
         * @return
         */
        public List<ColConfig> colsConfig() {
            return Collections.emptyList();
        }

        public static class ColConfig {
            /**
             * 对应插件中被 @FormField 渲染的key
             */
            private final String titleKey;
            /**
             * 该列作为可被用户点击的列，点击该列可以更新该条记录
             */
            private Boolean clickable = null;
            /**
             * 对应属性在table列表中显示的宽度，可以为null
             */
            private final Integer width;

            public ColConfig(String titleKey) {
                this(titleKey, null);
            }

            public ColConfig(String titleKey, Integer width) {
                this.titleKey = titleKey;
                this.width = width;
            }

            public Boolean getClickable() {
                return clickable;
            }

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

        public enum ViewStyle {
            List, Table
        }
    }


    interface AfterPluginVerified {
        /**
         * Plugin 验证成功执行回调执行
         */
        void afterVerified(IPluginStoreSave pluginStore);
    }
}
