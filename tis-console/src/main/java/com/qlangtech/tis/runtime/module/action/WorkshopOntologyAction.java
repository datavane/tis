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
package com.qlangtech.tis.runtime.module.action;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.qlangtech.tis.plugin.workshop.service.WorkshopOntologyService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Workshop Ontology 集成 Action
 * 处理 Workshop 对 Ontology 的数据查询和 Action 执行请求
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/8
 */
public class WorkshopOntologyAction extends BasicModule {

    private static final Logger logger = LoggerFactory.getLogger(WorkshopOntologyAction.class);

    private static final String KEY_ONTOLOGY_NAME = "ontologyName";
    private static final String KEY_OBJECT_TYPE = "objectType";
    private static final String KEY_ACTION_ID = "actionId";
    private static final String KEY_LIMIT = "limit";
    private static final String KEY_OBJECT_RIDS = "objectRids";
    private static final String KEY_PARAMETERS = "parameters";
    private static final String KEY_FILTERS = "filters";
    private static final String KEY_OBJECT_RID = "objectRid";

    private final WorkshopOntologyService ontologyService = WorkshopOntologyService.getInstance();

    /**
     * 查询 Ontology Object Set
     * POST /runtime/workshop-ontology?event_submit_do_query_object_set=true
     */
    public void doQueryObjectSet(Context context) {
        try {
            String ontologyName = this.getString(KEY_ONTOLOGY_NAME);
            String objectType = this.getString(KEY_OBJECT_TYPE);
            String filtersJson = this.getString(KEY_FILTERS);
            int limit = this.getInt(KEY_LIMIT, 100);

            if (StringUtils.isEmpty(ontologyName)) {
                this.addErrorMessage(context, "ontologyName is required");
                return;
            }
            if (StringUtils.isEmpty(objectType)) {
                this.addErrorMessage(context, "objectType is required");
                return;
            }

            // 解析过滤条件
            List<WorkshopOntologyService.Filter> filters = new ArrayList<>();
            if (StringUtils.isNotEmpty(filtersJson)) {
                JSONArray filtersArray = JSON.parseArray(filtersJson);
                for (int i = 0; i < filtersArray.size(); i++) {
                    JSONObject filterObj = filtersArray.getJSONObject(i);
                    WorkshopOntologyService.Filter filter = new WorkshopOntologyService.Filter();
                    filter.setProperty(filterObj.getString("property"));
                    filter.setOperator(filterObj.getString("operator"));
                    filter.setValue(filterObj.get("value"));
                    filters.add(filter);
                }
            }

            // 执行查询
            List<Map<String, Object>> results = ontologyService.queryObjectSet(
                    ontologyName, objectType, filters, limit);

            // 设置返回结果
            this.setBizResult(context, results);
            this.addActionMessage(context, "Query successful: " + results.size() + " results");

        } catch (Exception e) {
            logger.error("Failed to query object set", e);
            this.addErrorMessage(context, "Failed to query object set: " + e.getMessage());
        }
    }

    /**
     * 执行 Ontology Action
     * POST /runtime/workshop-ontology?event_submit_do_execute_action=true
     */
    public void doExecuteAction(Context context) {
        try {
            String ontologyName = this.getString(KEY_ONTOLOGY_NAME);
            String actionId = this.getString(KEY_ACTION_ID);
            String objectRidsJson = this.getString(KEY_OBJECT_RIDS);
            String parametersJson = this.getString(KEY_PARAMETERS);

            if (StringUtils.isEmpty(ontologyName)) {
                this.addErrorMessage(context, "ontologyName is required");
                return;
            }
            if (StringUtils.isEmpty(actionId)) {
                this.addErrorMessage(context, "actionId is required");
                return;
            }

            // 解析对象 RID 列表
            List<String> objectRids = new ArrayList<>();
            if (StringUtils.isNotEmpty(objectRidsJson)) {
                JSONArray ridsArray = JSON.parseArray(objectRidsJson);
                for (int i = 0; i < ridsArray.size(); i++) {
                    objectRids.add(ridsArray.getString(i));
                }
            }

            // 解析参数
            Map<String, Object> parameters = null;
            if (StringUtils.isNotEmpty(parametersJson)) {
                JSONObject paramsObj = JSON.parseObject(parametersJson);
                parameters = paramsObj.getInnerMap();
            }

            // 执行 Action
            WorkshopOntologyService.ActionResult result = ontologyService.executeAction(
                    ontologyName, actionId, objectRids, parameters);

            // 设置返回结果
            JSONObject resultJson = new JSONObject();
            resultJson.put("success", result.isSuccess());
            resultJson.put("message", result.getMessage());
            resultJson.put("data", result.getData());
            this.setBizResult(context, resultJson);

            if (result.isSuccess()) {
                this.addActionMessage(context, result.getMessage());
            } else {
                this.addErrorMessage(context, result.getMessage());
            }

        } catch (Exception e) {
            logger.error("Failed to execute action", e);
            this.addErrorMessage(context, "Failed to execute action: " + e.getMessage());
        }
    }

    /**
     * 获取 Object 详情
     * POST /runtime/workshop-ontology?event_submit_do_get_object=true
     */
    public void doGetObject(Context context) {
        try {
            String objectRid = this.getString(KEY_OBJECT_RID);

            if (StringUtils.isEmpty(objectRid)) {
                this.addErrorMessage(context, "objectRid is required");
                return;
            }

            Map<String, Object> object = ontologyService.getObject(objectRid);

            this.setBizResult(context, object);
            this.addActionMessage(context, "Object retrieved successfully");

        } catch (Exception e) {
            logger.error("Failed to get object", e);
            this.addErrorMessage(context, "Failed to get object: " + e.getMessage());
        }
    }
}
