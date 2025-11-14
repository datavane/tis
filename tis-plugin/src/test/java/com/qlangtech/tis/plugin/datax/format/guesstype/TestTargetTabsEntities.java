/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.datax.format.guesstype;

import com.qlangtech.tis.common.utils.Assert;
import junit.framework.TestCase;

import java.util.Set;

/**
 * 测试 {@link TargetTabsEntities#parseLogicalTable(String)} 方法
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/13
 */
public class TestTargetTabsEntities extends TestCase {

    /**
     * 测试精确匹配 - 不带通配符的场景
     */
    public void testParseLogicalTableWithExactMatch() {
        // 构造测试数据：普通表名（不带通配符）
        String[] focusTabs = new String[]{"order_detail_extend", "order_detail*", "customer"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        // 测试精确匹配
        KafkaLogicalTableName result = entities.parseLogicalTable("order_detail");
        Assert.assertNotNull(result);
        Assert.assertEquals("order_detail", result.getLogicalTableName());

        // 验证物理表名被添加到逻辑表中
        Set<String> physicalTabs = result.getPhysicalTableNames();
        Assert.assertEquals(1, physicalTabs.size());
        Assert.assertTrue(physicalTabs.contains("order_detail"));
    }

    /**
     * 测试通配符匹配 - 分库分表场景
     */
    public void testParseLogicalTableWithWildcardMatch() {
        // 构造测试数据：带通配符的表名
        String[] focusTabs = new String[]{"order_detail*"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        // 测试匹配第一个物理表
        KafkaLogicalTableName result1 = entities.parseLogicalTable("order_detail_001");
        Assert.assertNotNull(result1);
        Assert.assertEquals("order_detail", result1.getLogicalTableName());

        // 测试匹配第二个物理表
        KafkaLogicalTableName result2 = entities.parseLogicalTable("order_detail_002");
        Assert.assertNotNull(result2);
        Assert.assertEquals("order_detail", result2.getLogicalTableName());

        // 验证两次返回的是同一个逻辑表实例
        Assert.assertSame(result1, result2);

        // 验证所有物理表都被记录
        Set<String> physicalTabs = result1.getPhysicalTableNames();
        Assert.assertEquals(2, physicalTabs.size());
        Assert.assertTrue(physicalTabs.contains("order_detail_001"));
        Assert.assertTrue(physicalTabs.contains("order_detail_002"));
    }

    /**
     * 测试混合场景 - 同时包含通配符和精确匹配
     */
    public void testParseLogicalTableWithMixedPatterns() {
        // 构造测试数据：同时包含通配符和精确匹配的表名
        String[] focusTabs = new String[]{"customer", "order_detail*"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        // 测试精确匹配
        KafkaLogicalTableName customerResult = entities.parseLogicalTable("customer");
        Assert.assertEquals("customer", customerResult.getLogicalTableName());

        // 测试通配符匹配
        KafkaLogicalTableName orderResult = entities.parseLogicalTable("order_detail_100");
        Assert.assertEquals("order_detail", orderResult.getLogicalTableName());

        // 验证它们是不同的逻辑表实例
        Assert.assertNotSame(customerResult, orderResult);
    }

    /**
     * 测试大小写不敏感
     */
    public void testParseLogicalTableCaseInsensitive() {
        String[] focusTabs = new String[]{"Order_Detail*"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        // 测试不同大小写的物理表名
        KafkaLogicalTableName result1 = entities.parseLogicalTable("ORDER_DETAIL_001");
        KafkaLogicalTableName result2 = entities.parseLogicalTable("order_detail_002");
        KafkaLogicalTableName result3 = entities.parseLogicalTable("Order_Detail_003");

        // 验证都匹配到同一个逻辑表
        Assert.assertSame(result1, result2);
        Assert.assertSame(result2, result3);

        // 验证逻辑表名被转换为小写
        Assert.assertEquals("order_detail", result1.getLogicalTableName());

        // 验证所有物理表都被记录（保持原始大小写）
        Set<String> physicalTabs = result1.getPhysicalTableNames();
        Assert.assertEquals(3, physicalTabs.size());
        Assert.assertTrue(physicalTabs.contains("ORDER_DETAIL_001"));
        Assert.assertTrue(physicalTabs.contains("order_detail_002"));
        Assert.assertTrue(physicalTabs.contains("Order_Detail_003"));
    }

    /**
     * 测试多次调用同一个物理表名 - 去重场景
     */
    public void testParseLogicalTableWithDuplicatePhysicalTable() {
        String[] focusTabs = new String[]{"order_detail*"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        // 多次解析同一个物理表名
        KafkaLogicalTableName result1 = entities.parseLogicalTable("order_detail_001");
        KafkaLogicalTableName result2 = entities.parseLogicalTable("order_detail_001");
        KafkaLogicalTableName result3 = entities.parseLogicalTable("order_detail_001");

        // 验证返回同一个实例
        Assert.assertSame(result1, result2);
        Assert.assertSame(result2, result3);

        // 验证物理表名只被记录一次（Set去重）
        Set<String> physicalTabs = result1.getPhysicalTableNames();
        Assert.assertEquals(1, physicalTabs.size());
    }

    /**
     * 测试匹配优先级 - 短表名优先匹配
     */
    public void testParseLogicalTableMatchPriority() {
        // 构造测试数据：包含可能冲突的表名模式，短的在前（构造函数会按长度排序）
        String[] focusTabs = new String[]{"order*", "order_detail*"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        // "order_detail_001" 应该匹配第一个更长的 "order_detail*"
        KafkaLogicalTableName result = entities.parseLogicalTable("order_detail_001");
        Assert.assertEquals("order_detail", result.getLogicalTableName());

        result = entities.parseLogicalTable("order_001");
        Assert.assertEquals("order", result.getLogicalTableName());
    }

    /**
     * 测试异常场景 - 找不到匹配的逻辑表
     */
    public void testParseLogicalTableNotFound() {
        String[] focusTabs = new String[]{"order_detail*", "customer"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        try {
            // 尝试解析一个不在focusTabs中的表名
            entities.parseLogicalTable("product_info");
            fail("应该抛出IllegalStateException异常");
        } catch (IllegalStateException e) {
            // 验证异常消息包含必要信息
            Assert.assertTrue(e.getMessage().contains("product_info"));
            Assert.assertTrue(e.getMessage().contains("relevant kafkaLogicalTableName can not be null"));
        }
    }

    /**
     * 测试空物理表名场景
     */
    public void testParseLogicalTableWithEmptyPhysicalTableName() {
        String[] focusTabs = new String[]{"order_detail"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        try {
            // 尝试解析空表名
            entities.parseLogicalTable("");
            fail("应该抛出IllegalStateException异常");
        } catch (IllegalStateException e) {
            // 期望抛出异常
            Assert.assertTrue(e.getMessage().contains("relevant kafkaLogicalTableName can not be null"));
        }
    }

    /**
     * 测试多个通配符表名的匹配场景
     */
    public void testParseLogicalTableWithMultipleWildcards() {
        String[] focusTabs = new String[]{"order_detail*", "order_info*", "user_profile*"};
        TargetTabsEntities entities = new TargetTabsEntities(focusTabs);

        // 测试匹配不同的逻辑表
        KafkaLogicalTableName orderDetail = entities.parseLogicalTable("order_detail_100");
        KafkaLogicalTableName orderInfo = entities.parseLogicalTable("order_info_200");
        KafkaLogicalTableName userProfile = entities.parseLogicalTable("user_profile_300");

        // 验证都匹配到正确的逻辑表
        Assert.assertEquals("order_detail", orderDetail.getLogicalTableName());
        Assert.assertEquals("order_info", orderInfo.getLogicalTableName());
        Assert.assertEquals("user_profile", userProfile.getLogicalTableName());

        // 验证它们是不同的实例
        Assert.assertNotSame(orderDetail, orderInfo);
        Assert.assertNotSame(orderInfo, userProfile);
    }
}
