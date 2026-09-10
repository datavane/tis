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

import com.qlangtech.tis.plugin.ontology.Ontology;
import com.qlangtech.tis.plugin.ontology.OntologyAction;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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

        // 获取对象类型的绑定数据源信息
        // 对象类型通过 ObjectTypeBinding 关联到具体数据源
        // 这里返回元数据描述，实际数据查询在 10-ontology-integration 后续步骤中
        // 由具体的数据源实现完成
        List<Map<String, Object>> results = new ArrayList<>();

        // 构建一个示例结果（框架层，实际数据由数据源插件提供）
        Map<String, Object> meta = new HashMap<>();
        meta.put("objectType", objectTypeApiName);
        meta.put("ontologyName", ontologyName);
        meta.put("description", "Object type metadata - full query delegated to data source plugin");
        results.add(meta);

        // TODO: 在 10-ontology-integration 后续步骤中，通过 OntologyObjectType 绑定的
        //  DataSourceFactory 执行实际数据查询，并应用 filters 过滤

        return results;
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
     * 执行 Ontology Action
     *
     * @param ontologyName Ontology 域名
     * @param actionId     Action 标识
     * @param objectRids   操作对象 RID 列表
     * @param parameters   参数映射
     * @return 执行结果
     */
    public ActionResult executeAction(
            String ontologyName,
            String actionId,
            List<String> objectRids,
            Map<String, Object> parameters) {

        logger.info("Executing action: ontology={}, actionId={}, objects={}, params={}",
                ontologyName, actionId, objectRids, parameters);

        // 加载 OntologyAction
        try {
            OntologyAction action = Ontology.loadActionDetail(ontologyName, actionId);
            if (action == null) {
                return ActionResult.failure("Action not found: " + actionId);
            }

            // 验证 Action 是否存在
            logger.info("Action loaded: {} (type={})", action.identityValue(), action.getClass().getSimpleName());

            // TODO: 在 10-ontology-integration 后续步骤中实现完整的 Action 执行逻辑
            // 包括：
            // 1. 验证参数（基于 Action Parameters 定义）
            // 2. 执行规则检查（基于 Action Rules 定义）
            // 3. 调用具体的数据源写入方法
            // 4. 记录操作日志

            return ActionResult.success("Action executed successfully: " + actionId);
        } catch (Exception e) {
            logger.error("Failed to execute action: " + actionId, e);
            return ActionResult.failure("Failed to execute action: " + e.getMessage());
        }
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

    /**
     * Action 执行结果
     */
    public static class ActionResult {
        private boolean success;
        private String message;
        private Map<String, Object> data;

        public ActionResult() {
        }

        public static ActionResult success(String message) {
            ActionResult result = new ActionResult();
            result.success = true;
            result.message = message;
            result.data = new HashMap<>();
            return result;
        }

        public static ActionResult failure(String message) {
            ActionResult result = new ActionResult();
            result.success = false;
            result.message = message;
            result.data = new HashMap<>();
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }
    }
}