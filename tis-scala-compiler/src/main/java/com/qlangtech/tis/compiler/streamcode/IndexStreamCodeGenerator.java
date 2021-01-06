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
package com.qlangtech.tis.compiler.streamcode;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.compiler.java.JavaCompilerProcess;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.manage.common.incr.StreamContextConstant;
import com.qlangtech.tis.sql.parser.DBNode;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.sql.parser.stream.generate.FacadeContext;
import com.qlangtech.tis.sql.parser.stream.generate.StreamComponentCodeGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class IndexStreamCodeGenerator {
    public final String collection;

    private SqlTaskNodeMeta.SqlDataFlowTopology dfTopology;

    public final long incrScriptTimestamp;

    public List<FacadeContext> facadeList;

    private StreamComponentCodeGenerator streamCodeGenerator;

    private File streamScriptRootDir;

    private final IDBTableNamesGetter dbTableNamesGetter;

    private String workflowName;

    // 自动生成的incr脚本中需要dao支持吗？
    private final boolean excludeFacadeDAOSupport;

    public IndexStreamCodeGenerator(String collection, String workflowName, long incrScriptTimestamp
            , IDBTableNamesGetter dbTableNamesGetter, boolean excludeFacadeDAOSupport) throws Exception {
        if (StringUtils.isEmpty(collection)) {
            throw new IllegalArgumentException("argument collection can not be null");
        }
        // this.appDomain = CoreAction.this.getAppDomain();
        // this.appDomain.getAppName();
        this.collection = collection;
        this.workflowName = workflowName;
        this.dbTableNamesGetter = dbTableNamesGetter;
        if (incrScriptTimestamp < 1) {
            throw new IllegalArgumentException("illegal incrScriptTimestamp can not small than 1");
        }
        this.excludeFacadeDAOSupport = excludeFacadeDAOSupport;
        // 增量脚本时间戳
        // ManageUtils.formatNowYyyyMMddHHmmss(latestOptime);
        this.incrScriptTimestamp = incrScriptTimestamp;
        this.initialize();
    }

    /**
     * 删除生成的脚本
     */
    public void deleteScript() {
        try {
            FileUtils.forceDelete(streamScriptRootDir);
        } catch (IOException e) {
            throw new RuntimeException("path:" + streamScriptRootDir, e);
        }
    }

    private void initialize() throws Exception {
        // FullbuildWorkflowAction.getDataflowTopology(CoreAction.this, this.workFlow);
        this.dfTopology = SqlTaskNodeMeta.getSqlDataFlowTopology(this.workflowName);
        this.dbTables = getDependencyTables(dfTopology);
        facadeList = Lists.newArrayList();
        streamCodeGenerator = new StreamComponentCodeGenerator(
                this.collection, incrScriptTimestamp, facadeList, dfTopology, excludeFacadeDAOSupport);
        this.streamScriptRootDir = StreamContextConstant.getStreamScriptRootDir(this.collection, incrScriptTimestamp);
    }

    private Map<DBNode, List<String>> /* tables */
            dbTables;

    public Map<DBNode, List<String>> getDbTables() {
        return this.dbTables;
    }

    // public Set<DBNode> getDbSet() {
    // return this.dbTables.keySet();
    // }
    public boolean isIncrScriptDirCreated() {
        return this.streamCodeGenerator.isIncrScriptDirCreated();
    }

    // getIncrScriptDir().getAbsolutePath()
    public String getIncrScriptDirPath() {
        return this.streamCodeGenerator.getIncrScriptDir().getAbsolutePath();
    }

    // public AppDomainInfo getAppDomain() {
    // return appDomain;
    // }
    public SqlTaskNodeMeta.SqlDataFlowTopology getDfTopology() {
        return dfTopology;
    }

    public long getIncrScriptTimestamp() {
        return this.incrScriptTimestamp;
    }

    public List<FacadeContext> getFacadeList() {
        return this.facadeList;
    }

    public void generateConfigFiles() throws Exception {
        this.streamCodeGenerator.generateConfigFiles();
    }

    public JavaCompilerProcess.FileObjectsContext getSpringXmlConfigsObjectsContext() {
        JavaCompilerProcess.FileObjectsContext xmlConfigs = new JavaCompilerProcess.FileObjectsContext();
        Stack<String> childPath = new Stack<>();
        File parent = streamCodeGenerator.getSpringConfigFilesDir();
        if (!parent.exists()) {
            throw new IllegalStateException("file:" + parent.getAbsolutePath() + " is not exist");
        }
        JavaCompilerProcess.traversingFiles(childPath, parent, xmlConfigs, (zp, child) -> {
            JavaCompilerProcess.ZipPath zipPath = new JavaCompilerProcess.ZipPath(zp, child.getName(), JavaFileObject.Kind.OTHER);
            JavaCompilerProcess.ResourcesFile res = new JavaCompilerProcess.ResourcesFile(zipPath, child);
            xmlConfigs.resources.add(res);
        });
        return xmlConfigs;
    }

    /**
     * 读出增量scala脚本中的内容
     *
     * @return
     * @throws Exception
     */
    public String readIncrScriptMainFileContent() throws Exception {
        File incrScript = this.streamCodeGenerator.getIncrScriptMainFile();
        if (!incrScript.exists()) {
            throw new IllegalStateException("incrScript:" + incrScript.getAbsolutePath() + " is not exist");
        }
        return FileUtils.readFileToString(incrScript, TisUTF8.getName());
    }

    public StreamComponentCodeGenerator getStreamCodeGenerator() {
        return this.streamCodeGenerator;
    }

    public void generateStreamScriptCode() throws Exception {
        // 生成scala代码
        this.streamCodeGenerator.build();
        // 生成spring配置文件
        // this.streamCodeGenerator.generateConfigFiles(this.mqConfigMetas);
    }

    public void saveDbDependencyMetaConfig() throws Exception {
        this.dbTables.keySet();
    }

    // public File getMqConfigMetaFile() {
    // return new File(this.streamScriptRootDir, "meta/mq_config.yaml");
    // }
    // /**
    // * db 依赖版本配置依赖元数据
    // * @return
    // */
    // public File getDbDependencyConfigMetaFile() {
    // return new File(this.streamScriptRootDir
    // , StreamContextConstant.DIR_META + "/" + StreamContextConstant.FILE_DB_DEPENDENCY_CONFIG);
    // }

    /**
     * 取得依赖的db->table映射关系
     */
    private Map<DBNode, /* dbname */
            List<String>> getDependencyTables(SqlTaskNodeMeta.SqlDataFlowTopology dfTopology) throws Exception {
        Map<DBNode, List<String>> /* tables */
                dbNameMap = Maps.newHashMap();
        List<String> tables = null;
        DBNode dbNode = null;
        for (DependencyNode node : dfTopology.getDumpNodes()) {
            dbNode = new DBNode(node.getDbName(), Integer.parseInt(node.getDbid()));
            node.parseEntityName();
            tables = dbNameMap.get(dbNode);
            if (tables == null) {
                // DB 下的全部table
                tables = Lists.newArrayList();
                dbNameMap.put(dbNode, tables);
                // DatasourceTableCriteria tableCriteria = new DatasourceTableCriteria();
                // tableCriteria.createCriteria().andDbIdEqualTo(Integer.parseInt(node.getDbid()));
                // List<DatasourceTable> tableList = CoreAction.this.getWorkflowDAOFacade().getDatasourceTableDAO()
                // .selectByExample(tableCriteria);
                // dbNameMap.put(dbNode,
                // tableList.stream().map((t) -> t.getName()).collect(Collectors.toList()));
            }
            tables.add(node.getName());
        }
        for (Map.Entry<DBNode, List<String>> /* tables */
                entry : dbNameMap.entrySet()) {
            entry.setValue(dbTableNamesGetter.getTableNames(entry.getKey().getDbId(), entry.getValue()));
        }
        return dbNameMap;
    }


    public interface IDBTableNamesGetter {

        /**
         * @param dbId
         * @param reWriteableTables 为dataflow中已经使用到的table枚举，在实现方法中可以通过 dbId在系统库中补充
         * @return
         */
        public List<String> getTableNames(Integer dbId, List<String> reWriteableTables);
    }
}
