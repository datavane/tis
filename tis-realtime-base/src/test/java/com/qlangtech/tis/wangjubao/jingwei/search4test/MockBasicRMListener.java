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
package com.qlangtech.tis.wangjubao.jingwei.search4test;

import java.util.Map;
import java.util.Set;
import com.qlangtech.tis.realtime.transfer.BasicRMListener;
import com.qlangtech.tis.realtime.transfer.BasicPojoConsumer;
import com.qlangtech.tis.realtime.transfer.DTO;
import com.qlangtech.tis.realtime.transfer.ITable;
import com.qlangtech.tis.realtime.transfer.impl.DefaultPk;
import com.qlangtech.tis.wangjubao.jingwei.AliasList;
import com.qlangtech.tis.wangjubao.jingwei.AliasList.BuilderList;
import com.qlangtech.tis.wangjubao.jingwei.Table;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MockBasicRMListener extends BasicRMListener {

    // @Override
    // public String getPrimaryTableName(IPojo pojo) {
    // 
    // return null;
    // }
    @Override
    protected Map<String, AliasList> createPrimaryTables() {
        return null;
    }

    @Override
    protected BasicPojoConsumer createPojoConsumer() {
        return null;
    }

    @Override
    protected void processColsMeta(BuilderList builder) {
    }

    public Table getTableProcessor(String name) {
        return null;
    }

    @Override
    protected ITable getTable(DTO dto) {
        return null;
    }

    @Override
    protected DefaultPk getPk(ITable table) throws InterruptedException {
        return null;
    }

    @Override
    public Set<String> getTableFocuse() {
        return null;
    }
}
