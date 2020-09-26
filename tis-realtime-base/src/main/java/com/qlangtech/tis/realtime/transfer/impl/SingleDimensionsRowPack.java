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

import com.qlangtech.tis.realtime.transfer.BasicPojoConsumer.ShareId;
import com.qlangtech.tis.realtime.transfer.DTO;
import com.qlangtech.tis.realtime.transfer.IAllRowsVisitor;
import com.qlangtech.tis.realtime.transfer.IRowVisitor;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.wangjubao.jingwei.Alias;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年12月23日
 */
public class SingleDimensionsRowPack extends AbstractRowPack {

    private static final String FIELD_LAST_VERSION = "last_ver";

    private ITable row;

    private int matched = 1;

    public SingleDimensionsRowPack(ITable row, Alias timeVersionCol) {
        super(row.getTableName(), timeVersionCol);
        Assert.assertNotNull(row);
        this.row = row;
    }

    public static boolean isRecordDirty(ITable old, ITable newt, Alias timeVersionCol) {
        int oldVersion = 0;
        int newVersion = 0;
        if ((oldVersion = (old.getInt(FIELD_LAST_VERSION, false))) > (newVersion = (newt.getInt(FIELD_LAST_VERSION, false)))) {
            return true;
        }
        long oldModifyTime = 0;
        long newModifyTime = 0;
        if (// 
        (oldVersion == newVersion) && (// 
        (oldModifyTime = timeVersionCol.getLong(old)) >= /* sub */
        (newModifyTime = timeVersionCol.getLong(newt)))) {
            return true;
        }
        return false;
    }

    @Override
    public long getVersion() {
        return this.getVersion(this.row);
    }

    @Override
    public Long getTimeWindow() {
        return null;
    }

    @Override
    public boolean isNew() {
        synchronized (this) {
            return row.getEventType() == DTO.EventType.ADD;
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
                if (this.row != null && (this.row.getEventType() == DTO.EventType.ADD)) {
                    ((DefaultTable) table).setEventType(DTO.EventType.ADD);
                }
                this.row = table;
                return false;
            }
        }
    }

    // @Override
    // public boolean isDirty(ITable old, ITable newt) {
    // long oldVer = 0;
    // long newVer = 0;
    // if ((oldVer = getRowVersion(old)) >= (newVer = getRowVersion(newt))) {
    // this.processDirty(oldVer, old, newVer, newt);
    // return true;
    // } else {
    // return false;
    // }
    // 
    // }
    protected void processDirty(long oldVer, ITable old, long newVer, ITable newt) {
    }

    // protected long getRowVersion(ITable row) {
    // return row.getVersion();
    // }
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
