/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.impl;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.ds.TISTable;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.sql.parser.er.IPrimaryTabFinder;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.workflow.pojo.DatasourceDb;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-31 11:20
 */
public class SingleTableAppSource implements IAppSource {
    private final DatasourceDb db;
    private final Integer tabId;
    private final String tabName;

    public SingleTableAppSource(DatasourceDb db, Integer tabId, String tabName) {
        this.db = db;
        this.tabId = tabId;
        this.tabName = tabName;
    }

    @Override
    public List<ColumnMetaData> reflectCols() {

        DataSourceFactoryPluginStore dataBasePluginStore = TIS.getDataBasePluginStore(new PostedDSProp(db.getName()));
        TISTable table = dataBasePluginStore.loadTableMeta(tabName);
//    table.getReflectCols().stream().map((c)->{
//      ColName cname = new ColName(c.getKey());
//
//    });
        return table.getReflectCols();
    }

    @Override
    public IPrimaryTabFinder getPrimaryTabFinder() {
        return null;
    }

    @Override
    public EntityName getTargetEntity() {
        return EntityName.parse(db.getName() + "." + this.tabName);
    }

    @Override
    public boolean triggerFullIndexSwapeValidate(IMessageHandler msgHandler, Context ctx) {
        return true;
    }
}
