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

import com.qlangtech.tis.realtime.transfer.IRowPack;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.ruledriven.RuleDrivenSingleDimensionsRowPack;
import com.qlangtech.tis.wangjubao.jingwei.Alias;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class AbstractRowPack implements IRowPack {

    private final String tabname;

    private final Alias timeVersionCol;

    public AbstractRowPack(String tabname, Alias timeVersionCol) {
        this.tabname = tabname;
        this.timeVersionCol = timeVersionCol;
    }

    public final String getTableName() {
        return this.tabname;
    }

    public final boolean isDirty(ITable old, ITable newt) {
        return RuleDrivenSingleDimensionsRowPack.isRecordDirty(old, newt, timeVersionCol);
    }

    protected long getVersion(ITable t) {
        try {
            return timeVersionCol.getLong(t);
        } catch (Exception e) {
            throw new RuntimeException("table:" + this.tabname + "," + timeVersionCol + "\ntable:" + t, e);
        }
    }
}
