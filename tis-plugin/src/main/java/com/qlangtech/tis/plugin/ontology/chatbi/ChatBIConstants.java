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

import com.qlangtech.tis.manage.common.Config;

import java.io.File;

/**
 * ChatBI 相关的常量定义，用于统一管理 JSON 字段名等字符串常量。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/6/25
 */
public final class ChatBIConstants {

    private ChatBIConstants() {
        // 工具类，禁止实例化
    }

    public static File getOntologyDoaminTraceDir(String domain) {
        return new File(Config.getDataDir(), "chatbi/trace/" + domain);
    }

    // ===== TraceStep 字段名 =====
    /**
     * TraceStep JSON 字段：执行步骤名称
     */
    public static final String FIELD_STEP = "step";

    /**
     * TraceStep JSON 字段：是否成功
     */
    public static final String FIELD_OK = "ok";

    /**
     * TraceStep JSON 字段：描述信息
     */
    public static final String FIELD_MESSAGE = "message";

    /**
     * TraceStep JSON 字段：耗时（毫秒）
     */
    public static final String FIELD_MILLIS = "millis";

    /**
     * TraceStep JSON 字段：步骤相关数据
     */
    public static final String FIELD_DATA = "data";

    // ===== ChatBIResult 字段名 =====
    /**
     * ChatBIResult JSON 字段：生成的 SQL 语句
     */
    public static final String FIELD_SQL = "sql";

    /**
     * ChatBIResult JSON 字段：错误信息
     */
    public static final String FIELD_ERROR = "error";

    /**
     * ChatBIResult JSON 字段：查询结果轨迹
     */
    public static final String FIELD_TRACE = "trace";

    /**
     * ChatBIResult JSON 字段：请求唯一标识符
     */
    public static final String FIELD_REQ_ID = "reqId";

    /**
     * ChatBIResult JSON 字段：执行成功标识
     */
    public static final String FIELD_SUCCESS = "success";

    // ===== QueryResult 字段名 =====
    /**
     * QueryResult JSON 字段：列名列表
     */
    public static final String FIELD_COLUMNS = "columns";

    /**
     * QueryResult JSON 字段：行数据
     */
    public static final String FIELD_ROWS = "rows";

    /**
     * QueryResult JSON 字段：返回的行数
     */
    public static final String FIELD_ROW_COUNT = "rowCount";

    /**
     * QueryResult JSON 字段：结果是否被截断
     */
    public static final String FIELD_TRUNCATED = "truncated";

    /**
     * QueryResult JSON 字段：实际查询到的总行数
     */
    public static final String FIELD_ACTUAL_ROWS = "actualRows";

    // ===== Trace 文件和历史记录字段 =====
    /**
     * Trace 文件 header 字段：自然语言问句
     */
    public static final String FIELD_NLQ = "nlq";

    /**
     * Trace 文件 header 字段：时间戳
     */
    public static final String FIELD_TIMESTAMP = "timestamp";

    /**
     * Trace 详情字段：重试次数
     */
    public static final String FIELD_RETRY_CNT = "retryCnt";

    /**
     * Trace 详情字段：LLM 总耗时（毫秒）
     */
    public static final String FIELD_LLM_MS = "llmMs";

    /**
     * Trace 详情字段：执行耗时（毫秒）
     */
    public static final String FIELD_EXECUTE_MS = "executeMs";

    /**
     * Trace 详情字段：步骤列表
     */
    public static final String FIELD_STEPS = "steps";

    // ===== 步骤类型常量 =====
    /**
     * TraceStep 步骤类型：LLM 调用
     */
    public static final String STEP_LLM = "llm";

    /**
     * TraceStep 步骤类型：执行查询
     */
    public static final String STEP_EXECUTE = "execute";

    /**
     * TraceStep 步骤类型：错误
     */
    public static final String STEP_ERROR = "error";
}
