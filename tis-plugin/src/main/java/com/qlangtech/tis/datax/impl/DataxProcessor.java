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

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.datax.TableAlias;
import com.qlangtech.tis.datax.TableAliasMapper;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorExtensionList;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.IBasicAppSource;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.StoreResourceType;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.trigger.JobTrigger;
import com.qlangtech.tis.sql.parser.tuple.creator.IStreamIncrGenerateStrategy;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * DataX任务执行方式的抽象
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 16:46
 */
public abstract class DataxProcessor implements IBasicAppSource, IDataxProcessor, IStreamIncrGenerateStrategy {

    public static final String DEFAULT_DATAX_PROCESSOR_NAME = "DataxProcessor";
    public static final String DEFAULT_WORKFLOW_PROCESSOR_NAME = "WorkflowProcessor";
    public static final String DATAX_CFG_DIR_NAME = "dataxCfg";
    public static final String DATAX_CREATE_DDL_DIR_NAME = "createDDL";

    private List<TableAlias> tableMaps;
    private transient PluginStore<IAppSource> pluginStore;

    public interface IDataxProcessorGetter {
        DataxProcessor get(String dataXName);
    }

    @Override
    public void setPluginStore(PluginStore<IAppSource> pluginStore) {
        this.pluginStore = pluginStore;
    }

    // for TEST
    public static IDataxProcessorGetter processorGetter;


    public static IDataxProcessor load(IPluginContext pluginContext, String dataXName) {

        if (processorGetter != null) {
            return processorGetter.get(dataXName);
        }

        return load(pluginContext, StoreResourceType.DataApp, dataXName);
    }

//    public static IDataxProcessor load(IPluginContext pluginContext, StoreResourceType resType, String dataXName) {
//        return load(pluginContext, resType, dataXName);
//    }

    public static IDataxProcessor load(IPluginContext pluginContext, StoreResourceType resType, String dataXName) {
        if (processorGetter != null) {
            return processorGetter.get(dataXName);
        }

//        try {
        Optional<IAppSource> appSource = IAppSource.loadNullable(pluginContext, resType, dataXName);
        if (appSource.isPresent()) {
            return (IDataxProcessor) appSource.get();
        } else {
            KeyedPluginStore<IAppSource> store = IAppSource.getPluginStore(pluginContext, resType, dataXName);
            throw new RuntimeException("targetName:" + dataXName + ",resType:"
                    + resType + ",store file is not exist:" + store.getTargetFile().getFile());
        }

    }

    public static Descriptor<IAppSource> getPluginDescMeta() {
        return getPluginDescMeta(DEFAULT_DATAX_PROCESSOR_NAME);
    }

    public static Descriptor<IAppSource> getPluginDescMeta(String targetProcessName) {
        DescriptorExtensionList<IAppSource, Descriptor<IAppSource>> descs = TIS.get().getDescriptorList(IAppSource.class);
        Optional<Descriptor<IAppSource>> dataxProcessDescs
                = descs.stream().filter((des) -> targetProcessName.equals(des.getDisplayName())).findFirst();
        if (!dataxProcessDescs.isPresent()) {
            throw new IllegalStateException("dataX process descriptor:" + targetProcessName + " relevant descriptor can not be null");
        }
        return dataxProcessDescs.get();
    }

    public static DataXCreateProcessMeta getDataXCreateProcessMeta(IPluginContext pluginContext, String dataxPipeName) {
        return getDataXCreateProcessMeta(pluginContext, dataxPipeName, true);
    }

    public static DataXCreateProcessMeta getDataXCreateProcessMeta(IPluginContext pluginContext, String dataxPipeName, boolean writerNullValidate) {
        DataxWriter writer = DataxWriter.load(pluginContext, dataxPipeName, writerNullValidate);
        DataxWriter.BaseDataxWriterDescriptor writerDesc = null;
        if (!writerNullValidate && writer == null) {
            writerDesc = (DataxWriter.BaseDataxWriterDescriptor) IDataxProcessor.getWriterDescriptor(pluginContext, dataxPipeName);
        } else {
            writerDesc = (DataxWriter.BaseDataxWriterDescriptor)
                    Objects.requireNonNull(writer, "name:" + dataxPipeName + " relevant dataXWriter can not be null").getDescriptor();
        }

        DataxReader dataxReader = DataxReader.load(pluginContext, dataxPipeName);
        DataxReader.BaseDataxReaderDescriptor readDescriptor = (DataxReader.BaseDataxReaderDescriptor) dataxReader.getDescriptor();

        boolean dataXReaderRDBMSSwitchOn = false;
        if ((dataxReader instanceof DataXBasicProcessMeta.IRDBMSSupport)) {
            // 单独开通了通过dataXReader在运行期通过Reader内部状态设置改变是否支持RDBS的状态
            dataXReaderRDBMSSwitchOn = ((DataXBasicProcessMeta.IRDBMSSupport) dataxReader).isRDBMSSupport();
        }

        DataXCreateProcessMeta processMeta = new DataXCreateProcessMeta(writer, dataxReader);
        // 使用这个属性来控制是否要进入创建流程的第三步
        processMeta.setReaderRDBMS(dataXReaderRDBMSSwitchOn || readDescriptor.isRdbms());
        processMeta.setReaderHasExplicitTable(readDescriptor.hasExplicitTable());
        processMeta.setWriterRDBMS(writerDesc.isRdbms());
        processMeta.setWriterSupportMultiTableInReader(writerDesc.isSupportMultiTable());

        return processMeta;
    }

    public abstract Application buildApp();


    @Override
    public <T> T accept(IAppSourceVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public TableAliasMapper getTabAlias() {
        return this.getTabAlias(null);
    }

    /**
     * key:Source Table Name
     *
     * @return
     */
    @Override
    public TableAliasMapper getTabAlias(IPluginContext pluginCtx) {
        boolean isReaderUnStructed = false;
//        if (this.isRDBMS2RDBMS(null)
//                || this.isRDBMS2UnStructed(null)
//                || (isReaderUnStructed = this.isReaderUnStructed(null))) {

        if ((this.isRDBMS2RDBMS(pluginCtx))
                || (isReaderUnStructed = this.isReaderUnStructed(pluginCtx))
                // 支持ElasticSearch
                || CollectionUtils.isNotEmpty(tableMaps)
        ) {

            if (CollectionUtils.isEmpty(tableMaps)) {
                return TableAliasMapper.Null;
            }
            return new TableAliasMapper(this.tableMaps.stream()
                    .collect(Collectors.toMap((m) -> {
                        if (StringUtils.isEmpty(m.getFrom())) {
                            throw new IllegalArgumentException("table mapper from can not be empty");
                        }
                        return m.getFrom();
                    }, (m) -> {
                        return m;
                    })));

        } else {

            IDataxReader reader = this.getReader(pluginCtx);
            List<ISelectedTab> tabs = reader.getSelectedTabs();

//            if (isReaderUnStructed) {
//                throw new IllegalStateException("isReaderUnStructed must be false");
//            }

            // if (!isReaderUnStructed) {
            Map<String, TableAlias> mapper = Maps.newHashMap();
            for (ISelectedTab tab : tabs) {
                mapper.put(tab.getName(), new TableMap(tab));
            }
            return new TableAliasMapper(mapper);


//            } else {
//                throw new UnsupportedOperationException("reader shall be RDBMS");
//            }
        }


    }

    @Override
    public void saveCreateTableDDL(IPluginContext pluginCtx
            , StringBuffer createDDL, String sqlFileName, boolean overWrite) throws IOException {
        File createDDLDir = this.getDataxCreateDDLDir(pluginCtx);
        saveCreateTableDDL(createDDL, createDDLDir, sqlFileName, overWrite);
        // 主要更新一下最后更新时间，这样在执行powerjob任务可以顺利将更新后的ddl文件同步到powerjob的worker节点上去
        Objects.requireNonNull(this.pluginStore, "pluginStore can be null,shall be set by method  setPluginStore ahead")
                .writeLastModifyTimeStamp();
    }

    public static void saveCreateTableDDL(StringBuffer createDDL, File createDDLDir, String sqlFileName, boolean overWrite) throws IOException {
        if (StringUtils.isEmpty(sqlFileName)) {
            throw new IllegalArgumentException("param sqlFileName can not be empty");
        }

        File f = new File(createDDLDir, sqlFileName);
        // 判断文件是否已经存在，如果已经存在的话就不需要生成了
        if (overWrite || !f.exists()) {
            FileUtils.write(f, createDDL.toString(), TisUTF8.get());
        }
    }

    @Override
    public final boolean isReaderUnStructed(IPluginContext pluginCtx) {
        DataXCreateProcessMeta dataXCreateProcessMeta = getDataXCreateProcessMeta(pluginCtx, this.identityValue());
        return dataXCreateProcessMeta.isReaderUnStructed();
    }

    @Override
    public boolean isRDBMS2UnStructed(IPluginContext pluginCtx) {
        DataXCreateProcessMeta dataXCreateProcessMeta = getDataXCreateProcessMeta(pluginCtx, this.identityValue());
        return dataXCreateProcessMeta.isReaderRDBMS() && !dataXCreateProcessMeta.isWriterRDBMS();
    }

    @Override
    public boolean isRDBMS2RDBMS(IPluginContext pluginCtx) {
        DataXCreateProcessMeta dataXCreateProcessMeta = getDataXCreateProcessMeta(pluginCtx, this.identityValue());
        return dataXCreateProcessMeta.isReaderRDBMS() && dataXCreateProcessMeta.isWriterRDBMS();
    }

    @Override
    public boolean isWriterSupportMultiTableInReader(IPluginContext pluginCtx) {
        DataXCreateProcessMeta dataXCreateProcessMeta = getDataXCreateProcessMeta(pluginCtx, this.identityValue());
        return dataXCreateProcessMeta.isWriterSupportMultiTableInReader();
    }

    @Override
    public IDataxReader getReader(IPluginContext pluginCtx) {
        return DataxReader.load(pluginCtx, this.identityValue());
    }

    @Override
    public IDataxWriter getWriter(IPluginContext pluginCtx, boolean validateNull) {
        return DataxWriter.load(pluginCtx, StoreResourceType.DataApp, this.identityValue(), validateNull);
    }

    //
//    private transient static Cache<String, Map<String, INotebookable.NotebookEntry>> scanNotebookCache
//            = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();


    /**
     * 从Reader和Writer实例中扫面可以作为notebook的实例
     *
     * @return
     * @throws Exception
     */
//    public Map<String, INotebookable.NotebookEntry> scanNotebook() throws Exception {
//
//        return scanNotebookCache.get(this.identityValue(), () -> {
//            Map<String, INotebookable.NotebookEntry> notebookable = Maps.newHashMap();
//
//            IDataxReader reader = this.getReader(null);
//            IDataxWriter writer = this.getWriter(null, true);
//
//            scanNotebook(notebookable, reader);
//            scanNotebook(notebookable, writer);
//            return notebookable;
//        });
//    }

//    private void scanNotebook(Map<String, INotebookable.NotebookEntry> notebookable, Object bean)
//            throws IllegalAccessException, InvocationTargetException {
//        PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();
//        PropertyDescriptor[] readerProps = propertyUtils.getPropertyDescriptors(bean.getClass());
//        Class<?> propertyType = null;
//        Describable plugin = null;
//        for (PropertyDescriptor prop : readerProps) {
//            propertyType = prop.getPropertyType();
//            if (Describable.class.isAssignableFrom(propertyType)) {
//                plugin = (Describable) prop.getReadMethod().invoke(bean);
//                if (plugin instanceof IdentityName
//                        && plugin.getDescriptor() instanceof INotebookable) {
//                    notebookable.put(((IdentityName) plugin).identityValue(),
//                            new INotebookable.NotebookEntry((INotebookable) plugin.getDescriptor(), plugin));
//                    return;
//                }
//            }
//        }
//    }

    public void setTableMaps(List<TableAlias> tableMaps) {
        this.tableMaps = tableMaps;
    }

    @Override
    public File getDataxCfgDir(IPluginContext pluginContext) {
//        File dataXWorkDir = getDataXWorkDir(pluginContext);
//        return new File(dataXWorkDir, DATAX_CFG_DIR_NAME);
        return getDataxCfgDir(pluginContext, this);
    }


    public static File getDataxCfgDir(IPluginContext pluginContext, IDataxProcessor processor) {
        File dataXWorkDir = processor.getDataXWorkDir(pluginContext);
        return new File(dataXWorkDir, DATAX_CFG_DIR_NAME);
    }

    @Override
    public File getDataxCreateDDLDir(IPluginContext pluginContext) {
        return getDataxCreateDDLDir(pluginContext, this);
    }


    public static File getDataxCreateDDLDir(IPluginContext pluginContext, IDataxProcessor processor) {
        File dataXWorkDir = processor.getDataXWorkDir(pluginContext);
        File ddlDir = new File(dataXWorkDir, DATAX_CREATE_DDL_DIR_NAME);
        try {
            FileUtils.forceMkdir(ddlDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ddlDir;
    }

    @Override
    public File getDataXWorkDir(IPluginContext pluginContext) {
        return IDataxProcessor.getDataXWorkDir(pluginContext, this.identityValue());
    }

//    /**
//     * 创建一个临时工作目录
//     *
//     * @param execId
//     * @throws Exception
//     */
//    public void makeTempDir(String execId) throws Exception {
//
//        File workingDir = getDataXWorkDir((IPluginContext) null);
//        FileUtils.copyDirectory(workingDir, new File(workingDir.getParentFile(), KeyedPluginStore.TMP_DIR_NAME + workingDir.getName() + "-" + execId));
//    }

    /**
     * dataX配置文件列表
     *
     * @return
     */
    @Override
    public DataXCfgGenerator.GenerateCfgs getDataxCfgFileNames(IPluginContext pluginContext, Optional<JobTrigger> partialTrigger) {
//        File dataxCfgDir = getDataxCfgDir(pluginContext);
//        if (!dataxCfgDir.exists()) {
//            throw new IllegalStateException("dataxCfgDir is not exist:" + dataxCfgDir.getAbsolutePath());
//        }
//        if (dataxCfgDir.list().length < 1) {
//            throw new IllegalStateException("dataxCfgDir is empty can not find any files:" + dataxCfgDir.getAbsolutePath());
//        }
//        DataXCfgGenerator.GenerateCfgs genCfgs = DataXCfgGenerator.GenerateCfgs.readFromGen(dataxCfgDir);
//        return genCfgs;
        return DataxProcessor.getDataxCfgFileNames(pluginContext, partialTrigger, this);
    }

    public static DataXCfgGenerator.GenerateCfgs getDataxCfgFileNames( //
                                                                       IPluginContext pluginContext //
            , Optional<JobTrigger> partialTrigger, IDataxProcessor processor) {
        File dataxCfgDir = processor.getDataxCfgDir(pluginContext);
        if (!dataxCfgDir.exists()) {
            throw new IllegalStateException("dataxCfgDir is not exist:" + dataxCfgDir.getAbsolutePath());
        }
        if (dataxCfgDir.list().length < 1) {
            throw new IllegalStateException("dataxCfgDir is empty can not find any files:" + dataxCfgDir.getAbsolutePath());
        }
        DataXCfgGenerator.GenerateCfgs genCfgs = DataXCfgGenerator.GenerateCfgs.readFromGen(pluginContext, dataxCfgDir, partialTrigger);
        return genCfgs;
    }

    public static class DataXCreateProcessMeta extends DataXBasicProcessMeta {


        private final DataxWriter writer;
        private final DataxReader reader;

        public DataXCreateProcessMeta(DataxWriter writer, DataxReader reader) {
            this.writer = writer;
            this.reader = reader;
        }

        @JSONField(serialize = false)
        public DataxWriter getWriter() {
            return writer;
        }

        @JSONField(serialize = false)
        public DataxReader getReader() {
            return reader;
        }


    }

//     "setting": {
//        "speed": {
//            "channel": 3
//        },
//        "errorLimit": {
//            "record": 0,
//                    "percentage": 0.02
//        }
//    },


    /**
     * =======================================
     * impl:IStreamIncrGenerateStrategy
     * =========================================
     */
    @Override
    public boolean isExcludeFacadeDAOSupport() {
        return true;
    }
//
//    @Override
//    public Map<IEntityNameGetter, List<IValChain>> getTabTriggerLinker() {
//
//        Map<IEntityNameGetter, List<IValChain>> tabColsMapper = Maps.newHashMap();
//        IDataxReader reader = this.getReader(null);
//        Objects.requireNonNull(reader, "dataXReader can not be null");
//        List<ISelectedTab> selectedTabs = reader.getSelectedTabs();
//        for (ISelectedTab tab : selectedTabs) {
//            tabColsMapper.put(() -> EntityName.parse(tab.getName()), Collections.emptyList());
//        }
//
//        return tabColsMapper;
//    }
//
//    @Override
//    public Map<DBNode, List<String>> getDependencyTables(IDBTableNamesGetter dbTableNamesGetter) {
//        return Collections.emptyMap();
//    }
//
//    @Override
//    public IERRules getERRule() {
//        ERRules erRules = new ERRules() {
//            public List<PrimaryTableMeta> getPrimaryTabs() {
//                TabExtraMeta tabMeta = new TabExtraMeta();
//                PrimaryTableMeta ptab = new PrimaryTableMeta("tabName", tabMeta);
//                return Collections.singletonList(ptab);
//            }
//        };
//
//        return erRules;
//    }
}
