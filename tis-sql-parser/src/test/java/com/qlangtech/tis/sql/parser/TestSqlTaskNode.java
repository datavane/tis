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
package com.qlangtech.tis.sql.parser;

import com.google.common.collect.Sets;
import com.qlangtech.tis.offline.module.pojo.ColumnMetaData;
import junit.framework.TestCase;
import java.util.List;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestSqlTaskNode extends TestCase {

    public void testDumpSqlReflect() {
        final String sql = "select A,b,c as cc,d,e as ee from A";
        Set<String> cols = Sets.newHashSet("a", "b", "c", "d", "e");
        List<ColumnMetaData> rows = SqlTaskNode.reflectTableCols(sql);
        for (ColumnMetaData r : rows) {
            assertTrue(r.getKey() + " must contain in set", cols.contains(r.getKey()));
        }
        assertEquals(5, rows.size());
    }

    public void testDumpNodes() {
    // Map<String, List<TableTupleCreator>> dumpNodes = SqlTaskNode.dumpNodes;
    // 
    // List<TableTupleCreator> tables = dumpNodes.get("totalpayinfo");
    // Assert.assertEquals(1, tables.size());
    // 
    // Assert.assertEquals("order", tables.get(0).getEntityName().getDbname());
    // 
    // tables = dumpNodes.get("payinfo");
    // Assert.assertEquals(1, tables.size());
    // 
    // Assert.assertEquals("order", tables.get(0).getEntityName().getDbname());
    }
}
