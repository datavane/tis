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

import java.util.List;

/**
 * ChatBI 查询结果。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/6/2
 */
public class ChatBIResult {

    private final String sql;
    private final QueryResult data;
    private final List<TraceStep> trace;
    private final String error;
    private final Exception exception;
    private final String reqId;

    public ChatBIResult(String sql, QueryResult data, List<TraceStep> trace, String error, Exception exception, String reqId) {
        this.sql = sql;
        this.data = data;
        this.trace = trace;
        this.error = error;
        this.exception = exception;
        this.reqId = reqId;
    }

    public String sql() {
        return sql;
    }

    public QueryResult data() {
        return data;
    }

    public List<TraceStep> trace() {
        return trace;
    }

    public String error() {
        return error;
    }

    public Exception exception() {
        return exception;
    }

    /**
     * 请求 ID，格式为 yyyyMMddHHmmss-{uuid32}，与磁盘 trace 文件名对应。
     */
    public String reqId() {
        return reqId;
    }

    public static ChatBIResult success(String sql, QueryResult data, List<TraceStep> trace, String reqId) {
        return new ChatBIResult(sql, data, trace, null, null, reqId);
    }

    public static ChatBIResult fail(String error, List<TraceStep> trace, String reqId) {
        return new ChatBIResult(null, null, trace, error, null, reqId);
    }

    public static ChatBIResult fail(String error, List<TraceStep> trace, String reqId, Exception exception) {
        return new ChatBIResult(null, null, trace, error, exception, reqId);
    }

    public boolean isSuccess() {
        return error == null;
    }
}
