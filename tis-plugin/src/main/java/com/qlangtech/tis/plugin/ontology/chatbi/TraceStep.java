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
package com.qlangtech.tis.plugin.ontology.chatbi;

import com.alibaba.fastjson.JSONObject;

/**
 * Trace 步骤记录。
 *
 * @param step    步骤名（retrieve/prompt/llm/extract/validate/execute）
 * @param ok      是否成功
 * @param message 描述信息
 * @param data    JSON 数据（含 model/tokens/sql/issues 等字段）
 * @param millis  耗时（ms）
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/6/2
 */
public record TraceStep(
        String step,
        boolean ok,
        String message,
        JSONObject data,
        long millis
) {
    public static TraceStep of(String step, boolean ok, String message, JSONObject data, long millis) {
        return new TraceStep(step, ok, message, data, millis);
    }

    public static TraceStep retrieve(int otsCount, int linkersCount, long millis) {
        JSONObject data = new JSONObject();
        data.put("otsCount", otsCount);
        data.put("linkersCount", linkersCount);
        return new TraceStep("retrieve", true, "GraphRAG retrieval completed", data, millis);
    }

    public static TraceStep prompt(int tokens, String systemPrompt, String userPrompt) {
        JSONObject data = new JSONObject();
        data.put("tokens", tokens);
        data.put("system", systemPrompt);
        data.put("user", userPrompt);
        return new TraceStep("prompt", true, "Prompt assembled", data, 0);
    }

    public static TraceStep llm(String model, long promptTokens, long completionTokens, String raw, long millis) {
        JSONObject data = new JSONObject();
        data.put("model", model);
        data.put("promptTokens", promptTokens);
        data.put("completionTokens", completionTokens);
        data.put("raw", raw);
        return new TraceStep("llm", true, "LLM invoked", data, millis);
    }

    public static TraceStep extract(String sql) {
        JSONObject data = new JSONObject();
        data.put("sql", sql);
        return new TraceStep("extract", true, "SQL extracted", data, 0);
    }

    public static TraceStep validate(boolean ok, String message, JSONObject issues) {
        return new TraceStep("validate", ok, message, issues, 0);
    }

    public static TraceStep execute(int rowCount, long millis) {
        JSONObject data = new JSONObject();
        data.put("rows", rowCount);
        return new TraceStep("execute", true, "Query executed", data, millis);
    }

    public static TraceStep error(String step, String message) {
        return new TraceStep(step, false, message, new JSONObject(), 0);
    }
}
