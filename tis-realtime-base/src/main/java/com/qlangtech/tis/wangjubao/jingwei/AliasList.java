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
package com.qlangtech.tis.wangjubao.jingwei;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.transfer.IPk;
import com.qlangtech.tis.realtime.transfer.IRowValueGetter;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.TisSolrInputDocument;
import com.qlangtech.tis.realtime.transfer.ruledriven.AllThreadLocal;
import com.qlangtech.tis.realtime.transfer.ruledriven.MediaResultKey;
import com.qlangtech.tis.sql.parser.er.JoinerKey;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.wangjubao.jingwei.Alias.ILazyTransfer;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocumentBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年11月2日
 */
public class AliasList {

    private static final Logger logger = LoggerFactory.getLogger(AliasList.class);

    private final List<Alias> aliasList;

    private final Map<String, Alias> /**
     * col origin name
     */
            aliasColMap;

    private final Set<String> focusColumns;

    private final String tableName;

    // 是否是索引的主表
    private final boolean primaryOfIndex;

    // 不需要监听增量消息
    private final boolean ignoreIncrTrigger;

    // 数据库的物理主键
    private Alias pk;

    // 表记录时间戳生成
    private Alias timeVersionCol;

    // 子表用,关联键一般是多个
    private final Map<List<Alias>, AliasList> /**
     * 主表的FK列
     */
            parentTabReference = Maps.newHashMap();

    // 主表 外表的引用
    // Lists.newArrayList();
    private final Map<List<Alias>, AliasList> /* 子表的PK列*/ childTabReference = Maps.newHashMap();

    private AliasList(String tableName, boolean primaryOfIndex, boolean ignoreIncrTrigger, Alias... alias) {
        this.aliasList = Collections.unmodifiableList(Lists.newArrayList(alias));
        this.primaryOfIndex = primaryOfIndex;
        this.ignoreIncrTrigger = ignoreIncrTrigger;
        Map<String, Alias> /*col origin name*/ colMap = Maps.newHashMap();
        this.aliasList.stream().forEach((r) -> colMap.put(r.getName(), r));
        this.aliasColMap = Collections.unmodifiableMap(colMap);
        this.focusColumns = this.aliasList.stream().filter((a) -> {
            if (a.isPk()) {
                if (pk != null) {
                    throw new IllegalStateException("pk have been set,pk:" + pk.getName() + ",new:" + a.getName());
                }
                pk = a;
            }
//            if (a.timeVer) {
//                if (timeVersionCol != null) {
//                    throw new IllegalStateException("timeVersionCol have been set,timeVersionCol:" + timeVersionCol.getName() + ",new:" + a.getName());
//                }
//                timeVersionCol = a;
//            }
            return !a.ignoreChange;
        }).map(Alias::getName).collect(toSet());
        this.tableName = tableName;
        if (pk == null) {
            throw new IllegalStateException("table:" + tableName + " have not been set PK");
        }
//        if (!this.isIgnoreIncrTrigger() && timeVersionCol == null) {
//            throw new IllegalStateException("table:" + tableName + " have not set 'timeVersionCol'");
//        }
    }

    /**
     * 时间戳生成processor
     *
     * @return
     */
    public Alias getTimeVersionCol() {
        //Objects.requireNonNull(this.timeVersionCol, "timeVersionCol can not be null");
        return this.timeVersionCol;
    }

    public void setTimeVersionCol(Alias timeVersionCol) {
        this.timeVersionCol = timeVersionCol;
    }

    private static final List<RowMap> emptyGetterList = Collections.emptyList();

    private IGetterRowsFromOuterPersistence getterRowsFromOuterPersistence = new IGetterRowsFromOuterPersistence() {

        @Override
        public String toString() {
            return "DummpGetterRowsFromOuterPersistence";
        }

        @Override
        public List<RowMap> process(String rowTabName, IRowValueGetter rowVals, IPk pk) {
            return emptyGetterList;
        }
    };

    public boolean isIgnoreIncrTrigger() {
        return this.ignoreIncrTrigger;
    }

    /**
     * 取得外部表引用
     *
     * @return
     */
    public Map<List<Alias>, AliasList> getChildTabReference() {
        return this.childTabReference;
    }

    // 是否是嵌套式文档的父文档记录
    private boolean nestChildRow = false;

    public Alias getPk() {
        return this.pk;
    }

    /**
     * 从外部持久化数据源中提取数据
     *
     * @param pk
     * @return
     */
    @SuppressWarnings("all")
    public List<RowMap> getRowsFromOuterPersistence(String rowTabName, IRowValueGetter rowVals, IPk pk) {
        if (getterRowsFromOuterPersistence == null) {
            throw new IllegalStateException("table:" + this.tableName + " relevant getterRowsFromOuterPersistence can not be null");
        }
        return // (List<IRowValueGetter>)
                getterRowsFromOuterPersistence.process(rowTabName, rowVals, pk);
    }

    /**
     * 添加外部表引用
     */
    private void addChildTableRef(List<Alias> pks, AliasList outTab) {
        this.childTabReference.put(pks, outTab);
    }

    private void addParentTableRef(List<Alias> fks, AliasList outTab) {
        this.parentTabReference.put(fks, outTab);
    }

    public static class BuilderList {

        private List<Builder> builders = Lists.newArrayList();

        private BuilderList() {
        }

        public static BuilderList create() {
            return new BuilderList();
        }

        public Builder add(String name) {
            Builder builder = Builder.create(name);
            builders.add(builder);
            return builder;
        }

        public Map<String, /** * TableName  */
                AliasList> build() {
            Map<String, AliasList> /*** TableName  */
                    tabColumnMetaMap = Maps.newHashMap();
            for (AliasList.Builder colsBuilder : builders) {
                tabColumnMetaMap.put(colsBuilder.getTabName(), colsBuilder.buildMetaList());
            }
            // 设置表的关联
            for (AliasList.Builder colsBuilder : builders) {
                this.buildTableRelation(tabColumnMetaMap, colsBuilder, true);
                this.buildTableRelation(tabColumnMetaMap, colsBuilder, false);
            }
            return Collections.unmodifiableMap(tabColumnMetaMap);
        }

        private void buildTableRelation(Map<String, AliasList> tabColumnMetaMap, Builder colsBuilder, boolean parent2Child) {
            AliasList tabProcess = null;
            for (TableRef ref : (parent2Child ? colsBuilder.childTabRefs : colsBuilder.parentTabRefs)) {
                List<Alias> /* 链接 字表的列 */
                        keys = Lists.newArrayList();
                AliasList tab = tabColumnMetaMap.get(colsBuilder.getTabName());
                if (tab == null) {
                    throw new IllegalStateException("table:" + colsBuilder.getTabName() + " can not find relevant AliasList");
                }
                for (JoinerKey joinerKey : ref.getJoinerKeys()) {
                    keys.add(tab.getColMeta((parent2Child ? joinerKey.getParentKey() : joinerKey.getChildKey())));
                }
                tabProcess = tabColumnMetaMap.get(ref.getEntityName().getTabName());
                if (parent2Child) {
                    tab.addChildTableRef(keys, tabProcess);
                } else {
                    tab.addParentTableRef(keys, tabProcess);
                }
            }
        }
    }

    public static class TableRef {

        private final EntityName parentName;

        private final List<JoinerKey> joinerKeys;

        public TableRef(EntityName parentName, List<JoinerKey> joinerKeys) {
            this.parentName = parentName;
            this.joinerKeys = joinerKeys;
        }

        public EntityName getEntityName() {
            return parentName;
        }

        public List<JoinerKey> getJoinerKeys() {
            return joinerKeys;
        }
    }

    public static class Builder {

        private String tabName;

        private boolean nestChildRow = false;

        private boolean primaryIndexOfIndex = false;

        private boolean ignoreIncrTrigger = false;

        private List<Alias.Builder> aliasListBuilder = Lists.newArrayList();

        // 是否已经构建过
        private boolean build = false;

        private IGetterRowsFromOuterPersistence getterRowsFromOuterPersistence;

        public List<TableRef> parentTabRefs = Lists.newArrayList();

        public List<TableRef> childTabRefs = Lists.newArrayList();

        // 添加主表关联
        public void addParentTabRef(EntityName parentName, List<JoinerKey> joinerKeys) {
            this.parentTabRefs.add(new TableRef(parentName, joinerKeys));
        }

        // 添加子表关联
        public void addChildTabRef(EntityName childName, List<JoinerKey> joinerKeys) {
            this.childTabRefs.add(new TableRef(childName, joinerKeys));
        }

        public String getTabName() {
            return this.tabName;
        }

        public Builder setGetterRowsFromOuterPersistence(IGetterRowsFromOuterPersistence getterRowsFromOuterPersistence) {
            this.getterRowsFromOuterPersistence = getterRowsFromOuterPersistence;
            return this;
        }

        public Builder setNestChildRow() {
            checkAccess();
            this.nestChildRow = true;
            return this;
        }

        public Builder setIgnoreIncrTrigger() {
            checkAccess();
            this.ignoreIncrTrigger = true;
            return this;
        }

        // 设置为索引的主表
        public Builder setPrimaryTableOfIndex() {
            checkAccess();
            this.primaryIndexOfIndex = true;
            return this;
        }

        private Builder(String tabName) {
            this.tabName = tabName;
        }

        private static Builder create(String tableName) {
            return new Builder(tableName);
        }

        public Builder add(Alias.Builder... colBuilder) {
            checkAccess();
            for (Alias.Builder b : colBuilder) {
                aliasListBuilder.add(b);
            }
            return this;
        }

        public List<Alias.Builder> getAliasListBuilder() {
            return this.aliasListBuilder;
        }

        private void checkAccess() {
            if (build) {
                throw new IllegalStateException(" can not add Alias.Builder after build");
            }
        }

        private AliasList buildMetaList() {
            // <<<<<<<<<< 为了去除集合中有重复的列
            Map<String, AliasColBuilderWrapper> /*** from colum name */removeDuplicateColumnMap = Maps.newHashMap();

            AliasColBuilderWrapper preBuilder = null;
            // new Alias[aliasListBuilder.size()];
            List<Alias> aliasArray = Lists.newArrayList();
            for (Alias.Builder cbuilder : aliasListBuilder) {
                if (cbuilder.processTime) {
                    continue;
                }
                preBuilder = removeDuplicateColumnMap.get(cbuilder.getToName());
                if (preBuilder == null) {
                    removeDuplicateColumnMap.put(cbuilder.getToName(), new AliasColBuilderWrapper(tabName, cbuilder));
                } else {
                    preBuilder.colBuilders.add(cbuilder);
                }
            }
            for (Map.Entry<String, AliasColBuilderWrapper> /*** from colum name */entry : removeDuplicateColumnMap.entrySet()) {
                aliasArray.addAll(entry.getValue().buildColumnAlias());
            }


            AliasList result = new AliasList(tabName, this.primaryIndexOfIndex, this.ignoreIncrTrigger, aliasArray.toArray(new Alias[aliasArray.size()]));


            for (Alias.Builder cb : aliasListBuilder) {
                if (cb.timeVer) {
                    if (result.getTimeVersionCol() != null) {
                        throw new IllegalStateException("tabName:" + tabName + " have set");
                    }
                    if (cb.processTime) {
                        result.setTimeVersionCol(Alias.processTimeColCreator);
                    } else {
                        result.setTimeVersionCol(cb.build());
                    }
                }
            }
            if(!result.isIgnoreIncrTrigger()){
                Objects.requireNonNull(result.getTimeVersionCol(), "timeVersionCol can not be null");
            }
            result.nestChildRow = this.nestChildRow;
            result.getterRowsFromOuterPersistence = this.getterRowsFromOuterPersistence;
            build = true;
            return result;
        }
    }

    private static class AliasColBuilderWrapper {

        private final List<Alias.Builder> colBuilders = Lists.newArrayList();

        private final Alias.Builder firstColBuilder;

        private final String tableName;

        public AliasColBuilderWrapper(String tableName, Alias.Builder colBuilder) {
            this.colBuilders.add(colBuilder);
            this.firstColBuilder = colBuilder;
            this.tableName = tableName;
        }


        public Collection<Alias> buildColumnAlias() {
            if (firstColBuilder.pk) {
                // 则要打notcopy的标记，以免最后在tisDocument中被设置值
                if (colBuilders.size() < 2) {
                    logger.info("table:{},col transfer will not copy:[{}]", this.tableName, firstColBuilder);
                    // 先去掉 2020/10/30
                    // firstColBuilder.notCopy();
                } else {
                    for (Alias.Builder b : colBuilders) {
                        if (!b.copy) {
                            // 后续的列如果有notcopy的话就不拷贝
                            firstColBuilder.notCopy();
                            break;
                        }
                    }
                }
                return Collections.singleton(firstColBuilder.build());
            }

            Collection<Alias> aliases = colBuilders.stream().map((b) -> {
                return b.build();
            }).collect(Collectors.toList());
            return aliases;
        }
    }

    /**
     * 没有设置外键就认为是主表
     *
     * @return
     */
    public boolean isPrimaryTable() {
        // ;this.parentTabReference.isEmpty();
        return this.primaryOfIndex;
    }

    // 是否是嵌套式文档的父文档记录
    public boolean isNestChildRow() {
        return this.nestChildRow;
    }

    public List<Alias> getAliasList() {
        return this.aliasList;
    }

    /**
     * 取得其中一个col meta
     *
     * @param colName
     * @return
     */
    public Alias getColMeta(String colName) {
        Alias colMeta = this.aliasColMap.get(colName);
        if (colMeta == null) {
            throw new IllegalStateException("col name:" + colName + " can not find ColMeta in tab meta:" + this.getTableName());
        }
        return colMeta;
    }

    public boolean getColumnsChange(ITable table) {
        Assert.assertEquals(this.tableName, table.getTableName());
        return table.columnChange(focusColumns);
    }

    /**
     * 在单元测试的时候使用，当从TIS中查询出数据之后，在单元测试中测试是否一致
     *
     * @param tab
     * @param document
     */
    public <T, K> void assertEqual(IRowValueGetter tab, SolrDocumentBase<T, K> document) {
        aliasList.forEach((a) -> {
            if (a.copy) {
                if (a.valLazyTransfer != null) {
                    // TODO 涉及到多表join的先不在这里断言测试，单元测试中显示申明断言
                    // Callable<Object> call = a.valLazyTransfer.process(tab);
                    //
                    // try {
                    // Assert.assertEquals(
                    // "[" + this.tableName + ",from  '" + a.getName() + "' to '" + a.getToName() + "']\n" + document.toString(),
                    // String.valueOf(call.call()),
                    // String.valueOf(document.getFieldValue(a.getToName())));
                    // } catch (Exception e) {
                    // throw new RuntimeException(e);
                    // }
                } else if (a.creator != null) {
                    Assert.assertEquals("[" + this.tableName + ",create column '" + a.getToName() + "']\n" + document.toString(), String.valueOf(a.creator.process(tab)), String.valueOf(document.getFieldValue(a.getToName())));
                } else {
                    Object docVal = document.getFieldValue(a.getToName());
                    if (docVal == null) {
                        throw new IllegalStateException("table:" + this.getTableName() + ",transfer:" + a.toString() + " processed val can not be null");
                    }
                    Assert.assertEquals("[" + this.tableName + " from '" + a.getName() + "' to '" + a.getToName() + "'" + "]\n" + document.toString(), String.valueOf(a.getValTransfer().process(tab, tab.getColumn(a.getName()))), String.valueOf(docVal));
                }
            }
        });
    }

    /**
     * @param row
     * @param document
     * @param recover  挡在更新流程中，发现tis中没有对应的记录需要从数据库中将原有记录恢复出来
     */
    public void copy2TisDocument(IRowValueGetter row, TisSolrInputDocument document, boolean recover) {

        aliasList.forEach((a) -> {
            ILazyTransfer lazyTransfer = a.getValLazyTransfer();
            if (lazyTransfer != null) {
                // TODO 先注释掉后期再改
                MediaResultKey.putMediaResult(a.getToName(), true, lazyTransfer.process(row));
                return;
                // throw new NotImplementedException("tablename:" + this.getTableName() + ",transfer,from:" + a.getName() + ",to:" + a.getToName());
            }
            if (a.copy) {
                Object val = a.getVal(row);
                if (val != null) {
                    document.setField(this.tableName, a.getToName(), val);
                }
            }
        });
        AliasList value = null;
        List<Alias> key = null;
        List<RowMap> rows = null;
        // 通过字表查主表记录
        for (Map.Entry<List<Alias>, AliasList> /**
         * 主表
         */
                entry : this.parentTabReference.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if (recover || value.isIgnoreIncrTrigger()) /*说明是维表不参与增量消息监听*/ {
                rows = value.getRowsFromOuterPersistence(this.getTableName(), row, AllThreadLocal.getPkThreadLocal());
                if (rows != null) {
                    for (IRowValueGetter getter : rows) {
                        value.copy2TisDocument(getter, document, recover);
                        break;
                    }
                }
            }
        }
        // 通过主表查字表记录
        for (Map.Entry<List<Alias>, AliasList> /**
         * 子表
         */
                entry : this.getChildTabReference().entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if (recover || value.isIgnoreIncrTrigger()) {
                rows = value.getRowsFromOuterPersistence(this.getTableName(), row, AllThreadLocal.getPkThreadLocal());
                if (rows != null) {
                    for (IRowValueGetter getter : rows) {
                        value.copy2TisDocument(getter, document, recover);
                        break;
                    }
                }
            }
        }
        // Map<List<Alias>, AliasList> ;
        //
        // val outerTableRefs:Map[_, /* FK */
        // AliasList] =this.getChildTabReference();//.asScala
        // var outerTabRow:IRowPack = null
        // for (outerTabs< -outerTableRefs.values) {
        // outerTabRow = pojo.getRowPack(outerTabs.getTableName)
        // if (outerTabRow == null) {
        // for (getter< -outerTabs.getRowsFromOuterPersistence(pk).asScala) {
        // if (outerTabs.isNestChildRow) {
        // addDoc.mergeOrCreateChild(getter, outerTabs)
        // } else {
        // outerTabs.copy2TisDocument(getter, addDoc)
        // }
        // }
        // }
        // }
    }

    public void cleanTisDocument(TisSolrInputDocument document) {
        aliasList.forEach((a) -> {
            try {
                if (a.copy) {
                    document.clearField(a.getToName());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 取得主键值
     *
     * @param tab
     * @return
     */
    public String getPKVal(IRowValueGetter tab) {
        Object val = this.pk.getVal(tab, true);
        if (val == null) {
            throw new IllegalStateException("can not find PK val,pk:" + this.pk.getName() + ",table:" + tab.toString());
        }
        return String.valueOf(val);
    }

    /**
     * 取得主表主键PK值
     *
     * @param tabRow 主表记录
     * @return
     */
    public String getVal(IRowValueGetter tabRow) {
        // return getVal(getFirstParentTab().getKey(), tabRow);
        return null;
    }

    private String getVal(Alias c, IRowValueGetter tabRow) {
        Object val = c.getVal(tabRow, true);
        if (val == null) {
            throw new IllegalStateException("can not find FK val,fk:" + c.getName() + ",table:" + tabRow.toString());
        }
        return String.valueOf(val);
    }

    public Map.Entry<Alias, /**
     * 主表的PK列
     */
            AliasList> getFirstParentTab() {
        if (this.isPrimaryTable()) {
            throw new IllegalStateException("primary table can not get FK");
        }
        throw new UnsupportedOperationException();
        // Optional<Map.Entry<Alias/** 主表的PK列 */, AliasList>> parent = this.parentTabReference.entrySet().stream().findFirst();
        // if (!parent.isPresent()) {
        // return null;
        // }
        // return parent.get();// .get(getFirstForignTableName());
        // return c;
    }

    // /**
    // * 取一个默认的
    // *
    // * @param tab
    // * @return
    // */
    // public String getFKVal(IRowValueGetter tab) {
    // return getFKVal(getFirstForignTableName(), tab);
    // }
    // private String getFirstForignTableName() {
    // if (this.isPrimaryTable()) {
    // throw new IllegalStateException("primary table can not get FK");
    // }
    // return this.fkMap.keySet().stream().findFirst().get();
    // }
    // 取得表名
    public String getTableName() {
        return this.tableName;
    }

    public boolean isTable(String tableName) {
        return StringUtils.equals(this.getTableName(), tableName);
    }

    public interface IGetterRowsFromOuterPersistence {

        /**
         * @param rowTabName
         * @param rowVals
         * @param pk
         * @return
         */
        public List<RowMap> process(String rowTabName, IRowValueGetter rowVals, IPk pk);
    }
}
