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

import com.alibaba.fastjson.JSONObject;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 编辑器草稿模型。
 * <p>
 * 对应设计文档 §8.3 草稿机制：每个用户对同一 module 只能有一份活跃草稿，
 * 下次打开时自动恢复，只有在手动发布时才以 final 态写入正式存储。
 * </p>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/9
 */
public class EditorDraft {

    private String userId;

    private String moduleId;

    private JSONObject draftData;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public EditorDraft() {
    }

    public EditorDraft(String userId, String moduleId, JSONObject draftData) {
        this.userId = Objects.requireNonNull(userId, "userId can not be null");
        this.moduleId = Objects.requireNonNull(moduleId, "moduleId can not be null");
        this.draftData = Objects.requireNonNull(draftData, "draftData can not be null");
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public JSONObject getDraftData() {
        return draftData;
    }

    public void setDraftData(JSONObject draftData) {
        this.draftData = draftData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}