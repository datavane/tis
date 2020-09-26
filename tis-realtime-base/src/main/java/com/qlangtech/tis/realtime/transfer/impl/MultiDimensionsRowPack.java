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
import com.qlangtech.tis.realtime.transfer.IAllRowsVisitor;
import com.qlangtech.tis.realtime.transfer.IRowPack;
import com.qlangtech.tis.realtime.transfer.IRowVisitor;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.ruledriven.RuleDrivenSingleDimensionsRowPack;
import com.qlangtech.tis.wangjubao.jingwei.Alias;
import org.apache.commons.lang.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年12月23日
 */
public abstract class MultiDimensionsRowPack extends AbstractRowPack implements IRowPack {

    protected final Map<String, ITable> /* pk value */
    rows = new HashMap<>();

    // private final String tabname;
    int matched = 1;

    public MultiDimensionsRowPack(ITable table, Alias timeVersionCol) {
        super(table.getTableName(), timeVersionCol);
        rows.put(getPk(table), table);
    }

    @Override
    public Long getTimeWindow() {
        return null;
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
        if (!StringUtils.equals(this.getTableName(), newrow.getTableName())) {
            throw new IllegalArgumentException("table:" + this.getTableName() + " can not put other table:" + newrow.getTableName());
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
