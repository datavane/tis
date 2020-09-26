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
package com.qlangtech.tis.realtime.transfer.ruledriven;

import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.impl.MultiDimensionsRowPack;
import com.qlangtech.tis.wangjubao.jingwei.Alias;

/**
 * 多维表
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年4月27日
 */
public class RuleDrivenMulitRowPack extends MultiDimensionsRowPack {

    private final String pkColName;

    public RuleDrivenMulitRowPack(String pkColName, Alias timeVersionCol, ITable table) {
        super(table, timeVersionCol);
        this.pkColName = pkColName;
    }

    @Override
    protected String getPk(ITable table) {
        return table.getColumn(this.pkColName);
    }
}
