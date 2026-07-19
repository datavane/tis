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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.ILoginUser;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.credentials.ParamsConfigPluginStore;
import com.qlangtech.tis.plugin.llm.DeepSeekProvider;
import com.qlangtech.tis.plugin.llm.Sampling;
import com.qlangtech.tis.plugin.llm.TokenUsageSummary;
import com.qlangtech.tis.plugin.llm.log.ExecuteLog;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_VALUE_DEFAULT;
import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_VALUE_PATTERN;

/**
 * 大模型接口抽象
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 * @see com.qlangtech.tis.plugin.llm.QWenLLMProvider
 * @see DeepSeekProvider
 */
@SuppressWarnings("all")
public abstract class LLMProvider extends ParamsConfig {


    public enum LLMChatPhase {
        Start, ERROR, Complete
    }

    protected static final String KEY_DISPLAY_NAME = "LLM";

    @FormField(advance = false, ordinal = 6, validate = {Validator.require})
    public Sampling sampling;

    @FormField(type = FormFieldType.DURATION_OF_SECOND, advance = true, ordinal = 7, validate = {Validator.require,
            Validator.integer})
    public Duration readTimeout;

    @FormField(type = FormFieldType.INT_NUMBER, advance = true, ordinal = 8, validate = {Validator.require,
            Validator.integer})
    public Integer maxRetry;


    /**
     * 是否打印日志
     */
    @FormField(type = FormFieldType.ENUM, advance = true, ordinal = 99, validate = {Validator.require})
    public Boolean printLog;

    /**
     * 是否开启流式输出
     */
    public final Boolean stream = false;


    public static List<ParamsConfig> getExistProviders() {
        IPluginContext context = IPluginContext.getThreadLocalInstance();
        List<ParamsConfig> llmProviders = loadAllProvidersBindWithUser(context);
        return llmProviders;
    }


    /**
     *
     * @param context
     * @return
     */
    private static List<ParamsConfig> loadAllProvidersBindWithUser(IPluginContext context) {
        UploadPluginMeta pluginMeta = ParamsConfigPluginStore.createParamsConfigUserIsolation(KEY_DISPLAY_NAME);
        List<ParamsConfig> llmProviders = HeteroEnum.PARAMS_CONFIG_USER_ISOLATION.getPlugins(context, pluginMeta);
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
            if (StringUtils.equals(config.identityValue(), identityName)) {
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


    //    public abstract LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt);


    /**
     * 调用LLM进行文本生成
     *
     * @param prompt       提示词
     * @param systemPrompt 系统提示词
     * @return 生成的文本和token使用情况
     */
    public final LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt) {
        return chat(context, prompt, systemPrompt, true, TISJsonSchema.off());
    }

    public final LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
                                  boolean logSummary,
                                  ITISJsonSchema jsonOutput) {
        return chat(context, prompt, systemPrompt, logSummary, jsonOutput, new LLMOptionParams());
    }

    protected abstract Logger getLogger();

    protected abstract Integer getMaxTokens();

    protected abstract void addCustomizeParams(ITISJsonSchema jsonOutput, List<String> systemPrompt,
                                               LLMOptionParams params,
                                               List<HttpUtils.PostParam> postParams);

    /**
     *
     * @param prompt
     * @param systemPrompt
     * @param postParams
     */
    protected void addMessage(UserPrompt prompt, List<String> systemPrompt, List<HttpUtils.PostParam> postParams) {
        JSONArray messages = new JSONArray();
        if (CollectionUtils.isNotEmpty(systemPrompt)) {
            for (String sysP : systemPrompt) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", sysP);
                messages.add(systemMessage);
            }
        }

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt.getPrompt());
        messages.add(userMessage);
        postParams.add(new HttpUtils.PostParam("messages", messages));
    }

    public final LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
                                  boolean logSummary,
                                  ITISJsonSchema jsonOutput, LLMOptionParams params) {
        ExecuteLog executeLog = ExecuteLog.create(this.printLog, prompt, context, getLogger());
        try {
            // 构建请求参数
            List<HttpUtils.PostParam> postParams = new ArrayList<>();
            postParams.add(new HttpUtils.PostParam("model", getModel()));
            addMaxTokenParam(postParams);

            params.setSamplingParams(this.sampling, postParams);


            //            // Anthropic API: 系统提示放在顶级 "system" 字段
            //            if (CollectionUtils.isNotEmpty(systemPrompt)) {
            //                String combinedSystem = String.join("\n\n", systemPrompt);
            //                postParams.add(new HttpUtils.PostParam("system", combinedSystem));
            //            }


            //            JSONArray messages = new JSONArray();
            //            JSONObject userMessage = new JSONObject();
            //            userMessage.put("role", "user");
            //            userMessage.put("content", prompt.getPrompt());
            //            messages.add(userMessage);
            //            postParams.add(new HttpUtils.PostParam("messages", messages));
            // 用户消息
            this.addMessage(prompt, systemPrompt, postParams);

            // JSON 输出模式：通过提示增强实现
            if (jsonOutput.isContainSchema()) {
                // Anthropic 不支持原生 response_format JSON 模式，使用提示引导
                // （已在 chatJson 中增强 prompt，此处无需再次添加 schema 提示）
            }
            this.addCustomizeParams(jsonOutput, systemPrompt, params, postParams);

            // 采样参数
            // params.setSamplingParams(sampling, postParams);

            //            if (sampling != null) {
            //                sampling.setHttpParams(postParams);
            //            }
            final boolean streamOutput = isStreamOutput(params);

            postParams.add(new HttpUtils.PostParam("stream", streamOutput));

            executeLog.setPostParams(postParams);

            return HttpUtils.post(getApiUrl(), postParams, new PostFormStreamProcess<LLMResponse>(appendHeaders()) {
                @Override
                public ContentType getContentType() {
                    return ContentType.JSON;
                }

                @Override
                public int getMaxRetry() {
                    return Objects.requireNonNull(maxRetry, "maxRetry can not be null");
                }

                @Override
                public Duration getSocketReadTimeout() {
                    return readTimeout;
                }

                @Override
                public void error(int status, InputStream errstream, IOException e) {
                    if (errstream != null) {
                        try {
                            String errContent = IOUtils.toString(errstream, TisUTF8.get());
                            JSONObject errBody = null;
                            try {
                                errBody = JSONObject.parseObject(errContent);
                                executeLog.setError(errBody);
                            } catch (Exception ex) {
                                throw TisException.create("API Error: " + errContent);
                            }

                            // Anthropic 错误格式: { "type": "error", "error": { "type": "...", "message": "..." } }
                            processErrorResponseBody(status, e, errBody);
                        } catch (IOException ex) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public LLMResponse p(int status, InputStream stream, Map headerFields) throws IOException {
                    LLMResponse response = new LLMResponse(executeLog);
                    if (streamOutput) {
                        processStreamResponse(stream, getDeltaContentConsumer(params));
                        response.setSuccess(true);
                        return response;
                    }
                    String responseStr = IOUtils.toString(stream, TisUTF8.get());
                    JSONObject responseJson = JSON.parseObject(responseStr);

                    if (!processResponseJson(response, responseJson)) {
                        if (response.isSuccess()) {
                            throw new IllegalStateException("response must be faild");
                        }
                        return response;
                    }
                    //                    if (responseJson.containsKey("error")) {
                    //                        Object error = responseJson.get("error");
                    //                        response.setErrorMessage(JsonUtil.toString(error, true));
                    //                        return response;
                    //                    }
                    //
                    //                    // 检查错误
                    //                    if ("error".equals(responseJson.getString("type"))) {
                    //                        JSONObject errDetail = responseJson.getJSONObject("error");
                    //                        response.setSuccess(false);
                    //                        if (errDetail != null) {
                    //                            response.setErrorMessage(errDetail.getString("type") + ": " +
                    //                            errDetail.getString(
                    //                                    "message"));
                    //                        } else {
                    //                            response.setErrorMessage(responseStr);
                    //                        }
                    //                        return response;
                    //                    }

                    executeLog.setResponse(responseJson);

                    StringBuilder content = getResponseBodyContent(responseJson);
                    if (content != null) {
                        response.setContent(content.toString());
                        response.setSuccess(true);
                    }

                    // stop_reason 检查
                    //                    String stopReason = responseJson.getString("stop_reason");
                    //                    if ("max_tokens".equals(stopReason)) {
                    //                        getLogger().warn("Response was truncated due to max_tokens limit");
                    //                    }

                    // token 使用统计

                    TokenUsageSummary tokenUsageSummary = getTokenUsageSummary(responseJson);
                    if (tokenUsageSummary != null) {
                        response.setPromptTokens(tokenUsageSummary.inputTokens());
                        response.setCompletionTokens(tokenUsageSummary.outputTokens());
                        context.updateTokenUsage(tokenUsageSummary.inputTokens() + tokenUsageSummary.outputTokens());
                    }

                    //                    if (responseJson.containsKey("usage")) {
                    //                        JSONObject usage = responseJson.getJSONObject("usage");
                    //                        long inputTokens = usage.getLongValue("input_tokens");
                    //                        long outputTokens = usage.getLongValue("output_tokens");
                    //                        response.setPromptTokens(inputTokens);
                    //                        response.setCompletionTokens(outputTokens);
                    //                        context.updateTokenUsage(inputTokens + outputTokens);
                    //                    }

                    //                    response.setModel(responseJson.getString("model"));
                    return response;
                }
            });
            //        } catch (MalformedURLException e) {
            //            throw new RuntimeException("Invalid URL: " + getApiUrl(), e);
        } finally {
            if (logSummary) {
                executeLog.summary();
            }
        }
    }

    private boolean isStreamOutput(LLMOptionParams params) {
        final boolean streamOutput = params.getStreamOutput() != null ?
                params.getStreamOutput() : stream;
        return streamOutput;
    }

    protected void addMaxTokenParam(List<HttpUtils.PostParam> postParams) {
        postParams.add(new HttpUtils.PostParam("max_tokens",
                Objects.requireNonNull(getMaxTokens(), "maxTokens can not be null")));
    }

    protected abstract TokenUsageSummary getTokenUsageSummary(JSONObject responseJson);


    /**
     *
     * @param responseJson
     * @return false: 有错误需要立即退出
     */
    protected abstract boolean processResponseJson(LLMResponse response, JSONObject responseJson);

    protected abstract StringBuilder getResponseBodyContent(JSONObject responseJson);

    protected abstract Consumer<JSONObject> getDeltaContentConsumer(LLMOptionParams params);


    protected abstract void processErrorResponseBody(int status, IOException e, JSONObject errBody);
    //    {
    //        if (errBody.containsKey("error")) {
    //            JSONObject errDetail = errBody.getJSONObject("error");
    //            String errMessage = errDetail.getString("message");
    //            String errType = errDetail.getString("type");
    //            if (org.apache.commons.lang.StringUtils.isNotEmpty(errMessage)) {
    //                throw TisException.create(
    //                        String.format("Anthropic API Error [%s]: %s", errType, errMessage));
    //            }
    //        } else if (errBody.containsKey("message")) {
    //            String errMessage = errBody.getString("message");
    //            throw TisException.create("Anthropic API Error: " + errMessage);
    //        }
    //    }

    protected abstract List<ConfigFileContext.Header> appendHeaders();

    protected abstract URL getApiUrl();

    protected abstract String getModel();

    /**
     * 调用LLM进行JSON格式化输出
     *
     * @param prompt          提示词
     * @param systemPrompt    系统提示词
     * @param jsonSchema      JSON Schema定义
     * @param LLMOptionParams 内设置的参数可覆盖LLMProvider中定义的属性如stream参数
     * @return 生成的JSON对象和token使用情况
     * @see DeepSeekProvider#chatJson(String, List, String)
     */
    //    public abstract LLMResponse chatJson(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
    //                                         ITISJsonSchema jsonSchema, LLMOptionParams params);
    public final LLMResponse chatJson(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
                                      ITISJsonSchema jsonSchema) {
        return this.chatJson(context, prompt, systemPrompt, jsonSchema, new LLMOptionParams());
    }

    /**
     * 获取提供商名称
     */
    public abstract String getProviderName();

    /**
     * 检查是否可用
     */
    public abstract boolean isAvailable();


    /**
     * 是否支持json schema在在参数设置上，如果支持的话就不需要在user prompt上设置json schema了
     *
     * @return
     */
    protected boolean supportJsonSchemaOnParamsSetting() {
        return false;
    }

    /**
     * 调用LLM进行JSON格式化输出
     *
     * @param prompt          提示词
     * @param systemPrompt    系统提示词
     * @param jsonSchema      JSON Schema定义
     * @param LLMOptionParams 内设置的参数可覆盖LLMProvider中定义的属性如stream参数
     * @return 生成的JSON对象和token使用情况
     * @see DeepSeekProvider#chatJson(String, List, String)
     */
    public LLMResponse chatJson(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
                                ITISJsonSchema jsonSchema, LLMOptionParams params) {
        // 增强 prompt，要求返回 JSON 格式（与QWenLLMProvider保持一致）
        StringBuilder enhancedPrompt = new StringBuilder(prompt.getPrompt());
        if (jsonSchema.isContainSchema()) {
            enhancedPrompt.append("\n\n重要：请确保返回的是有效的JSON格式，不要包含markdown标记或其他文本。");
            enhancedPrompt.append("\n请务必严格按照 'response_format' 中定义的 JSON Schema "
                    + "格式输出，不要输出任何其他内容或格式。以下是对'response_format'JSON Schema说明：\n");
            if (this.supportJsonSchemaOnParamsSetting()) {
                jsonSchema.appendFieldDescToPrompt(enhancedPrompt);
            } else {
                enhancedPrompt.append(jsonSchema.root());
            }
            enhancedPrompt.append("\n\n**注意**：分析用户输入内容必须遵守如下纪律：");
            enhancedPrompt.append("\n**默认值处理**：");
            enhancedPrompt.append("\n- 对于填充的字段内容，有以下要求：");
            enhancedPrompt.append("\na) 如果用户提供了对应信息 → 按 schema 要求处理（如替换非法字符以符合 `" + SCHEMA_VALUE_PATTERN + "`）；");
            enhancedPrompt.append("\nb) 如果用户**未提供**，但相应属性中定义了 `" + SCHEMA_VALUE_DEFAULT + "`属性 → **必须使用该 " + SCHEMA_VALUE_DEFAULT + " 值**`");
            enhancedPrompt.append("\nc) 如果用户未提供，且 相应属性 中**无 " + SCHEMA_VALUE_DEFAULT + "** → 填入空字符串 `\"\"`。");
        } else {
            enhancedPrompt.append("\n\n请以JSON格式返回结果，只返回JSON，不要包含其他说明文字。");
        }

        LLMResponse response = chat(context, prompt.setNewPrompt(enhancedPrompt.toString()), systemPrompt,
                this.printLog,
                jsonSchema, params);

        // 解析 JSON 内容
        if (response.isSuccess() //
                && (!isStreamOutput(params)) //
                && org.apache.commons.lang.StringUtils.isNotEmpty(response.getContent())) {
            String content = org.apache.commons.lang.StringUtils.trim(response.getContent());//.trim();
            //            // 去除 markdown 代码块
            //            if (content.startsWith("```json")) {
            //                content = content.substring(7);
            //                int endIdx = content.lastIndexOf("```");
            //                if (endIdx >= 0) {
            //                    content = content.substring(0, endIdx);
            //                }
            //            } else if (content.startsWith("```")) {
            //                content = content.substring(3);
            //                int endIdx = content.lastIndexOf("```");
            //                if (endIdx >= 0) {
            //                    content = content.substring(0, endIdx);
            //                }
            //            }
            //            content = content.trim();
            //try {
            response.setJsonContent(parseJsonObjectFromLLMResponse(content));
            //            } catch (Exception e) {
            //                this.getLogger().warn("Failed to parse JSON response: {}", content, e);
            //            }
        }

        return response;
    }

    public static JSONObject parseJsonObjectFromLLMResponse(String content) {
        if (StringUtils.isEmpty(content)) {
            throw new IllegalArgumentException("illegal param content can not be empty");
        }
        // 去除 markdown 代码块
        if (content.startsWith("```json")) {
            content = content.substring(7);
            int endIdx = content.lastIndexOf("```");
            if (endIdx >= 0) {
                content = content.substring(0, endIdx);
            }
        } else if (content.startsWith("```")) {
            content = content.substring(3);
            int endIdx = content.lastIndexOf("```");
            if (endIdx >= 0) {
                content = content.substring(0, endIdx);
            }
        }
        try {
            return JSON.parseObject(StringUtils.trim(content));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理增量执行内容
     *
     * @param stream
     * @param deltaContent
     * @throws IOException
     */
    private static void processStreamResponse(InputStream stream,
                                              Consumer<JSONObject> deltaContent) throws IOException {
        try (BufferedReader buffer = IOUtils.buffer(new InputStreamReader(stream, TisUTF8.get()))) {
            buffer.lines().forEach((line) -> {
                if (StringUtils.isEmpty(line) || "data: [DONE]".equals(line)) {
                    return;
                }
                try {
                    JSONObject data = JSONObject.parseObject(SSEEventWriter.getDataContent(line));
                    if (data == null) {
                        return;
                    }
                    deltaContent.accept(data);

                } catch (Exception e) {
                    throw new RuntimeException(line, e);
                }
            });
        }
    }


    /**
     * LLM响应结果
     */
    public static class LLMResponse {
        private String content;
        private JSONObject jsonContent;
        private long promptTokens;
        private long completionTokens;
        //private long totalTokens;
        private String model;
        private boolean success;
        private String errorMessage;

        public final ExecuteLog executeLog;

        //        public LLMResponse() {
        //            this(new NoneExecuteLog());
        //        }

        public LLMResponse(ExecuteLog executeLog) {
            this.executeLog = executeLog;
        }

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

        //        public long getTotalTokens() {
        //            return totalTokens;
        //        }
        //
        //        public void setTotalTokens(long totalTokens) {
        //            this.totalTokens = totalTokens;
        //        }

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
