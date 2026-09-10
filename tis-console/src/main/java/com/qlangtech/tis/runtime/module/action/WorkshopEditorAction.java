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

import com.qlangtech.tis.plugin.workshop.editor.EditorDraft;
import com.qlangtech.tis.plugin.workshop.editor.WidgetBulkUpdateRequest.EditorOperation;
import com.qlangtech.tis.plugin.workshop.editor.SectionBulkUpdateRequest.SectionOperation;
import com.qlangtech.tis.plugin.workshop.editor.WorkshopEditorService;
import com.qlangtech.tis.plugin.workshop.editor.WorkshopEditorService.BulkUpdateResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Workshop 编辑器 Action。
 * <p>
 * 处理 Workshop 编辑器的 Widget/Section 批量操作和草稿持久化请求。
 * </p>
 * <p>
 * URL 映射示例：
 * POST /runtime/workshop_editor.ajax?action=workshop_editor_action&event_submit_do_bulk_update_widgets=y
 * </p>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/10
 */
public class WorkshopEditorAction extends BasicModule {

    private static final Logger logger = LoggerFactory.getLogger(WorkshopEditorAction.class);

    private final WorkshopEditorService editorService = WorkshopEditorService.getInstance();

    // ========================================================================
    // Widget 批量操作
    // ========================================================================

    /**
     * 批量更新 Widget。
     * <p>
     * POST /runtime/workshop_editor.ajax?action=workshop_editor_action&event_submit_do_bulk_update_widgets=y
     * <br>
     * 请求参数：moduleId, batchKey, operations (JSON string)
     * </p>
     *
     * @param context Turbine 上下文
     */
    public void doBulkUpdateWidgets(Context context) {
        try {
            String moduleId = this.getString("moduleId");
            String batchKey = this.getString("batchKey");
            String operationsJson = this.getString("operations");

            if (StringUtils.isEmpty(moduleId)) {
                this.addErrorMessage(context, "moduleId is required");
                return;
            }
            if (StringUtils.isEmpty(batchKey)) {
                this.addErrorMessage(context, "batchKey is required");
                return;
            }
            if (StringUtils.isEmpty(operationsJson)) {
                this.addErrorMessage(context, "operations is required");
                return;
            }

            // 解析操作列表
            List<EditorOperation> operations = parseEditorOperations(operationsJson);

            if (operations.isEmpty()) {
                this.addErrorMessage(context, "operations must not be empty");
                return;
            }

            // 执行批量操作
            BulkUpdateResult result = editorService.applyBatch(moduleId, batchKey, operations);

            // 构造返回结果
            JSONObject resultJson = new JSONObject();
            resultJson.put("allSuccess", result.allSuccess());
            resultJson.put("appliedCount", result.getAppliedCount());
            resultJson.put("firstError", result.firstError());

            this.setBizResult(context, resultJson);
            logger.info("doBulkUpdateWidgets: moduleId={}, batchKey={}, operations={}, allSuccess={}",
                    moduleId, batchKey, operations.size(), result.allSuccess());

        } catch (Exception e) {
            logger.error("Failed to bulk update widgets", e);
            this.addErrorMessage(context, "Failed to bulk update widgets: " + e.getMessage());
        }
    }

    /**
     * 重新排序分区内 Widget。
     * <p>
     * POST /runtime/workshop_editor.ajax?action=workshop_editor_action&event_submit_do_reorder_widgets=y
     * <br>
     * 请求参数：sectionId, widgetIds (JSON string array)
     * </p>
     *
     * @param context Turbine 上下文
     */
    public void doReorderWidgets(Context context) {
        try {
            String sectionId = this.getString("sectionId");
            String widgetIdsJson = this.getString("widgetIds");

            if (StringUtils.isEmpty(sectionId)) {
                this.addErrorMessage(context, "sectionId is required");
                return;
            }
            if (StringUtils.isEmpty(widgetIdsJson)) {
                this.addErrorMessage(context, "widgetIds is required");
                return;
            }

            // 解析 Widget ID 列表
            JSONArray widgetIdsArray = JSON.parseArray(widgetIdsJson);
            List<String> widgetIds = new ArrayList<>();
            for (int i = 0; i < widgetIdsArray.size(); i++) {
                widgetIds.add(widgetIdsArray.getString(i));
            }

            if (widgetIds.isEmpty()) {
                this.addErrorMessage(context, "widgetIds must not be empty");
                return;
            }

            // 构造 reorder-widgets 操作的参数
            JSONObject params = new JSONObject();
            params.put("sectionId", sectionId);
            params.put("widgetIds", widgetIds);

            List<EditorOperation> ops = new ArrayList<>();
            ops.add(new EditorOperation("reorder-widgets", params));

            // 使用空的 batchKey（这里是单次业务操作，每次调用生成一个新 key）
            String batchKey = UUID.randomUUID().toString();
            BulkUpdateResult result = editorService.applyBatch(sectionId, batchKey, ops);

            JSONObject resultJson = new JSONObject();
            resultJson.put("allSuccess", result.allSuccess());
            resultJson.put("firstError", result.firstError());

            this.setBizResult(context, resultJson);
            logger.info("doReorderWidgets: sectionId={}, widgetIds={}, allSuccess={}",
                    sectionId, widgetIds, result.allSuccess());

        } catch (Exception e) {
            logger.error("Failed to reorder widgets", e);
            this.addErrorMessage(context, "Failed to reorder widgets: " + e.getMessage());
        }
    }

    /**
     * 移动 Widget 到其他分区或调整排序位置。
     * <p>
     * POST /runtime/workshop_editor.ajax?action=workshop_editor_action&event_submit_do_move_widget=y
     * <br>
     * 请求参数：widgetId, targetSectionId, targetIndex
     * </p>
     *
     * @param context Turbine 上下文
     */
    public void doMoveWidget(Context context) {
        try {
            String widgetId = this.getString("widgetId");
            String targetSectionId = this.getString("targetSectionId");
            int targetIndex = this.getInt("targetIndex", 0);

            if (StringUtils.isEmpty(widgetId)) {
                this.addErrorMessage(context, "widgetId is required");
                return;
            }
            if (StringUtils.isEmpty(targetSectionId)) {
                this.addErrorMessage(context, "targetSectionId is required");
                return;
            }
            if (targetIndex < 0) {
                this.addErrorMessage(context, "targetIndex must be non-negative");
                return;
            }

            // 构造 move-widget 操作的参数
            JSONObject params = new JSONObject();
            params.put("widgetId", widgetId);
            params.put("targetSectionId", targetSectionId);
            params.put("targetIndex", targetIndex);

            List<EditorOperation> ops = new ArrayList<>();
            ops.add(new EditorOperation("move-widget", params));

            String batchKey = UUID.randomUUID().toString();
            BulkUpdateResult result = editorService.applyBatch(targetSectionId, batchKey, ops);

            JSONObject resultJson = new JSONObject();
            resultJson.put("allSuccess", result.allSuccess());
            resultJson.put("firstError", result.firstError());

            this.setBizResult(context, resultJson);
            logger.info("doMoveWidget: widgetId={}, targetSectionId={}, targetIndex={}, allSuccess={}",
                    widgetId, targetSectionId, targetIndex, result.allSuccess());

        } catch (Exception e) {
            logger.error("Failed to move widget", e);
            this.addErrorMessage(context, "Failed to move widget: " + e.getMessage());
        }
    }

    /**
     * 深拷贝 Widget 并分配新 UUID。
     * <p>
     * POST /runtime/workshop_editor.ajax?action=workshop_editor_action&event_submit_do_duplicate_widget=y
     * <br>
     * 请求参数：widgetId
     * </p>
     *
     * @param context Turbine 上下文
     */
    public void doDuplicateWidget(Context context) {
        try {
            String widgetId = this.getString("widgetId");

            if (StringUtils.isEmpty(widgetId)) {
                this.addErrorMessage(context, "widgetId is required");
                return;
            }

            // 构造 duplicate-widget 操作的参数
            JSONObject params = new JSONObject();
            params.put("widgetId", widgetId);

            List<EditorOperation> ops = new ArrayList<>();
            ops.add(new EditorOperation("duplicate-widget", params));

            String batchKey = UUID.randomUUID().toString();
            BulkUpdateResult result = editorService.applyBatch(widgetId, batchKey, ops);

            // 返回新的 Widget JSON 给前端
            JSONObject resultJson = new JSONObject();
            resultJson.put("allSuccess", result.allSuccess());
            resultJson.put("firstError", result.firstError());

            this.setBizResult(context, resultJson);
            logger.info("doDuplicateWidget: widgetId={}, allSuccess={}", widgetId, result.allSuccess());

        } catch (Exception e) {
            logger.error("Failed to duplicate widget", e);
            this.addErrorMessage(context, "Failed to duplicate widget: " + e.getMessage());
        }
    }

    // ========================================================================
    // Section 批量操作
    // ========================================================================

    /**
     * 批量更新 Section（增/删/重排序）。
     * <p>
     * POST /runtime/workshop_editor.ajax?action=workshop_editor_action&event_submit_do_bulk_update_sections=y
     * <br>
     * 请求参数：moduleId, sectionOps (JSON string)
     * </p>
     *
     * @param context Turbine 上下文
     */
    public void doBulkUpdateSections(Context context) {
        try {
            String moduleId = this.getString("moduleId");
            String sectionOpsJson = this.getString("sectionOps");

            if (StringUtils.isEmpty(moduleId)) {
                this.addErrorMessage(context, "moduleId is required");
                return;
            }
            if (StringUtils.isEmpty(sectionOpsJson)) {
                this.addErrorMessage(context, "sectionOps is required");
                return;
            }

            // 解析 Section 操作列表
            List<SectionOperation> sectionOps = parseSectionOperations(sectionOpsJson);

            if (sectionOps.isEmpty()) {
                this.addErrorMessage(context, "sectionOps must not be empty");
                return;
            }

            // 执行批量操作
            BulkUpdateResult result = editorService.applySectionOps(moduleId, sectionOps);

            // 构造返回结果
            JSONObject resultJson = new JSONObject();
            resultJson.put("allSuccess", result.allSuccess());
            resultJson.put("appliedCount", result.getAppliedCount());
            resultJson.put("firstError", result.firstError());

            this.setBizResult(context, resultJson);
            logger.info("doBulkUpdateSections: moduleId={}, operations={}, allSuccess={}",
                    moduleId, sectionOps.size(), result.allSuccess());

        } catch (Exception e) {
            logger.error("Failed to bulk update sections", e);
            this.addErrorMessage(context, "Failed to bulk update sections: " + e.getMessage());
        }
    }

    // ========================================================================
    // 草稿持久化
    // ========================================================================

    /**
     * 保存编辑器草稿。
     * <p>
     * POST /runtime/workshop_editor.ajax?action=workshop_editor_action&event_submit_do_save_editor_draft=y
     * <br>
     * 请求参数：moduleId, draft (JSON string)
     * </p>
     *
     * @param context Turbine 上下文
     */
    public void doSaveEditorDraft(Context context) {
        try {
            String moduleId = this.getString("moduleId");
            String draftJson = this.getString("draft");

            if (StringUtils.isEmpty(moduleId)) {
                this.addErrorMessage(context, "moduleId is required");
                return;
            }
            if (StringUtils.isEmpty(draftJson)) {
                this.addErrorMessage(context, "draft is required");
                return;
            }

            // 获取当前用户标识
            String userId = this.getUserId();

            if (StringUtils.isEmpty(userId)) {
                this.addErrorMessage(context, "userId is required");
                return;
            }

            // 解析草稿数据
            JSONObject draftData = JSON.parseObject(draftJson);

            // 构造草稿对象
            EditorDraft draft = new EditorDraft(userId, moduleId, draftData);

            // 保存草稿
            editorService.saveDraft(moduleId, userId, draft);

            JSONObject resultJson = new JSONObject();
            resultJson.put("success", true);
            resultJson.put("message", "Draft saved");

            this.setBizResult(context, resultJson);
            logger.info("doSaveEditorDraft: moduleId={}, userId={}", moduleId, userId);

        } catch (Exception e) {
            logger.error("Failed to save editor draft", e);
            this.addErrorMessage(context, "Failed to save editor draft: " + e.getMessage());
        }
    }

    // ========================================================================
    // 辅助方法
    // ========================================================================

    /**
     * 从 JSON 字符串解析 EditorOperation 列表。
     * <p>
     * 期望格式：[{"kind": "add-widget", "params": {...}}, ...]
     * </p>
     *
     * @param operationsJson 操作的 JSON 字符串
     * @return 操作列表
     */
    private List<EditorOperation> parseEditorOperations(String operationsJson) {
        List<EditorOperation> operations = new ArrayList<>();
        if (StringUtils.isNotEmpty(operationsJson)) {
            JSONArray opsArray = JSON.parseArray(operationsJson);
            for (int i = 0; i < opsArray.size(); i++) {
                JSONObject opObj = opsArray.getJSONObject(i);
                String kind = opObj.getString("kind");
                JSONObject params = opObj.getJSONObject("params");
                if (StringUtils.isNotEmpty(kind)) {
                    operations.add(new EditorOperation(kind, params));
                }
            }
        }
        return operations;
    }

    /**
     * 从 JSON 字符串解析 SectionOperation 列表。
     * <p>
     * 期望格式：[{"kind": "add-section", "params": {...}}, ...]
     * </p>
     *
     * @param sectionOpsJson 操作的 JSON 字符串
     * @return 操作列表
     */
    private List<SectionOperation> parseSectionOperations(String sectionOpsJson) {
        List<SectionOperation> operations = new ArrayList<>();
        if (StringUtils.isNotEmpty(sectionOpsJson)) {
            JSONArray opsArray = JSON.parseArray(sectionOpsJson);
            for (int i = 0; i < opsArray.size(); i++) {
                JSONObject opObj = opsArray.getJSONObject(i);
                String kind = opObj.getString("kind");
                JSONObject params = opObj.getJSONObject("params");
                if (StringUtils.isNotEmpty(kind)) {
                    operations.add(new SectionOperation(kind, params));
                }
            }
        }
        return operations;
    }
}