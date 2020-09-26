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
package com.qlangtech.tis.full.dump;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import com.qlangtech.tis.fullbuild.taskflow.ITask;
import com.qlangtech.tis.fullbuild.taskflow.WorkflowTaskConfigParser.ProcessTask;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年1月4日 上午10:54:56
 */
public class TestTaskConfigParser extends TestCase {
    // public void test() throws Exception {
    // // HiveTaskFactory hiveTaskFactory = new HiveTaskFactory();
    // TaskConfigParser parser = TaskConfigParser.getInstance();// (hiveTaskFactory);
    // final AtomicInteger taskCount = new AtomicInteger();
    // parser.traverseTask("search4totalpay", new ProcessTask() {
    // public void process(ITask task) {
    // System.out.println(task.getName());
    // taskCount.incrementAndGet();
    // }
    // });
    // 
    // Assert.assertEquals(20, taskCount.get());
    // }
    // public void testParseTask() throws Exception {
    // // HiveTaskFactory hiveTaskFactory = new HiveTaskFactory();
    // TaskConfigParser parser = TaskConfigParser.getInstance();// (hiveTaskFactory);
    // String task = "<execute partitionSaveCount=\"1\">\n" +
    // "    <unionTask name=\"task1\" table_name=\"union_tab_name\" partition=\"pt,pmod\">\n" +
    // "        <subTab>\n" +
    // "            select id, goods_id, entity_id, is_valid, last_ver,\n" +
    // "            supplier_id, create_time, 'supplier_goods' AS table_name,\n" +
    // "            pt, '0' AS pmod\n" +
    // "            FROM union_supplier_goods WHERE is_valid=1\n" +
    // "        </subTab>\n" +
    // "\n" +
    // "        <subTab>\n" +
    // "            select id, goods_id, entity_id, self_entity_id, is_valid, last_ver,\n" +
    // "            warehouse_id, create_time, 'warehouse_goods' AS table_name,\n" +
    // "            pt, '0' AS pmod\n" +
    // "            FROM union_warehouse_goods WHERE is_valid=1\n" +
    // "        </subTab>\n" +
    // "\n" +
    // "        <subTab>\n" +
    // "            select id, goods_id, entity_id, self_entity_id, is_valid, last_ver,\n" +
    // "            warehouse_id, op_time, 'stock_change_log' AS table_name,\n" +
    // "            pt, '0' AS pmod\n" +
    // "            FROM union_stock_change_log WHERE is_valid=1\n" +
    // "        </subTab>\n" +
    // "\n" +
    // "        <subTab>\n" +
    // "            select id, goods_id, entity_id, is_valid, last_ver,\n" +
    // "            create_time, 'goods_sale_allow' AS table_name,\n" +
    // "            pt, '0' AS pmod\n" +
    // "            FROM union_goods_sale_allow WHERE is_valid=1\n" +
    // "        </subTab>\n" +
    // "\n" +
    // "        <subTab>\n" +
    // "            select id, goods_id, entity_id, is_valid, last_ver,\n" +
    // "            create_time, 'provide_goods' AS table_name,\n" +
    // "            pt, '0' AS pmod\n" +
    // "            FROM union_provide_goods WHERE is_valid=1\n" +
    // "        </subTab>\n" +
    // "    </unionTask>\n" +
    // "</execute>";
    // List<ITask> list = parser.parseTask(new ByteArrayInputStream(task.getBytes()));
    // int i = 1;
    // }
}
