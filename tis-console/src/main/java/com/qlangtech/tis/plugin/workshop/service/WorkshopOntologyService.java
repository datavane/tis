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
package com.qlangtech.tis.plugin.workshop.service;

import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.plugin.ontology.Ontology;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.OntologyProperty;
import com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeBinding;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Workshop Ontology 查询服务
 * 负责查询 Ontology Object Set、执行 Ontology Action、执行 SQL 等操作
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/8
 */
public class WorkshopOntologyService {

    private static final Logger logger = LoggerFactory.getLogger(WorkshopOntologyService.class);

    private static final WorkshopOntologyService INSTANCE = new WorkshopOntologyService();

    public static WorkshopOntologyService getInstance() {
        return INSTANCE;
    }

    /**
     * 查询 Ontology Object Set
     *
     * @param ontologyName   Ontology 域名
     * @param objectTypeApiName 对象类型 API 名称
     * @param filters        过滤条件
     * @param limit          限制数量
     * @return 对象列表（Map 格式）
     */
    public List<Map<String, Object>> queryObjectSet(
            String ontologyName,
            String objectTypeApiName,
            List<Filter> filters,
            int limit) {

        logger.info("Querying object set: ontology={}, objectType={}, filters={}, limit={}",
                ontologyName, objectTypeApiName, filters, limit);

        // 加载 OntologyObjectType
        OntologyObjectType objectType = Ontology.loadObjectTypeDetail(ontologyName, objectTypeApiName);
        if (objectType == null) {
            logger.warn("Object type not found: {}", objectTypeApiName);
            return Collections.emptyList();
        }

        // 对象类型通过 ObjectTypeBinding 关联到具体数据源；未绑定时没有可查的物理表
        ObjectTypeBinding binding = objectType.getDataSourceBinding();
        if (binding == null || !binding.hasBound()) {
            throw new IllegalStateException("objectType:" + objectType.getName() + " has not bind dataSource");
        }
        if (objectType.getCols().isEmpty()) {
            throw new IllegalStateException("objectType:" + objectType.getName() + " has no property to query");
        }

        ObjectTypeBinding.DataSourceQueryCriteria criteria = new ObjectTypeBinding.DataSourceQueryCriteria();
        objectType.getCols().forEach((col) -> criteria.addTargetCol(col.getName()));

        // 主键：既补进 targetCols（允许按主键过滤），也用来给每行生成 __rid 保留键。
        // 前端不猜主键列名，交互侧（Button Group → OntologyAction）只认 __rid。
        final String identityProp;
        if (objectType.hasDisablePK()) {
            identityProp = null;
        } else {
            Optional<OntologyProperty> pk = objectType.getPk();
            identityProp = pk.map(OntologyProperty::getName).orElse(null);
            if (identityProp != null) {
                criteria.addTargetCol(identityProp);
            }
        }

        if (filters != null) {
            for (Filter filter : filters) {
                // 空属性名不连坐整次查询：一个前端没填全的条件不该让整张表报错
                if (filter == null || StringUtils.isEmpty(filter.getProperty())) {
                    continue;
                }
                criteria.addFilter(filter.getProperty(), filter.getOperator(), filter.getValue());
            }
        }

        // 列表变量只取前 N 行，不做翻页；但 BasicCriteria 的 page/pageSize 不能为 null
        criteria.setPage(1);
        criteria.setPageSize(clampLimit(limit));

        List<RowMap> rows = binding.executeQuery(criteria);
        if (identityProp != null) {
            for (RowMap row : rows) {
                row.put(ObjectTypeBinding.KEY_ROW_IDENTITY, row.get(identityProp));
            }
        }

        List<Map<String, Object>> results = new ArrayList<>(rows.size());
        results.addAll(rows);
        return results;
    }

    private static final int DEFAULT_LIMIT = 1000;
    private static final int MAX_LIMIT = 10000;

    /**
     * 对象集查询的行数上限：缺省 1000、最多 10000。
     */
    static int clampLimit(int limit) {
        if (limit < 1) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    /**
     * 执行 SQL 查询
     *
     * @param sqlTemplate SQL 模板（含参数占位符 :paramName）
     * @param parameters  参数映射
     * @return 查询结果
     */
    public Object executeSQL(String sqlTemplate, Map<String, Object> parameters) {
        logger.info("Executing SQL: template={}, params={}", sqlTemplate, parameters);

        // 参数替换
        String sql = replaceSQLParameters(sqlTemplate, parameters);

        // TODO: 在 10-ontology-integration 后续步骤中通过 DataSourceFactory 执行 SQL
        logger.info("SQL after parameter replacement: {}", sql);

        return Collections.emptyList();
    }

    /**
     * 获取 Object 详情
     *
     * @param objectRid 对象 RID
     * @return 对象详情
     */
    public Map<String, Object> getObject(String objectRid) {
        logger.info("Getting object detail: rid={}", objectRid);

        // TODO: 在 10-ontology-integration 后续步骤中实现完整查询
        Map<String, Object> result = new HashMap<>();
        result.put("rid", objectRid);
        result.put("description", "Object detail - full query pending data source integration");
        return result;
    }

    /**
     * SQL 参数替换
     */
    private String replaceSQLParameters(String template, Map<String, Object> params) {
        String result = template;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            result = StringUtils.replace(result, ":" + entry.getKey(), value);
        }
        return result;
    }

    /**
     * 过滤条件定义
     */
    public static class Filter {
        private String property;
        private String operator; // equals, contains, greaterThan, lessThan, in
        private Object value;

        public Filter() {
        }

        public Filter(String property, String operator, Object value) {
            this.property = property;
            this.operator = operator;
            this.value = value;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

}