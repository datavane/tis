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
package com.qlangtech.tis.datax.impl;

import com.alibaba.datax.plugin.writer.hdfswriter.HdfsColMeta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IStreamTableMeta;
import com.qlangtech.tis.datax.TableAlias;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.IncrSourceExtendSelected;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.CompanionPluginFactory;
import com.qlangtech.tis.plugin.IDataXEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.datax.SelectedTabExtend;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.TableNotFoundException;
import com.qlangtech.tis.plugin.incr.ISelectedTabExtendFactory;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * datax Reader
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:48
 */
@Public
public abstract class DataxReader implements Describable<DataxReader>, IDataxReader, IPluginStore.RecyclableController {
    private static final Logger logger = LoggerFactory.getLogger(DataxReader.class);
    public static final String HEAD_KEY_REFERER = "Referer";
    public static final String SUB_PROP_FIELD_NAME = "selectedTabs";
    public static final ThreadLocal<DataxReader> dataxReaderThreadLocal = new ThreadLocal<>();

    public static DataxReader getThreadBingDataXReader() {
        DataxReader reader = dataxReaderThreadLocal.get();
        return reader;
    }


    public static <T extends DataxReader> T getDataxReader(SubFormFilter filter) {
        return getDataxReader(filter.uploadPluginMeta);
    }

    public static <T extends DataxReader> T getDataxReader(UploadPluginMeta uploadPluginMeta) {
        IPluginStore<?> pluginStore = HeteroEnum.getDataXReaderAndWriterStore(uploadPluginMeta.getPluginContext(),
                true, uploadPluginMeta);
        DataxReader reader = (DataxReader) pluginStore.getPlugin();
        if (reader == null) {
            throw new IllegalStateException("dataXReader can not be null:" + uploadPluginMeta.toString());
        }
        return (T) reader;
    }

    public static Optional<Descriptor<SelectedTabExtend>> getBatchSourceSelectedTabExtendDescriptor(UploadPluginMeta uploadPluginMeta) {

        DataxReader dataxReader = getDataxReader(uploadPluginMeta);

        Descriptor<DataxReader> descriptor = dataxReader.getDescriptor();
        if (!(descriptor instanceof ISelectedTabExtendFactory)) {
            //            throw new IllegalStateException("descriptor:" + descriptor.getClass().getName() +
            // " must be instance of "
            //                    + IIncrSourceSelectedTabExtendFactory.class.getName());
            return Optional.empty();
        }
        // Field subFormField, Class instClazz, Descriptor subFormFieldsDescriptor
        Descriptor<SelectedTabExtend> selectedTableExtendDesc =
                ((ISelectedTabExtendFactory) descriptor).getSelectedTableExtendDescriptor();

        return Optional.ofNullable(selectedTableExtendDesc);

    }


    private transient LoadingCache<String, IStreamTableMeta> tabMetaCache;

    /**
     * 根据ISelectedTab 定义的默认值初始化一个默认的表实例
     *
     * @param pluginContext
     * @param selectedTabs
     * @param pluginMeta
     * @param tabMetaConsumer
     * @return
     */
    public List<ISelectedTab> createDefaultTables(IPluginContext pluginContext, List<String> selectedTabs
            , UploadPluginMeta pluginMeta, Consumer<Map.Entry<String /*tableName*/, List<ColumnMetaData>>> tabMetaConsumer, boolean validateMapColsNull) {
        List<ISelectedTab> allNewTabs = Lists.newArrayList();
        Map<String, List<ColumnMetaData>> mapCols;
        mapCols = selectedTabs.stream().collect(Collectors.toMap((tab) -> tab, (tab) -> {
            try {
                return this.getTableMetadata(false, pluginContext, EntityName.parse(tab));
            } catch (TableNotFoundException e) {
                throw new RuntimeException(e);
            }
        }));
        if (MapUtils.isEmpty(mapCols)) {
            if (validateMapColsNull) {
                throw new IllegalStateException("mapCols can not be empty");
            }
            return Collections.emptyList();
        }
        PluginFormProperties pluginFormPropertyTypes = this.getDescriptor().getPluginFormPropertyTypes(pluginMeta.getSubFormFilter());
        for (Map.Entry<String /*tableName*/, List<ColumnMetaData>> tab2cols : mapCols.entrySet()) {
            try {
                SuFormProperties.setSuFormGetterContext(this, pluginMeta, tab2cols.getKey());
                allNewTabs.add(createNewSelectedTab(pluginFormPropertyTypes, tab2cols));
                // 需要将desc中的取option列表解析一下（JsonUtil.UnCacheString）
                tabMetaConsumer.accept(tab2cols);
            } finally {
                SuFormProperties.subFormGetterProcessThreadLocal.remove();
            }
        }
        return allNewTabs;
    }

    /**
     * 通过表名和列创建新tab实例，如果SelectedTab对象中有其他字段但是没有设置默认值，创建过程中就会出错
     *
     * @param pluginFormPropertyTypes
     * @param tab2cols
     * @return
     */
    private static ISelectedTab createNewSelectedTab(PluginFormProperties pluginFormPropertyTypes //
            , Map.Entry<String, List<ColumnMetaData>> tab2cols) {
        return pluginFormPropertyTypes.accept(new PluginFormProperties.IVisitor() {

            @Override
            public ISelectedTab visit(BaseSubFormProperties props) {

                try {
                    SelectedTab subForm = props.newSubDetailed();

                    fillDefaultVals(props, subForm);

                    Descriptor parentDesc = props.getParentPluginDesc();

                    if (parentDesc instanceof CompanionPluginFactory) {
                        Descriptor<Describable> companionDesc = ((CompanionPluginFactory) parentDesc).getCompanionDescriptor();
                        SelectedTabExtend tabExt = (SelectedTabExtend) companionDesc.clazz.newInstance();
                        fillDefaultVals(companionDesc.getPluginFormPropertyTypes(), tabExt);
                        subForm.setSourceProps(tabExt);
                    }

                    return subForm;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


            }

            private void fillDefaultVals(PluginFormProperties props, Describable subForm) {
                Set<Map.Entry<String, PropertyType>> kvs = props.getKVTuples();
                PropertyType pp = null;

                final Set<String> skipProps = Sets.newHashSet();
                ppDftValGetter:
                for (Map.Entry<String, PropertyType> pentry : kvs) {
                    pp = pentry.getValue();
                    if (pp.isIdentity()) {
                        pp.setVal(subForm, tab2cols.getKey());
                        skipProps.add(pentry.getKey());
                        continue;
                    }

                    if (pp.formField.type() == FormFieldType.MULTI_SELECTABLE) {
                        skipProps.add(pentry.getKey());
                        pp.setVal(subForm, tab2cols.getValue().stream() //
                                .map(ColumnMetaData::convert).collect(Collectors.toList()));
                        continue ppDftValGetter;
                    }
                }

                createPluginByDefaultVals(new StringBuffer(subForm.getClass().getName()), skipProps, kvs, subForm);
            }

            private Describable createPluginByDefaultVals(StringBuffer propPath, final Set<String> skipProps,
                                                          Set<Map.Entry<String, PropertyType>> kvTuples, Describable plugin) {
                PropertyType pp = null;
                ppDftValGetter:
                for (Map.Entry<String, PropertyType> pentry : kvTuples) {
                    pp = pentry.getValue();
                    if (skipProps.contains(pentry.getKey())) {
                        continue;
                    }


                    if (pp.dftVal() != null) {
                        if (pp.isDescribable()) {
                            List<? extends Descriptor> descriptors = pp.getApplicableDescriptors();
                            try {
                                for (Descriptor desc : descriptors) {
                                    if (StringUtils.endsWithIgnoreCase(String.valueOf(pp.dftVal()), desc.getDisplayName())) {
                                        pp.setVal(plugin, createPluginByDefaultVals((new StringBuffer(propPath)).append("->") //
                                                        .append(pentry.getKey()).append(":").append(pp.fieldClazz.getName()) //
                                                , Sets.newHashSet() //
                                                , desc.getPluginFormPropertyTypes().getKVTuples() //
                                                , (Describable) desc.clazz.newInstance()));
                                        continue ppDftValGetter;
                                    }
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            pp.setVal(plugin, pp.dftVal());
                            continue ppDftValGetter;
                        }


                    } else {


                        FormFieldType fieldType = pp.formField.type();
                        if (fieldType == FormFieldType.SELECTABLE || fieldType == FormFieldType.ENUM) {
                            List<Option> propOptions = pp.getEnumPropOptions();

                            for (int i = 0; i < propOptions.size(); i++) {
                                Option opt = propOptions.get(i);
                                pp.setVal(plugin, opt.getValue());
                                continue ppDftValGetter;
                            }
                        }
                    }

                    if (pp.isInputRequired()) {
                        throw new IllegalStateException("have not prepare for table:" + tab2cols.getKey()
                                + " creating:" + propPath + ",prop name:'" + pentry.getKey() + "',subform class:" + plugin.getClass().getName());
                    }
                }
                return plugin;
            }
        });
    }

    @Override
    public final IStreamTableMeta getStreamTableMeta(TableAlias tableAlias) {
        final String tableName = tableAlias.getFrom();
        if (this.tabMetaCache == null) {
            tabMetaCache = CacheBuilder.newBuilder().expireAfterWrite(40, TimeUnit.SECONDS).build(new CacheLoader<String, IStreamTableMeta>() {
                @Override
                public IStreamTableMeta load(String tableName) throws Exception {
                    try {
                        List<ColumnMetaData> cols = getTableMetadata(false, null, EntityName.parse(tableName));
                        final List<IColMetaGetter> colsMeta = cols.stream().map((c) -> new HdfsColMeta(c.getName(), c.isNullable(), c.isPk(),
                                c.getType())).collect(Collectors.toList());
                        return new IStreamTableMeta() {
                            @Override
                            public List<IColMetaGetter> getColsMeta() {
                                return colsMeta;
                            }
                        };
                    } catch (TableNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        try {
            return tabMetaCache.get(tableName);
        } catch (ExecutionException e) {
            throw new RuntimeException("tableName:" + tableName, e);
        }
    }

    @Override
    public void refresh() {

    }

    public static KeyedPluginStore<DataxReader> getPluginStore(IPluginContext pluginContext, String appname) {
        return getPluginStore(pluginContext, false, appname);
    }

    /**
     * @param pluginContext
     * @param db            是否是db相关配置
     * @param appname
     * @return
     */
    public static KeyedPluginStore<DataxReader> getPluginStore(IPluginContext pluginContext, boolean db,
                                                               String appname) {
        return TIS.dataXReaderPluginStore.get(createDataXReaderKey(pluginContext, db, appname));
    }

    public static void cleanPluginStoreCache(IPluginContext pluginContext, boolean db, String appname) {
        TIS.DataXReaderAppKey appKey = createDataXReaderKey(pluginContext, db, appname);
        TIS.dataXReaderPluginStore.clear(appKey);
        if (pluginContext != null) {
            // 保证在DataXAction+doUpdateDatax 方法中两次调用以下方法块只被调用一次
            return;
        }
        // 需要将非编辑模式下的对象也跟新一下=====================================
        Set<Map.Entry<SubFieldFormAppKey<? extends Describable>, KeyedPluginStore<? extends Describable>>> entries =
                TIS.dataXReaderSubFormPluginStore.getEntries();
        List<SubFieldFormAppKey<? extends Describable>> willDelete = Lists.newArrayList();
        entries.forEach((e) -> {
            SubFieldFormAppKey key = e.getKey();
            if (StringUtils.equals(key.keyVal.getVal(), appname) && key.isDB() == db) {
                willDelete.add(key);
            }
        });
        willDelete.forEach((deleteKey) -> {
            TIS.dataXReaderSubFormPluginStore.clear(deleteKey);
        });
    }

    private static TIS.DataXReaderAppKey createDataXReaderKey(IPluginContext pluginContext, boolean db,
                                                              String appname) {
        final TIS.DataXReaderAppKey key = new TIS.DataXReaderAppKey(pluginContext, db, appname,
                new PluginStore.IPluginProcessCallback<DataxReader>() {
                    @Override
                    public void afterDeserialize(PluginStore<DataxReader> ps, final DataxReader reader) {

                        List<PluginFormProperties> subFieldFormPropertyTypes =
                                reader.getDescriptor().getSubPluginFormPropertyTypes();
                        if (subFieldFormPropertyTypes.size() > 0) {
                            // 加载子字段
                            subFieldFormPropertyTypes.forEach((pt) -> {
                                pt.accept(new PluginFormProperties.IVisitor() {
                                    @Override
                                    public Void visit(final BaseSubFormProperties props) {
                                        SubFieldFormAppKey<? extends Describable> subFieldKey =
                                                new SubFieldFormAppKey<>(pluginContext, db, appname, props, DataxReader.class);

                                        KeyedPluginStore<? extends Describable> subFieldStore = KeyedPluginStore.getPluginStore(subFieldKey);

//                                        UploadPluginMeta extMeta = UploadPluginMeta.parse(pluginContext,
//                                                "name:" + DataxUtils.DATAX_NAME + "_" + appname, true);
                                        Map<String, SelectedTab> tabsExtend = SelectedTabExtend.getTabExtend(pluginContext, appname);
                                        // 子表单中的内容更新了之后，要同步父表单中的状态
                                        subFieldStore.addPluginsUpdateListener(new PluginStore.PluginsUpdateListener(subFieldKey.getSerializeFileName(), reader) {
                                            @Override
                                            public void accept(PluginStore<Describable> pluginStore) {
                                                // logger.info("execute setReaderSubFormProp2,subitem count:" + pluginStore.getPlugins().size() + ",extendCount:" + tabsExtend.size());
                                                setReaderSubFormProp(props, pluginStore.getPlugins(), Collections.emptyMap() /**千万要为emptyMap()，不然在和上面getTabExtend中的listener的回调重复了，且会覆盖脏数据*/);
                                            }
                                        });
                                        List<? extends Describable> subItems = subFieldStore.getPlugins();
                                        if (CollectionUtils.isEmpty(subItems)) {
                                            return null;
                                        }
                                        setReaderSubFormProp(props, subItems, tabsExtend);
                                        return null;
                                    }

                                    private void setReaderSubFormProp(BaseSubFormProperties props,
                                                                      List<? extends Describable> subItems //
                                            , Map<String, SelectedTab> subItemsExtend) {
                                        setReaderSubFormProp(props, reader, subItems, subItemsExtend);
                                    }

                                    private void setReaderSubFormProp(BaseSubFormProperties props, DataxReader reader, List<?
                                            extends Describable> subItems, Map<String, SelectedTab> subItemsExtend) {
                                        if (reader == null) {
                                            return;
                                        }
                                        subItems.forEach((item) -> {
                                            if (!props.instClazz.isAssignableFrom(item.getClass())) {
                                                throw new IllegalStateException("appname:" + appname
                                                        + ",item class[" + item.getClass().getSimpleName() + "] is not type of " + props.instClazz.getName());
                                            }
                                            if (item instanceof SelectedTab) {
                                                SelectedTab tab = ((SelectedTab) item);
                                                SelectedTab ext = subItemsExtend.get(tab.identityValue());
                                                if (ext != null) {
                                                    tab.setIncrSourceProps(ext.getIncrSourceProps());
                                                    tab.setIncrSinkProps(ext.getIncrSinkProps());
                                                    tab.setSourceProps(ext.getSourceProps());
                                                }
                                            }
                                        });
                                        try {
                                            props.subFormField.set(reader, subItems);
                                        } catch (IllegalAccessException e) {
                                            throw new RuntimeException("get subField:" + props.getSubFormFieldName(), e);
                                        }
                                    }
                                });
                            });
                        }
                    }
                });
        return key;
    }


    public interface IDataxReaderGetter {
        DataxReader get(String appName);
    }

    /**
     * 测试用
     */
    public static IDataxReaderGetter dataxReaderGetter;

    public static DataxReader load(IPluginContext pluginContext, String appName) {
        return load(pluginContext, false, appName);
    }

    /**
     * load
     *
     * @param appName
     * @return
     */
    public static DataxReader load(IPluginContext pluginContext, boolean isDB, String appName) {

        DataxReader reader = null;
        if (dataxReaderGetter != null) {
            reader = dataxReaderGetter.get(appName);
            DataxReader.dataxReaderThreadLocal.set(reader);
            return reader;
        }

        reader = getPluginStore(pluginContext, isDB, appName).getPlugin();
        Objects.requireNonNull(reader, "appName:" + appName + " relevant appSource can not be null");
        DataxReader.dataxReaderThreadLocal.set(reader);
        return reader;
    }

    public static class SubFieldFormAppKey<TT extends Describable> extends KeyedPluginStore.AppKey<TT> {
        public final BaseSubFormProperties subfieldForm;

        /**
         * @param pluginContext
         * @param isDB
         * @param appname
         * @param subfieldForm
         * @param clazz
         */
        public SubFieldFormAppKey(IPluginContext pluginContext, boolean isDB, String appname,
                                  BaseSubFormProperties subfieldForm, Class<TT> clazz) {
            super(pluginContext, StoreResourceType.parse(isDB), Objects.requireNonNull(appname,
                    "appname can not be " + "empty"), clazz);
            this.subfieldForm = subfieldForm;
        }

        @Override
        public String getSerializeFileName() {
            //  return this.getSubDirPath() + File.separator + this.subfieldForm.getSubFormFieldName() + File.separator + subformDetailId;
            return super.getSerializeFileName() + "." + this.subfieldForm.getSubFormFieldName();
        }
    }

    private transient boolean dirty = false;

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void signDirty() {
        // 标记可以在PluginStore中被剔出了
        this.dirty = true;
    }

    @Override
    public final Descriptor<DataxReader> getDescriptor() {
        Descriptor<DataxReader> descriptor = TIS.get().getDescriptor(this.getClass());
        Objects.requireNonNull(descriptor, "class:" + this.getClass() + " relevant descriptor can not be null");
        Class<BaseDataxReaderDescriptor> expectClass = getExpectDescClass();
        if (!(expectClass.isAssignableFrom(descriptor.getClass()))) {
            throw new IllegalStateException(descriptor.getClass() + " must implement the Descriptor of " + expectClass.getName());
        }

        BaseDataxReaderDescriptor readDesc = (BaseDataxReaderDescriptor) descriptor;
        if (readDesc.isRDBMSChangeableInLifetime() ^ (this instanceof DataXBasicProcessMeta.IRDBMSSupport)) {

            throw new IllegalStateException(this.getClass().getSimpleName() + " bool status shall be same:\n" + " " + "readDesc.isRDBMSChangeableInLifetime(): " + readDesc.isRDBMSChangeableInLifetime() + "\n (" + this.getClass().getSimpleName() + " instanceof DataXBasicProcessMeta.IRDBMSSupport):" + (this instanceof DataXBasicProcessMeta.IRDBMSSupport));

        }
        return descriptor;
    }

    protected <TT extends DataxReader.BaseDataxReaderDescriptor> Class<TT> getExpectDescClass() {
        return (Class<TT>) BaseDataxReaderDescriptor.class;
    }

    @Override
    public final boolean isSupportBatch() {
        return ((BaseDataxReaderDescriptor) this.getDescriptor()).isSupportBatch();
    }

    public static abstract class BaseDataxReaderDescriptor extends Descriptor<DataxReader> implements IDataXEndTypeGetter {
        @Override
        public PluginFormProperties getPluginFormPropertyTypes(Optional<SubFormFilter> subFormFilter) {
            SubFormFilter filter = null;
            if (subFormFilter.isPresent()) {
                filter = subFormFilter.get();
                if (filter.isIncrProcessExtend()) {
                    Descriptor parentDesc = filter.getTargetDescriptor();
                    SuFormProperties subProps =
                            (SuFormProperties) parentDesc.getSubPluginFormPropertyTypes(filter.subFieldName);
                    return new IncrSourceExtendSelected(filter.uploadPluginMeta, subProps.subFormField);
                }
            }

            return super.getPluginFormPropertyTypes(subFormFilter);
        }

        @Override
        public final PluginVender getVender() {
            return PluginVender.DATAX;
        }

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = super.getExtractProps();
            eprops.put("rdbms", this.isRdbms());
            eprops.put(KEY_SUPPORT_INCR, this.isSupportIncr());
            eprops.put(KEY_SUPPORT_BATCH, this.isSupportBatch());

            // this.getEndType().appendProps(eprops);
//            eprops.put(KEY_END_TYPE, this.getEndType().getVal());
//            eprops.put(KEY_SUPPORT_ICON, this.getEndType().getIcon() != null);
            return eprops;
        }

        /**
         * 是否支持DataX 批量执行
         *
         * @return
         */
        @Override
        public boolean isSupportBatch() {
            return true;
        }

        /**
         * 像Mysql会有明确的表名，而OSS没有明确的表名,RDBMS 关系型数据库 应该都为true
         *
         * @return
         */
        public boolean hasExplicitTable() {
            return this.isRdbms();
        }

        /**
         * 是否可以选择多个表，像Mysql这样的 ,RDBMS 关系型数据库 应该都为true
         *
         * @return
         */
        public abstract boolean isRdbms();

        /**
         * 需要使DataX Reader实现 接口：DataXBasicProcessMeta.IRDBMSSupport
         *
         * @return
         * @see DataXBasicProcessMeta.IRDBMSSupport
         */
        public boolean isRDBMSChangeableInLifetime() {
            return false;
        }
    }
}
