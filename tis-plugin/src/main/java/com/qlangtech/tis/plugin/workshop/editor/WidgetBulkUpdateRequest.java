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

import java.util.List;
import java.util.Objects;

/**
 * Widget 批量更新请求体。
 * 对应设计文档 §8.2 批量操作协议：一次请求携带多个 widget 操作，
 * 通过 batchKey（UUID）保证幂等性（防重复提交）。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/9
 */
public class WidgetBulkUpdateRequest {

    private String moduleId;

    private String batchKey;

    private List<EditorOperation> operations;

    public WidgetBulkUpdateRequest() {
    }

    public WidgetBulkUpdateRequest(String moduleId, String batchKey, List<EditorOperation> operations) {
        this.moduleId = Objects.requireNonNull(moduleId, "moduleId can not be null");
        this.batchKey = Objects.requireNonNull(batchKey, "batchKey can not be null");
        this.operations = Objects.requireNonNull(operations, "operations can not be null");
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getBatchKey() {
        return batchKey;
    }

    public void setBatchKey(String batchKey) {
        this.batchKey = batchKey;
    }

    public List<EditorOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<EditorOperation> operations) {
        this.operations = operations;
    }

    /**
     * 单条 Widget 编辑操作（JSON 可序列化）。
     * <pre>
     * kind 取值：
     *   - add-widget        添加控件
     *   - remove-widget     删除控件
     *   - update-widget     更新控件属性
     *   - duplicate-widget  复制控件
     *   - move-widget       移动控件到其他分区
     *   - reorder-widgets   调整分区内控件顺序
     * </pre>
     */
    public static class EditorOperation {

        private String kind;

        private JSONObject params;

        public EditorOperation() {
        }

        public EditorOperation(String kind, JSONObject params) {
            this.kind = Objects.requireNonNull(kind, "kind can not be null");
            this.params = params;
        }

        public String getKind() {
            return kind;
        }

        public void setKind(String kind) {
            this.kind = kind;
        }

        public JSONObject getParams() {
            return params;
        }

        public void setParams(JSONObject params) {
            this.params = params;
        }
    }
}