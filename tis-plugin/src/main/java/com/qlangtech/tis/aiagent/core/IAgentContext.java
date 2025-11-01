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

package com.qlangtech.tis.aiagent.core;

import com.qlangtech.tis.aiagent.llm.LLMProvider;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/30
 */
public interface IAgentContext {

    public static IAgentContext createNull() {
        return new IAgentContext() {
            @Override
            public void updateTokenUsage(long tokens) {

            }

            @Override
            public void sendLLMStatus(LLMProvider.LLMChatPhase llmChatPhase, String detailMsg) {

            }
        };
    }

    void updateTokenUsage(long tokens);

    /**
     * 与大模型交互一次访问需要比较多时间，需要在前端控制台上用转菊花的方式展示loading
     *
     * @param llmChatPhase
     * @param detailMsg
     */
    void sendLLMStatus(LLMProvider.LLMChatPhase llmChatPhase, String detailMsg);

}
