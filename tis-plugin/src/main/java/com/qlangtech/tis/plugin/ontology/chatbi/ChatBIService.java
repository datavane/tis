/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.plugin.ontology.chatbi;

/**
 * ChatBI 服务接口：NL2SQL 主流程。
 * <p>
 * 设计参见 {@code design/chat-bi/04-nl-to-sql.md}。
 * <p>
 * 每个 ontology 域对应一个独立实例，由 { com.qlangtech.tis.plugin.ontology.EnableChatBI} 持有。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/6/2
 * //@see DefaultChatBIService default implementation
 */
public interface ChatBIService {

    /**
     * 给定自然语言问句，生成并执行 SQL。
     * <p>
     * 检索参数、是否执行、是否 EXPLAIN 校验等选项均由实现类从自身配置中读取。
     *
     * @param domain 本体域名
     * @param nlq    自然语言问句
     * @return ChatBI 结果（含 SQL、执行结果、trace、错误）
     */
    ChatBIResult ask(String domain, String nlq);

    /**
     * 给定自然语言问句，生成并执行 SQL，同时通过回调实时推送每个 TraceStep。
     * <p>
     * 默认实现忽略回调，保持向后兼容。
     *
     * @param domain       本体域名
     * @param nlq          自然语言问句
     * @param stepCallback 每产生一个 TraceStep 时立即调用（用于 SSE 推送）
     * @return ChatBI 结果（含 SQL、执行结果、trace、错误）
     */
    default ChatBIResult ask(String domain, String nlq, java.util.function.Consumer<TraceStep> stepCallback) {
        return ask(domain, nlq);
    }
}
