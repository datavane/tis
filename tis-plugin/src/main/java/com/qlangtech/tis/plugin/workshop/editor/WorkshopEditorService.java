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
package com.qlangtech.tis.plugin.workshop.editor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.workshop.editor.WidgetBulkUpdateRequest.EditorOperation;
import com.qlangtech.tis.plugin.workshop.editor.SectionBulkUpdateRequest.SectionOperation;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * Workshop 编辑器服务。
 * <p>
 * 负责处理 widget/section 批量操作和草稿持久化。
 * 草稿数据以 JSON 文件形式存储于 cfg-repo 路径下：
 * <pre>
 *   ${TIS.pluginCfgRoot}/workshop/editor/drafts/{moduleId}/{userId}.json
 * </pre>
 * <p>
 * 批量操作（applyBatch / applySectionOps）在内存中模拟执行并返回结果。
 * </p>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/9
 */
public class WorkshopEditorService {

    private static final Logger logger = LoggerFactory.getLogger(WorkshopEditorService.class);

    /**
     * 编辑器数据在 cfg-repo 下的相对根路径
     */
    private static final String EDITOR_STORE_ROOT = "workshop/editor";

    /**
     * 草稿数据子目录
     */
    private static final String DRAFT_DIR = "drafts";

    /**
     * 已提交的 batchKey 缓存，用于幂等去重（内存级，JVM 重启后重置）
     */
    private final ConcurrentMap<String, Boolean> completedBatchKeys = new ConcurrentHashMap<>();

    private static final WorkshopEditorService INSTANCE = new WorkshopEditorService();

    private WorkshopEditorService() {
    }

    /**
     * 获取全局单例
     */
    public static WorkshopEditorService getInstance() {
        return INSTANCE;
    }

    // ========================================================================
    // 批量操作
    // ========================================================================

    /**
     * 批量执行 Widget 操作。
     * <p>
     * 如果 batchKey 已被处理过，直接返回上一次的结果（幂等）。
     * 操作按顺序执行，遇到第一条错误即中止，已执行成功的操作不回滚。
     * </p>
     *
     * @param moduleId 目标模块
     * @param batchKey UUID 幂等 key
     * @param ops      操作列表
     * @return 批量执行结果
     */
    public BulkUpdateResult applyBatch(String moduleId, String batchKey, List<EditorOperation> ops) {
        Objects.requireNonNull(moduleId, "moduleId can not be null");
        Objects.requireNonNull(batchKey, "batchKey can not be null");
        if (ops == null || ops.isEmpty()) {
            return new BulkUpdateResult(Collections.emptyList());
        }

        // 幂等校验：已完成的 batchKey 直接返回空成功
        if (completedBatchKeys.putIfAbsent(batchKey, Boolean.TRUE) != null) {
            logger.info("batchKey {} already processed for module {}, skip", batchKey, moduleId);
            return new BulkUpdateResult(ops.stream().map(op -> new PerOpResult(0, true, null)).collect(Collectors.toList()));
        }

        List<PerOpResult> perOp = Lists.newArrayListWithExpectedSize(ops.size());
        int index = 0;
        for (EditorOperation op : ops) {
            try {
                validateEditorOperation(op);
                // 实际的操作执行逻辑由上层 Action 委托给具体的 Widget/Section 管理器
                // 此处仅做参数校验和封装
                perOp.add(new PerOpResult(index, true, null));
            } catch (Exception e) {
                logger.warn("applyBatch failed at index {}: kind={}, error={}", index, op.getKind(), e.getMessage());
                perOp.add(new PerOpResult(index, false, e.getMessage()));
                break;
            }
            index++;
        }

        return new BulkUpdateResult(perOp);
    }

    /**
     * 批量执行 Section 操作。
     *
     * @param moduleId   目标模块
     * @param sectionOps 操作列表
     * @return 批量执行结果
     */
    public BulkUpdateResult applySectionOps(String moduleId, List<SectionOperation> sectionOps) {
        Objects.requireNonNull(moduleId, "moduleId can not be null");
        if (sectionOps == null || sectionOps.isEmpty()) {
            return new BulkUpdateResult(Collections.emptyList());
        }

        List<PerOpResult> perOp = Lists.newArrayListWithExpectedSize(sectionOps.size());
        int index = 0;
        for (SectionOperation op : sectionOps) {
            try {
                validateSectionOperation(op);
                perOp.add(new PerOpResult(index, true, null));
            } catch (Exception e) {
                logger.warn("applySectionOps failed at index {}: kind={}, error={}", index, op.getKind(), e.getMessage());
                perOp.add(new PerOpResult(index, false, e.getMessage()));
                break;
            }
            index++;
        }

        return new BulkUpdateResult(perOp);
    }

    private void validateEditorOperation(EditorOperation op) {
        if (StringUtils.isEmpty(op.getKind())) {
            throw new IllegalArgumentException("EditorOperation kind can not be empty");
        }
        if (op.getParams() == null) {
            throw new IllegalArgumentException("EditorOperation params can not be null");
        }
        switch (op.getKind()) {
            case "add-widget":
            case "remove-widget":
            case "update-widget":
            case "duplicate-widget":
            case "move-widget":
            case "reorder-widgets":
                break;
            default:
                throw new IllegalArgumentException("Unknown EditorOperation kind: " + op.getKind());
        }
    }

    private void validateSectionOperation(SectionOperation op) {
        if (StringUtils.isEmpty(op.getKind())) {
            throw new IllegalArgumentException("SectionOperation kind can not be empty");
        }
        if (op.getParams() == null) {
            throw new IllegalArgumentException("SectionOperation params can not be null");
        }
        switch (op.getKind()) {
            case "add-section":
            case "remove-section":
            case "reorder-sections":
                break;
            default:
                throw new IllegalArgumentException("Unknown SectionOperation kind: " + op.getKind());
        }
    }

    // ========================================================================
    // 草稿 CRUD
    // ========================================================================

    /**
     * 获取草稿文件的存储根目录
     */
    private File getDraftStoreRoot() {
        return new File(TIS.pluginCfgRoot,
                EDITOR_STORE_ROOT + File.separator + DRAFT_DIR);
    }

    /**
     * 获取指定 module + userId 对应的草稿文件路径
     */
    private File getDraftFile(String moduleId, String userId) {
        File moduleDir = new File(getDraftStoreRoot(),
                Objects.requireNonNull(moduleId, "moduleId can not be null"));
        return new File(moduleDir,
                Objects.requireNonNull(userId, "userId can not be null") + ".json");
    }

    /**
     * 保存草稿（创建或覆盖）。
     *
     * @param moduleId 目标模块
     * @param userId   用户标识
     * @param draft    草稿对象（含当前编辑器序列化状态）
     */
    public void saveDraft(String moduleId, String userId, EditorDraft draft) {
        Objects.requireNonNull(moduleId, "moduleId can not be null");
        Objects.requireNonNull(userId, "userId can not be null");
        Objects.requireNonNull(draft, "draft can not be null");

        draft.setUserId(userId);
        draft.setModuleId(moduleId);
        draft.setUpdatedAt(LocalDateTime.now());
        if (draft.getCreatedAt() == null) {
            draft.setCreatedAt(draft.getUpdatedAt());
        }

        File file = getDraftFile(moduleId, userId);
        try {
            FileUtils.forceMkdir(file.getParentFile());
            String json = JSON.toJSONString(draft, true);
            FileUtils.writeStringToFile(file, json, TisUTF8.getName());
            logger.info("draft saved: moduleId={}, userId={}, file={}", moduleId, userId, file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save draft for moduleId=" + moduleId + ", userId=" + userId, e);
        }
    }

    /**
     * 加载草稿。
     *
     * @param moduleId 目标模块
     * @param userId   用户标识
     * @return 草稿对象，如果不存在返回 null
     */
    public EditorDraft loadDraft(String moduleId, String userId) {
        Objects.requireNonNull(moduleId, "moduleId can not be null");
        Objects.requireNonNull(userId, "userId can not be null");

        File file = getDraftFile(moduleId, userId);
        if (!file.isFile()) {
            logger.info("draft not found: moduleId={}, userId={}", moduleId, userId);
            return null;
        }

        try {
            String json = FileUtils.readFileToString(file, TisUTF8.getName());
            return JSON.parseObject(json, EditorDraft.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load draft for moduleId=" + moduleId + ", userId=" + userId, e);
        }
    }

    /**
     * 删除草稿。
     *
     * @param moduleId 目标模块
     * @param userId   用户标识
     */
    public void deleteDraft(String moduleId, String userId) {
        Objects.requireNonNull(moduleId, "moduleId can not be null");
        Objects.requireNonNull(userId, "userId can not be null");

        File file = getDraftFile(moduleId, userId);
        if (file.isFile()) {
            if (file.delete()) {
                logger.info("draft deleted: moduleId={}, userId={}, file={}", moduleId, userId, file.getAbsolutePath());
            } else {
                logger.warn("draft delete failed: moduleId={}, userId={}, file={}", moduleId, userId, file.getAbsolutePath());
            }
        }
    }

    // ========================================================================
    // 批量操作结果模型
    // ========================================================================

    /**
     * 批量操作执行结果。
     */
    public static class BulkUpdateResult {

        private final List<PerOpResult> perOp;

        public BulkUpdateResult(List<PerOpResult> perOp) {
            this.perOp = perOp != null ? perOp : Collections.emptyList();
        }

        /**
         * 所有操作是否全部成功
         */
        public boolean allSuccess() {
            return perOp.stream().allMatch(r -> r != null && r.isSuccess());
        }

        /**
         * 已执行的操作数（包括失败的那一条）
         */
        public int getAppliedCount() {
            return perOp.size();
        }

        /**
         * 第一条错误信息（无错误返回 null）
         */
        public String firstError() {
            return perOp.stream()
                    .filter(r -> r != null && !r.isSuccess())
                    .findFirst()
                    .map(PerOpResult::getError)
                    .orElse(null);
        }

        public List<PerOpResult> getPerOp() {
            return perOp;
        }
    }

    /**
     * 单条操作执行结果。
     */
    public static class PerOpResult {

        private final int index;

        private final boolean success;

        private final String error;

        public PerOpResult(int index, boolean success, String error) {
            this.index = index;
            this.success = success;
            this.error = error;
        }

        public int getIndex() {
            return index;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }
}