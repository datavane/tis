/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.sql.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.facebook.presto.sql.parser.ParsingOptions;
import com.facebook.presto.sql.parser.SqlParser;
import com.facebook.presto.sql.tree.Expression;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.fullbuild.taskflow.ITemplateContext;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import com.qlangtech.tis.sql.parser.er.ERRules;
import com.qlangtech.tis.sql.parser.er.IPrimaryTabFinder;
import com.qlangtech.tis.sql.parser.meta.ColumnTransfer;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.sql.parser.meta.NodeType;
import com.qlangtech.tis.sql.parser.meta.Position;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import com.qlangtech.tis.sql.parser.utils.DefaultDumpNodeMapContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import java.io.*;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 对应脚本配置的反序列化对象类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年7月29日
 */
public class SqlTaskNodeMeta implements ISqlTask {

    public static final Yaml yaml;

    private static final String FILE_NAME_DEPENDENCY_TABS = "dependency_tabs.yaml";

    private static final String FILE_NAME_PROFILE = "profile.json";

    public static final String KEY_PROFILE_TIMESTAMP = "timestamp";

    public static final String KEY_PROFILE_TOPOLOGY = "topology";

    public static final String KEY_PROFILE_ID = "id";

    private static final SqlParser sqlParser = new com.facebook.presto.sql.parser.SqlParser();

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(FlowStyle.BLOCK);
        dumperOptions.setIndent(4);
        dumperOptions.setDefaultScalarStyle(ScalarStyle.PLAIN);
        dumperOptions.setAnchorGenerator((n) -> "a");
        // dumperOptions.setAnchorGenerator(null);
        dumperOptions.setPrettyFlow(false);
        dumperOptions.setSplitLines(true);
        dumperOptions.setLineBreak(LineBreak.UNIX);
        dumperOptions.setWidth(1000000);
        yaml = new Yaml(new Constructor(), new Representer() {

            @Override
            protected Node representScalar(Tag tag, String value, ScalarStyle style) {
                // 大文本实用block
                if (Tag.STR == tag && value.length() > 100) {
                    style = ScalarStyle.FOLDED;
                }
                return super.representScalar(tag, value, style);
            }

            @Override
            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
                if (propertyValue == null) {
                    return null;
                }
                if (DependencyNode.class.equals(property.getType())) {
                    return null;
                } else {
                    return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
                }
            }
        }, dumperOptions);
        yaml.addTypeDescription(new TypeDescription(DependencyNode.class, Tag.MAP, DependencyNode.class));
        yaml.addTypeDescription(new TypeDescription(SqlTaskNodeMeta.class, Tag.MAP, SqlTaskNodeMeta.class));
        yaml.addTypeDescription(new TypeDescription(DumpNodes.class, Tag.MAP, DumpNodes.class));
    }

    @Override
    public RewriteSql getRewriteSql(String taskName, Map<IDumpTable, ITabPartition> dumpPartition, IPrimaryTabFinder erRules, ITemplateContext templateContext, boolean isFinalNode) {
        if (dumpPartition.size() < 1) {
            throw new IllegalStateException("dumpPartition set size can not small than 1");
        }
        Optional<List<Expression>> parameters = Optional.empty();
        IJoinTaskContext joinContext = templateContext.joinTaskContext();
        SqlStringBuilder builder = new SqlStringBuilder();
        SqlRewriter rewriter = new SqlRewriter(builder, dumpPartition, erRules, parameters, isFinalNode, joinContext);
        // 执行rewrite
        try {
            rewriter.process(sqlParser.createStatement(this.getSql(), new ParsingOptions()), 0);
        } catch (Exception e) {
            String dp = dumpPartition.entrySet().stream().map((ee) -> "[" + ee.getKey() + "->" + ee.getValue().getPt() + "]").collect(Collectors.joining(","));
            throw new IllegalStateException("task:" + taskName + ",isfinalNode:" + isFinalNode + ",dump tabs pt:" + dp + "\n" + e.getMessage(), e);
        }
        SqlRewriter.AliasTable primaryTable = rewriter.getPrimayTable();
        if (primaryTable == null) {
            throw new IllegalStateException("task:" + taskName + " has not find primary table");
        }
        // return ;
        return new RewriteSql(builder.toString(), rewriter.getPrimayTable());
    }

    /**
     * Content用于保存到yaml内容中 <br>
     * 保证每行开头为空格 <br>
     * 保证文本最后部位空格<br>
     *
     * @param content
     * @return
     */
    public static String processBigContent(String content) {
        LineIterator lIt = null;
        String line = null;
        StringBuffer result = new StringBuffer();
        // final boolean firstLine = true;
        try (StringReader reader = new StringReader(processFileContent(content))) {
            lIt = IOUtils.lineIterator(reader);
            while (lIt.hasNext()) {
                line = lIt.next();
                if (!StringUtils.startsWith(line, " ")) {
                    result.append(" ");
                }
                result.append(line).append("\n");
            // firstLine = false;
            }
        }
        return StringUtils.trimToEmpty(result.toString());
    }

    private static String processFileContent(String content) {
        return content.replace("\r\n", "\n");
    }

    /**
     * 将对象持久化
     *
     * @param topology
     */
    public static void persistence(SqlDataFlowTopology topology, File parent) throws Exception {
        if (topology.profile == null || StringUtils.isEmpty(topology.getName()) || topology.getTimestamp() < 1 || topology.getDataflowId() < 1) {
            throw new IllegalArgumentException("param topology's prop name timestamp or dataflowid neither can be null");
        }
        for (SqlTaskNodeMeta process : topology.getNodeMetas()) {
            try (OutputStreamWriter output = new OutputStreamWriter(FileUtils.openOutputStream(new File(parent, process.getExportName() + ".yaml")))) {
                yaml.dump(process, output);
            }
        }
        try (OutputStreamWriter output = new OutputStreamWriter(FileUtils.openOutputStream(new File(parent, FILE_NAME_DEPENDENCY_TABS)))) {
            yaml.dump(new DumpNodes(topology.getDumpNodes()), output);
        }
        try (OutputStreamWriter output = new OutputStreamWriter(FileUtils.openOutputStream(new File(parent, FILE_NAME_PROFILE)), TisUTF8.get())) {
            JSONObject profile = new JSONObject();
            profile.put(KEY_PROFILE_TIMESTAMP, topology.getTimestamp());
            profile.put(KEY_PROFILE_TOPOLOGY, topology.getName());
            profile.put(KEY_PROFILE_ID, topology.getDataflowId());
            IOUtils.write(profile.toJSONString(), output);
        }
    }

    @SuppressWarnings("all")
    public static SqlDataFlowTopology getSqlDataFlowTopology(String topologyName) throws Exception {
        SqlDataFlowTopology result = getSqlDataFlowTopology(getTopologyDir(topologyName));
        ERRules.getErRule(topologyName);
        return result;
    }

    private static TopologyDir getTopologyDir(String topologyName) {
        File wfDir = SqlTaskNode.parent;
        wfDir = new File(wfDir, topologyName);
        try {
            FileUtils.forceMkdir(wfDir);
        } catch (IOException e) {
            throw new RuntimeException("wfDir:" + wfDir.getAbsolutePath(), e);
        }
        return new // 
        TopologyDir(// 
        wfDir, SqlTaskNode.NAME_DATAFLOW_DIR + "/" + topologyName);
    }

    private static class TopologyDir {

        private final File dir;

        private final String relativePath;

        public TopologyDir(File dir, String relativePath) {
            this.dir = dir;
            this.relativePath = relativePath;
        }

        public File synchronizeRemoteRes(String resName) {
            // CenterResource.copyFromRemote2Local(url, localFile);
            return CenterResource.copyFromRemote2Local(CenterResource.getPath(relativePath, resName), true);
        // return localFile;
        }

        public List<File> synchronizeSubRemoteRes() {
            // URL url = CenterResource.getPathURL(relativePath);
            List<String> subFiles = CenterResource.getSubFiles(relativePath, false, true);
            List<File> subs = Lists.newArrayList();
            for (String f : subFiles) {
                subs.add(synchronizeRemoteRes(f));
            }
            return subs;
        }
    }

    public static TopologyProfile getTopologyProfile(String topologyName) throws Exception {
        TopologyDir topologyDir = getTopologyDir(topologyName);
        return getTopologyProfile(topologyDir.synchronizeRemoteRes(FILE_NAME_PROFILE));
    }

    @SuppressWarnings("all")
    public static SqlDataFlowTopology getSqlDataFlowTopology(TopologyDir topologyDir) throws Exception {
        SqlDataFlowTopology topology = new SqlDataFlowTopology();
        List<File> subFiles = topologyDir.synchronizeSubRemoteRes();
        if (subFiles.size() < 1) {
            throw new IllegalStateException("subFiles size can not small than 1,file:" + topologyDir.dir);
        }
        // topologyDir.synchronizeRemoteRes(FILE_NAME_DEPENDENCY_TABS);
        File dependencyTabFile = new File(topologyDir.dir, FILE_NAME_DEPENDENCY_TABS);
        try {
            // dump节点
            try (Reader reader = new InputStreamReader(FileUtils.openInputStream(dependencyTabFile), TisUTF8.get())) {
                DumpNodes dumpTabs = yaml.loadAs(reader, DumpNodes.class);
                // topology.set
                topology.addDumpTab(dumpTabs.getDumps());
            }
        } catch (Exception e) {
            throw new RuntimeException(dependencyTabFile.getAbsolutePath(), e);
        }
        Iterator<File> fit = FileUtils.iterateFiles(topologyDir.dir, new String[] { "yaml" }, false);
        File next = null;
        while (fit.hasNext()) {
            next = fit.next();
            if (ERRules.ER_RULES_FILE_NAME.equals(next.getName()) || FILE_NAME_DEPENDENCY_TABS.equals(next.getName())) {
                continue;
            }
            SqlTaskNodeMeta sqlTaskNodeMeta = deserializeTaskNode(next);
            topology.addNodeMeta(sqlTaskNodeMeta);
        }
        // 设置profile内容信息
        topology.setProfile(getTopologyProfile(new File(topologyDir.dir, FILE_NAME_PROFILE)));
        return topology;
    }

    /**
     * 取得topology的基本信息
     *
     * @param profileFile
     * @return
     * @throws Exception
     */
    public static TopologyProfile getTopologyProfile(File profileFile) throws Exception {
        if (!profileFile.exists()) {
            throw new IllegalStateException("profile not exist:" + profileFile.getAbsolutePath());
        }
        // 设置profile内容信息
        try (InputStream r = FileUtils.openInputStream(profileFile)) {
            JSONObject j = JSON.parseObject(IOUtils.toString(r, TisUTF8.get()));
            TopologyProfile profile = new TopologyProfile();
            profile.setDataflowId(j.getLong(KEY_PROFILE_ID));
            profile.setName(j.getString(KEY_PROFILE_TOPOLOGY));
            profile.setTimestamp(j.getLong(KEY_PROFILE_TIMESTAMP));
            return profile;
        }
    }

    public static SqlTaskNodeMeta deserializeTaskNode(File file) {
        try {
            try (Reader scriptReader = new InputStreamReader(FileUtils.openInputStream(file), TisUTF8.get())) {
                return deserializeTaskNode(scriptReader);
            }
        } catch (Exception e) {
            throw new RuntimeException(file.getAbsolutePath(), e);
        }
    }

    public static SqlTaskNodeMeta deserializeTaskNode(Reader scriptReader) throws Exception {
        SqlTaskNodeMeta sqlTaskNodeMeta = null;
        sqlTaskNodeMeta = yaml.loadAs(scriptReader, SqlTaskNodeMeta.class);
        return sqlTaskNodeMeta;
    }

    public static class TopologyProfile {

        private long timestamp;

        private String name;

        private long dataflowId;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getDataflowId() {
            return dataflowId;
        }

        public void setDataflowId(long dataflowId) {
            this.dataflowId = dataflowId;
        }
    }

    public static class SqlDataFlowTopology {

        private TopologyProfile profile;

        private List<SqlTaskNodeMeta> nodeMetas = Lists.newArrayList();

        private List<DependencyNode> dumpNodes = Lists.newArrayList();

        public static SqlDataFlowTopology deserialize(String jsonContent) {
            return JSON.parseObject(jsonContent, SqlDataFlowTopology.class);
        }

        @JSONField(serialize = false)
        public long getTimestamp() {
            return profile.getTimestamp();
        }

        @JSONField(serialize = false)
        public String getName() {
            if (this.profile == null) {
                throw new IllegalStateException("profile can not be null");
            }
            return this.profile.getName();
        }

        @JSONField(serialize = false)
        public long getDataflowId() {
            return profile.dataflowId;
        }

        public void setProfile(TopologyProfile profile) {
            this.profile = profile;
        }

        public TopologyProfile getProfile() {
            return this.profile;
        }

        // ///////////////////////////////////////
        // ================================================================
        // 
        private Map<String, DependencyNode> dumpNodesMap;

        @JSONField(serialize = false)
        public Map<String, /**
         * table name
         */
        DependencyNode> getDumpNodesMap() {
            if (this.dumpNodesMap == null) {
                this.dumpNodesMap = Maps.newHashMap();
                List<DependencyNode> dumpNodes = this.getDumpNodes();
                dumpNodes.stream().forEach((r) -> {
                    this.dumpNodesMap.put(r.getName(), r);
                    r.setExtraSql(null);
                });
            }
            return this.dumpNodesMap;
        }

        // /**
        // * 取得dump表的Map数据结构
        // *
        // * @return
        // */
        // public Map<String /**table name*/, DependencyNode> getDumpNodesMap() {
        // Map<String, DependencyNode> result = Maps.newHashMap();
        // dumpNodes.stream().forEach((r) -> result.put(r.getName(), r));
        // return result;
        // }
        @JSONField(serialize = false)
        public String getDAGSessionSpec() {
            StringBuffer dagSessionSpec = new StringBuffer();
            // ->a ->b a,b->c
            for (DependencyNode dump : this.getDumpNodes()) {
                dagSessionSpec.append("->").append(dump.getId()).append(" ");
            }
            for (SqlTaskNodeMeta pnode : this.getNodeMetas()) {
                dagSessionSpec.append(Joiner.on(",").join(pnode.getDependencies().stream().map((r) -> r.getId()).iterator()));
                dagSessionSpec.append("->").append(pnode.getId()).append(" ");
            }
            return dagSessionSpec.toString();
        }

        /**
         * 取得dataflow中的表依赖关系
         *
         * @return
         */
        // public List<TabPair> getTabPair() {
        // List<TabPair> result = null;
        // for (SqlTaskNodeMeta pnode : this.getNodeMetas()) {
        // pnode.getSql();
        // }
        // return result;
        // }
        private Map<EntityName, List<TableTupleCreator>> createDumpNodesMap() {
            final Map<EntityName, List<TableTupleCreator>> result = Maps.newHashMap();
            this.dumpNodes.stream().forEach((node) -> {
                List<TableTupleCreator> tables = null;
                TableTupleCreator tupleCreator = null;
                EntityName entityName = EntityName.parse(node.getDbName() + "." + node.getName());
                tupleCreator = new TableTupleCreator(entityName.toString(), NodeType.DUMP);
                tables = result.get(entityName);
                if (tables == null) {
                    tables = Lists.newArrayList();
                    result.put(entityName, tables);
                }
                tupleCreator.setRealEntityName(entityName);
                tables.add(tupleCreator);
            });
            return result;
        }

        private List<SqlTaskNode> allNodes = null;

        public List<SqlTaskNode> parseTaskNodes() throws Exception {
            if (this.allNodes == null) {
                final DefaultDumpNodeMapContext dumpNodsContext = new DefaultDumpNodeMapContext(this.createDumpNodesMap());
                this.allNodes = this.getNodeMetas().stream().map((m) -> {
                    SqlTaskNode node = new SqlTaskNode(EntityName.parse(m.getExportName()), m.getNodeType(), dumpNodsContext);
                    node.setContent(m.getSql());
                    return node;
                }).collect(Collectors.toList());
                dumpNodsContext.setAllJoinNodes(allNodes);
            }
            return this.allNodes;
        }

        public TableTupleCreator parseFinalSqlTaskNode() throws Exception {
            List<SqlTaskNode> taskNodes = this.parseTaskNodes();
            SqlTaskNode task = null;
            final String finalNodeName = this.getFinalNode().getExportName();
            // 
            Optional<SqlTaskNode> f = // 
            taskNodes.stream().filter((n) -> finalNodeName.equals(n.getExportName().getTabName())).findFirst();
            if (!f.isPresent()) {
                String setStr = taskNodes.stream().map((n) -> n.getExportName().getJavaEntityName()).collect(Collectors.joining(","));
                throw new IllegalStateException("finalNodeName:" + finalNodeName + " can not find node in[" + setStr + "]");
            }
            /**
             * *******************************
             * 开始解析
             * *******************************
             */
            task = f.get();
            return task.parse();
        }

        public SqlDataFlowTopology() {
            super();
        }

        public void addNodeMeta(SqlTaskNodeMeta nodeMeta) {
            this.nodeMetas.add(nodeMeta);
        }

        public List<SqlTaskNodeMeta> getNodeMetas() {
            return this.nodeMetas;
        }

        public void addDumpTab(List<DependencyNode> ns) {
            this.dumpNodes.addAll(ns);
        }

        public void addDumpTab(DependencyNode ns) {
            this.dumpNodes.add(ns);
        }

        public void setNodeMetas(List<SqlTaskNodeMeta> nodeMetas) {
            this.nodeMetas = nodeMetas;
        }

        public void setDumpNodes(List<DependencyNode> dumpNodes) {
            this.dumpNodes = dumpNodes;
        }

        public List<DependencyNode> getDumpNodes() {
            return this.dumpNodes;
        }

        @JSONField(serialize = false)
        public SqlTaskNodeMeta getFinalNode() throws Exception {
            List<SqlTaskNodeMeta> finalNodes = getFinalNodes();
            if (finalNodes.size() != 1) {
                throw new IllegalStateException(// 
                "finalNodes size must be 1,but now is:" + finalNodes.size() + ",nodes:[" + // 
                finalNodes.stream().map((r) -> r.getExportName()).collect(Collectors.joining(",")) + "]");
            }
            Optional<SqlTaskNodeMeta> taskNode = finalNodes.stream().findFirst();
            if (!taskNode.isPresent()) {
                throw new IllegalStateException("final node shall be exist");
            }
            return taskNode.get();
        }

        /**
         * 取dataflow的最终的输出节点(没有下游节点的节点)
         *
         * @return
         * @throws Exception
         */
        public List<SqlTaskNodeMeta> getFinalNodes() throws Exception {
            Map<String, RefCountTaskNode> /*export Name*/
            exportNameRefs = Maps.newHashMap();
            for (SqlTaskNodeMeta meta : getNodeMetas()) {
                exportNameRefs.put(meta.getId(), new RefCountTaskNode(meta));
            }
            // List<SqlTaskNode> taskNodes = parseTaskNodes();// SqlTaskNode.parseTaskNodes(topology);
            RefCountTaskNode refCount = null;
            for (SqlTaskNodeMeta meta : getNodeMetas()) {
                for (DependencyNode entry : meta.getDependencies()) {
                    refCount = exportNameRefs.get(entry.getId());
                    if (refCount == null) {
                        continue;
                    }
                    refCount.incr();
                }
            }
            // 
            List<SqlTaskNodeMeta> finalNodes = // 
            exportNameRefs.values().stream().filter(// 
            (e) -> e.refCount.get() < 1).map((r) -> r.taskNode).collect(Collectors.toList());
            return finalNodes;
        }
    }

    private static class RefCountTaskNode {

        private final AtomicInteger refCount = new AtomicInteger();

        private final SqlTaskNodeMeta taskNode;

        public RefCountTaskNode(SqlTaskNodeMeta taskNode) {
            this.taskNode = taskNode;
        }

        public void incr() {
            this.refCount.incrementAndGet();
        }
    }

    private String id;

    private String exportName;

    private String type;

    private Position position;

    private String sql;

    private List<DependencyNode> dependencies = Lists.newArrayList();

    public List<DependencyNode> getDependencies() {
        return this.dependencies;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDependencies(List<DependencyNode> required) {
        this.dependencies = required;
    }

    public void addDependency(DependencyNode required) {
        this.dependencies.add(required);
    }

    @Override
    public String getSql() {
        return this.sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String getExportName() {
        return exportName;
    }

    public void setExportName(String exportName) {
        this.exportName = exportName;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public String getType() {
        return this.type;
    }

    @JSONField(serialize = false)
    public NodeType getNodeType() {
        return NodeType.parse(this.type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class HiveColTransfer {

        public static HiveColTransfer instance = new HiveColTransfer();

        public String transfer(String base, String field, ColumnTransfer transfer) {
            try {
                Method method = HiveColTransfer.class.getMethod(transfer.getTransfer(), String.class, String.class, ColumnTransfer.class);
                return (String) method.invoke(instance, base, field, transfer);
            } catch (Exception e) {
                throw new RuntimeException("base:" + base + ",field:" + field + "," + transfer.toString(), e);
            }
        }

        // regexp_replace(tp.curr_date,'-',''),
        // select tp.curr_date, from_unixtime(int(tp.op_time/1000), 'yyyyMMddHHmmss') as op_time
        // ,from_unixtime(int(tp.operate_date/1000), 'yyyyMMddHHmmss') as operate_date
        // , int(tp.load_time+'0000') as int1
        // , int(tp.load_time) as int2
        // ,from_unixtime(int(tp.load_time+'0000'), 'yyyyMMddHHmmss') as load_time
        // ,from_unixtime(int(tp.load_time), 'yyyyMMddHHmmss') as load_time2
        // ,from_unixtime(int(tp.modify_time), 'yyyyMMddHHmmss') as modify_time
        // from ods_order_compare_out.totalpayinfo as tp limit 10
        public String dateYYYYmmdd(String base, String field, ColumnTransfer transfer) {
            final String param = getParam(base, field, transfer);
            return "regexp_replace(" + param + ",'-','') as " + transfer.getColKey();
        }

        public String dateYYYYMMddHHmmss(String base, String field, ColumnTransfer transfer) {
            final String param = getParam(base, field, transfer);
            return "from_unixtime(int(" + param + "), 'yyyyMMddHHmmss') as " + transfer.getColKey();
        }

        private String getParam(String base, String field, ColumnTransfer transfer) {
            return StringUtils.replace(transfer.getParam(), "value", base + "." + field);
        }
    }
}
