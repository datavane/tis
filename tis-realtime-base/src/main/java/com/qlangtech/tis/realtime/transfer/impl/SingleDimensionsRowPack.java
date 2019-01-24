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

import com.qlangtech.tis.realtime.transfer.IAllRowsVisitor;
import com.qlangtech.tis.realtime.transfer.IRowPack;
import com.qlangtech.tis.realtime.transfer.IRowVisitor;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.BasicPojoConsumer.ShareId;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable.EventType;
import com.qlangtech.tis.common.utils.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SingleDimensionsRowPack implements IRowPack {

    private ITable row;

    private final String tabname;

    private int matched = 1;

    public SingleDimensionsRowPack(ITable row) {
        super();
        Assert.assertNotNull(row);
        this.row = row;
        this.tabname = row.getTableName();
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
        synchronized (this) {
            return row.getEventType() == EventType.ADD;
        }
    }

    @Override
    public long getVersion() {
        synchronized (this) {
            return row.getVersion();
        }
    }

    @Override
    public int getRowSize() {
        return this.matched;
    }

    @Override
    public final boolean isNotDirtyAndPut(ITable table) {
        synchronized (this) {
            matched++;
            if (isDirty(row, table)) {
                return true;
            } else {
                if (this.row != null && (this.row.getEventType() == EventType.ADD)) {
                    ((DefaultTable) table).setEventType(EventType.ADD);
                }
                this.row = table;
                return false;
            }
        }
    }

    @Override
    public boolean isDirty(ITable old, ITable newt) {
        long oldVer = 0;
        long newVer = 0;
        if ((oldVer = getRowVersion(old)) >= (newVer = getRowVersion(newt))) {
            this.processDirty(oldVer, old, newVer, newt);
            return true;
        } else {
            return false;
        }
    }

    protected void processDirty(long oldVer, ITable old, long newVer, ITable newt) {
    }

    protected long getRowVersion(ITable row) {
        return row.getVersion();
    }

    public String getShareId(String shareIdName) {
        try {
            ShareId sharedId = new ShareId();
            this.vistRow((g) -> {
                sharedId.value = g.getColumn(shareIdName);
                return false;
            });
            return sharedId.getValue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void vistRow(IRowVisitor visitor) throws Exception {
        synchronized (this) {
            visitor.visit(row);
        }
    }

    @Override
    public void vistAllRow(IAllRowsVisitor visitor) throws Exception {
        synchronized (this) {
            visitor.visit(row);
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
