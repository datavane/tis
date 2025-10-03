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
package com.qlangtech.tis.aiagent.llm;

import com.alibaba.fastjson.JSONObject;
import java.util.List;

/**
 * 大模型接口抽象
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public interface LLMProvider {

    /**
     * 调用LLM进行文本生成
     * @param prompt 提示词
     * @param systemPrompt 系统提示词
     * @param temperature 温度参数
     * @return 生成的文本和token使用情况
     */
    LLMResponse chat(String prompt, String systemPrompt, double temperature);

    /**
     * 调用LLM进行JSON格式化输出
     * @param prompt 提示词
     * @param systemPrompt 系统提示词
     * @param jsonSchema JSON Schema定义
     * @return 生成的JSON对象和token使用情况
     * @see DeepSeekProvider#chatJson(String, String, String)
     */
    LLMResponse chatJson(String prompt, String systemPrompt, String jsonSchema);

    /**
     * 获取提供商名称
     */
    String getProviderName();

    /**
     * 检查是否可用
     */
    boolean isAvailable();

    /**
     * 获取配置信息
     */
    LLMConfig getConfig();

    /**
     * LLM响应结果
     */
    class LLMResponse {
        private String content;
        private JSONObject jsonContent;
        private long promptTokens;
        private long completionTokens;
        private long totalTokens;
        private String model;
        private boolean success;
        private String errorMessage;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public JSONObject getJsonContent() {
            return jsonContent;
        }

        public void setJsonContent(JSONObject jsonContent) {
            this.jsonContent = jsonContent;
        }

        public long getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(long promptTokens) {
            this.promptTokens = promptTokens;
        }

        public long getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(long completionTokens) {
            this.completionTokens = completionTokens;
        }

        public long getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(long totalTokens) {
            this.totalTokens = totalTokens;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    /**
     * LLM配置信息
     */
    class LLMConfig {
        private String apiKey;
        private String apiUrl;
        private String model;
        private int maxTokens;
        private double defaultTemperature;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getApiUrl() {
            return apiUrl;
        }

        public void setApiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public double getDefaultTemperature() {
            return defaultTemperature;
        }

        public void setDefaultTemperature(double defaultTemperature) {
            this.defaultTemperature = defaultTemperature;
        }
    }
}
