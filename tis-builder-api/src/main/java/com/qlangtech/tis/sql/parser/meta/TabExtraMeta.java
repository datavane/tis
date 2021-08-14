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
package com.qlangtech.tis.sql.parser.meta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TabExtraMeta {

    // ermeta.getBoolean("primaryIndexTab");
    // ermeta.getBoolean("monitorTrigger");
    // 是否是索引主表
    private boolean primaryIndexTab;

    private List<PrimaryLinkKey> primaryIndexColumnNames;

    // 主表的分区键，一般用于在构建宽表过程给数据分区用
    private String sharedKey;

    // 是否监听增量消息
    private boolean monitorTrigger;

    // 表中对应的增量时间戳对应的字段
    private String timeVerColName;

    public String getTimeVerColName() {
        return this.timeVerColName;
    }

    public void setTimeVerColName(String timeVerColName) {
        this.timeVerColName = timeVerColName;
    }

    private List<ColumnTransfer> colTransfers = new ArrayList<>();

    public List<ColumnTransfer> getColTransfers() {
        return colTransfers;
    }

    /**
     * colKey
     */
    private Map<String, ColumnTransfer> colTransfersMap;

    public void addColumnTransfer(ColumnTransfer colTransfer) {
        this.colTransfers.add(colTransfer);
    }

    public List<PrimaryLinkKey> getPrimaryIndexColumnNames() {
        return this.primaryIndexColumnNames;
    }

    public void setPrimaryIndexColumnNames(List<PrimaryLinkKey> primaryIndexColumnName) {
        if (primaryIndexColumnName == null || primaryIndexColumnName.isEmpty()) {
            throw new IllegalArgumentException("param primaryIndexColumnName can not be empty");
        }
        this.primaryIndexColumnNames = primaryIndexColumnName;
    }

    public void setColTransfers(List<ColumnTransfer> colTransfers) {
        this.colTransfers = colTransfers;
    }

    public boolean isPrimaryIndexTab() {
        return this.primaryIndexTab;
    }

    public void setPrimaryIndexTab(boolean primaryIndexTab) {
        this.primaryIndexTab = primaryIndexTab;
    }

    public boolean isMonitorTrigger() {
        return monitorTrigger;
    }

    public void setMonitorTrigger(boolean monitorTrigger) {
        this.monitorTrigger = monitorTrigger;
    }

    public String getSharedKey() {
        return this.sharedKey;
    }

    public void setSharedKey(String sharedKey) {
        this.sharedKey = sharedKey;
    }
}
