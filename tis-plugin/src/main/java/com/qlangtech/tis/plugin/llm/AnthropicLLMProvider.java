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

package com.qlangtech.tis.plugin.llm;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.llm.ITISJsonSchema;
import com.qlangtech.tis.aiagent.llm.LLMOptionParams;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.llm.log.ExecuteLog;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_VALUE_DEFAULT;
import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_VALUE_PATTERN;

/**
 * Anthropic Claude 大模型Provider实现<br/>
 * API文档：<a href="https://docs.anthropic.com/en/api/messages">...</a><br/>
 * <p>
 * API Key 管理页面<br/>
 * <a href="https://console.anthropic.com/settings/keys">...</a>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/27
 */

public class AnthropicLLMProvider extends LLMProvider {
    private static final Logger logger = LoggerFactory.getLogger(AnthropicLLMProvider.class);

    private static final String URL_PATH = "/v1/messages";
    public static final String DEFAULT_MODEL = "claude-sonnet-4-6";
    public static final String DEFAULT_BASE_URL = "https://api.anthropic.com";
    /**
     * Anthropic API 版本
     */
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {Validator.require,
            Validator.identity})
    public String name;

    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 1, validate = {Validator.require, Validator.url})
    public String baseUrl;

    @FormField(type = FormFieldType.PASSWORD, ordinal = 2, validate = {Validator.require})
    public String apiKey;

    @FormField(type = FormFieldType.INT_NUMBER, ordinal = 3, validate = {Validator.require, Validator.integer})
    public Integer maxTokens;

    /**
     * Anthropic Claude 模型列表
     * <a href="https://docs.anthropic.com/en/docs/about-claude/models">...</a>
     */
    @FormField(type = FormFieldType.ENUM, ordinal = 4, validate = {Validator.require})
    public String model;

//    @FormField(type = FormFieldType.DECIMAL_NUMBER, advance = true, ordinal = 6, validate = {Validator.require})
//    public Sampling sampling;


    //    @Override
    //    public LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt) {
    //        return chat(context, prompt, systemPrompt, true, TISJsonSchema.off());
    //    }
    //
    //    public LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt, boolean
    //    logSummary,
    //                            ITISJsonSchema jsonOutput) {
    //        return chat(context, prompt, systemPrompt, logSummary, jsonOutput, new LLMOptionParams());
    //    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Integer getMaxTokens() {
        return this.maxTokens;
    }

    @Override
    protected void addCustomizeParams(ITISJsonSchema jsonOutput, List<String> systemPrompt, LLMOptionParams params,
                                      List<HttpUtils.PostParam> postParams) {
        // 采样参数
       // params.setSamplingParams(sampling, postParams);

        //            if (sampling != null) {
        //                sampling.setHttpParams(postParams);
        //            }
        final boolean streamOutput = params.getStreamOutput() != null ?
                params.getStreamOutput() : stream;
        postParams.add(new HttpUtils.PostParam("stream", streamOutput));

        // Anthropic API: 系统提示放在顶级 "system" 字段
        if (CollectionUtils.isNotEmpty(systemPrompt)) {
            String combinedSystem = String.join("\n\n", systemPrompt);
            postParams.add(new HttpUtils.PostParam("system", combinedSystem));
        }
    }

    @Override
    protected void addMessage(UserPrompt prompt, List<String> systemPrompt, List<HttpUtils.PostParam> postParams) {
        super.addMessage(prompt, Collections.emptyList(), postParams);
    }

    @Override
    protected TokenUsageSummary getTokenUsageSummary(JSONObject responseJson) {
        // token 使用统计
        if (responseJson.containsKey("usage")) {
            JSONObject usage = responseJson.getJSONObject("usage");
            long inputTokens = usage.getLongValue("input_tokens");
            long outputTokens = usage.getLongValue("output_tokens");
            //            response.setPromptTokens(inputTokens);
            //            response.setCompletionTokens(outputTokens);
            //            context.updateTokenUsage(inputTokens + outputTokens);
            return new TokenUsageSummary(inputTokens, outputTokens);
        }
        return null;
    }

    @Override
    protected boolean processResponseJson(LLMResponse response, JSONObject responseJson) {
        if (responseJson.containsKey("error")) {
            Object error = responseJson.get("error");
            response.setErrorMessage(JsonUtil.toString(error, true));
            return false;
        }

        // 检查错误
        if ("error".equals(responseJson.getString("type"))) {
            JSONObject errDetail = responseJson.getJSONObject("error");
            response.setSuccess(false);
            if (errDetail != null) {
                response.setErrorMessage(errDetail.getString("type") + ": " + errDetail.getString(
                        "message"));
            } else {
                response.setErrorMessage(JsonUtil.toString(responseJson, false));
            }
            return false;
        }

        // stop_reason 检查
        String stopReason = responseJson.getString("stop_reason");
        if ("max_tokens".equals(stopReason)) {
            logger.warn("Response was truncated due to max_tokens limit");
            response.setErrorMessage("Response was truncated due to max_tokens limit");
            return false;
        }

        response.setModel(responseJson.getString("model"));

        return true;
    }

    @Override
    protected void processErrorResponseBody(int status, IOException e, JSONObject errBody) {
        // Anthropic 错误格式: { "type": "error", "error": { "type": "...", "message": "..." } }
        if (errBody.containsKey("error")) {
            JSONObject errDetail = errBody.getJSONObject("error");
            String errMessage = errDetail.getString("message");
            String errType = errDetail.getString("type");
            if (StringUtils.isNotEmpty(errMessage)) {
                throw TisException.create(
                        String.format("Anthropic API Error [%s]: %s", errType, errMessage));
            }
        } else if (errBody.containsKey("message")) {
            String errMessage = errBody.getString("message");
            throw TisException.create("Anthropic API Error: " + errMessage);
        }
    }

    @Override
    protected List<ConfigFileContext.Header> appendHeaders() {
        return Lists.newArrayList(
                new ConfigFileContext.Header("x-api-key", getApiKey()),
                new ConfigFileContext.Header("anthropic-version", ANTHROPIC_VERSION),
                new ConfigFileContext.Header("Content-Type", "application/json"));
    }

    //    public LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt, boolean
    //    logSummary,
    //                            ITISJsonSchema jsonOutput, LLMOptionParams params) {
    //        ExecuteLog executeLog = ExecuteLog.create(this.printLog, prompt, context, logger);
    //        try {
    //            // 构建请求参数
    //            List<HttpUtils.PostParam> postParams = new ArrayList<>();
    //            postParams.add(new HttpUtils.PostParam("model", getModel()));
    //            postParams.add(new HttpUtils.PostParam("max_tokens",
    //                    Objects.requireNonNull(maxTokens, "maxTokens can not be null")));
    //
    //            // Anthropic API: 系统提示放在顶级 "system" 字段
    //            if (CollectionUtils.isNotEmpty(systemPrompt)) {
    //                String combinedSystem = String.join("\n\n", systemPrompt);
    //                postParams.add(new HttpUtils.PostParam("system", combinedSystem));
    //            }
    //
    //            // 用户消息
    //            JSONArray messages = new JSONArray();
    //            JSONObject userMessage = new JSONObject();
    //            userMessage.put("role", "user");
    //            userMessage.put("content", prompt.getPrompt());
    //            messages.add(userMessage);
    //            postParams.add(new HttpUtils.PostParam("messages", messages));
    //
    //            // JSON 输出模式：通过提示增强实现
    //            if (jsonOutput.isContainSchema()) {
    //                // Anthropic 不支持原生 response_format JSON 模式，使用提示引导
    //                // （已在 chatJson 中增强 prompt，此处无需再次添加 schema 提示）
    //            }
    //            // 采样参数
    //            params.setSamplingParams(sampling, postParams);
    //
    //            //            if (sampling != null) {
    //            //                sampling.setHttpParams(postParams);
    //            //            }
    //            final boolean streamOutput = params.getStreamOutput() != null ?
    //                    params.getStreamOutput() : stream;
    //            postParams.add(new HttpUtils.PostParam("stream", streamOutput));
    //
    //            executeLog.setPostParams(postParams);
    //
    //            return HttpUtils.post(new URL(getApiUrl()), postParams, new PostFormStreamProcess<LLMResponse>(
    //                    Lists.newArrayList(
    //                            new ConfigFileContext.Header("x-api-key", getApiKey()),
    //                            new ConfigFileContext.Header("anthropic-version", ANTHROPIC_VERSION),
    //                            new ConfigFileContext.Header("Content-Type", "application/json"))) {
    //                @Override
    //                public ContentType getContentType() {
    //                    return ContentType.JSON;
    //                }
    //
    //                @Override
    //                public int getMaxRetry() {
    //                    return Objects.requireNonNull(maxRetry, "maxRetry can not be null");
    //                }
    //
    //                @Override
    //                public Duration getSocketReadTimeout() {
    //                    return readTimeout;
    //                }
    //
    //                @Override
    //                public void error(int status, InputStream errstream, IOException e) {
    //                    if (errstream != null) {
    //                        try {
    //                            String errContent = IOUtils.toString(errstream, TisUTF8.get());
    //                            JSONObject errBody = null;
    //                            try {
    //                                errBody = JSONObject.parseObject(errContent);
    //                                executeLog.setError(errBody);
    //                            } catch (Exception ex) {
    //                                throw TisException.create("API Error: " + errContent);
    //                            }
    //
    //                            // Anthropic 错误格式: { "type": "error", "error": { "type": "...", "message": "..." } }
    //                            if (errBody.containsKey("error")) {
    //                                JSONObject errDetail = errBody.getJSONObject("error");
    //                                String errMessage = errDetail.getString("message");
    //                                String errType = errDetail.getString("type");
    //                                if (StringUtils.isNotEmpty(errMessage)) {
    //                                    throw TisException.create(
    //                                            String.format("Anthropic API Error [%s]: %s", errType, errMessage));
    //                                }
    //                            } else if (errBody.containsKey("message")) {
    //                                String errMessage = errBody.getString("message");
    //                                throw TisException.create("Anthropic API Error: " + errMessage);
    //                            }
    //                        } catch (IOException ex) {
    //                            throw new RuntimeException(e);
    //                        }
    //                    } else {
    //                        throw new RuntimeException(e);
    //                    }
    //                }
    //
    //                @Override
    //                public LLMResponse p(int status, InputStream stream, Map headerFields) throws IOException {
    //                    LLMResponse response = new LLMResponse(executeLog);
    //                    if (streamOutput) {
    //                        processStreamResponse(stream, (data) -> {
    //                            /**
    //                             * <pre>
    //                             * {
    //                             * 	"message": {
    //                             * 		"content": [],
    //                             * 		"id": "msg_iiqAJbCCvP028M2oSiw13zlM",
    //                             * 		"model": "claude-sonnet-4-6",
    //                             * 		"role": "assistant",
    //                             * 		"stop_reason": null,
    //                             * 		"stop_sequence": null,
    //                             * 		"type": "message",
    //                             * 		"usage": {
    //                             * 			"cache_creation_input_tokens": 0,
    //                             * 			"cache_read_input_tokens": 1403,
    //                             * 			"input_tokens": 646,
    //                             * 			"output_tokens": 0
    //                             *                }    },
    //                             * 	"type": "message_start"
    //                             * }
    //                             * </pre>
    //                             * <pre>
    //                             * {
    //                             *   "delta" : {
    //                             *     "text" : "\n{\n  \"linkTypes\": [\n  ",
    //                             *     "type" : "text_delta"
    //                             *   },
    //                             *   "index" : 0,
    //                             *   "type" : "content_block_delta"
    //                             * }
    //                             * </pre>
    //                             */
    //                            String deltaContent;
    //                            if ("content_block_delta".equals(data.getString("type"))) {
    //                                JSONObject delta = data.getJSONObject("delta");
    //                                if (delta != null && "text_delta".equals(delta.getString("type"))) {
    //                                    deltaContent = delta.getString("text");
    //                                    if (StringUtils.isNotEmpty(deltaContent)) {
    //                                        Objects.requireNonNull(params.getStreamOutputConsumer()
    //                                                , "streamOutputConsumer can not be null").accept(deltaContent);
    //                                    }
    //                                }
    //                            }
    //                        });
    //                        response.setSuccess(true);
    //                        return response;
    //                    }
    //                    String responseStr = IOUtils.toString(stream, TisUTF8.get());
    //                    JSONObject responseJson = JSON.parseObject(responseStr);
    //                    /**
    //                     * {
    //                     *   "error" : {
    //                     *     "code" : "",
    //                     *     "message" : "Invalid token (request id: 202606170629416373795898268d9d67xqimk9A)",
    //                     *     "type" : "new_api_error"
    //                     *   }
    //                     * }
    //                     */
    //
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
    //                            response.setErrorMessage(errDetail.getString("type") + ": " + errDetail.getString(
    //                                    "message"));
    //                        } else {
    //                            response.setErrorMessage(responseStr);
    //                        }
    //                        return response;
    //                    }
    //
    //                    executeLog.setResponse(responseJson);
    //
    //                    StringBuilder content = getContent(responseJson);
    //                    if (content != null) {
    //                        response.setContent(content.toString());
    //                        response.setSuccess(true);
    //                    }
    //
    //                    // stop_reason 检查
    //                    String stopReason = responseJson.getString("stop_reason");
    //                    if ("max_tokens".equals(stopReason)) {
    //                        logger.warn("Response was truncated due to max_tokens limit");
    //                    }
    //
    //                    // token 使用统计
    //                    if (responseJson.containsKey("usage")) {
    //                        JSONObject usage = responseJson.getJSONObject("usage");
    //                        long inputTokens = usage.getLongValue("input_tokens");
    //                        long outputTokens = usage.getLongValue("output_tokens");
    //                        response.setPromptTokens(inputTokens);
    //                        response.setCompletionTokens(outputTokens);
    //                        context.updateTokenUsage(inputTokens + outputTokens);
    //                    }
    //
    //                    response.setModel(responseJson.getString("model"));
    //                    return response;
    //                }
    //            });
    //        } catch (MalformedURLException e) {
    //            throw new RuntimeException("Invalid URL: " + getApiUrl(), e);
    //        } finally {
    //            if (logSummary) {
    //                executeLog.summary();
    //            }
    //        }
    //    }


    @Override
    protected StringBuilder getResponseBodyContent(JSONObject responseJson) {
        if (responseJson.containsKey("content")) {
            JSONArray contentArray = responseJson.getJSONArray("content");
            if (contentArray != null && !contentArray.isEmpty()) {
                // 收集所有 text 块
                StringBuilder textBuilder = new StringBuilder();
                for (int i = 0; i < contentArray.size(); i++) {
                    JSONObject block = contentArray.getJSONObject(i);
                    if ("text".equals(block.getString("type"))) {
                        textBuilder.append(block.getString("text"));
                    }
                }
                //                response.setContent(textBuilder.toString());
                //                response.setSuccess(true);
                return textBuilder;
            }
        }
        return null;
    }



    @Override
    protected Consumer<JSONObject> getDeltaContentConsumer(LLMOptionParams params) {
        return (data) -> {
            /**
             * <pre>
             * {
             * 	"message": {
             * 		"content": [],
             * 		"id": "msg_iiqAJbCCvP028M2oSiw13zlM",
             * 		"model": "claude-sonnet-4-6",
             * 		"role": "assistant",
             * 		"stop_reason": null,
             * 		"stop_sequence": null,
             * 		"type": "message",
             * 		"usage": {
             * 			"cache_creation_input_tokens": 0,
             * 			"cache_read_input_tokens": 1403,
             * 			"input_tokens": 646,
             * 			"output_tokens": 0
             *                }    },
             * 	"type": "message_start"
             * }
             * </pre>
             * <pre>
             * {
             *   "delta" : {
             *     "text" : "\n{\n  \"linkTypes\": [\n  ",
             *     "type" : "text_delta"
             *   },
             *   "index" : 0,
             *   "type" : "content_block_delta"
             * }
             * </pre>
             */
            String deltaContent;
            if ("content_block_delta".equals(data.getString("type"))) {
                JSONObject delta = data.getJSONObject("delta");
                if (delta != null && "text_delta".equals(delta.getString("type"))) {
                    deltaContent = delta.getString("text");
                    if (org.apache.commons.lang.StringUtils.isNotEmpty(deltaContent)) {
                        Objects.requireNonNull(params.getStreamOutputConsumer()
                                , "streamOutputConsumer can not be null").accept(deltaContent);
                    }
                }
            }
        };
    }

//    @Override
//    public LLMResponse chatJson(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
//                                ITISJsonSchema jsonSchema, LLMOptionParams params) {
//        // 增强 prompt，要求返回 JSON 格式（与QWenLLMProvider保持一致）
//        StringBuilder enhancedPrompt = new StringBuilder(prompt.getPrompt());
//        if (jsonSchema.isContainSchema()) {
//            enhancedPrompt.append("\n\n重要：请确保返回的是有效的JSON格式，不要包含markdown标记或其他文本。");
//            enhancedPrompt.append("\n请务必严格按照 'response_format' 中定义的 JSON Schema "
//                    + "格式输出，不要输出任何其他内容或格式。以下是对'response_format'JSON Schema说明：\n");
//            //jsonSchema.appendFieldDescToPrompt(enhancedPrompt);
//            enhancedPrompt.append(jsonSchema.root());
//            enhancedPrompt.append("\n\n**注意**：分析用户输入内容必须遵守如下纪律：");
//            enhancedPrompt.append("\n**默认值处理**：");
//            enhancedPrompt.append("\n- 对于填充的字段内容，有以下要求：");
//            enhancedPrompt.append("\na) 如果用户提供了对应信息 → 按 schema 要求处理（如替换非法字符以符合 `" + SCHEMA_VALUE_PATTERN + "`）；");
//            enhancedPrompt.append("\nb) 如果用户**未提供**，但相应属性中定义了 `" + SCHEMA_VALUE_DEFAULT + "`属性 → **必须使用该 " + SCHEMA_VALUE_DEFAULT + " 值**`");
//            enhancedPrompt.append("\nc) 如果用户未提供，且 相应属性 中**无 " + SCHEMA_VALUE_DEFAULT + "** → 填入空字符串 `\"\"`。");
//        } else {
//            enhancedPrompt.append("\n\n请以JSON格式返回结果，只返回JSON，不要包含其他说明文字。");
//        }
//
//        LLMResponse response = chat(context, prompt.setNewPrompt(enhancedPrompt.toString()), systemPrompt,
//                this.printLog,
//                jsonSchema, params);
//
//        // 解析 JSON 内容
//        if (response.isSuccess() && !params.getStreamOutput() && StringUtils.isNotEmpty(response.getContent())) {
//            String content = StringUtils.trim(response.getContent());//.trim();
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
//            try {
//                response.setJsonContent(JSON.parseObject(content));
//            } catch (Exception e) {
//                logger.warn("Failed to parse JSON response: {}", content, e);
//            }
//        }
//
//        return response;
//    }

    @Override
    public String getProviderName() {
        return "Anthropic";
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.isNotEmpty(apiKey) && StringUtils.isNotEmpty(baseUrl);
    }

    @Override
    public URL getApiUrl() {
        try {
            return new URL((StringUtils.isEmpty(baseUrl) ? DEFAULT_BASE_URL : baseUrl) + URL_PATH);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getModel() {
        return StringUtils.isEmpty(model) ? DEFAULT_MODEL : model;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String identityValue() {
        return name;
    }

    @Override
    public LLMProvider createConfigInstance() {
        return this;
    }

    @TISExtension
    public static final class DefaultDescriptor extends BasicParamsConfigDescriptor implements IEndTypeGetter {
        public DefaultDescriptor() {
            super(KEY_DISPLAY_NAME);
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            AnthropicLLMProvider anthropic = postFormVals.newInstance();

            try {
                LLMResponse chat = anthropic.chat(IAgentContext.createNull(), new UserPrompt("test", "hello"), null);
                if (!chat.isSuccess()) {
                    msgHandler.addErrorMessage(context, chat.getErrorMessage());
                    return false;
                }
            } catch (Exception e) {
                msgHandler.addErrorMessage(context, e.getMessage());
                return false;
            }
            return super.verify(msgHandler, context, postFormVals);
        }

        public boolean validateMaxTokens(IFieldErrorHandler msgHandler, Context context, String fieldName,
                                         String value) {
            int tokens = Integer.parseInt(value);
            int min = 1;
            int max = 32768;
            if (tokens < min || tokens > max) {
                msgHandler.addFieldError(context, fieldName, "必须在：" + min + "至" + max + "之间");
                return false;
            }
            return true;
        }

        public boolean validateMaxRetry(IFieldErrorHandler msgHandler, Context context, String fieldName,
                                        String value) {
            int retryCount = Integer.parseInt(value);
            if (retryCount < 1) {
                msgHandler.addFieldError(context, fieldName, "不能小于1");
                return false;
            }
            if (retryCount > 3) {
                msgHandler.addFieldError(context, fieldName, "不能大于3");
                return false;
            }
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Anthropic";
        }

        @Override
        public EndType getEndType() {
            return EndType.Anthropic;
        }

        public boolean validateModel(IFieldErrorHandler msgHandler, Context context, String fieldName,
                                     String value) {
            return true;
        }


    }
}