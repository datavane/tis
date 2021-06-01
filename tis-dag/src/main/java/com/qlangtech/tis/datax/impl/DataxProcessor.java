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

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorExtensionList;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.IBasicAppSource;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DataX任务执行方式的抽象
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 16:46
 */
public abstract class DataxProcessor implements IBasicAppSource, IdentityName, IDataxProcessor {

    protected static final String DEFAULT_DATAX_PROCESSOR_NAME = "DataxProcessor";
    public static final String DATAX_CFG_DIR_NAME = "dataxCfg";

    public static DataxProcessor load(IPluginContext pluginContext, String dataXName) {
        Optional<DataxProcessor> appSource = IAppSource.loadNullable(pluginContext, dataXName);
        if (appSource.isPresent()) {
            return appSource.get();
        } else {
            Descriptor<IAppSource> pluginDescMeta = DataxProcessor.getPluginDescMeta();
            Map<String, /** * attr key */com.alibaba.fastjson.JSONObject> formData
                    = new HashMap<String, com.alibaba.fastjson.JSONObject>() {
                @Override
                public JSONObject get(Object key) {
                    JSONObject o = new JSONObject();
                    o.put(Descriptor.KEY_primaryVal, null);
                    return o;
                }
            };
            Descriptor.ParseDescribable<IAppSource> appSourceParseDescribable
                    = pluginDescMeta.newInstance(pluginContext, formData, Optional.empty());
            return (DataxProcessor) appSourceParseDescribable.instance;
        }
    }

    public static Descriptor<IAppSource> getPluginDescMeta() {
        DescriptorExtensionList<IAppSource, Descriptor<IAppSource>> descs = TIS.get().getDescriptorList(IAppSource.class);
        Optional<Descriptor<IAppSource>> dataxProcessDescs
                = descs.stream().filter((des) -> DEFAULT_DATAX_PROCESSOR_NAME.equals(des.getDisplayName())).findFirst();
        if (!dataxProcessDescs.isPresent()) {
            throw new IllegalStateException("dataX process descriptor:" + DEFAULT_DATAX_PROCESSOR_NAME + " relevant descriptor can not be null");
        }
        return dataxProcessDescs.get();
    }

    public static DataXCreateProcessMeta getDataXCreateProcessMeta(IPluginContext pluginContext, String dataxPipeName) {
        DataxWriter writer = DataxWriter.load(pluginContext, dataxPipeName);
        DataxWriter.BaseDataxWriterDescriptor writerDesc = (DataxWriter.BaseDataxWriterDescriptor) writer.getDescriptor();
        DataxReader dataxReader = DataxReader.load(pluginContext, dataxPipeName);
        DataxReader.BaseDataxReaderDescriptor descriptor = (DataxReader.BaseDataxReaderDescriptor) dataxReader.getDescriptor();

        DataXCreateProcessMeta processMeta = new DataXCreateProcessMeta(writer, dataxReader);
        // 使用这个属性来控制是否要进入创建流程的第三步
        processMeta.setReaderRDBMS(descriptor.isRdbms());
        processMeta.setReaderHasExplicitTable(descriptor.hasExplicitTable());
        processMeta.setWriterRDBMS(writerDesc.isRdbms());
        return processMeta;
    }

    public abstract Application buildApp();

    private List<TableAlias> tableMaps;

    @Override
    public <T> T accept(IAppSourceVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Map<String, TableAlias> getTabAlias() {
        if (tableMaps == null) {
            return Collections.emptyMap();
        }
        return this.tableMaps.stream().collect(Collectors.toMap((m) -> m.getFrom(), (m) -> m));
    }

    @Override
    public final boolean isUnStructed2RDBMS(IPluginContext pluginCtx) {
        DataXCreateProcessMeta dataXCreateProcessMeta = getDataXCreateProcessMeta(pluginCtx, this.identityValue());
        return dataXCreateProcessMeta.isUnStructed2RDBMS();
    }

    @Override
    public boolean isRDBMS2UnStructed(IPluginContext pluginCtx) {
        DataXCreateProcessMeta dataXCreateProcessMeta = getDataXCreateProcessMeta(pluginCtx, this.identityValue());
        return dataXCreateProcessMeta.isReaderRDBMS() && !dataXCreateProcessMeta.isWriterRDBMS();
    }

    @Override
    public IDataxReader getReader(IPluginContext pluginCtx) {
        return DataxReader.load(pluginCtx, this.identityValue());
    }

    @Override
    public IDataxWriter getWriter(IPluginContext pluginCtx) {
        return DataxWriter.load(pluginCtx, this.identityValue());
    }

    public void setTableMaps(List<TableAlias> tableMaps) {
        this.tableMaps = tableMaps;
    }

    @Override
    public File getDataxCfgDir(IPluginContext pluginContext) {
        File dataXWorkDir = getDataXWorkDir(pluginContext);
        return new File(dataXWorkDir, DATAX_CFG_DIR_NAME);
    }

    @Override
    public File getDataXWorkDir(IPluginContext pluginContext) {

        KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(pluginContext, this.identityValue());
        File targetFile = readerStore.getTargetFile();
        return targetFile.getParentFile();
    }

    /**
     * 创建一个临时工作目录
     *
     * @param execId
     * @throws Exception
     */
    public void makeTempDir(String execId) throws Exception {

        File workingDir = getDataXWorkDir(null);
        FileUtils.copyDirectory(workingDir, new File(workingDir.getParentFile(), workingDir.getName() + "-" + execId));
    }

    /**
     * dataX配置文件列表
     *
     * @return
     */
    public List<String> getDataxCfgFileNames(IPluginContext pluginContext) {
        File dataxCfgDir = getDataxCfgDir(pluginContext);
        if (!dataxCfgDir.exists()) {
            throw new IllegalStateException("dataxCfgDir is not exist:" + dataxCfgDir.getAbsolutePath());
        }
        if (dataxCfgDir.list().length < 1) {
            throw new IllegalStateException("dataxCfgDir is empty can not find any files:" + dataxCfgDir.getAbsolutePath());
        }
        return Lists.newArrayList(dataxCfgDir.list((d, fileName) -> {
            return !DataXCfgGenerator.FILE_GEN.equals(fileName);
        }));
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

}
