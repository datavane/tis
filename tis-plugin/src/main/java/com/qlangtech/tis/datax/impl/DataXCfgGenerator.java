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

import com.alibaba.datax.core.util.container.TransformerConstant;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.qlangtech.tis.datax.DBDataXChildTask;
import com.qlangtech.tis.datax.DataXCfgFile;
import com.qlangtech.tis.datax.DataXJobInfo;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataXNameAware;
import com.qlangtech.tis.datax.IDataXPluginMeta;
import com.qlangtech.tis.datax.IDataxContext;
import com.qlangtech.tis.datax.IDataxGlobalCfg;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxProcessor.TableMap;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxReaderContext;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.datax.IGroupChildTaskIterator;
import com.qlangtech.tis.datax.SourceColMetaGetter;
import com.qlangtech.tis.datax.TableAliasMapper;
import com.qlangtech.tis.manage.common.AppAndRuntime;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.datax.CreateTableSqlBuilder;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.trigger.JobTrigger;
import com.qlangtech.tis.datax.IDataXGenerateCfgs;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: baisui 百岁
 * @create: 2021-04-20 18:06
 **/
public class DataXCfgGenerator implements IDataXNameAware {

    private transient static final VelocityEngine velocityEngine;

    static {
        try {
            velocityEngine = new VelocityEngine();
            Properties prop = new Properties();
            prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");

            prop.setProperty("resource.loader", "tisLoader");
            prop.setProperty("tisLoader.resource.loader.class", TISClasspathResourceLoader.class.getName());

            velocityEngine.init(prop);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private final IDataxProcessor dataxProcessor;
    private final IDataxGlobalCfg globalCfg;
    private final String dataxName;
    private final IPluginContext pluginCtx;
    private final TableMapCreator tableMapCreator;

    @Override
    public DataXName getCollectionName() {
        return new DataXName(this.dataxName, dataxProcessor.getResType());
    }

    public DataXCfgGenerator(IPluginContext pluginCtx, String dataxName, IDataxProcessor dataxProcessor) {
        Objects.requireNonNull(dataxProcessor, "dataXprocessor can not be null");
        IDataxGlobalCfg dataXGlobalCfg = dataxProcessor.getDataXGlobalCfg();
        Objects.requireNonNull(dataXGlobalCfg, "globalCfg can not be null");
        this.dataxProcessor = dataxProcessor;
        this.globalCfg = dataXGlobalCfg;
        this.dataxName = dataxName;
        this.pluginCtx = pluginCtx;
        this.tableMapCreator = new TableMapCreator();
    }

    protected String getTemplateContent(IDataxReaderContext readerContext //
            , IDataxReader reader, IDataxWriter writer, Optional<RecordTransformerRules> transformerRules) {
        final String tpl = globalCfg.getTemplate();


        if (StringUtils.isEmpty(reader.getTemplate())) {
            throw new IllegalStateException("readerTpl of '" + reader.getDataxMeta().getName() + "' can not be null");
        }
        StringBuffer readerTpl = new StringBuffer(reader.getTemplate());
        String writerTpl = writer.getTemplate();

        if (StringUtils.isEmpty(writerTpl)) {
            throw new IllegalStateException("writerTpl of '" + writer.getDataxMeta().getName() + "' can not be null");
        }

        readerTpl.append(",\n\t\"")
                .append(TransformerConstant.JOB_TRANSFORMER).append("\":\t\t\n {\"").append(TransformerConstant.JOB_TRANSFORMER_NAME)
                .append("\":\"").append(readerContext.getSourceTableName()).append("\",\n\"")
                .append(TransformerConstant.JOB_TRANSFORMER_RELEVANT_KEYS).append("\":").append("[");
        if (transformerRules.isPresent()) {
            readerTpl.append(transformerRules.get().relevantColKeys().stream().map((col) -> "\"" + col + "\"").collect(Collectors.joining(",")));
        }
        readerTpl.append("]").append("}");


        String template = StringUtils.replace(tpl, "<!--reader-->", readerTpl.toString());
        template = StringUtils.replace(template, "<!--writer-->", writerTpl);
        return template;
    }


    /**
     * 取得之前已经存在的文件
     *
     * @param parentDir
     * @return
     */
    public GenerateCfgs getExistCfg(File parentDir) throws Exception {
        File dataxCfgDir = dataxProcessor.getDataxCfgDir(this.pluginCtx);
        GenerateCfgs generateCfgs = new GenerateCfgs(dataxProcessor, this.pluginCtx, dataxCfgDir);

        File genFile = new File(parentDir, FILE_GEN);
        if (!genFile.exists()) {
            return generateCfgs;
        }


        generateCfgs.createDDLFiles = getExistDDLFiles();

        GenerateCfgs cfgs = GenerateCfgs.readFromGen(dataxProcessor, this.pluginCtx, dataxCfgDir, Optional.empty());
        generateCfgs.setGenTime(cfgs.getGenTime());
        generateCfgs.setGroupedChildTask(cfgs.getGroupedChildTask());
        return generateCfgs;
    }

    /**
     * 取得已经存在的DDL Sql文件
     *
     * @return
     */
    private List<String> getExistDDLFiles() {
        File dataxCreateDDLDir = dataxProcessor.getDataxCreateDDLDir(this.pluginCtx);
        return Lists.newArrayList(dataxCreateDDLDir.list((dir, f) -> {
            return StringUtils.endsWith(f, DataXCfgFile.DATAX_CREATE_DDL_FILE_NAME_SUFFIX);
        }));
    }


    public static final String FILE_GEN = "gen";

    public GenerateCfgs startGenerateCfg(final File dataXCfgDir) throws Exception {
        final boolean supportDataXBatch = this.dataxProcessor.isSupportBatch(this.pluginCtx);
        if (supportDataXBatch) {
            // 先清空文件
            FileUtils.cleanDirectory(dataXCfgDir);
        }
        return startGenerateCfg(new IGenerateScriptFile() {
            @Override
            public void generateScriptFile(SourceColMetaGetter colMetaGetter
                    , IDataxReader reader, IDataxWriter writer, DataxWriter.BaseDataxWriterDescriptor writerDescriptor,
                                           IDataxReaderContext readerContext, Set<String> createDDLFiles
                    , Optional<IDataxProcessor.TableMap> tableMapper) throws IOException {

                if (writerDescriptor.isSupportTabCreate()) {
                    generateTabCreateDDL(pluginCtx, dataxProcessor, colMetaGetter, writer, readerContext, createDDLFiles
                            , tableMapper, false);
                }


                //  generateDataXAndSQLDDLFile(dataXCfgDir, reader, writer, readerContext, createDDLFiles, tableMapper, colMetaGetter);
                if (supportDataXBatch) {


                    if (StringUtils.isEmpty(readerContext.getTaskName())) {
                        throw new IllegalStateException("readerContext.getTaskName() must be present");
                    }
                    File configFile = DataXJobInfo.getJobPath(dataXCfgDir, readerContext.getReaderContextId(),
                            readerContext.getTaskName() + DataXCfgFile.DATAX_CREATE_DATAX_CFG_FILE_NAME_SUFFIX);
                    FileUtils.write(configFile, generateDataxConfig(readerContext, writer, reader, tableMapper
                    ), TisUTF8.get(), false);
                }

            }
        });
    }


    public GenerateCfgs startGenerateCfg(IGenerateScriptFile scriptFileGenerator) throws Exception {


        //        FileUtils.forceMkdir(dataXCfgDir);
        //        // 先清空文件
        //        FileUtils.cleanDirectory(dataXCfgDir);

        boolean unStructedReader = dataxProcessor.isReaderUnStructed(this.pluginCtx);


        IDataxWriter writer = dataxProcessor.getWriter(this.pluginCtx);
        DataxWriter.BaseDataxWriterDescriptor writerDescriptor = writer.getWriterDescriptor();
        SourceColMetaGetter colMetaGetter = null;
//        TableAliasMapper tabAlias = Objects.requireNonNull(dataxProcessor.getTabAlias(this.pluginCtx), "tabAlias can "
//                + "not be null");
        Set<String> createDDLFiles = Sets.newHashSet();
        List<String> existDDLFiles = getExistDDLFiles();

        GenerateCfgs cfgs = new GenerateCfgs(dataxProcessor, this.pluginCtx, this.dataxProcessor.getDataxCfgDir(this.pluginCtx));
        List<IDataxReader> readers = dataxProcessor.getReaders(this.pluginCtx);
        if (CollectionUtils.isEmpty(readers)) {
            throw new IllegalStateException(dataxName + " relevant readers can not be empty");
        }

        for (IDataxReader reader : readers) {

            colMetaGetter = reader.createSourceColMetaGetter();// new SourceColMetaGetter(reader);
            try (IGroupChildTaskIterator subTasks
                         = Objects.requireNonNull(reader.getSubTasks(), "subTasks can not be null")) {
                IDataxReaderContext readerContext = null;
                while (subTasks.hasNext()) {
                    readerContext = subTasks.next();
                    Optional<IDataxProcessor.TableMap> tableMapper = buildTabMapper(reader, readerContext);
                    scriptFileGenerator.generateScriptFile(
                            colMetaGetter, reader, writer, writerDescriptor, readerContext, createDDLFiles, tableMapper);
                }
                // if (supportDataXBatch) {
                Map<String, List<DBDataXChildTask>> groupedInfo = subTasks.getGroupedInfo();
                if (MapUtils.isEmpty(groupedInfo)) {
                    throw new IllegalStateException("groupedInfo can not be empty");
                }
                cfgs.groupedChildTask.putAll(groupedInfo);
                //}
            }
            //  IDataxReader reader = dataxProcessor.getReader(this.pluginCtx);
        }

        // 将老的已经没有用的ddl sql文件删除调
        if (writerDescriptor.isSupportTabCreate()) {
            File createDDLDir = this.dataxProcessor.getDataxCreateDDLDir(this.pluginCtx);
            for (String oldDDLFile : existDDLFiles) {
                if (!createDDLFiles.contains(oldDDLFile)) {
                    FileUtils.deleteQuietly(new File(createDDLDir, oldDDLFile));
                }
            }
            if (!writer.isGenerateCreateDDLSwitchOff() && CollectionUtils.isEmpty(createDDLFiles)) {
                throw new IllegalStateException("createDDLFiles can not be empty ");
            }
        }


        long current = System.currentTimeMillis();
        //        FileUtils.write(new File(dataXCfgDir, FILE_GEN), String.valueOf(current), TisUTF8.get(), false);
        cfgs.createDDLFiles = Lists.newArrayList(createDDLFiles);

        //  cfgs.dataxFiles = subTaskName;
        cfgs.genTime = current;
        return cfgs;
    }

    public Optional<TableMap> buildTabMapper(IDataxReader reader, IDataxReaderContext readerContext) {
        return this.tableMapCreator.build(reader, readerContext);
    }

    private class TableMapCreator {
        final boolean unStructedReader;
        final IDataxWriter writer;
        final TableAliasMapper tabAlias;
        private Function<IDataxReader, Map<String, ISelectedTab>> selectedTabsCall;

        public TableMapCreator() {
            this.unStructedReader = dataxProcessor.isReaderUnStructed(pluginCtx);
            this.writer = dataxProcessor.getWriter(pluginCtx);
            this.tabAlias = Objects.requireNonNull(dataxProcessor.getTabAlias(pluginCtx,false), "tabAlias can not be null");


            Map<IDataxReader, Map<String, ISelectedTab>> selectedTabsRef = new HashMap<>();
            this.selectedTabsCall = (reader) -> {
                Map<String, ISelectedTab> readerSelectedTabs = null;
                if ((readerSelectedTabs = selectedTabsRef.get(reader)) == null) {
                    readerSelectedTabs =
                            reader.getSelectedTabs().stream().collect(Collectors.toMap((t) -> t.getName(), (t) -> t));
                    selectedTabsRef.put(reader, readerSelectedTabs);
                }
                return readerSelectedTabs;
            };
        }

        Optional<TableMap> build(IDataxReader reader, IDataxReaderContext readerContext) {
            if (readerContext == null) {
                throw new IllegalArgumentException("param readerContext can not be null");
            }
            if (reader == null) {
                throw new IllegalArgumentException("param reader can not be null");
            }
            Optional<TableMap> tableMapper = null;
            if (!dataxProcessor.isWriterSupportMultiTableInReader(pluginCtx)) {

                if (tabAlias.isSingle()) {
                    // 针对ES的情况
                    Optional<TableMap> first = tabAlias.getFirstTableMap();
                    //                            = tabAlias.values().stream().filter((t) -> t instanceof
                    //                            IDataxProcessor.TableMap)
                    //                            .map((t) -> (IDataxProcessor.TableMap) t).findFirst();
                    if (first.isPresent()) {
                        tableMapper = first;
                    }
                } else {
                    //IDataxWriter writer = dataxProcessor.getWriter(this.pluginCtx);
                    if (writer instanceof IDataxProcessor.INullTableMapCreator) {
                        tableMapper = Optional.empty();
                    }
                }
                Objects.requireNonNull(tableMapper,
                        "tabMapper can not be null,tabAlias.size()=" + tabAlias.size() + ",tabs:[" + tabAlias.getFromTabDesc() + "]");
            } else if (unStructedReader) {
                // 是在DataxAction的doSaveWriterColsMeta() 方法中持久化保存的
                Optional<TableMap> f = tabAlias.getFirstTableMap();
                if (!f.isPresent()) {
                    // Objects.requireNonNull(tableMapper, "tableMap can not be null");
                    throw new IllegalStateException("tableMap can not be null");
                }
                tableMapper = f;
                //                for (TableAlias tab : tabAlias.values()) {
                //                    tableMapper = Optional.of((IDataxProcessor.TableMap) tab);
                //                    break;
                //                }

            } else if (dataxProcessor.isRDBMS2UnStructed(pluginCtx)) {
                // example: mysql -> oss
                TableMap m = createTableMap(tabAlias, selectedTabsCall.apply(reader), readerContext);

                tableMapper = Optional.of(m);
            } else if (dataxProcessor.isRDBMS2RDBMS(pluginCtx)) {
                // example: mysql -> mysql
                tableMapper = Optional.of(createTableMap(tabAlias, selectedTabsCall.apply(reader), readerContext));
            } else {
                // example:oss -> oss
                // tableMapper = Optional.of(createTableMap(tabAlias, selectedTabsCall.call(), readerContext));
                throw new IllegalStateException("unexpect status");
            }
            return tableMapper;
        }
    }


    public interface IGenerateScriptFile {
        void generateScriptFile(SourceColMetaGetter colMetaGetter, IDataxReader reader, IDataxWriter writer, DataxWriter.BaseDataxWriterDescriptor writerDescriptor, IDataxReaderContext readerContext,
                                Set<String> createDDLFiles, Optional<IDataxProcessor.TableMap> tableMapper) throws IOException;
    }

    public static void generateTabCreateDDL(IPluginContext pluginCtx, IDataxProcessor dataxProcessor,
                                            SourceColMetaGetter colMetaGetter, IDataxWriter writer, IDataxReaderContext readerContext,
                                            Set<String> createDDLFiles,
                                            Optional<IDataxProcessor.TableMap> tableMapper, boolean overWrite) throws IOException {
        DataxWriter.BaseDataxWriterDescriptor writerDescriptor = writer.getWriterDescriptor();
        if (writer.isGenerateCreateDDLSwitchOff()) {
            return;
        }
        if (tableMapper.isPresent() && writerDescriptor.isSupportTabCreate()) {
            for (CMeta colMeta : tableMapper.get().getSourceCols()) {
                if (colMeta.getType() == null) {
                    throw new IllegalStateException("reader context:" + readerContext.getSourceTableName() + " " +
                            "relevant col type which's name " + colMeta.getName() + " can not be null");
                }
                if (StringUtils.isEmpty(colMeta.getName())) {
                    throw new IllegalStateException("reader context:" + readerContext.getSourceTableName() + " " +
                            "relevant col name  can not be null");
                }
            }
            // 创建ddl
            IDataxProcessor.TableMap mapper = tableMapper.get();
            String sqlFileName = mapper.getFrom() + DataXCfgFile.DATAX_CREATE_DDL_FILE_NAME_SUFFIX;
            if (!createDDLFiles.contains(sqlFileName)) {

                Optional<RecordTransformerRules> transformers
                        = (RecordTransformerRules.loadTransformerRules(pluginCtx, dataxProcessor, mapper.getFrom()));


                CreateTableSqlBuilder.CreateDDL createDDL = Objects.requireNonNull(writer.generateCreateDDL(colMetaGetter, mapper, transformers),
                        "createDDL can not be null");

                createDDLFiles.add(sqlFileName);
                // 由于用户可能已经手动改动过生成的DDL文件，所以不能强行覆盖已经存在的DDL文件，overWrite参数应该为false
                dataxProcessor.saveCreateTableDDL(pluginCtx, createDDL.getDDLScript(), sqlFileName, overWrite);
            }
        }
    }

    public static class GenerateCfgs implements IDataXGenerateCfgs {
        private List<DataXCfgFile> _dataxFiles;
        private List<String> createDDLFiles = Collections.emptyList();
        private Map<String, List<DBDataXChildTask>> groupedChildTask = Maps.newHashMap();
        private long genTime;

        private final File dataxCfgDir;
        private final IPluginContext pluginCtx;
        private final IDataxProcessor dataxProcessor;

        public GenerateCfgs(IDataxProcessor dataxProcessor, IPluginContext pluginCtx, File dataxCfgDir) {
            this.dataxCfgDir = dataxCfgDir;
            this.pluginCtx = pluginCtx;// Objects.requireNonNull(, "pluginCtx can not be null");
            this.dataxProcessor = dataxProcessor;
        }

        public List<DataXCfgFile> getDataxFiles() {
            return getDataXCfgFiles().stream().map((file) -> file).collect(Collectors.toList());
        }

        /**
         * 取得当前运行时可选表
         *
         * @return
         */
        public static List<Option> getTabsCandidate() {
            AppAndRuntime appAndRuntime = AppAndRuntime.getAppAndRuntime();
            if (appAndRuntime == null) {
                return Collections.emptyList();
            }

            IDataxProcessor dataxProcessor = DataxProcessor.load(null, appAndRuntime.getAppName());
            if (!dataxProcessor.isSupportBatch(null)) {
                return Collections.emptyList();
            }
            File dataxCfgDir = dataxProcessor.getDataxCfgDir(null);
            if (!dataxCfgDir.exists()) {
                return Collections.emptyList();
            }
            GenerateCfgs dataxCfgFileNames = dataxProcessor.getDataxCfgFileNames(null, Optional.empty());
            return (dataxCfgFileNames.getTargetTabs().stream().map((tab) -> new Option(tab)).collect(Collectors.toList()));
        }

        @JSONField(serialize = false)
        public List<DataXCfgFile> getDataXCfgFiles() {

            if (this._dataxFiles == null) {
                this._dataxFiles =
                        this.getGroupedChildTask().values().stream().flatMap((tasks) -> tasks.stream()).map((task) -> {
                            task.getDbFactoryId();
                            File dataXCfg = task.getJobPath(this.dataxCfgDir);

                            if (!dataXCfg.exists()) {
                                throw new IllegalStateException("dataXCfg is not exist, path:" + dataXCfg.getAbsolutePath());
                            }
                            return (new DataXCfgFile()).setFile(dataXCfg).setDbFactoryId(task.dbFactoryId);
                        }).collect(Collectors.toList());
            }
            return this._dataxFiles;
        }


        public final Set<String> getTargetTabs() {
            return this.getGroupedChildTask().keySet();
        }

        /**
         * 给前端用的
         *
         * @return
         */
        @JSONField(name = "transformerInfo", serialize = true)
        public final Set<TransformerInfo> getTransformerInfo() {
            return this.dataxProcessor.getTransformerInfo(this.pluginCtx, groupedChildTask);
        }

        /**
         * Map<String, List<String>> key: logicTableName
         *
         * @return
         */
        private Map<String, List<DBDataXChildTask>> getGroupedChildTask() {
            if (groupedChildTask == null) {
                throw new IllegalStateException("groupedChildTask can not be null");
            }
            return groupedChildTask;
        }

        public void setGroupedChildTask(Map<String, List<DBDataXChildTask>> groupedChildTask) {
            this.groupedChildTask = groupedChildTask;
        }

        /**
         * 取得一个任务组（通常是一个逻辑表）对应的子任务（导入分库分表的子任务）
         *
         * @param taskGroupName 通常是一个表的名称
         * @return
         */
        public List<DBDataXChildTask> getDataXTaskDependencies(String taskGroupName) {
            List<DBDataXChildTask> subChildTask = null;
            Map<String, List<DBDataXChildTask>> groupdTsk = this.getGroupedChildTask();
            if (CollectionUtils.isEmpty(subChildTask = groupdTsk.get(taskGroupName))) {
                throw new IllegalStateException("taskGroupName:" + taskGroupName + " relevant childTask:" + String.join(",", groupdTsk.keySet()) + " can not be empty");
            }
            return subChildTask;
        }


        public static final String KEY_GEN_TIME = "genTime";
        public static final String KEY_GROUP_CHILD_TASKS = "groupChildTasks";


        public void write2GenFile(File dataxCfgDir) {
            try {
                JSONObject o = new JSONObject();
                o.put(KEY_GROUP_CHILD_TASKS, this.getGroupedChildTask());
                o.put(KEY_GEN_TIME, this.getGenTime());
                FileUtils.write(new File(dataxCfgDir, DataXCfgGenerator.FILE_GEN), JsonUtil.toString(o),
                        TisUTF8.get(), false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Example:
         * <pre>
         * {
         * 	"genTime":1675309210655,
         * 	"groupChildTasks":{
         * 		"totalpayinfo":[
         *         {
         * 			"dataXCfgFileName":"totalpayinfo_0",
         * 			"dbIdenetity":"jdbc:mysql://192.168.28.200:3306/order2?useUnicode=yes&useCursorFetch=true&useSSL=false&serverTimezone=Asia%2FShanghai&useCompression=true&characterEncoding=utf8"
         *         }
         * 		]
         *    }
         * }
         * </pre>
         *
         * @param dataxCfgDir
         * @return
         */
        public static GenerateCfgs readFromGen(IDataxProcessor processor, IPluginContext pluginCtx, File dataxCfgDir, Optional<JobTrigger> partialTrigger) {
            try {
                GenerateCfgs cfgs = new GenerateCfgs(processor, pluginCtx, dataxCfgDir);
                JSONObject o = JSON.parseObject(FileUtils.readFileToString(new File(dataxCfgDir,
                        DataXCfgGenerator.FILE_GEN), TisUTF8.get()));

                cfgs.genTime = o.getLongValue(KEY_GEN_TIME);

                Map<String, List<DBDataXChildTask>> groupedChildTasks = Maps.newHashMap();

                Map<String, JSONArray> childTasks = o.getObject(KEY_GROUP_CHILD_TASKS, Map.class);
                Set<String> filterTabsName = null;
                if (partialTrigger.isPresent()) {
                    filterTabsName = partialTrigger.get()
                            .selectedTabs().stream().map((tab) -> tab.identityValue()).collect(Collectors.toSet());
                }

                List<DBDataXChildTask> tasks = null;
                for (Map.Entry<String, JSONArray> entry : childTasks.entrySet()) {

                    if (filterTabsName != null) {
                        if (!filterTabsName.contains(entry.getKey())) {
                            // 如果不存在则跳过
                            continue;
                        }
                    }

                    tasks = entry.getValue().toJavaList(DBDataXChildTask.class);
                    groupedChildTasks.put(entry.getKey(), tasks);
                }

                cfgs.groupedChildTask = groupedChildTasks;
                return cfgs;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 建表script
         *
         * @return
         */
        public List<String> getCreateDDLFiles() {
            return this.createDDLFiles;
        }

        public long getGenTime() {
            return genTime;
        }


        public void setGenTime(long genTime) {
            this.genTime = genTime;
        }
    }

    private IDataxProcessor.TableMap createTableMap(
            TableAliasMapper tabAlias, Map<String, ISelectedTab> selectedTabs
            , IDataxReaderContext readerContext) {
        return readerContext.createTableMap(tabAlias, selectedTabs);
    }


    public String generateDataxConfig(
            IDataxReaderContext readerContext
            , IDataxWriter writer, IDataxReader reader, Optional<IDataxProcessor.TableMap> tableMapper) throws IOException {
        Optional<RecordTransformerRules> transformerRules
                = RecordTransformerRules.loadTransformerRules(this.pluginCtx
                , Objects.requireNonNull(dataxProcessor, "dataxProcessor can not be null")
                , readerContext.getSourceTableName());
        return generateDataxConfig(readerContext, writer, reader, transformerRules, tableMapper);
    }

    /**
     * @param readerContext
     * @return 生成的配置文件内容
     * @throws IOException
     */
    public String generateDataxConfig(
            IDataxReaderContext readerContext, IDataxWriter writer, IDataxReader reader
            , Optional<RecordTransformerRules> transformerRules, Optional<IDataxProcessor.TableMap> tableMap) {
        Objects.requireNonNull(writer, "writer can not be null");


        final String tpl = getTemplateContent(readerContext, reader, writer, transformerRules);
        if (StringUtils.isEmpty(tpl)) {
            throw new IllegalStateException("velocity template content can not be null");
        }
        try {
            VelocityContext mergeData = createContext(readerContext, writer.getSubTask(tableMap, (transformerRules)));
            //      writerContent = new StringWriter();
            //  velocityEngine.evaluate(mergeData, writerContent, "tablex-writer.vm", tpl);

            String content = evaluateTemplate(mergeData, tpl);
            JSONObject cfg = JSON.parseObject(content);
            validatePluginName(writer, reader, cfg);
            return JsonUtil.toString(cfg, true);
        } catch (Exception e) {
            throw new RuntimeException(tpl + "\n", e);
        }

    }

    /**
     * 利用velocity渲染模版
     *
     * @param mergeData
     * @param tpl       velocity 模版
     * @return
     */
    public static String evaluateTemplate(VelocityContext mergeData, final String tpl) {
        try (StringWriter writerContent = new StringWriter()) {
            velocityEngine.evaluate(mergeData, writerContent, "tablex-writer.vm", tpl);
            return writerContent.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void validatePluginName(IDataxWriter writer, IDataxReader reader, JSONObject cfg) {
        JSONObject job = cfg.getJSONObject("job");
        if (job != null) {
            JSONArray contentAry = job.getJSONArray("content");
            JSONObject rw = contentAry.getJSONObject(0);
            String readerName = rw.getJSONObject("reader").getString("name");
            String writerName = rw.getJSONObject("writer").getString("name");
            validatePluginName(writer.getDataxMeta(), reader.getDataxMeta()
                    , Objects.requireNonNull(writerName, "writerName can not be null")
                    , Objects.requireNonNull(readerName, "readerName can not be null"));
        } else {
            // 在单元测试流程中
            return;
        }

    }

    public static void validatePluginName(IDataXPluginMeta.DataXMeta writer, IDataXPluginMeta.DataXMeta reader,
                                          String writerName, String readerName) {
        if (!StringUtils.equals(readerName, reader.getName())) {
            throw new IllegalStateException("reader plugin name:" + readerName + " must equal with '" + reader.getName() + "'");
        }
        if (!StringUtils.equals(writerName, writer.getName())) {
            throw new IllegalStateException("writer plugin name:" + writerName + " must equal with '" + writer.getName() + "'");
        }
    }

    private VelocityContext createContext(IDataxContext reader, IDataxContext writer) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put(StoreResourceType.DATAX_NAME, this.dataxName);
        velocityContext.put("reader", reader);
        velocityContext.put("writer", writer);
        velocityContext.put("cfg", this.globalCfg);
        return velocityContext;
    }

}
