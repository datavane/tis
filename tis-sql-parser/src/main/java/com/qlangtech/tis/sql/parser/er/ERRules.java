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
package com.qlangtech.tis.sql.parser.er;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.sql.parser.SqlTaskNode;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import com.qlangtech.tis.sql.parser.meta.TabExtraMeta;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ERRules implements IPrimaryTabFinder {

    public static final String ER_RULES_FILE_NAME = "er_rules.yaml";

    private static final Yaml yaml;

    private List<TableRelation> relationList = Lists.newArrayList();

    private List<DependencyNode> dumpNodes = Lists.newArrayList();

    private Map<String, DependencyNode> /**
     * TODO 先用String，将来再改成EntityName
     */
    dumpNodesMap;

    private List<String> ignoreIncrTriggerEntities;

    private List<PrimaryTableMeta> primaryTabs;

    private List<TabFieldProcessor> processors = null;

    private Map<EntityName, TabFieldProcessor> processorMap;

    static {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setIndent(4);
        dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        // dumperOptions.setAnchorGenerator((n) -> "b");
        // dumperOptions.setAnchorGenerator();
        // dumperOptions.setAnchorGenerator(null);
        dumperOptions.setPrettyFlow(false);
        dumperOptions.setSplitLines(true);
        dumperOptions.setLineBreak(DumperOptions.LineBreak.UNIX);
        dumperOptions.setWidth(1000000);
        yaml = new Yaml(new Constructor(), new Representer() {

            @Override
            protected Node representScalar(Tag tag, String value, DumperOptions.ScalarStyle style) {
                // 大文本实用block
                if (Tag.STR == tag && value.length() > 100) {
                    style = DumperOptions.ScalarStyle.FOLDED;
                }
                return super.representScalar(tag, value, style);
            }

            @Override
            protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
                if (propertyValue == null) {
                    return null;
                }
                return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
            }
        }, dumperOptions);
        yaml.addTypeDescription(new TypeDescription(TableRelation.class, Tag.MAP, TableRelation.class));
        yaml.addTypeDescription(new TypeDescription(ERRules.class, Tag.MAP, ERRules.class));
        yaml.addTypeDescription(new TypeDescription(DependencyNode.class, Tag.MAP, DependencyNode.class));
    }

    private DependencyNode getDumpNode(EntityName tabName) {
        if (this.dumpNodesMap == null) {
            this.dumpNodesMap = this.dumpNodes.stream().collect(Collectors.toMap((r) -> r.getName(), (r) -> r));
        }
        DependencyNode result = this.dumpNodesMap.get(tabName.getTabName());
        if (result == null) {
            throw new IllegalStateException("can not find table:" + tabName + " in " + this.dumpNodes.stream().map((r) -> String.valueOf(r.parseEntityName())).collect(Collectors.joining(",")));
        }
        return result;
    }

    public static void write(String topology, ERRules erRules) throws Exception {
        File parent = new File(SqlTaskNode.parent, topology);
        FileUtils.forceMkdir(parent);
        FileUtils.write(new File(parent, ER_RULES_FILE_NAME), ERRules.serialize(erRules), TisUTF8.getName(), false);
    }

    /**
     * 规则已经存在
     *
     * @param topology
     * @return
     */
    public static boolean ruleExist(String topology) {
        return getErRuleFile(topology).exists();
    }

    private static File getErRuleFile(String topology) {
        final String topologyPath = topology + File.separator + ER_RULES_FILE_NAME;
        CenterResource.copyFromRemote2Local(SqlTaskNode.NAME_DATAFLOW_DIR + File.separator + topologyPath, true);
        return new File(SqlTaskNode.parent, topologyPath);
    }

    public static String serialize(ERRules rules) {
        return yaml.dump(rules);
    }

    public void addDumpNode(DependencyNode node) {
        this.dumpNodes.add(node);
    }

    public List<DependencyNode> getDumpNodes() {
        return dumpNodes;
    }

    public void setDumpNodes(List<DependencyNode> dumpNodes) {
        this.dumpNodes = dumpNodes;
    }

    public static ERRules deserialize(String rulesStr) {
        return yaml.loadAs(rulesStr, ERRules.class);
    }

    public static Optional<ERRules> getErRule(String topology) {
        if (!ruleExist(topology)) {
            return Optional.empty();
        }
        File erRuleFile = getErRuleFile(topology);
        try {
            return Optional.of(deserialize(FileUtils.readFileToString(erRuleFile, TisUTF8.get())));
        } catch (Exception e) {
            throw new RuntimeException("topology:" + topology + ",file path:" + erRuleFile.getAbsolutePath(), e);
        }
    }

    // 配置需要忽略执行增量的表的集合
    // private List<String> ignoreIncrTriggerEntities = Lists.newArrayList();
    public void setRelationList(List<TableRelation> relationList) {
        this.relationList = relationList;
    }

    public void addRelation(TableRelation relation) {
        this.relationList.add(relation);
    }

    public List<TableRelation> getRelationList() {
        return this.relationList;
    }

    /**
     * 取得表的所有主表信息
     *
     * @param
     * @return
     */
    public List<TableRelation> getAllParent(EntityName entityName) {
        List<TableRelation> parentRefs = Lists.newArrayList();
        DependencyNode child;
        TableRelation resultRel = null;
        for (TableRelation relation : this.getRelationList()) {
            child = relation.getChild();
            if (StringUtils.equals(child.getName(), entityName.getTabName())) {
                resultRel = relation;
                parentRefs.add(resultRel);
            }
        }
        return parentRefs;
    }

    /**
     * 取得第一个父表关系, 一个表有多个父表，优先取为主索引表的父表
     *
     * @param tabName
     * @return
     */
    public Optional<TableRelation> getFirstParent(String tabName) {
        List<PrimaryTableMeta> primaryTableNames = this.getPrimaryTabs();
        DependencyNode child;
        TableRelation resultRel = null;
        Optional<PrimaryTableMeta> hasPrimaryParent = null;
        for (TableRelation relation : this.getRelationList()) {
            child = relation.getChild();
            if (StringUtils.equals(child.getName(), tabName)) {
                resultRel = relation;
                hasPrimaryParent = primaryTableNames.stream().filter((r) -> StringUtils.equals(relation.getParent().getName(), r.getTabName())).findFirst();
                // 优先选取主索引表
                if (hasPrimaryParent.isPresent()) {
                    return Optional.of(relation);
                }
            }
        }
        if (resultRel != null) {
            return Optional.of(resultRel);
        }
        return Optional.empty();
    }

    /**
     * 取得子表引用集合
     *
     * @param tabName
     * @return
     */
    public List<TableRelation> getChildTabReference(EntityName tabName) {
        List<TableRelation> childRefs = Lists.newArrayList();
        for (TableRelation relation : this.getRelationList()) {
            if (StringUtils.equals(relation.getParent().getName(), tabName.getTabName())) {
                childRefs.add(relation);
            }
        }
        return childRefs;
    }

    // 添加增量忽略处理的表
    // public void addIgnoreIncrTriggerEntity(String tabName) {
    // this.ignoreIncrTriggerEntities.add(tabName);
    // }
    // 增量执行过程中某些维表不需要监听变更时间
    @JSONField(serialize = false)
    public List<String> getIgnoreIncrTriggerEntities() {
        if (ignoreIncrTriggerEntities == null) {
            ignoreIncrTriggerEntities = this.getDumpNodes().stream().filter((d) -> d.getExtraMeta() != null && !d.getExtraMeta().isMonitorTrigger()).map((d) -> d.getName()).collect(Collectors.toList());
        }
        return this.ignoreIncrTriggerEntities;
    }

    public boolean isTimestampVerColumn(EntityName tableName, String colName) {
        if (this.isTriggerIgnore(tableName)) {
            return false;
        }
        DependencyNode dumpNode = getDumpNode(tableName);
        TabExtraMeta extraMeta = dumpNode.getExtraMeta();
        if (extraMeta == null || StringUtils.isEmpty(extraMeta.getTimeVerColName())) {
            throw new IllegalStateException("table:" + tableName + " can not find 'timeVerColName' prop");
        }
        return StringUtils.equals(extraMeta.getTimeVerColName(), colName);
    }

    /**
     * 取得表的时间戳生成列
     *
     * @param tableName
     * @return
     */
    public String getTimestampVerColumn(EntityName tableName) {
        if (this.isTriggerIgnore(tableName)) {
            throw new IllegalStateException("tab:" + tableName + " is not monitor in incr process");
        }
        DependencyNode dumpNode = getDumpNode(tableName);
        TabExtraMeta extraMeta = dumpNode.getExtraMeta();
        if (extraMeta == null || StringUtils.isEmpty(extraMeta.getTimeVerColName())) {
            throw new IllegalStateException("table:" + tableName + " can not find 'timeVerColName' prop");
        }
        return extraMeta.getTimeVerColName();
    }

    @JSONField(serialize = false)
    public List<TabFieldProcessor> getTabFieldProcessors() {
        if (processors == null) {
            this.processors = this.getDumpNodes().stream().filter((d) -> d.getExtraMeta() != null && d.getExtraMeta().getColTransfers().size() > 0).map((d) -> new TabFieldProcessor(d.parseEntityName(), d.getExtraMeta().getColTransfers())).collect(Collectors.toList());
        }
        return processors;
    }

    @JSONField(serialize = false)
    public Map<EntityName, TabFieldProcessor> getTabFieldProcessorMap() {
        if (processorMap == null) {
            processorMap = getTabFieldProcessors().stream().collect(Collectors.toMap((r) -> r.tabName, (r) -> r));
        }
        return processorMap;
    }

    /**
     * 索引主标，如果索引是UNION结构，则返回的集合的size则大于1
     *
     * @return
     */
    @JSONField(serialize = false)
    public List<PrimaryTableMeta> getPrimaryTabs() {
        if (primaryTabs == null) {
            primaryTabs = this.getDumpNodes().stream().filter((d) -> d.getExtraMeta() != null && d.getExtraMeta().isPrimaryIndexTab()).map((d) -> new PrimaryTableMeta(d.getName(), d.getExtraMeta())).collect(Collectors.toList());
        }
        return primaryTabs;
    }

    @Override
    public Optional<TableMeta> getPrimaryTab(IDumpTable entityName) {
        Optional<TableMeta> first = this.getPrimaryTabs().stream().filter((p) -> StringUtils.equals(p.getTabName(), entityName.getTableName())).map((r) -> (TableMeta) r).findFirst();
        return first;
    }

    /**
     * 表实体在增量处理时候是否需要忽略
     *
     * @param entityName
     * @return
     */
    public boolean isTriggerIgnore(EntityName entityName) {
        return this.getIgnoreIncrTriggerEntities().contains(entityName.getTabName());
    }

    public static TableRelation $(String id, SqlTaskNodeMeta.SqlDataFlowTopology topology, String parent, String child, TabCardinality c) {
        if (StringUtils.isEmpty(id)) {
            throw new IllegalArgumentException("param id can not be null");
        }
        Map<String, DependencyNode> /**
         * table name
         */
        dumpNodesMap = topology.getDumpNodesMap();
        return new TableRelation(id, dumpNodesMap.get(parent), dumpNodesMap.get(child), c);
    }
}
