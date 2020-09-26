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

import com.qlangtech.tis.sql.parser.meta.NodeType;
import com.qlangtech.tis.sql.parser.tuple.creator.IDataTupleCreator;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.ColRef;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.FunctionDataTupleCreator;
import com.qlangtech.tis.sql.parser.tuple.creator.impl.TableTupleCreator;
import junit.framework.Assert;
import java.util.Map;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestOrderCustomersSqlParse extends SqlTaskBaseTestCase {

    public void testParse() throws Exception {
        final String order_customers = "order_customers";
        TableTupleCreator task = parseSqlTaskNode(order_customers);
        ColRef colRef = task.getColsRefs();
        Assert.assertNotNull(colRef);
        ColRef.ListMap /* colName */
        colRefMap = colRef.getColRefMap();
        // {name:has_fetch=FunctionDataTuple,
        // name:customer_ids=ref:a1,entity:innertab_a1,
        // name:order_id=ref:a1,entity:innertab_a1}
        IDataTupleCreator tupleCreator = null;
        Assert.assertEquals(3, colRefMap.size());
        ColName hasFetch = new ColName("has_fetch");
        tupleCreator = colRefMap.get(hasFetch);
        Assert.assertTrue(tupleCreator instanceof FunctionDataTupleCreator);
        FunctionDataTupleCreator funcDataTuple = (FunctionDataTupleCreator) tupleCreator;
        Assert.assertEquals(1, funcDataTuple.getParams().size());
        Optional<ColName> funcParam = funcDataTuple.getParams().keySet().stream().findFirst();
        Assert.assertTrue(funcParam.isPresent());
        ColName hasFetchParam = funcParam.get();
        Assert.assertEquals("has_fetch", hasFetchParam.getAliasName());
        Assert.assertEquals("has_fetch", hasFetchParam.getName());
        Optional<IDataTupleCreator> hasFetchRef = funcDataTuple.getParams().values().stream().findFirst();
        Assert.assertTrue(hasFetchRef.isPresent());
        IDataTupleCreator asTableTuple = hasFetchRef.get();
        Assert.assertTrue(asTableTuple instanceof TableTupleCreator);
        TableTupleCreator a2Tuple = (TableTupleCreator) asTableTuple;
        // EntitiyRef entityRef = a2Tuple.getEntityRef();
        // Assert.assertNotNull(entityRef);
        Assert.assertEquals("tis.innertab_a2", a2Tuple.getEntityName().toString());
        // task = entityRef.getTaskNode();
        // Assert.assertNotNull(task);
        ColName hasFetchOfinnertab_a2 = new ColName("has_fetch");
        tupleCreator = task.getColsRefs().getColRefMap().get(hasFetchOfinnertab_a2);
        Assert.assertTrue(tupleCreator instanceof FunctionDataTupleCreator);
        funcDataTuple = (FunctionDataTupleCreator) tupleCreator;
        Assert.assertEquals(1, funcDataTuple.getParams().size());
        ColName customerIds = new ColName("customer_ids");
        tupleCreator = colRefMap.get(customerIds);
        assertA1Tuple(tupleCreator);
        ColName orderid = new ColName("order_id");
        tupleCreator = colRefMap.get(orderid);
        assertA1Tuple(tupleCreator);
        Assert.assertEquals(2, colRef.getBaseRefKeys().size());
        Assert.assertTrue(colRef.getTupleCreator("a1") != null);
        Assert.assertTrue(colRef.getTupleCreator("a2") != null);
        Assert.assertTrue(colRef.getTupleCreator("a1") instanceof TableTupleCreator);
        Assert.assertTrue(colRef.getTupleCreator("a2") instanceof TableTupleCreator);
    }

    private void assertA1Tuple(IDataTupleCreator tupleCreator) {
        Assert.assertNotNull(tupleCreator);
        Assert.assertTrue(tupleCreator instanceof TableTupleCreator);
        TableTupleCreator a1Tuple = (TableTupleCreator) tupleCreator;
        Assert.assertEquals("a1", a1Tuple.getMediaTabRef());
        // EntitiyRef entityRef = a1Tuple.getEntityRef();
        // Assert.assertNotNull(entityRef);
        Assert.assertEquals("tis.innertab_a1", a1Tuple.getEntityName().toString());
        // Assert.assertNotNull(entityRef.getTaskNode());
        // SqlTaskNode a1Task = entityRef.getTaskNode();
        // Assert.assertNotNull(a1Task);
        Assert.assertEquals(1, a1Tuple.getColsRefs().getBaseRefSize());
        Optional<Map.Entry<String, IDataTupleCreator>> /* ref */
        e = a1Tuple.getColsRefs().getBaseRefEntities().stream().findFirst();
        Assert.assertTrue(e.isPresent());
        Map.Entry<String, IDataTupleCreator> /* ref */
        i_ref = e.get();
        Assert.assertEquals("i", i_ref.getKey());
        Assert.assertNotNull(i_ref.getValue());
        Assert.assertTrue(i_ref.getValue() instanceof TableTupleCreator);
        TableTupleCreator iTuple = (TableTupleCreator) i_ref.getValue();
        Assert.assertEquals("i", iTuple.getMediaTabRef());
        Assert.assertEquals("order.instancedetail", iTuple.getEntityName().toString());
        Assert.assertEquals(NodeType.DUMP, iTuple.getNodetype());
        ;
    }
}
