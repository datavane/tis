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
package com.qlangtech.tis.sql.parser;

import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta.SqlDataFlowTopology;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.List;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class SqlTaskBaseTestCase extends TestCase {

    @Override
    protected void setUp() throws Exception {
        CenterResource.setNotFetchFromCenterRepository();
    }

    protected TableTupleCreator parseSqlTaskNode(String mediaTableName) throws Exception {
        List<SqlTaskNode> taskNodes = parseTopologySqlTaskNodes("totalpay");
        SqlTaskNode task = null;
        Optional<SqlTaskNode> taskNode = taskNodes.stream().filter((r) -> mediaTableName.equals(r.getExportName().getTabName())).findFirst();
        Assert.assertTrue(mediaTableName + " shall be exist", taskNode.isPresent());
        // Map<ColName, ValueOperator> columnTracer = Maps.newHashMap();
        // Rewriter rewriter = Rewriter.create(columnTracer);
        task = taskNode.get();
        /**
         * *******************************
         * 开始解析
         * *******************************
         */
        return task.parse(true);
    }

    protected List<SqlTaskNode> parseTopologySqlTaskNodes(String topologyName) throws Exception {
        SqlDataFlowTopology topology = SqlTaskNodeMeta.getSqlDataFlowTopology(topologyName);
        // SqlTaskNode.parseTaskNodes(topology);
        List<SqlTaskNode> taskNodes = topology.parseTaskNodes();
        return taskNodes;
    }
}
