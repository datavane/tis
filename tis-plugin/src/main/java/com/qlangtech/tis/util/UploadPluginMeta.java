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
package com.qlangtech.tis.util;

import com.google.common.collect.Lists;
import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.coredefine.module.action.ProcessModel;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.IncrSourceExtendSelected;
import com.qlangtech.tis.manage.common.AppAndRuntime;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.datax.SelectedTabExtend;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 解析提交的plugin元数据信息，如果plugin为"xxxplugin:require" 则是在告诉服务端，该plugin必须要有输入内容，该plugin不可缺省
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-20 11:00
 */
public class UploadPluginMeta implements IUploadPluginMeta {

    public static final String KEY_PLUGIN_META = UploadPluginMeta.class.getName();

    public static final String ATTR_KEY_VALUE_SPLIT = "_";

    private static final String KEY_JUST_GET_ITEM_RELEVANT = "justGetItemRelevant";

    private static final Pattern PATTERN_PLUGIN_ATTRIBUTE = Pattern.compile("[" + ATTR_KEY_VALUE_SPLIT + "\\-\\w\\.]+");

    public static final Pattern PATTERN_PLUGIN_ATTRIBUTE_KEY_VALUE_PAIR =
            Pattern.compile("([^" + ATTR_KEY_VALUE_SPLIT + "]+?)" + ATTR_KEY_VALUE_SPLIT + "(" + PATTERN_PLUGIN_ATTRIBUTE.pattern() + ")");

    private static final Pattern PATTERN_PLUGIN_META = Pattern.compile("(.+?)(:(,?(" + PATTERN_PLUGIN_ATTRIBUTE + "))"
            + "+)?");

    public static final String KEY_REQUIRE = "require";

    public static final String KEY_UNCACHE = "uncache";

    //纯添加类型，更新之前需要将之前的类型plugin先load出来再更新
    public static final String KEY_APPEND = "append";
    // "targetDescriptorImpl"
    // 服务端对目标插件的Desc进行过滤
    public static final String KEY_TARGET_PLUGIN_DESC = "targetItemDesc";
    // 目标插件名称
    public static String PLUGIN_META_TARGET_DESCRIPTOR_NAME = "targetDescriptorName";
    public static String PLUGIN_META_TARGET_DESCRIPTOR_IMPLEMENTION = "targetDescriptorImpl";
    public static String PLUGIN_META_TARGET_PIPELINE_NAME_AWARE = "targetPipelineNameAware";
    // 禁止向context中写入biz状态
    public static final String KEY_DISABLE_BIZ_SET = "disableBizStore";


    private final String name;
    private final boolean useCache;

    // plugin form must contain field where prop required is true
    private boolean required;
    // 除去 required 之外的其他参数
    private Map<String, String> extraParams = new HashMap<>();
    private final IPluginContext context;

    public static UploadPluginMeta appnameMeta(IPluginContext pluginContext, String appname) {
        UploadPluginMeta extMeta = parse(pluginContext,
                "name:" + StoreResourceType.DATAX_NAME + "_" + appname, true);
        return extMeta;
    }

    /**
     *
     * @param pluginEnum
     * @return
     */
    public static UploadPluginMeta create(IPluginEnum pluginEnum) {
        return UploadPluginMeta.parse(pluginEnum.identityValue() + ":" + KEY_REQUIRE);
    }


    public boolean isUpdate() {
        return this.getBoolean(DBIdentity.KEY_UPDATE);
    }

    public boolean isUseCache() {
        return this.useCache;
    }

    @Override
    public UploadPluginMeta putExtraParams(String key, String val) {
        if (StringUtils.isEmpty(val)) {
            throw new IllegalArgumentException("key:" + key + " relevant val can not be null");
        }
        this.extraParams.put(key, val);
        return this;
    }

    /**
     * 纯添加类型，更新之前需要将之前的类型plugin先load出来再更新合并之后再更新
     *
     * @return
     */
    public boolean isAppend() {
        return this.getBoolean(KEY_APPEND);
    }

    public boolean isDisableBizSet() {
        return this.getBoolean(KEY_DISABLE_BIZ_SET);
    }

    public ProcessModel getProcessModel() {

        return ProcessModel.parse(this.getExtraParam(StoreResourceType.KEY_PROCESS_MODEL));
    }


    public static void main(String[] args) throws Exception {

        Matcher matcher = PATTERN_PLUGIN_ATTRIBUTE_KEY_VALUE_PAIR.matcher("dsname_dsname_yuqing_zj2_bak");

        System.out.println(matcher.matches());
        System.out.println(matcher.group(1));
        System.out.println(matcher.group(2));

    }

    public static List<UploadPluginMeta> parse(String[] plugins) {
        return parse(null, plugins);
    }

    public static List<UploadPluginMeta> parse(IPluginContext context, String[] plugins) {
        return parse(context, plugins, true, true).stream().map((meta) -> (UploadPluginMeta) meta).collect(Collectors.toList());
    }

    public static List<IUploadPluginMeta> parse(String[] plugins, boolean useCache) {
        return parse(null, plugins, useCache, true);
    }

    public static List<IUploadPluginMeta> parse(
            IPluginContext context, String[] plugins, boolean useCache) {
        return parse(context, plugins, useCache, true);
    }

    public static List<IUploadPluginMeta> parse(
            IPluginContext context, String[] plugins, boolean useCache, boolean validatePluginEmpty) {
        if (validatePluginEmpty && (plugins == null || plugins.length < 1)) {
            throw new IllegalArgumentException("plugin size:" + plugins.length + " length can not small than 1");
        }
        List<IUploadPluginMeta> metas = Lists.newArrayList();
        for (String plugin : plugins) {
            metas.add(parse(context, plugin, useCache));
        }
        if (plugins.length != metas.size()) {
            throw new IllegalStateException("param plugins length:" + plugins.length + " must equal with metaSize:" + metas.size());
        }
        return metas;
    }

    public IPluginContext getPluginContext() {
        return this.context;
    }


    public static UploadPluginMeta parse(String plugin, boolean useCache) {
        return parse(null, plugin, useCache);
    }

    public static UploadPluginMeta parse(String plugin) {
        return parse(null, plugin, true);
    }

    /**
     * @param plugin
     * @return
     */
    public static UploadPluginMeta parse(IPluginContext context, String plugin, boolean useCache) {
        Matcher matcher, attrKVMatcher;
        UploadPluginMeta pmeta;
        Matcher attrMatcher;
        String attr;

        if (StringUtils.endsWith(plugin, ",")) {
            throw new IllegalStateException("plugin:'" + plugin + "' can not endWith ','");
        }

        matcher = PATTERN_PLUGIN_META.matcher(plugin);
        if (matcher.matches()) {
            pmeta = new UploadPluginMeta(context, matcher.group(1), useCache);
            if (matcher.group(2) != null) {
                attrMatcher = PATTERN_PLUGIN_ATTRIBUTE.matcher(matcher.group(2));
                while (attrMatcher.find()) {
                    attr = attrMatcher.group();
                    switch (attr) {
                        case KEY_REQUIRE: {
                            pmeta.required = true;
                            break;
                        }
                        default: {
                            attrKVMatcher = PATTERN_PLUGIN_ATTRIBUTE_KEY_VALUE_PAIR.matcher(attr);
                            if (!attrKVMatcher.matches()) {
                                throw new IllegalStateException("attr:" + attr + " is not match:" + PATTERN_PLUGIN_ATTRIBUTE_KEY_VALUE_PAIR.pattern());
                            }
                            pmeta.putExtraParams(attrKVMatcher.group(1), attrKVMatcher.group(2));
                            // pmeta.extraParams.put(attrKVMatcher.group(1), attrKVMatcher.group(2));
                        }
                    }
                }
            }

            /**
             // 为了在dataX创建流程中，能够在流程中使用IControlMsgHandler.isCollectionAware() 为true ，例如： DataXKafkaReader.createDataXKafkaReader() 方法中
             // 需要在这里将当前的appAndRuntime 设置到当前的线程上下文中去。
             // 后续 CheckAppDomainExistValve.getAppDomain() 方法执行就能获得有appName aware的实例了
             */
            final DataXName pipe = pmeta.getDataXName(false);
            AppAndRuntime appAndRuntime = AppAndRuntime.getAppAndRuntime();
            if (pipe != null &&
                    (appAndRuntime == null || (appAndRuntime.getAppName()) == null)) {
                appAndRuntime = new AppAndRuntime();
                appAndRuntime.setRuntime(RunEnvironment.getSysRuntime());
                appAndRuntime.setAppName(pipe);
                // StoreResourceType type = pipe.getType();

                AppAndRuntime.setAppAndRuntime(appAndRuntime);
                //CheckAppDomainExistValve
            }

            return pmeta;
            //metas.add(pmeta);
        } else {
            throw new IllegalStateException("plugin:'" + plugin + "' is not match the pattern:" + PATTERN_PLUGIN_META);
        }
    }

    public Pair<List<DataxReader>, IPluginStore<DataxReader>> getDataxReaders(IPluginContext pluginContext) {
//        return HeteroEnum.DATAX_READER.getPlugins(pluginContext, UploadPluginMeta.parse(pluginContext,
//                this.name + ":" + DataxUtils.DATAX_NAME + "_" + this.getDataXName(),
//                useCache));
        // this.getDataXName()
        IPluginStore<DataxReader> store = (IPluginStore<DataxReader>) HeteroEnum.getDataXReaderAndWriterRelevantPluginStore(
                pluginContext, true, this);
        return Pair.of(store.getPlugins(), store);

    }

    public IPluginEnum getHeteroEnum() {

        Optional<SubFormFilter> subFormFilter = null;

        subFormFilter = this.getSubFormFilter();
        if (subFormFilter.isPresent()) {
            SubFormFilter subFilter = subFormFilter.get();
            final boolean[] incrExtend = new boolean[1];
            if ((incrExtend[0] = subFilter.isIncrProcessExtend()) || subFilter.isBatchSourceProcessExtend()) {

                SelectedTabExtend.IncrTabExtendSuit incrTabExtendSuit =
                        SelectedTabExtend.getIncrTabExtendSuit(incrExtend[0], this);

                HeteroEnum<MQListenerFactory> mq = HeteroEnum.MQ;
                return new HeteroEnum(mq.extensionPoint, mq.identity, mq.caption, mq.selectable, mq.isAppNameAware()) {
                    @Override
                    public List getPlugins(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
                        if (subFilter.subformDetailView) {
                            SelectedTab ext = null;
                            Map<String, SelectedTab> tabsExtend = SelectedTabExtend.getTabExtend(pluginMeta);
                            final String subformDetailId = subFilter.subformDetailId;
                            ext = tabsExtend.get(subformDetailId);
                            if (ext == null) {
                                return Collections.emptyList();
                            }
                            return incrExtend[0] ? ext.getIncrExtProp() :
                                    Collections.singletonList(ext.getSourceProps());
                        }

                        //  throw new IllegalStateException("subFilter.subformDetailView shall be true");
                        return pluginMeta.getDataxReaders(pluginContext).getKey();
                    }

//                    private List<DataxReader> getDataxReaders(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
//                        return DATAX_READER.getPlugins(pluginContext, UploadPluginMeta.parse(pluginContext,
//                                pluginMeta.name + ":" + DataxUtils.DATAX_NAME + "_" + pluginMeta.getDataXName(),
//                                useCache));
//                    }

                    @Override
                    public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
                        return (incrExtend[0] ? SelectedTabExtend.INCR_SELECTED_TAB_EXTEND :
                                SelectedTabExtend.BATCH_SOURCE_SELECTED_TAB_EXTEND) //
                                .getPluginStore(pluginContext, pluginMeta);

                    }

                    @Override
                    public List<Descriptor> descriptors() {
                        Descriptor selectedTabClassDesc =
                                TIS.get().getDescriptor(IncrSourceExtendSelected.selectedTabClass);
                        return subFilter.subformDetailView ? incrTabExtendSuit.getDescriptors() :
                                incrTabExtendSuit.getDescriptorsWithAppendDesc(selectedTabClassDesc);
                    }


                };
            }
        }

        return HeteroEnum.of(this.getName());
    }


    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }


    public Optional<SubFormFilter> getSubFormFilter() {

        TargetDesc targetDesc = this.getTargetDesc();
        String subFormField = this.getExtraParam(SubFormFilter.PLUGIN_META_SUB_FORM_FIELD);
        if (StringUtils.isNotEmpty(targetDesc.descDisplayName) && StringUtils.isNotEmpty(subFormField)) {
            return Optional.of(new SubFormFilter(this, targetDesc //, targetDescImpl
                    , subFormField));
        }
        return Optional.empty();
    }

    public static class TargetDesc {
        public final String descDisplayName;
        public final String impl;
        public final String matchTargetPluginDescName;
        private final boolean pipelineNameAware;

        public static final TargetDesc create(UploadPluginMeta meta) {
            return new TargetDesc(
                    // prop:matchTargetPluginDescName targetItemDesc
                    meta.getExtraParam(KEY_TARGET_PLUGIN_DESC) //
                    // targetDescriptorName
                    , meta.getExtraParam(PLUGIN_META_TARGET_DESCRIPTOR_NAME) //
                    // targetDescriptorImpl
                    , meta.getExtraParam(PLUGIN_META_TARGET_DESCRIPTOR_IMPLEMENTION)
                    , meta.getBoolean(PLUGIN_META_TARGET_PIPELINE_NAME_AWARE)
            );
        }

        public String pluginStoreGroupPath(UploadPluginMeta meta) {
            StringBuffer result = new StringBuffer(matchTargetPluginDescName);
            if (pipelineNameAware) {
                result.append(File.separator).append(
                        Objects.requireNonNull(meta, "meta can not be null").getPluginContext().getCollectionName());
            }
            return result.toString();
        }

        public Descriptor getTargetDescriptor() {
            Descriptor parentDesc = Objects.requireNonNull(TIS.get().getDescriptor(this.impl),
                    this + "->" + this.impl + " relevant desc can not be null");
            return parentDesc;
        }

        private TargetDesc(String matchTargetPluginDescName, String name, String impl, boolean pipelineNameAware) {
            this.matchTargetPluginDescName = matchTargetPluginDescName;
            this.descDisplayName = name;
            this.impl = impl;
            this.pipelineNameAware = pipelineNameAware;
        }

        public boolean shallMatchTargetDesc() {
            return StringUtils.isNotEmpty(this.matchTargetPluginDescName);
        }

        public boolean isNameMatch(String displayName) {
            return SubFormFilter.KEY_INCR_PROCESS_EXTEND.equals(matchTargetPluginDescName)  //
                    || StringUtils.equals(displayName, this.matchTargetPluginDescName);
        }

        @Override
        public String toString() {
            return "TargetDesc{" + "descDisplayName='" + descDisplayName + '\'' + ", impl='" + impl + '\'' + ", " +
                    "matchTargetPluginDescName='" + matchTargetPluginDescName + '\'' + '}';
        }
    }


    public TargetDesc getTargetDesc() {
        return TargetDesc.create(this);
    }

    public String getExtraParam(String key) {
        return this.extraParams.get(key);
    }


    public DataXName getDataXName(boolean validateNull) {
        final String dataxName = (this.getExtraParam(StoreResourceType.DATAX_NAME));
        if (StringUtils.isNotEmpty(dataxName)) {
            //
            //  return DataXName.createDataXPipeline(dataxName);
            return new DataXName(dataxName, this.getProcessModel().resType);
        }
        String dbName = this.getExtraParam(StoreResourceType.DATAX_DB_NAME);
        if (StringUtils.isNotEmpty(dbName)) {
            return new DataXName(dbName, StoreResourceType.DataBase);
        }

        if (validateNull) {
            throw new IllegalArgumentException("plugin extra param 'DataxUtils.DATAX_NAME'" + StoreResourceType.DATAX_NAME + " can not be null");
        }
        return null;
    }

    public DataXName getDataXName() {
        StoreResourceType resType = this.getProcessModel().resType;
        DataXName dataXName = getDataXName(true);
        if (dataXName.getType() != resType) {
            throw new IllegalStateException("dataXName.getType():"
                    + dataXName.getType() + " must be equal with resType:" + resType);
        }
        return dataXName;
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(this.getExtraParam(key));
    }

    /**
     * @param context
     * @param name
     * @param useCache 服务端会将某些资源进行缓存，客户端在取用之时，可以将最新的缓存失效，进而取用最新数据
     */
    private UploadPluginMeta(IPluginContext context, String name, boolean useCache) {
        this.name = name;
        this.context = context;
        this.useCache = useCache;
    }

    @Override
    public String toString() {
        return "UploadPluginMeta{" + "name='" + name + '\'' + ", required=" + required + "," + this.extraParams.entrySet().stream().map((e) -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(",")) + '}';
    }

    public <T extends Describable<T>> HeteroList<T> createEmptyItemAndDescriptorsHetero() {
        IPluginEnum hEnum = getHeteroEnum();
        HeteroList<T> hList = new HeteroList<>(this);
        hList.setCaption(hEnum.getCaption());
        hList.setIdentityId(hEnum.getIdentity());
        hList.setExtensionPoint(hEnum.getExtensionPoint());
        hList.setSelectable(hEnum.getSelectable());
        return hList;
    }


    public <T extends Describable<T>> HeteroList<T> getHeteroList(IPluginContext pluginContext) {
        IPluginEnum hEnum = getHeteroEnum();
        HeteroList<T> hList = createEmptyItemAndDescriptorsHetero();

        List<T> items = hEnum.getPlugins(pluginContext, this);
        hList.setItems(items);

        final TargetDesc targetDesc = this.getTargetDesc();
        boolean justGetItemRelevant = Boolean.parseBoolean(this.getExtraParam(KEY_JUST_GET_ITEM_RELEVANT));
        List<Descriptor<T>> descriptors = hEnum.descriptors(targetDesc, items, justGetItemRelevant);


//        if (targetDesc.shallMatchTargetDesc()) {
//            descriptors =
//                    descriptors.stream().filter((desc) -> targetDesc.isNameMatch(desc.getDisplayName())).collect(Collectors.toList());
//        } else {
//            boolean justGetItemRelevant = Boolean.parseBoolean(this.getExtraParam(KEY_JUST_GET_ITEM_RELEVANT));
//            if (justGetItemRelevant) {
//                Set<String> itemRelevantDescNames =
//                        items.stream().map((i) -> i.getDescriptor().getDisplayName()).collect(Collectors.toSet());
//                descriptors =
//                        descriptors.stream().filter((d) -> itemRelevantDescNames.contains(d.getDisplayName())).collect(Collectors.toList());
//            } else if (StringUtils.isNotEmpty(targetDesc.descDisplayName)) {
//                descriptors =
//                        descriptors.stream().filter((d) -> targetDesc.descDisplayName.equals(d.getDisplayName())).collect(Collectors.toList());
//            }
//        }
        hList.setDescriptors(descriptors);


        return hList;
    }
}
