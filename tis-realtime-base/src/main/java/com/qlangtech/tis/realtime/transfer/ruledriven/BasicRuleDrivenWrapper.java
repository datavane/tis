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

import com.qlangtech.tis.realtime.transfer.BasicRMListener;
import com.qlangtech.tis.realtime.transfer.IRowPack;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.impl.DefaultPojo;
import com.qlangtech.tis.wangjubao.jingwei.AliasList;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年12月21日
 */
public class BasicRuleDrivenWrapper extends DefaultPojo {

    public BasicRuleDrivenWrapper(BasicRMListener onsListener) {
        super(onsListener);
    }

    @Override
    public boolean isAdd() {
        boolean isAdd = super.isAdd();
        return isAdd;
    }

    // @Override
    // public String getPrimaryTableName() {
    // AliasList tabMeta = null;
    // 
    // for (String tabKey : this.onsListener.getFocusedTabs()) {
    // if (this.isTabRowExist(tabKey)) {
    // tabMeta = this.onsListener.getTabColumnMeta(tabKey);
    // if (tabMeta.isPrimaryTable()) {
    // return tabKey;
    // }
    // }
    // }
    // 
    // for (String tabKey : this.onsListener.getFocusedTabs()) {
    // if (this.isTabRowExist(tabKey)) {
    // tabMeta = this.onsListener.getTabColumnMeta(tabKey);
    // if (tabMeta.isPrimaryTable()) {
    // return tabKey;
    // } else {
    // return tabMeta.getFirstParentTab().getValue().getTableName();
    // }
    // }
    // }
    // 
    // throw new IllegalStateException("can not find primary table Name");
    // }
    @Override
    protected IRowPack createRowPack(ITable table) {
        final AliasList colsMeta = this.onsListener.getTabColumnMeta(table.getTableName());
        if (colsMeta != null) {
            // FIXME 这里可以根据ERRule来判断生成 是multi还是single的类型
            if (colsMeta.isPrimaryTable()) {
                return new RuleDrivenSingleDimensionsRowPack(table, colsMeta.getTimeVersionCol(), this);
            } else {
                return new RuleDrivenMulitRowPack(colsMeta.getPk().getName(), colsMeta.getTimeVersionCol(), table);
            }
        }
        throw new IllegalStateException(table.getTableName() + " is illegal");
    }

    public static void main(String[] args) {
    }
}
