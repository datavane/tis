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
import com.qlangtech.tis.realtime.transfer.impl.SingleDimensionsRowPack;
import com.qlangtech.tis.wangjubao.jingwei.Alias;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年12月24日
 */
public class RuleDrivenSingleDimensionsRowPack extends SingleDimensionsRowPack {

    private final BasicRuleDrivenWrapper pojo;

    public RuleDrivenSingleDimensionsRowPack(ITable row, Alias timeVersionCol, BasicRuleDrivenWrapper pojo) {
        super(row, timeVersionCol);
        this.pojo = pojo;
    // this.timeVersionCol = timeVersionCol;
    }
    // private static final String FIELD_MODIFY_TIME = "modify_time";
    // private static final String FIELD_OP_TIME = "op_time";
}
