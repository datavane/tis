/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.realtime.transfer.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.realtime.transfer.BasicONSListener;
import com.qlangtech.tis.realtime.transfer.IPk;
import com.qlangtech.tis.realtime.transfer.IPojo;
import com.qlangtech.tis.realtime.transfer.IRowPack;
import com.qlangtech.tis.realtime.transfer.ITable;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultPojo implements IPojo {

    private Map<String, IRowPack> /* tablename */
    tablesRowsPack = new HashMap<String, IRowPack>();

    private String collection;

    // private boolean add = false;
    boolean hasClosed = false;

    // private final RowVersionCreator[] rowVersionCreator;
    private final BasicONSListener onsListener;

    /**
     */
    public DefaultPojo(BasicONSListener onsListener) {
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

    /**
     * 这条记录是否是新增？<br>
     * 在consume的时候 判断是否要到solr中把原先那条记录load出来作列更新
     */
    @Override
    public boolean isAdd() {
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
        String ptabName = onsListener.getPrimaryTableName(this);
        if (StringUtils.isBlank(ptabName)) {
            throw new IllegalStateException("onsListener primaryTableName have not been set");
        }
        // 如果主表不为空，且执行的操作是更新的话就算作新增
        IRowPack table = tablesRowsPack.get(ptabName);
        if (table != null && table.isNew()) {
            // table.getEventType()) {
            return true;
        }
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
    public IRowPack getTable(String tableName) {
        return this.tablesRowsPack.get(tableName);
    // return null;
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
        return new SingleDimensionsRowPack(table);
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
