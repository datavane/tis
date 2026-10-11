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
}
