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
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.llm.impl.qwen.sampling.TemperatureSampling;
import com.qlangtech.tis.plugin.llm.impl.qwen.sampling.TopPSampling;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_VALUE_DEFAULT;
import static com.qlangtech.tis.aiagent.llm.TISJsonSchema.SCHEMA_VALUE_PATTERN;

/**
 * 智谱AI大模型Provider实现<br/>
 * API文档：<a href="https://open.bigmodel.cn/dev/api">智谱AI开放平台</a><br/>
 * <a href="https://docs.bigmodel.cn/api-reference/%E6%A8%A1%E5%9E%8B-api/%E5%AF%B9%E8%AF%9D%E8%A1%A5%E5%85%A8">...</a>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/18
 */
@TISExtension
public class ZhipuLLMProvider extends LLMProvider {
    private static final Logger logger = LoggerFactory.getLogger(ZhipuLLMProvider.class);

    public static final String DEFAULT_MODEL = "glm-5.2";
    public static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {Validator.require,
            Validator.identity})
    public String name;

    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 1, validate = {Validator.require, Validator.url})
    public String baseUrl;

    @FormField(type = FormFieldType.PASSWORD, ordinal = 2, validate = {Validator.require})
    public String apiKey;

    @FormField(type = FormFieldType.INT_NUMBER, ordinal = 3, validate = {Validator.require, Validator.integer})
    public Integer maxTokens;

    @FormField(type = FormFieldType.ENUM, ordinal = 4, validate = {Validator.require})
    public String model;

    /**
     * 使用混合思考模型时，是否开启思考模式
     */
    @FormField(type = FormFieldType.ENUM, ordinal = 5, validate = {Validator.require})
    public Boolean thinking;

//    @FormField(advance = true, ordinal = 6, validate = {Validator.require})
//    public Sampling sampling;

    /**
     * 是否开启流式输出
     */
    public final Boolean stream = false;

    public static List<Descriptor> filter(List<Descriptor> enumPropDescs) {
        return enumPropDescs.stream().filter((desc) ->
                desc instanceof TemperatureSampling.DefaultDesc || desc instanceof TopPSampling.DefaultDesc
        ).collect(Collectors.toList());
    }
    //    /**
    //     * 是否打印日志
    //     */
    //    @FormField(type = FormFieldType.ENUM, advance = true, ordinal = 9, validate = {Validator.require})
    //    public Boolean printLog;

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
    protected void addCustomizeParams(ITISJsonSchema jsonOutput, List<String> systemPrompt, LLMOptionParams params,
                                      List<HttpUtils.PostParam> postParams) {
        // 设置采样参数
       // Objects.requireNonNull(this.sampling, "sampling can not be null").setHttpParams(postParams);

        // 设置思考模式
        if (this.thinking != null && this.thinking) {
            JSONObject thinkingConfig = new JSONObject();
            thinkingConfig.put("type", "enabled");
            postParams.add(new HttpUtils.PostParam("thinking", thinkingConfig));
        }

        // 设置do_sample为true以启用采样
        postParams.add(new HttpUtils.PostParam("do_sample", true));

        // 如果需要JSON输出
        if (jsonOutput.isContainSchema()) {
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");
            postParams.add(new HttpUtils.PostParam("response_format", responseFormat));
        }
    }

    @Override
    protected TokenUsageSummary getTokenUsageSummary(JSONObject responseJson) {
        // 处理token使用统计
        if (responseJson.containsKey("usage")) {
            JSONObject usage = responseJson.getJSONObject("usage");
            //            response.setPromptTokens(usage.getLongValue("prompt_tokens"));
            //            response.setCompletionTokens(usage.getLongValue("completion_tokens"));
            //            context.updateTokenUsage(usage.getLongValue("total_tokens"));
            return new TokenUsageSummary(usage.getLongValue("prompt_tokens"), usage.getLongValue("completion_tokens"));
        }
        return null;
    }

    @Override
    protected boolean processResponseJson(LLMResponse response, JSONObject responseJson) {
        JSONObject error = null;
        if ((error = responseJson.getJSONObject("error")) != null) {
            response.setSuccess(false);
            response.setErrorMessage(error.entrySet().stream()
                    .map((entry) -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(
                            ",")));
            return false;
        }
        return true;
    }

    @Override
    protected StringBuilder getResponseBodyContent(JSONObject responseJson) {
        // 处理智谱AI的响应格式
        if (responseJson.containsKey("choices")) {
            JSONArray choices = responseJson.getJSONArray("choices");
            if (!choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                //                response.setContent(message.getString("content"));
                //                response.setSuccess(true);

                // 获取finish_reason
                String finishReason = choice.getString("finish_reason");
                if ("length".equals(finishReason)) {
                    logger.warn("Response was truncated due to max_tokens limit");
                }

                return new StringBuilder(message.getString("content"));


            }
        }
        return null;
    }

    @Override
    protected Consumer<JSONObject> getDeltaContentConsumer(LLMOptionParams params) {
        return (data) -> {
            JSONArray choices = data.getJSONArray("choices");
            for (Object c : choices) {
                if (c instanceof JSONObject choice) {
                    String content = choice.getJSONObject("delta").getString("content");
                    if (content != null) {
                        params.getStreamOutputConsumer().accept(content);
                    }
                }
            }
        };
    }

    @Override
    protected void processErrorResponseBody(int status, IOException e, JSONObject errBody) {
        // 智谱AI的错误格式
        if (errBody.containsKey("error")) {
            JSONObject errDetail = errBody.getJSONObject("error");
            String errMessage = errDetail.getString("message");
            String errCode = errDetail.getString("code");
            if (StringUtils.isNotEmpty(errMessage)) {
                throw TisException.create(String.format("Zhipu API Error [%s]: %s", errCode,
                        errMessage));
            }
        } else if (errBody.containsKey("message")) {
            String errMessage = errBody.getString("message");
            throw TisException.create("Zhipu API Error: " + errMessage);
        }
    }

    @Override
    protected List<ConfigFileContext.Header> appendHeaders() {
        return Lists.newArrayList((new ConfigFileContext.Header("Authorization", "Bearer " + getApiKey())),
                (new ConfigFileContext.Header("Content-Type", "application/json")));
    }


//    public LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt, boolean logSummary,
//                            ITISJsonSchema jsonOutput, LLMOptionParams params) {
//        ExecuteLog executeLog = ExecuteLog.create(this.printLog, prompt, context, logger);
//        try {
//            List<HttpUtils.PostParam> postParams = new ArrayList<>();
//            postParams.add(new HttpUtils.PostParam("model", getModel()));
//
//            // 设置消息列表
//            JSONArray messages = new JSONArray();
//            if (CollectionUtils.isNotEmpty(systemPrompt)) {
//                for (String sysP : systemPrompt) {
//                    JSONObject systemMessage = new JSONObject();
//                    systemMessage.put("role", "system");
//                    systemMessage.put("content", sysP);
//                    messages.add(systemMessage);
//                }
//            }
//
//            JSONObject userMessage = new JSONObject();
//            userMessage.put("role", "user");
//            userMessage.put("content", prompt.getPrompt());
//            messages.add(userMessage);
//            postParams.add(new HttpUtils.PostParam("messages", messages));
//
//            // 设置采样参数
//            Objects.requireNonNull(this.sampling, "sampling can not be null").setHttpParams(postParams);
//
//            // 设置思考模式
//            if (this.thinking != null && this.thinking) {
//                JSONObject thinkingConfig = new JSONObject();
//                thinkingConfig.put("type", "enabled");
//                postParams.add(new HttpUtils.PostParam("thinking", thinkingConfig));
//            }
//            // 设置do_sample为true以启用采样
//            postParams.add(new HttpUtils.PostParam("do_sample", true));
//            // 设置最大token数
//            postParams.add(new HttpUtils.PostParam("max_tokens", getMaxTokens()));
//
//
//            // 如果需要JSON输出
//            if (jsonOutput.isContainSchema()) {
//                JSONObject responseFormat = new JSONObject();
//                responseFormat.put("type", "json_object");
//                postParams.add(new HttpUtils.PostParam("response_format", responseFormat));
//            }
//
//            final boolean streamOutput = params.getStreamOutput() != null ?
//                    params.getStreamOutput() : stream;
//            postParams.add(new HttpUtils.PostParam("stream", streamOutput));
//
//            executeLog.setPostParams(postParams);
//
//            return HttpUtils.post(new URL(getApiUrl()), postParams, new PostFormStreamProcess<LLMResponse>(
//                    Lists.newArrayList((new ConfigFileContext.Header("Authorization", "Bearer " + getApiKey())),
//                            (new ConfigFileContext.Header("Content-Type", "application/json")))) {
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
//                            // 智谱AI的错误格式
//                            if (errBody.containsKey("error")) {
//                                JSONObject errDetail = errBody.getJSONObject("error");
//                                String errMessage = errDetail.getString("message");
//                                String errCode = errDetail.getString("code");
//                                if (StringUtils.isNotEmpty(errMessage)) {
//                                    throw TisException.create(String.format("Zhipu API Error [%s]: %s", errCode,
//                                            errMessage));
//                                }
//                            } else if (errBody.containsKey("message")) {
//                                String errMessage = errBody.getString("message");
//                                throw TisException.create("Zhipu API Error: " + errMessage);
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
//
//                    if (streamOutput) {
//                        return processStreamResponseWithDeepSeekStyle(stream, response, params);
//                    }
//
//                    String responseStr = IOUtils.toString(stream, TisUTF8.get());
//
//                    JSONObject responseJson = JSON.parseObject(responseStr);
//                    JSONObject error = null;
//                    if ((error = responseJson.getJSONObject("error")) != null) {
//                        response.setSuccess(false);
//                        response.setErrorMessage(error.entrySet().stream()
//                                .map((entry) -> entry.getKey() + ":" + entry.getValue()).collect(Collectors.joining(
//                                        ",")));
//                        return response;
//                    }
//                    executeLog.setResponse(responseJson);
//
//                    // 处理智谱AI的响应格式
//                    if (responseJson.containsKey("choices")) {
//                        JSONArray choices = responseJson.getJSONArray("choices");
//                        if (!choices.isEmpty()) {
//                            JSONObject choice = choices.getJSONObject(0);
//                            JSONObject message = choice.getJSONObject("message");
//                            response.setContent(message.getString("content"));
//                            response.setSuccess(true);
//
//                            // 获取finish_reason
//                            String finishReason = choice.getString("finish_reason");
//                            if ("length".equals(finishReason)) {
//                                logger.warn("Response was truncated due to max_tokens limit");
//                            }
//                        }
//                    }
//
//                    // 处理token使用统计
//                    if (responseJson.containsKey("usage")) {
//                        JSONObject usage = responseJson.getJSONObject("usage");
//                        response.setPromptTokens(usage.getLongValue("prompt_tokens"));
//                        response.setCompletionTokens(usage.getLongValue("completion_tokens"));
//                        context.updateTokenUsage(usage.getLongValue("total_tokens"));
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

//    @Override
//    public LLMResponse chatJson(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
//                                ITISJsonSchema jsonSchema, LLMOptionParams params) {
//        // 增强prompt，要求返回JSON格式
//        StringBuilder enhancedPrompt = new StringBuilder(prompt.getPrompt());
//        if (jsonSchema.isContainSchema()) {
//            enhancedPrompt.append("\n\n重要：请确保返回的是有效的JSON格式，不要包含markdown标记或其他文本。");
//            enhancedPrompt.append("\n请务必严格按照以下 JSON Schema 格式输出，不要输出任何其他内容或格式。");
//
//            jsonSchema.appendFieldDescToPrompt(enhancedPrompt);
//
//            enhancedPrompt.append("\n\n**注意**：分析用户输入内容必须遵守如下纪律：");
//            enhancedPrompt.append("\n**默认值处理**：");
//            enhancedPrompt.append("\n   - 对于填充的字段内容，有以下要求：");
//            enhancedPrompt.append("\n     a) 如果用户提供了对应信息 → 按 schema 要求处理（如替换非法字符以符合 `" + SCHEMA_VALUE_PATTERN + "`）；");
//            enhancedPrompt.append("\n     b) 如果用户**未提供**，但相应属性中定义了 `" + SCHEMA_VALUE_DEFAULT + "`属性 → **必须使用该 " + SCHEMA_VALUE_DEFAULT + " 值**`");
//            enhancedPrompt.append("\n     c) 如果用户未提供，且 相应属性 中**无 " + SCHEMA_VALUE_DEFAULT + "** → 填入空字符串 `\"\"`。");
//        }
//
//        LLMResponse response = chat(context, prompt.setNewPrompt(enhancedPrompt.toString()), systemPrompt, false,
//                jsonSchema, params);
//
//        try {
//            if (response.isSuccess() && response.getContent() != null) {
//                String content = response.getContent();
//
//                // 移除可能的markdown代码块标记
//                content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "");
//                content = content.trim();
//
//                // 找到JSON的开始和结束位置
//                int start = content.indexOf("{");
//                int end = content.lastIndexOf("}") + 1;
//
//                if (start >= 0 && end > start) {
//                    String jsonStr = content.substring(start, end);
//                    try {
//                        JSONObject jsonContent = JSON.parseObject(jsonStr);
//                        response.setJsonContent(jsonContent);
//                        response.executeLog.setResponse(jsonContent);
//                    } catch (Exception e) {
//                        logger.error("Failed to parse JSON response: " + jsonStr, e);
//                        response.setSuccess(false);
//                    }
//                } else {
//                    // 如果整个内容就是JSON，尝试直接解析
//                    try {
//                        JSONObject jsonContent = JSON.parseObject(content);
//                        response.setJsonContent(jsonContent);
//                        response.executeLog.setResponse(jsonContent);
//                    } catch (Exception e) {
//                        logger.error("Failed to parse JSON response: " + content, e);
//                        response.setSuccess(false);
//                    }
//                }
//            }
//        } finally {
//            response.executeLog.summary();
//        }
//
//        return response;
//    }

    @Override
    protected Logger getLogger() {
        return logger;
    }


    @Override
    public String getProviderName() {
        return "Zhipu";
    }

    @Override
    public boolean isAvailable() {
        return this.getApiKey() != null && !this.getApiKey().isEmpty();
    }

    @Override
    public LLMProvider createConfigInstance() {
        return this;
    }

    @Override
    public String identityValue() {
        return this.name;
    }

    @Override
    protected Integer getMaxTokens() {
        return this.maxTokens != null ? this.maxTokens : 8192;
    }

    @Override
    protected String getModel() {
        return StringUtils.isNotEmpty(this.model) ? this.model : DEFAULT_MODEL;
    }

    private String getApiKey() {
        return this.apiKey;
    }

    @Override
    protected URL getApiUrl() {
        try {
            return new URL(StringUtils.isNotEmpty(this.baseUrl) ? this.baseUrl : DEFAULT_BASE_URL);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @TISExtension
    public static final class DftDescriptor extends BasicParamsConfigDescriptor implements IEndTypeGetter {
        public DftDescriptor() {
            super(KEY_DISPLAY_NAME);
        }

        @Override
        public String getDisplayName() {
            return "Zhipu";
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.verify(msgHandler, context, postFormVals);
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            ZhipuLLMProvider llmProvider = postFormVals.newInstance();
            try {
                LLMResponse chat = llmProvider.chat(IAgentContext.createNull(), new UserPrompt("test", "hello"), null);
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
            int max = 131072; // GLM-5.2系列最大支持128K输出长度
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
        public EndType getEndType() {
            return EndType.Zhipu;
        }
    }
}