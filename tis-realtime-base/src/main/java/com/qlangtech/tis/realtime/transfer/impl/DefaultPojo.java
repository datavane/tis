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
package com.qlangtech.tis.realtime.transfer.impl;

import com.qlangtech.tis.realtime.transfer.*;
import com.qlangtech.tis.wangjubao.jingwei.AliasList;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月5日 下午5:26:18
 */
public class DefaultPojo implements IPojo {

    private Map<String, IRowPack> /* tablename */
    tablesRowsPack = new HashMap<String, IRowPack>();

    private String collection;

    // private boolean add = false;
    boolean hasClosed = false;

    // private final RowVersionCreator[] rowVersionCreator;
    protected final BasicRMListener onsListener;

    /**
     */
    public DefaultPojo(BasicRMListener onsListener) {
        // if (rowVersionCreator == null) {
        // throw new IllegalStateException("versionColumnName can not be null");
        // }
        this.onsListener = onsListener;
        // this.rowVersionCreator = rowVersionCreator;
        this.occurTime = System.currentTimeMillis();
    }

    @Override
    public boolean careRowpackTimeWindow() {
        return false;
    }

    // @Override
    // public String getPrimaryTableName() {
    // // return onsListener.getPrimaryTableName(this);
    // throw new NotImplementedException("mehtod shall be overwrite");
    // }
    /**
     * 这条记录是否是新增？<br>
     * 在consume的时候 判断是否要到solr中把原先那条记录load出来作列更新
     */
    @Override
    public boolean isAdd() {
        AliasList tabMeta = null;
        IRowPack row = null;
        final Set<String> focuseTabs = onsListener.getTableFocuse();
        if (focuseTabs.size() == 1) {
            for (String tab : focuseTabs) {
                IRowPack table = tablesRowsPack.get(tab);
                // EventType.ADD ==
                return (table != null && table.isNew());
            // table.getEventType());
            // {
            // return true;
            // }
            }
        }
        for (Map.Entry<String, IRowPack> entry : tablesRowsPack.entrySet()) {
            tabMeta = this.onsListener.getTabColumnMeta(entry.getKey());
            if (tabMeta.isPrimaryTable() && entry.getValue().isNew()) {
                return true;
            }
        }
        // }
        return false;
    }

    public static void main(String[] arg) {
        System.out.println(System.currentTimeMillis());
        // 1444707018925
        System.out.println(new Date(1406373076l * 1000));
    }

    /**
     * 取得版本号
     */
    @Override
    public long getVersion() {
        long latestversion = 0;
        long tmp = 0;
        for (IRowPack table : tablesRowsPack.values()) {
            tmp = table.getVersion();
            // }
            if (tmp > latestversion) {
                latestversion = tmp;
            }
        }
        // rowVersionCreator.getVersion(latestversion);
        return latestversion;
    }

    // public void setAdd(boolean add) {
    // this.add = add;
    // }
    @Override
    public String getCollection() {
        return this.collection;
    }

    public DefaultPojo setCollection(String collection) {
        this.collection = collection;
        return this;
    }

    @Override
    public IRowPack getRowPack(String tableName) {
        return this.tablesRowsPack.get(tableName);
    }

    @Override
    public boolean isTabRowExist(String... tableName) {
        for (String tab : tableName) {
            if (this.tablesRowsPack.get(tab) != null) {
                return true;
            }
        }
        return false;
    }

    private final long occurTime;

    private IPk primaryKey;

    public IPk getPrimaryKey() {
        return primaryKey;
    }

    public DefaultPojo setPrimaryKey(IPk primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    @Override
    public IPk getPK() {
        Objects.requireNonNull(this.primaryKey);
        return this.primaryKey;
    }

    // @Override
    // public ITable getTable(String tableName) {
    // 
    // return this.tables.get(tableName);
    // }
    public Set<Entry<String, IRowPack>> getRowsPack() {
        return this.tablesRowsPack.entrySet();
    // return null;
    }

    /**
     * 返回值说明说否需要将pojo重新压入Queue
     */
    @Override
    public final boolean setTable(String tableName, ITable table) {
        if (this.hasClosed) {
            return false;
        }
        // synchronized (this) {
        IRowPack rowPack = this.tablesRowsPack.get(tableName);
        if (rowPack != null && rowPack.isNotDirtyAndPut(table)) {
            return true;
        }
        if (rowPack == null) {
            this.tablesRowsPack.put(tableName, createRowPack(table));
        }
        // }
        return true;
    }

    protected IRowPack createRowPack(ITable table) {
        return new SingleDimensionsRowPack(table, this.onsListener.getTabColumnMeta(table.getTableName()).getTimeVersionCol());
    }

    // /**
    // * 是否是脏数据
    // *
    // * @param old
    // * 肯定不为空
    // * @param table
    // *
    // * @return
    // */
    // protected boolean isDirty(String tableName, ITable old, ITable table) {
    // return old.getVersion() >= table.getVersion();
    // }
    @Override
    public long occurTime() {
        return this.occurTime;
    }

    @Override
    public void close() throws IOException {
        this.hasClosed = true;
    }

    public boolean isClosed() {
        return this.hasClosed;
    }

    @Override
    public String toString() {
        try {
            final StringBuffer desc = new StringBuffer();
            for (Map.Entry<String, IRowPack> /* tablename */
            entry : tablesRowsPack.entrySet()) {
                desc.append("tablename:" + entry.getKey()).append(" ");
                entry.getValue().vistRow((r) -> {
                    desc.append("\t");
                    r.desc(desc);
                    desc.append("\n");
                    return false;
                });
            // desc.append("desc:").append(entry.getValue().toString());
            }
            return desc.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
