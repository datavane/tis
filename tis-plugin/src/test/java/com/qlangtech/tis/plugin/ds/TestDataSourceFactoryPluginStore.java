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
package com.qlangtech.tis.plugin.ds;

import com.qlangtech.tis.BasicTestCase;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.offline.DbScope;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-25 11:09
 */
public class TestDataSourceFactoryPluginStore extends BasicTestCase {

    private static final String DB_NAME = "order1";
    private static final String DB_EMPLOYEE_NAME = "employees";
    private static final String TABLE_NAME = "totalpayinfo";

    public void testLoadTableMeta() {
        DataSourceFactoryPluginStore dbPluginStore = TIS.getDataBasePluginStore(new PostedDSProp(DB_NAME));
        //dbPluginStore.getPlugin()
        assertNotNull("db:" + DB_NAME + " relevant plugin config", dbPluginStore.getPlugin());
        TISTable tab = dbPluginStore.loadTableMeta(TABLE_NAME);
        assertNotNull(tab);
        assertEquals(5, tab.getReflectCols().size());
    }


    public void testLoadFacadeTableMeta() throws Exception {
        DataSourceFactoryPluginStore employeesPluginStore
                = TIS.getDataBasePluginStore(new PostedDSProp(DB_EMPLOYEE_NAME, DbScope.FACADE));
        assertNotNull(employeesPluginStore);

        Class<?> aClass = Class.forName("com.qlangtech.tis.plugin.ds.DBConfig");
        System.out.println(aClass);

        DataSourceFactory plugin = employeesPluginStore.getPlugin();
        assertNotNull(plugin);
        // plugin.createFacadeDataSource();

    }
}
