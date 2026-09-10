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
 * Section 批量更新请求体。
 * 对应设计文档 §8.2：一次请求携带多个 section 操作（增/删/重排序），
 * 不要求幂等 key（section 级别操作由后端事务保证）。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/9
 */
public class SectionBulkUpdateRequest {

    private String moduleId;

    private List<SectionOperation> operations;

    public SectionBulkUpdateRequest() {
    }

    public SectionBulkUpdateRequest(String moduleId, List<SectionOperation> operations) {
        this.moduleId = Objects.requireNonNull(moduleId, "moduleId can not be null");
        this.operations = Objects.requireNonNull(operations, "operations can not be null");
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public List<SectionOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<SectionOperation> operations) {
        this.operations = operations;
    }

    /**
     * 单条 Section 编辑操作（JSON 可序列化）。
     * <pre>
     * kind 取值：
     *   - add-section        添加分区
     *   - remove-section     删除分区
     *   - reorder-sections   调整分区顺序
     * </pre>
     */
    public static class SectionOperation {

        private String kind;

        private JSONObject params;

        public SectionOperation() {
        }

        public SectionOperation(String kind, JSONObject params) {
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