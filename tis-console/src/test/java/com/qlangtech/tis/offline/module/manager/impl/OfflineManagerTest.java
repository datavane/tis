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
package com.qlangtech.tis.offline.module.manager.impl;

import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import junit.framework.TestCase;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月12日
 */
public class OfflineManagerTest extends TestCase {

    // @Override
    // protected void runTest() throws Throwable {
    //
    // super.runTest();
    // }
    private OfflineManager offline;

    @Override
    protected void setUp() throws Exception {
        this.offline = new OfflineManager();
    // super.setUp();
    }

  //  public void testGetTables() throws Exception {
//        List<String> tabs = this.offline.getTables("order");
//        assertNotNull(tabs.size());
//        for (String tab : tabs) {
//            System.out.println(tab);
//        }
   // }

    public void testGetColumn() throws Exception {
        List<ColumnMetaData> list = offline.getTableMetadata("order", "totalpayinfo");
        assertTrue(list.size() > 0);
        for (ColumnMetaData c : list) {
            System.out.println(c.getKey());
        }
    // List<String> tabs = this.offline.getTables("order");
    // Assert.assertNotNull(tabs.size());
    // for (String tab : tabs) {
    // System.out.println(tab);
    // }
    }
}
