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
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.llm.JsonSchema;
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

/**
 * DeepSeek大模型Provider实现
 * https://api-docs.deepseek.com/zh-cn/api/create-completion
 * https://api-docs.deepseek.com/zh-cn/api/create-chat-completion
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class DeepSeekProvider extends LLMProvider {
    private static final Logger logger = LoggerFactory.getLogger(DeepSeekProvider.class);

    private static final String URL_PATH = "/chat/completions";
    public static final String DEFAULT_MODEL = "deepseek-chat";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, ordinal = 0, validate = {Validator.require,
            Validator.identity})
    public String name;


    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 1, validate = {Validator.require, Validator.url})
    public String baseUrl;

    @FormField(type = FormFieldType.PASSWORD, ordinal = 2, validate = {Validator.require})
    public String apiKey;

    @FormField(type = FormFieldType.INT_NUMBER, ordinal = 3, validate = {Validator.require, Validator.integer})
    public Integer maxTokens;

    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 4, validate = {Validator.require})
    public String model;

    @FormField(type = FormFieldType.INT_NUMBER, advance = true, ordinal = 5, validate = {Validator.require,
            Validator.integer})
    public Integer temperature;

    //    @FormField(type = FormFieldType.DURATION_OF_SECOND, advance = true, ordinal = 6, validate = {Validator
    //    .require,
    //            Validator.integer})
    //    public Duration readTimeout;


    /**
     * 是否打印日志
     */
    @FormField(type = FormFieldType.ENUM, advance = true, ordinal = 8, validate = {Validator.require})
    public Boolean printLog;

    @Override
    public LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt) {
        return chat(context, prompt, systemPrompt, true, JsonSchema.off());
    }


    public LLMResponse chat(IAgentContext context, UserPrompt prompt, List<String> systemPrompt, boolean logSummary,
                            JsonSchema jsonOutput) {
        ExecuteLog executeLog = ExecuteLog.create(this.printLog, prompt, context, logger);
        try {
            List<HttpUtils.PostParam> postParams = new ArrayList<>();
            postParams.add(new HttpUtils.PostParam("model", getModel()));
            postParams.add(new HttpUtils.PostParam("temperature", temperature));
            postParams.add(new HttpUtils.PostParam("max_tokens", getMaxTokens()));

            if (jsonOutput.isContainSchema()) {
                JSONObject responseFormat = new JSONObject();
                responseFormat.put("type", "json_object");
                postParams.add(new HttpUtils.PostParam("response_format", responseFormat));
            }
            JSONObject thinking = new JSONObject();
            thinking.put("type", "disabled");
            postParams.add(new HttpUtils.PostParam("thinking", thinking));


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

            executeLog.setPostParams(postParams);

            return HttpUtils.post(new URL(getApiUrl()), postParams //
                    , new PostFormStreamProcess<LLMResponse>(new ConfigFileContext.Header("Authorization",
                            "Bearer " + getApiKey())) {
                        @Override
                        public ContentType getContentType() {
                            return ContentType.JSON;
                        }

                        @Override
                        public int getMaxRetry() {
                            return maxRetry;
                        }

                        @Override
                        public Duration getSocketReadTimeout() {
                            return readTimeout;
                        }

                        @Override
                        public void error(int status, InputStream errstream, IOException e) {
                            if (errstream != null) {
                                try {
                                    JSONObject errBody = JSONObject.parseObject(IOUtils.toString(errstream,
                                            TisUTF8.get()));
                                    executeLog.setError(errBody);
                                    checkInValidError(errBody);
                                } catch (IOException ex) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                throw new RuntimeException(e);
                            }
                        }

                        private void checkInValidError(JSONObject responseJson) {
                            if (responseJson.containsKey("error")) {
                                JSONObject errDetail = responseJson.getJSONObject("error");
                                String errMessage = errDetail.getString("message");
                                if (StringUtils.isNotEmpty(errMessage)) {
                                    throw TisException.create(errMessage);
                                }
                            }
                        }

                        @Override
                        public LLMResponse p(int status, InputStream stream, Map headerFields) throws IOException {
                            LLMResponse response = new LLMResponse(executeLog);
                            String responseStr = IOUtils.toString(stream, TisUTF8.get());

                            JSONObject responseJson = JSON.parseObject(responseStr);
                            executeLog.setResponse(responseJson);

                            /**
                             * <pre>
                             * {"error":{"code":"invalid_request_error"
                             * ,"message":"Prompt must contain the word 'json' in some form to use 'response_format' of type 'json_object'."
                             * ,"type":"invalid_request_error"}}
                             * </pre>
                             */
                            checkInValidError(responseJson);

                            if (responseJson.containsKey("choices")) {
                                JSONArray choices = responseJson.getJSONArray("choices");
                                if (!choices.isEmpty()) {
                                    JSONObject choice = choices.getJSONObject(0);
                                    JSONObject message = choice.getJSONObject("message");
                                    response.setContent(message.getString("content"));
                                    response.setSuccess(true);
                                }
                            }

                            if (responseJson.containsKey("usage")) {
                                JSONObject usage = responseJson.getJSONObject("usage");
                                response.setPromptTokens(usage.getLongValue("prompt_tokens"));
                                response.setCompletionTokens(usage.getLongValue("completion_tokens"));
                                // response.setTotalTokens();
                                context.updateTokenUsage(usage.getLongValue("total_tokens"));
                            }

                            response.setModel(getModel());
                            return response;
                        }

                        //                @Override
                        //                public List<ConfigFileContext.Header> getHeaders() {
                        //                    List<ConfigFileContext.Header> headers = new ArrayList<>(super
                        //                    .getHeaders());
                        //                    headers.add(new ConfigFileContext.Header("Authorization", "Bearer " +
                        //                    getApiKey()));
                        //                    return headers;
                        //                }
                    });
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } finally {
            if (logSummary) {
                executeLog.summary();
            }
        }
    }


    @Override
    public LLMResponse chatJson(IAgentContext context, UserPrompt prompt, List<String> systemPrompt,
                                JsonSchema jsonSchema) {
        String enhancedPrompt = prompt.getPrompt();
        if (jsonSchema.isContainSchema()) {
            // enhancedPrompt += "\n\n请严格按照以上JSON Schema格式返回结果：\n" + jsonSchema;
            enhancedPrompt += "\n\n请严格按照以下JSON Schema格式返回结果，只返回JSON，不要包含其他说明文字：\n" + jsonSchema.root();
            enhancedPrompt += "\n\n重要：请确保返回的是有效的JSON格式，不要包含markdown标记或其他文本。";
        }
        LLMResponse response = chat(context, new UserPrompt(prompt.getAbstractInfo(), enhancedPrompt), systemPrompt,
                false, jsonSchema);

        try {
            if (response.isSuccess() && response.getContent() != null) {
                String content = response.getContent();
                int start = content.indexOf("{");
                int end = content.lastIndexOf("}") + 1;
                if (start >= 0 && end > start) {
                    String jsonStr = content.substring(start, end);
                    JSONObject jsonContent = JSON.parseObject(jsonStr);
                    response.setJsonContent(jsonContent);
                    response.executeLog.setResponse(jsonContent);
                }
            }
        } finally {
            response.executeLog.summary();
        }

        return response;
    }

    @Override
    public String getProviderName() {
        return "DeepSeek";
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
        return this.maxTokens;
    }

    private String getModel() {
        return this.model;
    }

    private String getApiKey() {
        return this.apiKey;
    }

    private String getApiUrl() {
        return this.baseUrl + URL_PATH;
    }

    @TISExtension
    public static final class DftDescriptor extends BasicParamsConfigDescriptor implements IEndTypeGetter {
        public DftDescriptor() {
            super(KEY_DISPLAY_NAME);
        }

        @Override
        public String getDisplayName() {
            return "DeepSeek";
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.verify(msgHandler, context, postFormVals);
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {

            DeepSeekProvider deepSeek = postFormVals.newInstance();
            try {
                deepSeek.chat(IAgentContext.createNull(), new UserPrompt("test", "hello"), null);
            } catch (Exception e) {
                msgHandler.addErrorMessage(context, e.getMessage());
                return false;
            }

            return super.verify(msgHandler, context, postFormVals);
        }

        @Override
        public EndType getEndType() {
            return EndType.Deepseek;
        }
    }
}
