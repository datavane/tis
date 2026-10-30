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

package com.qlangtech.tis.plugin.ontology.impl.objtype;

import com.qlangtech.tis.plugin.ds.IDBReservedKeys;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * {@link ObjectTypeBinding.DataSourceQueryCriteria} 的 ORDER BY 片段。
 *
 * <p>纯单测：不碰 TIS descriptor 扫描（那需要 JDK17 的 --add-opens），也不依赖活的
 * DataSourceFactory —— 转义由 {@link IDBReservedKeys} 桩承担，所以能直接跑。
 *
 * @author 百岁 (baisui@qlangtech.com)
 */
public class TestDataSourceQueryCriteria {

    /** 只做标识符转义的桩：MySQL 口径的反引号 */
    private static final IDBReservedKeys MYSQL_KEYS = new IDBReservedKeys() {
        @Override
        public Optional<String> getEscapeChar() {
            return Optional.of("`");
        }
    };

    /** 不做任何转义的桩（PG/Oracle 之外的方言，getEscapeChar 为空） */
    private static final IDBReservedKeys NO_ESCAPE_KEYS = new IDBReservedKeys() {
    };

    private static ObjectTypeBinding.DataSourceQueryCriteria createCriteria() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = new ObjectTypeBinding.DataSourceQueryCriteria();
        criteria.addTargetCol("name");
        criteria.addTargetCol("create_time");
        return criteria;
    }

    /**
     * 没指定排序列时**什么都不追加** —— 这条是「接入排序不改变既有查询」的保险：
     * 少了 ORDER BY 的语句要与改动前逐字一致，否则等于顺手改了所有既有看板的取数行为。
     */
    @Test
    public void testNoOrderByWhenNotSet() {
        StringBuilder sql = new StringBuilder("select `name` from `order`");
        createCriteria().appendOrderBy(sql, MYSQL_KEYS);
        Assert.assertEquals("select `name` from `order`", sql.toString());
    }

    @Test
    public void testOrderByAsc() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        criteria.setOrderBy("create_time", true);

        StringBuilder sql = new StringBuilder();
        criteria.appendOrderBy(sql, MYSQL_KEYS);
        Assert.assertEquals(" ORDER BY `create_time` ASC", sql.toString());
        Assert.assertEquals("create_time", criteria.getOrderByCol());
        Assert.assertTrue(criteria.isOrderByAsc());
    }

    @Test
    public void testOrderByDesc() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        criteria.setOrderBy("name", false);

        StringBuilder sql = new StringBuilder();
        criteria.appendOrderBy(sql, MYSQL_KEYS);
        Assert.assertEquals(" ORDER BY `name` DESC", sql.toString());
        Assert.assertFalse(criteria.isOrderByAsc());
    }

    /** 转义符由 IDBReservedKeys 决定，不由本类硬编码 */
    @Test
    public void testEscapeCharComesFromDbReservedKeys() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        criteria.setOrderBy("name", true);

        StringBuilder sql = new StringBuilder();
        criteria.appendOrderBy(sql, NO_ESCAPE_KEYS);
        Assert.assertEquals(" ORDER BY name ASC", sql.toString());
    }

    /**
     * 排序列不在 select 列表里 → 直接拒。
     *
     * <p>这是**安全边界**：sortField 是 HTTP 请求参数，而标识符转义只是把列名包进转义符里
     * （`` a` , (select ...) `` 这种能穿出去），白名单才是真正拦住注入的那一层。
     * 顺带也拦住了「前端发了个不在列配置里的属性名」这类错位。
     */
    @Test
    public void testOrderByColMustBeOneOfTargetCols() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        try {
            criteria.setOrderBy("secret_col", true);
            Assert.fail("col not in targetCols should be rejected");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("secret_col"));
        }

        // 注入尝试同样被白名单挡下（而不是靠转义）
        try {
            criteria.setOrderBy("name` , (select 1) -- ", true);
            Assert.fail("injection attempt should be rejected");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("must be one of targetCols"));
        }

        Assert.assertNull("被拒绝的 setOrderBy 不得留下状态", criteria.getOrderByCol());
    }

    // ==================================================================
    //  过滤属性白名单（addFilterCol）与去重
    // ==================================================================

    /**
     * 过滤属性通常**不在**展示列里（按 create_time 过滤、但表格只显示 name），
     * {@code addFilterCol} 就是给这种列开的第二个白名单。
     */
    @Test
    public void testAddFilterColAcceptsNonTargetCol() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        criteria.addFilterCol("depart_time");
        criteria.addFilter("depart_time", ObjectTypeBinding.DataSourceQueryCriteria.OP_GREATER_THAN, 10);

        Assert.assertEquals(1, criteria.getFilters().size());
        ObjectTypeBinding.FilterCriterion criterion = criteria.getFilters().get(0);
        Assert.assertEquals("depart_time", criterion.property());
        Assert.assertEquals(ObjectTypeBinding.DataSourceQueryCriteria.OP_GREATER_THAN, criterion.operator());
        Assert.assertEquals(10, criterion.value());
    }

    /**
     * 未声明过的属性仍然被拒 —— 白名单没有因为多了一条通道而放宽。
     *
     * <p>这是**安全边界**：中继过滤的列名来自 HTTP 请求，转义标识符挡不住带转义符的列名，
     * 真正有效的是「只可能是调用方声明过的列」。
     */
    @Test
    public void testAddFilterRejectsUndeclaredCol() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        try {
            criteria.addFilter("secret_col", ObjectTypeBinding.DataSourceQueryCriteria.OP_EQUALS, 1);
            Assert.fail("filter property not in targetCols/filterCols should be rejected");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("secret_col"));
            Assert.assertTrue(e.getMessage().contains("targetCols or filterCols"));
        }

        Assert.assertTrue("被拒绝的 addFilter 不得留下条件", criteria.getFilters().isEmpty());
    }

    /**
     * 过滤列**不进** SELECT 列表。
     *
     * <p>{@code DataSourceBinding#readRow} 是按 targetCols 逐列取值的：过滤列一旦混进 targetCols，
     * 返回的每一行都会多带一个前端没申请过的键。
     */
    @Test
    public void testFilterColDoesNotEnterTargetCols() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        criteria.addFilterCol("depart_time");
        criteria.addFilter("depart_time", ObjectTypeBinding.DataSourceQueryCriteria.OP_EQUALS, "2026-10-29");

        Assert.assertFalse(criteria.getTargetCols().contains("depart_time"));
        Assert.assertTrue(criteria.getFilterCols().contains("depart_time"));
        Assert.assertEquals(2, criteria.getTargetCols().size());
    }

    /** 目标列同样可以过滤（不必额外 addFilterCol） */
    @Test
    public void testAddFilterAcceptsTargetCol() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        criteria.addFilter("name", ObjectTypeBinding.DataSourceQueryCriteria.OP_EQUALS, "abc");
        Assert.assertEquals(1, criteria.getFilters().size());
        Assert.assertTrue(criteria.getFilterCols().isEmpty());
    }

    /**
     * 同一 {@code (property, operator, value)} 只保留一条。
     *
     * <p>这不是语义变化（{@code WHERE a=1 AND a=1} 与 {@code WHERE a=1} 等价），而是
     * **绑定参数不翻倍**的保证。必要性在于同一个 criteria 常被复用两次：
     * {@code ObjectTableWidget#get_data} 先 {@code countQuery} 再 {@code executeQuery}，
     * 而静态条件在这两个方法内部各施加一次。
     */
    @Test
    public void testAddFilterIsIdempotent() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        criteria.addFilter("name", ObjectTypeBinding.DataSourceQueryCriteria.OP_EQUALS, "abc");
        criteria.addFilter("name", ObjectTypeBinding.DataSourceQueryCriteria.OP_EQUALS, "abc");

        Assert.assertEquals(1, criteria.getFilters().size());
    }

    /** 去重按三元组：换了算子或值就是另一条条件 */
    @Test
    public void testDedupKeyIsFullTriple() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        criteria.addFilter("name", ObjectTypeBinding.DataSourceQueryCriteria.OP_EQUALS, "abc");
        criteria.addFilter("name", ObjectTypeBinding.DataSourceQueryCriteria.OP_CONTAINS, "abc");
        criteria.addFilter("name", ObjectTypeBinding.DataSourceQueryCriteria.OP_EQUALS, "def");

        Assert.assertEquals(3, criteria.getFilters().size());
    }

    /** 非法操作符在进入 SQL 拼装之前就被挡下（与排序列同一条安全口径） */
    @Test
    public void testAddFilterRejectsUnsupportedOperator() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        try {
            criteria.addFilter("name", "drop table", "abc");
            Assert.fail("unsupported operator should be rejected");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("illegal filter operator"));
        }
    }

    /** {@code in} 必须给集合：否则 appendWhere 渲染出 {@code IN (?)} 绑定一个标量 */
    @Test
    public void testAddFilterInRequiresCollection() {
        ObjectTypeBinding.DataSourceQueryCriteria criteria = createCriteria();
        try {
            criteria.addFilter("name", ObjectTypeBinding.DataSourceQueryCriteria.OP_IN, "abc");
            Assert.fail("'in' with scalar value should be rejected");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("requires a collection value"));
        }

        criteria.addFilter("name", ObjectTypeBinding.DataSourceQueryCriteria.OP_IN, java.util.List.of("a", "b"));
        Assert.assertEquals(1, criteria.getFilters().size());
    }
}
