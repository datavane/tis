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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.realtime.transfer.BasicPojoConsumer.ShareId;
import com.qlangtech.tis.realtime.transfer.IAllRowsVisitor;
import com.qlangtech.tis.realtime.transfer.IRowPack;
import com.qlangtech.tis.realtime.transfer.IRowVisitor;
import com.qlangtech.tis.realtime.transfer.ITable;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class MultiDimensionsRowPack implements IRowPack {

    protected final Map<String, ITable> /* pk value */
    rows = new HashMap<>();

    private final String tabname;

    int matched = 1;

    public MultiDimensionsRowPack(ITable table) {
        super();
        this.tabname = table.getTableName();
        rows.put(getPk(table), table);
    }

    @Override
    public Long getTimeWindow() {
        return null;
    }

    @Override
    public String getTableName() {
        return this.tabname;
    }

    @Override
    public boolean isNew() {
        // 多维表默认为false，肯定是更新状态，多維表的存在必須主表存在
        return false;
    }

    @Override
    public int getRowSize() {
        return this.matched;
    }

    @Override
    public long getVersion() {
        long maxVersion = 0;
        long tmpVersion = 0;
        for (ITable t : rows.values()) {
            if ((tmpVersion = getVersion(t)) > maxVersion) {
                maxVersion = tmpVersion;
            }
        }
        return maxVersion;
    }

    protected long getVersion(ITable t) {
        return t.getVersion();
    }

    public String getShareId(String shareIdName) {
        try {
            ShareId shareid = new ShareId();
            this.vistRow((r) -> {
                shareid.value = r.getColumn(shareIdName);
                return shareid.value != null;
            });
            return shareid.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String getPk(ITable table);

    // protected abstract boolean isDirty(ITable old, ITable newt);
    @Override
    public final boolean isNotDirtyAndPut(ITable newrow) {
        if (!StringUtils.equals(this.tabname, newrow.getTableName())) {
            throw new IllegalArgumentException("table:" + this.tabname + " can not put other table:" + newrow.getTableName());
        }
        synchronized (rows) {
            String pk = getPk(newrow);
            ITable oldrow = rows.get(pk);
            matched++;
            if (oldrow != null && isDirty(oldrow, newrow)) {
                return true;
            }
            rows.put(pk, newrow);
            return false;
        }
    }

    @Override
    public void vistRow(IRowVisitor visitor) throws Exception {
        synchronized (rows) {
            for (ITable t : rows.values()) {
                if (visitor.visit(t)) {
                    return;
                }
            }
        }
    }

    @Override
    public void vistAllRow(IAllRowsVisitor visitor) throws Exception {
        synchronized (rows) {
            for (ITable t : rows.values()) {
                visitor.visit(t);
            }
        }
    }
}
