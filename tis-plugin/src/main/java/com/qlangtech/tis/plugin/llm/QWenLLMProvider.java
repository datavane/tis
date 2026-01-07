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
import com.qlangtech.tis.aiagent.llm.JsonSchema;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.llm.log.ExecuteLog;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_VALUE_CONST;
import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_VALUE_DEFAULT;
import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_VALUE_ENUM;
import static com.qlangtech.tis.aiagent.llm.JsonSchema.SCHEMA_VALUE_PATTERN;
import static com.qlangtech.tis.extension.Descriptor.KEY_primaryVal;

/**
 * 阿里通义千问大模型Provider实现<br/>
 * API文档：https://help.aliyun.com/document_detail/2712576.html<br/>
 * <p>
 * API Key 管理页面<br/>
 * https://bailian.console.aliyun.com/?spm=a2c4g.11186623.0.0.47fe10bdovIEgY&tab=globalset#/efm/api_key
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/27
 */
@TISExtension
public class QWenLLMProvider extends LLMProvider {
    private static final Logger logger = LoggerFactory.getLogger(QWenLLMProvider.class);

    private static final String URL_PATH = "/compatible-mode/v1/chat/completions";
    public static final String DEFAULT_MODEL = "qwen-plus";
    public static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com";

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
     * 阿里云各种模型 https://bailian.console.aliyun.com/?spm=5176.29619931.J_XNqYbJaEnpB5_cCJf7e6D.1
     * .544a10d7BmxbRi&tab=doc#/doc/?type=model&url=2987148
     */
    @FormField(type = FormFieldType.ENUM, ordinal = 4, validate = {Validator.require})
    public String model;

    @FormField(type = FormFieldType.DECIMAL_NUMBER, advance = true, ordinal = 5, validate = {Validator.require})
    public Float temperature;

    @FormField(type = FormFieldType.DECIMAL_NUMBER, advance = true, ordinal = 6, validate = {})
    public Float topP;


    /**
     * 是否开启流式输出
     */
    // @FormField(type = FormFieldType.ENUM, advance = true, ordinal = 8, validate = {})
    public final Boolean stream = false;

    /**
     * 是否打印日志
     */
    @FormField(type = FormFieldType.ENUM, advance = true, ordinal = 9, validate = {Validator.require})
    public Boolean printLog;

    @Override
    public LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt) {
        return chat(context, prompt, systemPrompt, true, JsonSchema.off());
    }

    public LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt, boolean logSummary,
                            JsonSchema jsonOutput) {
        ExecuteLog executeLog = ExecuteLog.create(this.printLog, prompt, context, logger);// new DefaultExecuteLog
        // (prompt, context, logger) : new NoneExecuteLog();
        try {
            // 构建请求参数
            List<HttpUtils.PostParam> postParams = new ArrayList<>();
            postParams.add(new HttpUtils.PostParam("model", getModel()));

            // 设置消息列表
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

            // 设置其他参数
            postParams.add(new HttpUtils.PostParam("temperature", temperature));
            postParams.add(new HttpUtils.PostParam("max_tokens", getMaxTokens()));

            if (jsonOutput.isContainSchema()) {
                JSONObject responseFormat = new JSONObject();
                /**
                 * 返回内容的格式。可选值：
                 *
                 * {"type": "text"}：输出文字回复；
                 * {"type": "json_object"}：输出标准格式的JSON字符串。
                 * {"type": "json_schema","json_schema": {...} }：输出指定格式的JSON字符串。
                 * 相关文档：结构化输出。
                 * 若指定为{"type": "json_object"}，需在提示词中明确指示模型输出JSON，如：“请按照json格式输出”，否则会报错。
                 * 支持的模型参见结构化输出。
                 */
                responseFormat.put("type", "json_schema");
                responseFormat.put("json_schema", jsonOutput.root());
                postParams.add(new HttpUtils.PostParam("response_format", responseFormat));
            }

            if (topP != null) {
                postParams.add(new HttpUtils.PostParam("top_p", topP));
            }

            if (stream != null) {
                postParams.add(new HttpUtils.PostParam("stream", stream));
            }

            //  postParams.add(new HttpUtils.PostParam("body", requestBody));

            executeLog.setPostParams(postParams);

            return HttpUtils.post(new URL(getApiUrl()), postParams, new PostFormStreamProcess<LLMResponse>( //
                    Lists.newArrayList((new ConfigFileContext.Header("Authorization", "Bearer " + getApiKey())) //
                            , (new ConfigFileContext.Header("Content-Type", "application/json")))) {
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
                                // 如果不是JSON格式，直接抛出错误内容
                                throw TisException.create("API Error: " + errContent);
                            }

                            // 通义千问的错误格式
                            if (errBody.containsKey("error")) {
                                JSONObject errDetail = errBody.getJSONObject("error");
                                String errMessage = errDetail.getString("message");
                                String errCode = errDetail.getString("code");
                                if (StringUtils.isNotEmpty(errMessage)) {
                                    throw TisException.create(String.format("QWen API Error [%s]: %s", errCode,
                                            errMessage));
                                }
                            } else if (errBody.containsKey("message")) {
                                // 直接的错误消息格式
                                String errMessage = errBody.getString("message");
                                throw TisException.create("QWen API Error: " + errMessage);
                            }
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
                    String responseStr = IOUtils.toString(stream, TisUTF8.get());

                    JSONObject responseJson = JSON.parseObject(responseStr);
                    executeLog.setResponse(responseJson);

                    // 处理通义千问的响应格式
                    if (responseJson.containsKey("choices")) {
                        JSONArray choices = responseJson.getJSONArray("choices");
                        if (!choices.isEmpty()) {
                            JSONObject choice = choices.getJSONObject(0);
                            JSONObject message = choice.getJSONObject("message");
                            response.setContent(message.getString("content"));
                            response.setSuccess(true);

                            // 获取finish_reason
                            String finishReason = choice.getString("finish_reason");
                            if ("length".equals(finishReason)) {
                                logger.warn("Response was truncated due to max_tokens limit");
                            }
                        }
                    }

                    // 处理token使用统计
                    if (responseJson.containsKey("usage")) {
                        JSONObject usage = responseJson.getJSONObject("usage");
                        response.setPromptTokens(usage.getLongValue("prompt_tokens"));
                        response.setCompletionTokens(usage.getLongValue("completion_tokens"));
                        // response.setTotalTokens(usage.getLongValue("total_tokens"));
                        context.updateTokenUsage(usage.getLongValue("total_tokens"));
                    }

                    response.setModel(responseJson.getString("model"));
                    return response;
                }

                //                @Override
                //                public List<ConfigFileContext.Header> getHeaders() {
                //                    List<ConfigFileContext.Header> headers = new ArrayList<>(super.getHeaders());
                //                    // 通义千问使用 Bearer Token 认证
                //                    headers.add(new ConfigFileContext.Header("Authorization", "Bearer " + getApiKey
                //                    ()));
                //                    headers.add(new ConfigFileContext.Header("Content-Type", "application/json"));
                //                    return headers;
                //                }
            });
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid URL: " + getApiUrl(), e);
        } finally {
            if (logSummary) {
                executeLog.summary();
            }
        }
    }

    @Override
    public LLMResponse chatJson(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
                                JsonSchema jsonSchema) {
        // 增强prompt，要求返回JSON格式
        StringBuilder enhancedPrompt = new StringBuilder(prompt.getPrompt());
        if (jsonSchema.isContainSchema()) {
            // enhancedPrompt += "\n\n请严格按照以上JSON Schema格式返回结果，只返回JSON，不要包含其他说明文字：\n" + JsonUtil.toString(jsonSchema
            // .schema(), false);
            enhancedPrompt.append("\n\n重要：请确保返回的是有效的JSON格式，不要包含markdown标记或其他文本。");
            enhancedPrompt.append("\n请务必严格按照 'response_format' 中定义的 JSON Schema " +
                    "格式输出，不要输出任何其他内容或格式。以下是对'response_format'中相关字段的说明：");
            //            public static final String SCHEMA_VALUE_ENUM = "enum";
            //            public static final String SCHEMA_VALUE_PATTERN = "pattern";

            jsonSchema.appendFieldDescToPrompt(enhancedPrompt);
            //  SCHEMA_VALUE_CONST
            enhancedPrompt.append("\n\n**注意**：分析用户输入内容必须遵守如下纪律：");
            enhancedPrompt.append("\n**默认值处理**：");
            enhancedPrompt.append("\n   - 对于 `"+KEY_primaryVal+"` 字段：");
            enhancedPrompt.append("\n     a) 如果用户提供了对应信息 → 按 schema 要求处理（如替换非法字符以符合 `"+SCHEMA_VALUE_PATTERN+"`）；");
            enhancedPrompt.append("\n     b) 如果用户**未提供**，但相应属性中定义了 `"+SCHEMA_VALUE_DEFAULT+"`属性 → **必须使用该 "+SCHEMA_VALUE_DEFAULT+" 值**作为`"+KEY_primaryVal+"`属性值；");
            enhancedPrompt.append("\n     c) 如果用户未提供，且 相应属性 中**无 "+SCHEMA_VALUE_DEFAULT+"** → 填入空字符串 `\"\"`。");

            //            enhancedPrompt.append("\n  1. `").append(KEY_primaryVal).append("`对应的属性如定义了" + "`").append
            //            (SCHEMA_VALUE_CONST).append("`则`").append(KEY_primaryVal).append("`对应的值**必须**取值为`").append
            //            (SCHEMA_VALUE_CONST).append("`对应的值");
            //            enhancedPrompt.append("\n  2. 不能分析得到对应'response_format'中`").append(KEY_primaryVal).append
            //            ("`对应的值，必须使用json"
            //                    + " " + "schema中相应属性：`").append(SCHEMA_VALUE_DEFAULT).append("`对应值，如该属性没有定义，则`")
            //                    .append(KEY_primaryVal).append("`对应的值设置为空(\"\")即可");
            //            enhancedPrompt.append("\n  3.`").append(KEY_primaryVal).append("`对应属性如定义了`").append
            //            (SCHEMA_VALUE_ENUM).append("`则对应的值**必须为**`").append(SCHEMA_VALUE_ENUM).append("`定义的值之一");
            //            enhancedPrompt.append("\n  4.`").append(KEY_primaryVal).append("`对应属性如定义了`").append
            //            (SCHEMA_VALUE_PATTERN).append("`则对应的值**必须符合**`").append(SCHEMA_VALUE_PATTERN).append
            //            ("`定义的模式规范");
        }

        LLMResponse response = chat(context, prompt.setNewPrompt(enhancedPrompt.toString()), systemPrompt, false,
                jsonSchema);

        try {
            if (response.isSuccess() && response.getContent() != null) {
                String content = response.getContent();

                // 尝试提取JSON内容
                // 移除可能的markdown代码块标记
                content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "");
                content = content.trim();

                // 找到JSON的开始和结束位置
                int start = content.indexOf("{");
                int end = content.lastIndexOf("}") + 1;

                if (start >= 0 && end > start) {
                    String jsonStr = content.substring(start, end);
                    try {
                        JSONObject jsonContent = JSON.parseObject(jsonStr);
                        response.setJsonContent(jsonContent);
                        response.executeLog.setResponse(jsonContent);
                    } catch (Exception e) {
                        logger.error("Failed to parse JSON response: " + jsonStr, e);
                        response.setSuccess(false);
                    }
                } else {
                    // 如果整个内容就是JSON，尝试直接解析
                    try {
                        JSONObject jsonContent = JSON.parseObject(content);
                        response.setJsonContent(jsonContent);
                        response.executeLog.setResponse(jsonContent);
                    } catch (Exception e) {
                        logger.error("Failed to parse JSON response: " + content, e);
                        response.setSuccess(false);
                    }
                }
            }
        } finally {
            response.executeLog.summary();
        }

        return response;
    }

    @Override
    public String getProviderName() {
        return "QWen";
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

    private int getMaxTokens() {
        return this.maxTokens != null ? this.maxTokens : 2048;
    }

    private String getModel() {
        return StringUtils.isNotEmpty(this.model) ? this.model : DEFAULT_MODEL;
    }

    private String getApiKey() {
        return this.apiKey;
    }

    private String getApiUrl() {
        String url = StringUtils.isNotEmpty(this.baseUrl) ? this.baseUrl : DEFAULT_BASE_URL;
        return url + URL_PATH;
    }


    @TISExtension
    public static final class DftDescriptor extends BasicParamsConfigDescriptor implements IEndTypeGetter {
        public DftDescriptor() {
            super(KEY_DISPLAY_NAME);
        }

        @Override
        public String getDisplayName() {
            return "QWen";
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.verify(msgHandler, context, postFormVals);
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {

            QWenLLMProvider llmProvider = postFormVals.newInstance();
            try {
                llmProvider.chat(IAgentContext.createNull(), new UserPrompt("test", "hello"), null);
            } catch (Exception e) {
                msgHandler.addErrorMessage(context, e.getMessage());
                return false;
            }

            return super.verify(msgHandler, context, postFormVals);
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
            return EndType.QWen;
        }
    }
}