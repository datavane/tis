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
package com.qlangtech.tis.sql.parser.supplyGoods;

import com.qlangtech.tis.sql.parser.SqlTaskBaseTestCase;
import com.qlangtech.tis.sql.parser.SqlTaskNode;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.ColRef;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-01 13:46
 */
public class TestSupplyGoodsParse extends SqlTaskBaseTestCase {

    public static final String topologyName = "supply_goods";

    public void testTopologyParse() throws Exception {
        // this.parseSqlTaskNode()
        List<SqlTaskNode> processNodes = this.parseTopologySqlTaskNodes(topologyName);
        assertTrue(processNodes.size() > 0);
        assertEquals(4, processNodes.size());
        SqlTaskNodeMeta.SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology(topologyName);
        TableTupleCreator tableTupleCreator = topology.parseFinalSqlTaskNode();
        ColRef.ListMap cols = tableTupleCreator.getColsRefs().getColRefMap();
        assertTrue(cols.size() > 0);
        assertEquals("parse from suppyGoods cols size", 58, cols.size());
        assertNotNull("tableTupleCreator", tableTupleCreator);
    }
}
