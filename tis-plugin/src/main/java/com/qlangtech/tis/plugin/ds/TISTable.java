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
package com.qlangtech.tis.plugin.ds;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import java.util.List;

/**
 * git仓库保存的table信息
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TISTable {

    private String tableLogicName;

    private String tableName;

    private int partitionNum;

    private Integer dbId;

    private Integer tabId;

    private String dbName;

    private int partitionInterval;

    private String selectSql;

    private List<ColumnMetaData> reflectCols = Lists.newArrayList();

    public TISTable() {
    }

    public void addColumnMeta(ColumnMetaData colMeta) {
        this.reflectCols.add(colMeta);
    }

    public List<ColumnMetaData> getReflectCols() {
        return reflectCols;
    }

    public void setReflectCols(List<ColumnMetaData> reflectCols) {
        this.reflectCols = reflectCols;
    }

    public TISTable(String tableLogicName, String tableName, int partitionNum, Integer dbId, int partitionInterval, String selectSql) {
        // this.tabId = tabid;
        this.tableLogicName = tableLogicName;
        this.tableName = tableName;
        this.partitionNum = partitionNum;
        this.dbId = dbId;
        this.partitionInterval = partitionInterval;
        this.selectSql = selectSql;
    }

    public Integer getTabId() {
        return tabId;
    }

    public void setTabId(Integer tabId) {
        this.tabId = tabId;
    }

    public String getDbName() {
        if (StringUtils.isEmpty(this.dbName)) {
            throw new IllegalStateException("param db name can not be null");
        }
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableLogicName() {
        return tableLogicName;
    }

    public void setTableLogicName(String tableLogicName) {
        this.tableLogicName = tableLogicName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getPartitionNum() {
        return partitionNum;
    }

    public void setPartitionNum(int partitionNum) {
        this.partitionNum = partitionNum;
    }

    public Integer getDbId() {
        return this.dbId;
    }

    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }

    public int getPartitionInterval() {
        return partitionInterval;
    }

    public void setPartitionInterval(int partitionInterval) {
        this.partitionInterval = partitionInterval;
    }

    public String getSelectSql() {
        return selectSql;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }
}
