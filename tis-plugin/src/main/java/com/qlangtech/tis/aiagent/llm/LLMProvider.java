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
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.manage.common.ILoginUser;
import com.qlangtech.tis.plugin.credentials.ParamsConfigPluginStore;
import com.qlangtech.tis.plugin.llm.DeepSeekProvider;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 大模型接口抽象
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public abstract class LLMProvider extends ParamsConfig {

    protected static final String KEY_DISPLAY_NAME = "LLM";


    public static List<ParamsConfig> getExistProviders() {
        IPluginContext context = IPluginContext.getThreadLocalInstance();
        List<ParamsConfig> llmProviders = loadAllProvidersBindWithUser(context);
        return llmProviders;
        //  return llmProviders.stream().map(ParamsConfig::map2SelectOption).collect(Collectors.toList());
    }


    /**
     *
     * @param context
     * @return
     */
    private static List<ParamsConfig> loadAllProvidersBindWithUser(IPluginContext context) {

//                = UploadPluginMeta.parse(HeteroEnum.PARAMS_CONFIG_USER_ISOLATION.identity + ":" + KEY_REQUIRE
//                + "," + KEY_TARGET_PLUGIN_DESC + "_" + KEY_DISPLAY_NAME);

        UploadPluginMeta pluginMeta = ParamsConfigPluginStore.createParamsConfigUserIsolation(KEY_DISPLAY_NAME);
        List<ParamsConfig> llmProviders
                = HeteroEnum.PARAMS_CONFIG_USER_ISOLATION.getPlugins(context, pluginMeta);
        return llmProviders;
    }

    /**
     * 加载实例
     *
     * @param pluginContext 需要根据当前登录的用户进行隔离
     * @param identityName
     * @return
     */
    public static LLMProvider load(IPluginContext pluginContext, String identityName) {
        if (StringUtils.isEmpty(identityName)) {
            throw new IllegalArgumentException("param identityName can not be null");
        }

        //PartialSettedPluginContext pluginContext = new PartialSettedPluginContext();
        List<ParamsConfig> providers = loadAllProvidersBindWithUser(pluginContext);
        for (ParamsConfig config : providers) {
            if (StringUtils.equals(config.identityValue(), config.identityValue())) {
                return (LLMProvider) config;
            }
        }
        throw new IllegalStateException("can not find llmProvider with name:" + identityName);
        //  return ParamsConfig.getItem(identityName, KEY_DISPLAY_NAME, Optional.of(user.getName()), true);
    }


    /**
     * 加载所有的实例
     *
     * @param user 需要根据当前登录的用户进行隔离
     * @return
     */
    public static List<LLMProvider> loadAll(ILoginUser user) {
        return getItems(KEY_DISPLAY_NAME, Optional.of(user.getName()));
    }


    /**
     * 调用LLM进行文本生成
     *
     * @param prompt       提示词
     * @param systemPrompt 系统提示词
     * @return 生成的文本和token使用情况
     */
    public abstract LLMResponse chat(String prompt, String systemPrompt);

    /**
     * 调用LLM进行JSON格式化输出
     *
     * @param prompt       提示词
     * @param systemPrompt 系统提示词
     * @param jsonSchema   JSON Schema定义
     * @return 生成的JSON对象和token使用情况
     * @see DeepSeekProvider#chatJson(String, String, String)
     */
    public abstract LLMResponse chatJson(String prompt, String systemPrompt, String jsonSchema);

    /**
     * 获取提供商名称
     */
    public abstract String getProviderName();

    /**
     * 检查是否可用
     */
    public abstract boolean isAvailable();


    /**
     * LLM响应结果
     */
    public static class LLMResponse {
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
}
