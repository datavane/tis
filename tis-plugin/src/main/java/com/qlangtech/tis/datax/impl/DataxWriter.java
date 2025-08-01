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

import com.alibaba.datax.core.job.IJobContainerContext;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.ElementPluginDesc;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.plugin.IDataXEndTypeGetter;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.IDataSourceFactoryGetter;
import com.qlangtech.tis.plugin.ds.IInitWriterTableExecutor;
import com.qlangtech.tis.plugin.ds.TableInDB;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:48
 */
@Public
public abstract class DataxWriter implements Describable<DataxWriter>, IDataxWriter {


    private static transient LoadingCache<String, TableInDB> tabsInDBCache;


    private TableInDB getExistTabsInSink(String dataXName) throws ExecutionException {
        if (tabsInDBCache == null) {
            tabsInDBCache = CacheBuilder.newBuilder().expireAfterAccess(3, TimeUnit.SECONDS)
                    .build(new CacheLoader<String, TableInDB>() {
                        @Override
                        public TableInDB load(String dataXName) throws Exception {
                            IDataSourceFactoryGetter writer = (IDataSourceFactoryGetter) DataxWriter.this;// (IDataSourceFactoryGetter) DataxWriter.load(null, dataXName);
                            final DataSourceFactory ds = writer.getDataSourceFactory();
                            return ds.getTablesInDB();
                        }
                    });
        }

        TableInDB result = tabsInDBCache.get(dataXName);
        if (result == null) {
            throw new IllegalStateException("dataXName:"
                    + dataXName + " relevant Sink end dataSource refects tables can not be null");
        }
        return result;

    }


    /**
     * 初始化表RDBMS的表，如果表不存在就创建表
     *
     * @param containerContext
     * @param sinkTableName    没有经过rename的表名
     * @param jdbcUrls
     * @throws Exception
     */
    public static void process(IJobContainerContext containerContext, String sinkTableName, List<String> jdbcUrls) throws Exception {
        DataXName dataXName = containerContext.getTISDataXName();
        if ((dataXName) == null) {
            throw new IllegalArgumentException("param dataXName can not be null");
        }
        DataxWriter writer = DataxWriter.load(null, dataXName.getType(), dataXName.getPipelineName(), true);
        // 由于TableInDB 实例缓存时间太长了，如果在数据库中直接drop表之后在此处，执行全量构建就不会创建表了，先把缓存去掉
//        TableInDB existTabs = writer.getExistTabsInSink(dataXName);
//        if (existTabs.contains(tableName)) {
//            // 表已经存在不用初始化啦
//            return;
//        }

        IInitWriterTableExecutor dataXWriter
                = (IInitWriterTableExecutor) writer;

        Objects.requireNonNull(dataXWriter, "dataXWriter can not be null,dataXName:" + dataXName);
        dataXWriter.initWriterTable(containerContext, sinkTableName, jdbcUrls);
    }

    public static KeyedPluginStore<DataxWriter> getPluginStore(IPluginContext context, String appname) {
        return getPluginStore(context, StoreResourceType.DataApp, appname);
    }


    public static KeyedPluginStore<DataxWriter> getPluginStore(IPluginContext context, StoreResourceType resType, String name) {
        return TIS.dataXWriterPluginStore.get(createDataXWriterKey(context, resType, name));
    }

    public static void cleanPluginStoreCache(IPluginContext context, DataXName appname) {
        TIS.dataXWriterPluginStore.clear(createDataXWriterKey(context, appname.getType(), appname.getPipelineName()));
    }

    private static KeyedPluginStore.AppKey createDataXWriterKey(IPluginContext context, String appname) {
        return createDataXWriterKey(context, StoreResourceType.DataApp, appname);
    }

    private static KeyedPluginStore.AppKey createDataXWriterKey(IPluginContext context
            , StoreResourceType resType, String appname) {
        if (StringUtils.isEmpty(appname)) {
            throw new IllegalArgumentException("param appname can not be null");
        }
        return new KeyedPluginStore.AppKey(context, resType, appname, DataxWriter.class);
    }


    public interface IDataxWriterGetter {
        DataxWriter get(String appName);
    }

    /**
     * 测试用
     */
    public static IDataxWriterGetter dataxWriterGetter;

    public static DataxWriter load(IPluginContext context, DataXName appName) {
        return load(context, appName.getType(), appName.getPipelineName(), true);
    }

    public static DataxWriter load(IPluginContext context, String appName) {
        return load(context, StoreResourceType.DataApp, appName, true);
    }

    public static DataxWriter load(IPluginContext context, String appName, boolean nullValidate) {
        return load(context, StoreResourceType.DataApp, appName, nullValidate);
    }

    /**
     * load
     *
     * @param appName
     * @return
     */
    public static DataxWriter load(IPluginContext context, StoreResourceType resType, String appName, boolean nullValidate) {
        DataxWriter appSource = null;
        if (dataxWriterGetter != null) {
            appSource = dataxWriterGetter.get(appName);
        } else {
            appSource = getPluginStore(context, resType, appName).getPlugin();
        }
        if (nullValidate) {
            Objects.requireNonNull(appSource, "appName:" + appName + ",resType:" + resType + " relevant appSource can not be null");
        }
        return appSource;
    }


//    public static class AppKey extends KeyedPluginStore.Key<DataxWriter> {
//        public AppKey(String dataxName) {
//            super(IFullBuildContext.NAME_APP_DIR, dataxName, DataxWriter.class);
//        }
//    }


    @Override
    public BaseDataxWriterDescriptor getWriterDescriptor() {
        return (BaseDataxWriterDescriptor) getDescriptor();
    }

    @Override
    public final Descriptor<DataxWriter> getDescriptor() {

        Descriptor<DataxWriter> descriptor = TIS.get().getDescriptor((Class<Describable>) this.getOwnerClass());
        Class<BaseDataxWriterDescriptor> expectClazz = getExpectDescClass();
        if (!(expectClazz.isAssignableFrom(descriptor.getClass()))) {
            throw new IllegalStateException(descriptor.getClass() + " must implement the Descriptor of " + expectClazz.getName());
        }
        return descriptor;
    }

    protected <TT extends BaseDataxWriterDescriptor> Class<TT> getExpectDescClass() {
        return (Class<TT>) BaseDataxWriterDescriptor.class;
    }

    /**
     * Hudi 需要rewrite SelectTab的prop
     */
    public interface IRewriteSuFormProperties {
        public <TAB extends SelectedTab> Descriptor<TAB> getRewriterSelectTabDescriptor();

        ConcurrentHashMap<Class, SuFormProperties> rewriteSubFormProperties = new ConcurrentHashMap<>();

        default SuFormProperties overwriteSubPluginFormPropertyType(SuFormProperties subformProps) throws Exception {
            final Descriptor<SelectedTab> newSubDescriptor = getRewriterSelectTabDescriptor();
            if (newSubDescriptor == null) {
                return null;
            }
            return rewriteSubFormProperties.computeIfAbsent(this.getClass() //subformProps.parentClazz
                    , (clazz) -> {
                        SuFormProperties rewriteSubFormProperties = SuFormProperties.copy(
                                PropertyType.filterFieldProp(PropertyType.buildPropertyTypes(ElementPluginDesc.create(newSubDescriptor), newSubDescriptor.clazz))
                                , newSubDescriptor.clazz
                                , newSubDescriptor
                                , subformProps);
                        return rewriteSubFormProperties;
                    });
        }

        //   SuFormProperties.SuFormPropertiesBehaviorMeta overwriteBehaviorMeta(SuFormProperties.SuFormPropertiesBehaviorMeta behaviorMeta) throws Exception;
    }


    public static abstract class BaseDataxWriterDescriptor extends Descriptor<DataxWriter> implements IDataXEndTypeGetter {

        @Override
        public PluginVender getVender() {
            return PluginVender.DATAX;
        }

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = super.getExtractProps();
            eprops.put("supportMultiTable", this.isSupportMultiTable());
            eprops.put("rdbms", this.isRdbms());
            eprops.put("createDDL", this.isSupportTabCreate());
            eprops.put(KEY_SUPPORT_INCR, this.isSupportIncr());
            eprops.put(KEY_SUPPORT_BATCH, this.isSupportBatch());
            // this.getEndType().appendProps(eprops);
//            eprops.put(KEY_END_TYPE, this.getEndType().getVal());
//            eprops.put(KEY_SUPPORT_ICON, this.getEndType().getIcon() != null);
            return eprops;
        }

        @Override
        public boolean isSupportBatch() {
            return true;
        }
//        /**
//         * 如果返回null则说明不支持增量同步功能
//         *
//         * @return
//         */
//        protected abstract boolean isSupportIncr();

//        /**
//         * 如果返回null则说明不支持增量同步功能
//         *
//         * @return
//         */
//        protected IDataXPluginMeta.EndType getEndType() {
//            return null;
//        }

        /**
         * reader 中是否可以选择多个表，例如像elastic这样的writer中对于column的设置比较复杂，
         * 需要在writer plugin页面中完成，所以就不能支持在reader中选择多个表了
         *
         * @return
         */
        public boolean isSupportMultiTable() {
            return true;
        }

        /**
         * 是否可以选择多个表，像Mysql这样的 ,RDBMS 关系型数据库 应该都为true
         *
         * @return
         */
        public abstract boolean isRdbms();

        /**
         * 是否支持自动创建
         *
         * @return
         */
        public boolean isSupportTabCreate() {
            return false;
        }
    }
}
