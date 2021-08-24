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
package com.qlangtech.tis.wangjubao.jingwei;

import com.qlangtech.tis.realtime.transfer.TisSolrInputDocument;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable;
import com.qlangtech.tis.solrdao.impl.ParseResult;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.Map;

import static com.qlangtech.tis.wangjubao.jingwei.Alias.Builder.$;
import static com.qlangtech.tis.wangjubao.jingwei.Alias.Builder.create;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年5月22日
 */
public class TestAliasList extends TestCase {

    /**
     * 主键
     */
    public void testRepeatNotCopyColumn() {
        String customerTab = "customer";
        AliasList.BuilderList builders = AliasList.BuilderList.create();
        AliasList.Builder customerBuilder = builders.add(customerTab).setIgnoreIncrTrigger();
        customerBuilder.add($("id").PK());
        // 
        // FK or primay key
        customerBuilder.add(// FK or primay key
                $("mobile", "card_customer_moble"), // FK or primay key
                $("name", "card_customer_name"), // FK or primay key
                $("phone", "card_customer_phone"), // FK or primay key
                $("spell", "card_customer_spell"), // FK or primay key
                $("id").notCopy(), $("entity_id").notCopy());
        // 重点测试的是这里的Id键需要去除重复
        Map<String, AliasList> build = builders.build();
        AliasList aliasList = build.get(customerTab);
        assertNotNull(aliasList);
        assertEquals(6, aliasList.getAliasList().size());
        Alias id = aliasList.getColMeta("id");
        assertNotNull(id);
        assertTrue(id.isPk());
        assertFalse("id.copy must be false", id.copy);
    }

    public void testIgnoreColumn() {
        // AliasList.BuilderList builders = AliasList.BuilderList.create();
        //
        // AliasList.Builder builder = builders.add("user");
        //
        // builder.add($("name"), $("name"), $("age"), $("last_ver").ignoreChange());
        //
        // DefaultTable table = new DefaultTable("user", new RowVersionCreator[] {});
        // table.setEventType(EventType.UPDATE);
        // table.addColumn("name", "百岁");
        // table.addBeforeColumn("name", "百岁");
        //
        // table.addColumn("age", "11");
        // table.addBeforeColumn("age", "11");
        //
        // table.addColumn("last_ver", "1");
        // table.addBeforeColumn("last_ver", "2");
        //
        // Assert.assertTrue(!aliasList.getColumnsChange(table));
        // builder = AliasList.Builder.create("user");
        // builder.add($("name"), $("age"), $("last_ver"));
        // aliasList = builder.build();
        //
        // Assert.assertTrue(aliasList.getColumnsChange(table));
    }

    public void testCreateColumn() throws Exception {
        AliasList.BuilderList builders = AliasList.BuilderList.create();
        final String testTable = "testtable";
        String toColumncard_customer_phone = "card_customer_phone";
        AliasList.Builder builder = builders.add(testTable);
        // AliasList.Builder builder = AliasList.Builder.create("testtable");
        // 
        // 
        builder.add(// 
                $("id").PK(), //
                $("is_valid"), //
                $("mobile", "receiver_mobile"), //
                $("name", "receiver_name"), //
                $("last_ver"), //
                $("entity_id"), //
                $("send_status"), //
                $("waitingorder_id"), //
                $("modify_time").timestampVer(), //
                $("status"), //
                $("inner_code", "order_code"), //
                create((r) -> "1", toColumncard_customer_phone));

        Map<String, AliasList> /* table name */        columnMeta = builders.build();
        AliasList ORDER_COLUMN_CHANGE_FOCUS = columnMeta.get(testTable);
        assertNotNull(ORDER_COLUMN_CHANGE_FOCUS.getTimeVersionCol());
        assertEquals("modify_time", ORDER_COLUMN_CHANGE_FOCUS.getTimeVersionCol().getName());
        assertTrue(ORDER_COLUMN_CHANGE_FOCUS.getTimeVersionCol().copy);
        // Set<String> acceptFields = Sets.newHashSet("id", "is_valid", "mobile", "name", "last_ver", "entity_id",
        // "send_status", "waitingorder_id", "status", "inner_code", "type");
        ParseResult parseResult = TestTableClusterParser.getSchemaReflect();
        TisSolrInputDocument document = new TisSolrInputDocument(parseResult);
        Table tableProcessor = null;
        DefaultTable tab = new DefaultTable(testTable, tableProcessor);
        tab.addColumn("id", String.valueOf(123));
        ORDER_COLUMN_CHANGE_FOCUS.copy2TisDocument(tab, document, false);
        Assert.assertEquals("1", document.getFieldValue(toColumncard_customer_phone));
    }
}
