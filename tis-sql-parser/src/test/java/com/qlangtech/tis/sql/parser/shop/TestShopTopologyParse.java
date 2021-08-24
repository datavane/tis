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
package com.qlangtech.tis.sql.parser.shop;

import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.sql.parser.SqlTaskBaseTestCase;
import com.qlangtech.tis.sql.parser.SqlTaskNode;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;

import java.io.File;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-07-17 10:23
 */
public class TestShopTopologyParse extends SqlTaskBaseTestCase {

    public static final String topologyName = "shop";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.clearProperty(Config.KEY_DATA_DIR);
        Config.setDataDir(Config.DEFAULT_DATA_DIR);
        SqlTaskNode.parent = new File(Config.getMetaCfgDir(), SqlTaskNode.NAME_DATAFLOW_DIR);
    }

    public void testTopologyParse() throws Exception {
        List<SqlTaskNode> processNodes = this.parseTopologySqlTaskNodes(topologyName);
        assertTrue(processNodes.size() > 0);
        assertEquals(3, processNodes.size());
        SqlTaskNodeMeta.SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology(topologyName);
        List<ColumnMetaData> finalTaskNodeCols = topology.getFinalTaskNodeCols();
        // 一共有32列
        assertEquals(32, finalTaskNodeCols.size());
    }
}
