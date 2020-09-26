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
package com.qlangtech.tis.sql.parser.visitor;

import java.util.List;
import java.util.Optional;
import com.qlangtech.tis.sql.parser.SqlTaskNode;
import com.qlangtech.tis.sql.parser.SqlTaskNodeMeta;
import com.facebook.presto.sql.tree.Query;
import com.qlangtech.tis.common.utils.Assert;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestTableDependencyVisitor extends TestCase {

    public void testDependencies() throws Exception {
        List<SqlTaskNode> taskNodes = SqlTaskNodeMeta.getSqlDataFlowTopology("totalpay").parseTaskNodes();
        String order_customers = "order_customers";
        Optional<SqlTaskNode> taskNode = taskNodes.stream().filter((r) -> order_customers.equals(r.getExportName())).findFirst();
        Assert.assertTrue(order_customers + " shall be exist", taskNode.isPresent());
        TableDependencyVisitor dependenciesVisitor = TableDependencyVisitor.create();
        Query query = SqlTaskNode.parseQuery(taskNode.get().getContent());
        dependenciesVisitor.process(query, null);
        // for (String dependency : dependenciesVisitor.getTabDependencies()) {
        // System.out.println(dependency);
        // }
        // Assert.assertTrue(dependenciesVisitor.getTabDependencies().size() > 0);
        Assert.assertEquals(1, dependenciesVisitor.getTabDependencies().size());
        Assert.assertTrue(dependenciesVisitor.getTabDependencies().contains("instance"));
    }
}
