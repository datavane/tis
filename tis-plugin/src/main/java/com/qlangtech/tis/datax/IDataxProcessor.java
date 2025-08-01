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
package com.qlangtech.tis.datax;

import com.alibaba.datax.plugin.writer.hdfswriter.HdfsColMeta;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.datax.impl.TransformerInfo;
import com.qlangtech.tis.extension.Describable.IRefreshable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.StoreResourceTypeGetter;
import com.qlangtech.tis.plugin.datax.transformer.OutputParameter;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules.TransformerOverwriteCols;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import com.qlangtech.tis.plugin.ds.IDBReservedKeys;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.trigger.JobTrigger;
import com.qlangtech.tis.realtime.yarn.rpc.SynResTarget;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Datax 执行器可以在各种容器上执行 https://github.com/alibaba/DataX
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:38
 */
public interface IDataxProcessor extends IdentityName, StoreResourceTypeGetter, IRefreshable {

    static File getWriterDescFile(IPluginContext pluginContext, String dataXName) {
        File workDir = getDataXWorkDir(pluginContext, dataXName);
        return new File(workDir, "writerDesc");
    }

    @Override
    default void refresh() {

    }

    default JSONObject createNode() {
        JSONObject initNode = new JSONObject();
        initNode.put(StoreResourceType.DATAX_NAME, this.identityValue());
        // 是否是dataflow的处理类型
        initNode.put(StoreResourceType.KEY_STORE_RESOURCE_TYPE, this.getResType().getType());
        return initNode;
    }

    /**
     * @param pluginCtx
     * @param tableName
     * @return
     */
    Pair<List<RecordTransformerRules>, IPluginStore>
    getRecordTransformerRulesAndPluginStore(IPluginContext pluginCtx, String tableName);


    default SynResTarget getResTarget() {
        switch (this.getResType()) {
            case DataApp:
                return SynResTarget.pipeline(this.identityValue());
            case DataFlow:
                return SynResTarget.transform(this.identityValue());
            default:
                throw new IllegalStateException("resType:" + this.getResType() + " is not support ");
        }
    }

    default DataXName getDataXName() {
        return new DataXName(this.identityValue(), this.getResType());
    }

    static File getDataXWorkDir(IPluginContext pluginContext, String appName) {
        KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(pluginContext, appName);
        File targetFile = readerStore.getTargetFile().getFile();
        return targetFile.getParentFile();
    }

    static Descriptor getWriterDescriptor(UploadPluginMeta pluginMeta //IPluginContext pluginContext, String dataXName
    ) throws Exception {
        DataXName dataXName = pluginMeta.getDataXName(false);
        if (dataXName != null) {
//            Descriptor descriptor = TIS.get().getDescriptor(FileUtils.readFileToString(getWriterDescFile(pluginMeta.getPluginContext(), dataXName), TisUTF8.get()));
//            return descriptor;
            return getWriterDescriptor(pluginMeta.getPluginContext(), dataXName.getPipelineName());
        } else {
            return null;
        }
    }

    static Descriptor getWriterDescriptor(IPluginContext pluginContext, String dataXName
    ) {
        try {
            Objects.requireNonNull(pluginContext, "pluginContext can not be null");
            if (StringUtils.isEmpty(dataXName)) {
                throw new IllegalArgumentException("param dataXName can not be empty");
            }
            File writerDescFile = getWriterDescFile(pluginContext, dataXName);
            if (!writerDescFile.exists()) {
                return null;
            }
            Descriptor descriptor = TIS.get().getDescriptor(FileUtils.readFileToString(writerDescFile, TisUTF8.get()));
            return descriptor;
        } catch (IOException e) {
            throw new RuntimeException("dataXName:" + dataXName, e);
        }
    }

    /**
     * 创建一个临时工作目录
     *
     * @param execId
     * @throws Exception
     */
    public default void makeTempDir(String execId) throws Exception {

        File workingDir = getDataXWorkDir((IPluginContext) null);
        FileUtils.copyDirectory(workingDir, new File(workingDir.getParentFile(), KeyedPluginStore.TMP_DIR_NAME + workingDir.getName() + "-" + execId));
    }


    /**
     * workflow 中支持
     *
     * @param pluginCtx
     * @return
     */
    default List<IDataxReader> getReaders(IPluginContext pluginCtx) {
        return Collections.singletonList(getReader(pluginCtx));
    }

    /**
     * 针对dataFlow的场景
     *
     * @param pluginContext
     * @param tab
     * @return
     */
    IDataxReader getReader(IPluginContext pluginContext, ISelectedTab tab);

    IDataxReader getReader(IPluginContext pluginCtx);

    default IDataxWriter getWriter(IPluginContext pluginCtx) {
        return getWriter(pluginCtx, true);
    }

    IDataxWriter getWriter(IPluginContext pluginCtx, boolean validateNull);

    IDataxGlobalCfg getDataXGlobalCfg();

    public File getDataxCfgDir(IPluginContext pluginCtx);

    /**
     * 取dataX配置文件
     *
     * @param pluginCtx
     * @param criteria
     * @return
     */
    public default File getDataXCfgFile(IPluginContext pluginCtx, DataXCfgFile criteria) {
        File cfgDir = getDataxCfgDir(pluginCtx);
        File cfgFile = new File(cfgDir, criteria.getDbFactoryId() + File.separator + criteria.getFileName());
        if (!cfgFile.exists()) {
            throw new IllegalStateException("target file:" + cfgFile.getAbsolutePath());
        }
        return cfgFile;
    }

    /**
     * 取得自动建表目录
     *
     * @param pluginContext
     * @return
     */
    public File getDataxCreateDDLDir(IPluginContext pluginContext);

    /**
     * 更新自动建表的脚本内容
     *
     * @param pluginCtx
     * @param createDDL
     * @param sqlFileName
     * @param overWrite
     */
    public void saveCreateTableDDL(IPluginContext pluginCtx, StringBuffer createDDL, String sqlFileName, boolean overWrite) throws IOException;

    public File getDataXWorkDir(IPluginContext pluginContext);

    /**
     * 从非结构化的数据源导入到结构化的数据源，例如从OSS导入到MySQL
     *
     * @return
     */
    public boolean isReaderUnStructed(IPluginContext pluginCtx);

    public boolean isRDBMS2UnStructed(IPluginContext pluginCtx);

    public boolean isRDBMS2RDBMS(IPluginContext pluginCtx);

    public boolean isWriterSupportMultiTableInReader(IPluginContext pluginCtx);

//    public default DataXCfgGenerator.GenerateCfgs getDataxCfgFileNames(IPluginContext pluginCtx) {
//        return getDataxCfgFileNames(pluginCtx, Optional.empty());
//    }

    /**
     * dataX配置文件列表
     *
     * @return
     */
    public DataXCfgGenerator.GenerateCfgs getDataxCfgFileNames(IPluginContext pluginCtx, Optional<JobTrigger> partialTrigger);

    /**
     * 表映射
     * Map<String, TableAlias>
     *
     * @return key: fromTabName
     */
    public TableAliasMapper getTabAlias(IPluginContext pluginCtx);

    /**
     * 是否支持批量执行
     *
     * @param pluginCtx
     * @return
     */
    default boolean isSupportBatch(IPluginContext pluginCtx) {
        List<IDataxReader> readers = this.getReaders(pluginCtx);
        DataxWriter writer = (DataxWriter) this.getWriter(pluginCtx);
        DataxWriter.BaseDataxWriterDescriptor writerDesc = (DataxWriter.BaseDataxWriterDescriptor) writer.getDescriptor();
        return isSupportBatch(readers) && writerDesc.isSupportBatch();
    }

    private boolean isSupportBatch(List<IDataxReader> readers) {
        for (IDataxReader readerDesc : readers) {
            if (!readerDesc.isSupportBatch()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param groupedChildTask
     * @return
     */
    Set<TransformerInfo> getTransformerInfo(IPluginContext pluginCtx, Map<String, List<DBDataXChildTask>> groupedChildTask);

    /**
     * 标示DataXWriter会自己创建IDataxProcessor.TableMap实例，使用这个标示必须满足isSupportMultiTable为false，具体例子可以看DataXMongodbWriter
     */
    public interface INullTableMapCreator {
    }

    /**
     * 类似MySQL(A库)导入MySQL(B库) A库中的一张a表可能对应的B库的表为aa表名称会不一致，
     */
    public class TableMap extends TableAlias {
        private final ISelectedTab tab;

        /**
         * 将transformer 规则添加到列末尾
         *
         * @param transformerRules
         * @return
         */
        public List<IColMetaGetter> appendTransformerRuleCols(Optional<RecordTransformerRules> transformerRules) {
            List<IColMetaGetter> cols = transformerRules.map((rule) -> {
                TransformerOverwriteCols<OutputParameter> outputParameters = rule.overwriteCols(getSourceCols());
                return outputParameters.getCols().stream().map((c) -> (IColMetaGetter) c).collect(Collectors.toList());
            }).orElseGet(() -> {
                return getSourceCols().stream().map((c) -> c).collect(Collectors.toList());
            });
            return cols;
        }

        public TableMap(TableAlias tabAlia, ISelectedTab tab) {
            this(tab);
            this.setTo(tabAlia.getTo());
            this.setFrom(tabAlia.getFrom());
        }

        public TableMap(ISelectedTab tab) {
            super(tab.getName());
            this.tab = tab;
        }

        public TableMap(final List<CMeta> cmetas) {
            this(Optional.empty(), cmetas);
        }

        public TableMap(Optional<String> tabName, final List<CMeta> cmetas) {
            List<CMeta> cMetas = rewriteCols(cmetas);
            List<String> pks = cMetas.stream()
                    .filter((c) -> c.isPk()).map((c) -> c.getName()).collect(Collectors.toUnmodifiableList());
            this.tab = (new ISelectedTab() {
                @Override
                public String getName() {
                    return tabName.get();
                }

                @Override
                public List<String> getPrimaryKeys() {
                    return pks;
                }

                @Override
                public List<CMeta> getCols() {
                    return cMetas;
                }
            });
        }

        protected List<CMeta> rewriteCols(final List<CMeta> cmetas) {
            return cmetas;
        }


        public List<String> getPrimaryKeys() {
            return tab.getPrimaryKeys();
        }

        public static TableMap create(String tableName, List<IColMetaGetter> cols) {
            List<CMeta> cmetas = Lists.newArrayList();
            CMeta cm = null;
            // HdfsColMeta c = null;

            for (IColMetaGetter col : cols) {
                cm = getCMeta(col);
                cmetas.add(cm);
            }
            return createByColMeta(tableName, cmetas);
        }

        public static CMeta getCMeta(IColMetaGetter col) {
            CMeta cm = new CMeta();
            if (col instanceof HdfsColMeta) {
                HdfsColMeta c = (HdfsColMeta) col;
                cm.setName(c.colName);
                cm.setNullable(c.nullable);
                cm.setType(c.type);
                cm.setPk(c.pk);
            } else {
                cm.setName(col.getName());
                cm.setNullable(true);
                cm.setType(col.getType());
                cm.setPk(col.isPk());
            }

            return cm;
        }

        public static TableMap createByColMeta(String tableName, List<CMeta> colMetas) {
            IDataxProcessor.TableMap tableMapper = new IDataxProcessor.TableMap(colMetas);
            tableMapper.setFrom(tableName);
            tableMapper.setTo(tableName);
            return tableMapper;
        }

        public List<CMeta> getSourceCols() {
            //Objects.requireNonNull(tab, "param tab can not be null");
            return this.getSourceTab().getCols();
        }

        @JSONField(serialize = false)
        public ISelectedTab getSourceTab() {
            Objects.requireNonNull(tab, "param tab can not be null");
            return this.tab;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.getFrom());
        }
//        public void setSourceCols(List<ISelectedTab.ColMeta> sourceCols) {
//            this.sourceCols = sourceCols;
//        }

    }

    public class TabCols {
        private final List<String> cols;
        private final IDBReservedKeys dbReservedKeys;

        public static TabCols create(
                IDBReservedKeys dbReservedKeys, TableMap tm, Optional<RecordTransformerRules> transformerRules) {

            List<IColMetaGetter> cols = tm.appendTransformerRuleCols(transformerRules);
//            transformerRules.map((rule) -> {
//                TransformerOverwriteCols<OutputParameter> outputParameters = rule.overwriteCols(tm.getSourceCols());
//                return outputParameters.getCols().stream().map((c) -> (IColMetaGetter) c).collect(Collectors.toList());
//            }).orElseGet(() -> {
//                return tm.getSourceCols().stream().map((c) -> c).collect(Collectors.toList());
//            });

            return new TabCols(dbReservedKeys, cols.stream().map((c) -> c.getName()).collect(Collectors.toList()));
        }

        private TabCols(IDBReservedKeys dbReservedKeys, List<String> cols) {
            this.cols = cols;
            this.dbReservedKeys = dbReservedKeys;
        }

        public List<String> getRawCols() {
            return this.cols;
        }

        public String getColsQuotes() {
            return getColumnWithLink(Optional.of("\""));
        }

        public String getCols() {
            return getColumnWithLink(Optional.empty());
        }

        protected String getColumnWithLink(Optional<String> includeIn) {
            return this.cols.stream().map(r -> this.dbReservedKeys.getEscapedEntity(includeIn, r)).collect(Collectors.joining(","));
        }
    }
}
