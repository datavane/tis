/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.datax.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.*;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author: baisui 百岁
 * @create: 2021-04-20 18:06
 **/
public class DataXCfgGenerator {

    private transient static final VelocityEngine velocityEngine;

    static {
        try {
            velocityEngine = new VelocityEngine();
            Properties prop = new Properties();
            prop.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
            velocityEngine.init(prop);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//  public final Map<String, Object> getSetting() {
//    Map<String, Object> setting = Maps.newHashMap();
//    Map<String, Object> speed = Maps.newHashMap();
//    speed.put("channel", dataxProcessor.getChannel());
//    Map<String, Object> errorLimit = Maps.newHashMap();
//    errorLimit.put("record", dataxProcessor.getErrorLimitCount());
//    errorLimit.put("percentage", dataxProcessor.getErrorLimitPercentage());
//    setting.put("speed", speed);
//    setting.put("errorLimit", errorLimit);
//    return setting;
//  }

    private final IDataxProcessor dataxProcessor;
    private final IDataxGlobalCfg globalCfg;
    private final String dataxName;
    private final IPluginContext pluginCtx;

    public DataXCfgGenerator(IPluginContext pluginCtx, String dataxName, IDataxProcessor dataxProcessor) {
        Objects.requireNonNull(dataxProcessor, "dataXprocessor can not be null");
        IDataxGlobalCfg dataXGlobalCfg = dataxProcessor.getDataXGlobalCfg();
        Objects.requireNonNull(dataXGlobalCfg, "globalCfg can not be null");
        this.dataxProcessor = dataxProcessor;
        this.globalCfg = dataXGlobalCfg;
        this.dataxName = dataxName;
        this.pluginCtx = pluginCtx;
    }

    public String getTemplateContent() {
        final String tpl = globalCfg.getTemplate();
        IDataxReader reader = dataxProcessor.getReader(pluginCtx);
        IDataxWriter writer = dataxProcessor.getWriter(pluginCtx);
        String readerTpl = reader.getTemplate();
        String writerTpl = writer.getTemplate();
        if (StringUtils.isEmpty(readerTpl)) {
            throw new IllegalStateException("readerTpl of '" + reader.getDataxMeta().getName() + "' can not be null");
        }
        if (StringUtils.isEmpty(writerTpl)) {
            throw new IllegalStateException("writerTpl of '" + writer.getDataxMeta().getName() + "' can not be null");
        }
        String template = StringUtils.replace(tpl, "<!--reader-->", readerTpl);
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
        GenerateCfgs generateCfgs = new GenerateCfgs();
        IDataxReader reader = dataxProcessor.getReader(this.pluginCtx);

        File genFile = new File(parentDir, FILE_GEN);
        if (!genFile.exists()) {
            return generateCfgs;
        }

        File dataxCreateDDLDir = dataxProcessor.getDataxCreateDDLDir(this.pluginCtx);
        generateCfgs.createDDLFiles = Lists.newArrayList(dataxCreateDDLDir.list((dir, f) -> {
            return StringUtils.endsWith(f, IDataxProcessor.DATAX_CREATE_DDL_FILE_NAME_SUFFIX);
        }));

        Iterator<IDataxReaderContext> subTasks = reader.getSubTasks();
        IDataxReaderContext readerContext = null;
        File configFile = null;
        List<String> subTaskName = Lists.newArrayList();
        while (subTasks.hasNext()) {
            readerContext = subTasks.next();
            configFile = new File(parentDir, readerContext.getTaskName() + ".json");
            subTaskName.add(configFile.getName());
        }

        generateCfgs.genTime = Long.parseLong(FileUtils.readFileToString(genFile, TisUTF8.get()));
        generateCfgs.dataxFiles = subTaskName;
        return generateCfgs;
    }


    public static final String FILE_GEN = "gen";

    public GenerateCfgs startGenerateCfg(final File parentDir) throws Exception {
        GenerateCfgs cfgs = new GenerateCfgs();

        boolean unStructedReader = dataxProcessor.isReaderUnStructed(this.pluginCtx);


        IDataxReader reader = dataxProcessor.getReader(this.pluginCtx);
        IDataxWriter writer = dataxProcessor.getWriter(this.pluginCtx);
        DataxWriter.BaseDataxWriterDescriptor writerDescriptor = writer.getWriterDescriptor();

        Map<String, IDataxProcessor.TableAlias> tabAlias = dataxProcessor.getTabAlias();

        AtomicReference<Map<String, ISelectedTab>> selectedTabsRef = new AtomicReference<>();
        java.util.concurrent.Callable<Map<String, ISelectedTab>> selectedTabsCall = () -> {
            if (selectedTabsRef.get() == null) {
                Map<String, ISelectedTab> selectedTabs = reader.getSelectedTabs().stream().collect(Collectors.toMap((t) -> t.getName(), (t) -> t));
                selectedTabsRef.set(selectedTabs);
            }
            return selectedTabsRef.get();
        };

        File createDDLDir = this.dataxProcessor.getDataxCreateDDLDir(this.pluginCtx);
        Iterator<IDataxReaderContext> subTasks = reader.getSubTasks();
        IDataxReaderContext readerContext = null;
        File configFile = null;
        List<String> subTaskName = Lists.newArrayList();
        List<String> createDDLFiles = Lists.newArrayList();
        Optional<IDataxProcessor.TableMap> tableMapper = null;
        //StringBuffer createDDL = new StringBuffer();

        while (subTasks.hasNext()) {
            readerContext = subTasks.next();
            if (!dataxProcessor.isWriterSupportMultiTableInReader(this.pluginCtx)) {

                if (tabAlias.size() == 1) {
                    // 针对ES的情况
                    Optional<IDataxProcessor.TableMap> first
                            = tabAlias.values().stream().filter((t) -> t instanceof IDataxProcessor.TableMap)
                            .map((t) -> (IDataxProcessor.TableMap) t).findFirst();
                    if (first.isPresent()) {
                        tableMapper = first;
                    }
                } else {
                    //IDataxWriter writer = dataxProcessor.getWriter(this.pluginCtx);
                    if (writer instanceof IDataxProcessor.INullTableMapCreator) {
                        tableMapper = Optional.empty();
                    }
                }
                Objects.requireNonNull(tableMapper, "tabMapper can not be null,tabAlias.size()=" + tabAlias.size()
                        + ",tabs:[" + tabAlias.keySet().stream().collect(Collectors.joining(",")) + "]");
            } else if (unStructedReader) {
                // 是在DataxAction的doSaveWriterColsMeta() 方法中持久化保存的
                for (IDataxProcessor.TableAlias tab : tabAlias.values()) {
                    tableMapper = Optional.of((IDataxProcessor.TableMap) tab);
                    break;
                }
                Objects.requireNonNull(tableMapper, "tableMap can not be null");
            } else if (dataxProcessor.isRDBMS2UnStructed(this.pluginCtx)) {
                // example: mysql -> oss
                Map<String, ISelectedTab> selectedTabs = selectedTabsCall.call();
                ISelectedTab tab = selectedTabs.get(readerContext.getSourceEntityName());
                Objects.requireNonNull(tab, readerContext.getSourceEntityName() + " relevant tab can not be null");
                IDataxProcessor.TableMap m = new IDataxProcessor.TableMap();
                m.setSourceCols(tab.getCols());
                m.setTo(tab.getName());
                m.setFrom(tab.getName());
                tableMapper = Optional.of(m);
            } else if (dataxProcessor.isRDBMS2RDBMS(this.pluginCtx)) {
                // example: mysql -> mysql
                tableMapper = Optional.of(createTableMap(tabAlias, selectedTabsCall.call(), readerContext));
            } else {
                // example:oss -> oss
                // tableMapper = Optional.of(createTableMap(tabAlias, selectedTabsCall.call(), readerContext));
                throw new IllegalStateException("unexpect status");
            }

            if (tableMapper.isPresent()) {
                for (ISelectedTab.ColMeta colMeta : tableMapper.get().getSourceCols()) {
                    if (colMeta.getType() == null) {
                        throw new IllegalStateException("reader context:" + readerContext.getSourceEntityName()
                                + " relevant col type which's name " + colMeta.getName() + " can not be null");
                    }
                }
                // 创建ddl
                if (writerDescriptor.isSupportTabCreate()) {
                    IDataxProcessor.TableMap mapper = tableMapper.get();
                    StringBuffer createDDL = writer.generateCreateDDL(mapper);
                    if (createDDL != null) {
                        String sqlFileName = mapper.getTo() + IDataxProcessor.DATAX_CREATE_DDL_FILE_NAME_SUFFIX;
                        createDDLFiles.add(sqlFileName);
                        dataxProcessor.saveCreateTableDDL(this.pluginCtx, createDDL, sqlFileName, false);
                    }
                }
            }


            configFile = new File(parentDir, readerContext.getTaskName() + ".json");
            FileUtils.write(configFile, generateDataxConfig(readerContext, writer, reader, (tableMapper)), TisUTF8.get(), false);
            subTaskName.add(configFile.getName());
        }


        long current = System.currentTimeMillis();
        FileUtils.write(new File(parentDir, FILE_GEN), String.valueOf(current), TisUTF8.get(), false);
        cfgs.createDDLFiles = createDDLFiles;
        cfgs.dataxFiles = subTaskName;
        cfgs.genTime = current;
        return cfgs;
    }



    private static class GenerateCfgs {
        private List<String> dataxFiles = Collections.emptyList();
        private List<String> createDDLFiles = Collections.emptyList();
        private long genTime;

        public List<String> getDataxFiles() {
            return this.dataxFiles;
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
    }

    private IDataxProcessor.TableMap createTableMap(Map<String, IDataxProcessor.TableAlias> tabAlias
            , Map<String, ISelectedTab> selectedTabs, IDataxReaderContext readerContext) {

        IDataxProcessor.TableAlias tableAlias = tabAlias.get(readerContext.getSourceEntityName());
        if (tableAlias == null) {
            throw new IllegalStateException("sourceTable:" + readerContext.getSourceEntityName() + " can not find relevant 'tableAlias' keys:[" + tabAlias.keySet().stream().collect(Collectors.joining(",")) + "]");
        }
        ISelectedTab selectedTab = selectedTabs.get(readerContext.getSourceEntityName());
        IDataxProcessor.TableMap
                tableMap = new IDataxProcessor.TableMap();
        tableMap.setFrom(tableAlias.getFrom());
        tableMap.setTo(tableAlias.getTo());
        tableMap.setSourceCols(selectedTab.getCols());
        return tableMap;
    }

    /**
     * @param readerContext
     * @param tableMap
     * @return 生成的配置文件内容
     * @throws IOException
     */
    public String generateDataxConfig(IDataxReaderContext readerContext, IDataxWriter writer, IDataxReader reader
            , Optional<IDataxProcessor.TableMap> tableMap) throws IOException {
        Objects.requireNonNull(writer, "writer can not be null");
        StringWriter writerContent = null;
        final String tpl = getTemplateContent();
        if (StringUtils.isEmpty(tpl)) {
            throw new IllegalStateException("velocity template content can not be null");
        }
        // IDataxWriter writer = dataxProcessor.getWriter(this.pluginCtx);
        // IDataxReader reader = dataxProcessor.getReader(this.pluginCtx);

//        DataxWriter.BaseDataxWriterDescriptor wdesc = writer.getWriterDescriptor();
//        if(wdesc.isSupportTabCreate()){
//
//        }

        try {
            VelocityContext mergeData = createContext(readerContext, writer.getSubTask(tableMap));
            writerContent = new StringWriter();
            velocityEngine.evaluate(mergeData, writerContent, "tablex-writer.vm", tpl);
        } catch (Exception e) {
            throw new RuntimeException(tpl + "\n", e);
        }
        String content = writerContent.toString();
        try {
            JSONObject cfg = JSON.parseObject(content);
            validatePluginName(writer, reader, cfg);
            return JsonUtil.toString(cfg);
        } catch (Exception e) {
            throw new RuntimeException(content, e);
        }
    }

    public void validatePluginName(IDataxWriter writer, IDataxReader reader, JSONObject cfg) {
        JSONObject job = cfg.getJSONObject("job");
        if (job != null) {
            JSONArray contentAry = job.getJSONArray("content");
            JSONObject rw = contentAry.getJSONObject(0);
            String readerName = rw.getJSONObject("reader").getString("name");
            String writerName = rw.getJSONObject("writer").getString("name");
            validatePluginName(writer.getDataxMeta(), reader.getDataxMeta(), writerName, readerName);
        } else {
            // 在单元测试流程中
            return;
        }

    }

    public static void validatePluginName(IDataXPluginMeta.DataXMeta writer, IDataXPluginMeta.DataXMeta reader, String writerName, String readerName) {
        if (!StringUtils.equals(readerName, reader.getName())) {
            throw new IllegalStateException("reader plugin name:" + readerName + " must equal with '" + reader.getName() + "'");
        }
        if (!StringUtils.equals(writerName, writer.getName())) {
            throw new IllegalStateException("writer plugin name:" + writerName + " must equal with '" + writer.getName() + "'");
        }
    }

    private VelocityContext createContext(IDataxContext reader, IDataxContext writer) {
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("reader", reader);
        velocityContext.put("writer", writer);
        velocityContext.put("cfg", this.globalCfg);
        return velocityContext;
    }
}
