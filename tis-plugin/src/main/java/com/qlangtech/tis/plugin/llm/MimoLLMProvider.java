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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.llm.ITISJsonSchema;
import com.qlangtech.tis.aiagent.llm.LLMOptionParams;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 小米 MiMo 大模型Provider实现（Chat Completions 接口，OpenAI 协议兼容）<br/>
 * API文档：<a href="https://mimo.mi.com/docs/zh-CN/api/chat/openai-api">...</a><br/>
 * <p>
 * 与 OpenAI 协议的差异：<br/>
 * 1. 鉴权请求头使用 {@code api-key}（非 {@code Authorization: Bearer}）；<br/>
 * 2. 输出 token 上限参数名为 {@code max_completion_tokens}；<br/>
 * 3. 支持 {@code thinking} 参数控制思维链的开启与关闭。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/15
 */
public class MimoLLMProvider extends LLMProvider {
    private static final Logger logger = LoggerFactory.getLogger(MimoLLMProvider.class);

    private static final String URL_PATH = "/v1/chat/completions";
    public static final String DEFAULT_MODEL = "mimo-v2.5";
    public static final String DEFAULT_BASE_URL = "https://api.xiaomimimo.com";

    /**
     * max_completion_tokens 取值上限
     */
    private static final int MAX_COMPLETION_TOKENS_UPPER = 131072;

    private static final String KEY_PARAM_TEMPERATURE = "temperature";
    private static final String KEY_PARAM_TOP_P = "top_p";

    /**
     * MiMo 对采样参数的可取值范围比 QWen 等模型更窄（文档 §3 请求体参数），
     * 而「高级采样」使用的是各模型共享的 Sampling 插件，其校验范围按 QWen 语义设定，
     * 因此需要在发出请求前按 MiMo 文档再兜一层，避免把必然被服务端拒绝的参数发出去
     */
    private static final float TEMPERATURE_MIN = 0f;
    private static final float TEMPERATURE_MAX = 1.5f;
    private static final float TOP_P_MIN = 0.01f;
    private static final float TOP_P_MAX = 1f;

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
     * 是否开启思维链，默认关闭：开启后模型会返回 reasoning_content，
     * 但 temperature / top_p 将被模型强制采用推荐默认值（1.0 / 0.95）
     */
    @FormField(type = FormFieldType.ENUM, ordinal = 5, validate = {Validator.require})
    public ThinkingMode thinking = ThinkingMode.disabled;

    /**
     * 思维链开关，取值与 MiMo API 保持一致
     */
    public enum ThinkingMode implements DescriptorUseableShortComment {
        enabled("开启"), disabled("关闭");

        public final String label;

        ThinkingMode(String label) {
            this.label = label;
        }

        @Override
        public String shortComment() {
            return this.label;
        }
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Integer getMaxTokens() {
        return this.maxTokens;
    }

    @Override
    protected void addMaxTokenParam(List<HttpUtils.PostParam> postParams) {
        // MiMo 中补全 token 上限的参数名为 max_completion_tokens（含推理 token），而非 OpenAI 传统的 max_tokens
        postParams.add(new HttpUtils.PostParam("max_completion_tokens",
                Objects.requireNonNull(getMaxTokens(), "maxTokens can not be null")));
    }

    @Override
    protected void addCustomizeParams(ITISJsonSchema jsonOutput, List<String> systemPrompt, LLMOptionParams params,
                                      List<HttpUtils.PostParam> postParams) {
        // 【注意】本方法在基类 chat() 中于采样参数写入之后被调用，此处对 postParams 的修改会作用于最终请求体
        if (jsonOutput.isContainSchema()) {
            JSONObject responseFormat = new JSONObject();
            responseFormat.put("type", "json_object");
            postParams.add(new HttpUtils.PostParam("response_format", responseFormat));
        }

        JSONObject thinkingParam = new JSONObject();
        thinkingParam.put("type", getThinkingMode().name());
        postParams.add(new HttpUtils.PostParam("thinking", thinkingParam));

        if (ThinkingMode.enabled == getThinkingMode()) {
            // 思考模式下 temperature / top_p 即使传入也不生效，主动剔除避免用户误以为采样配置已经起作用
            postParams.removeIf((param) -> KEY_PARAM_TEMPERATURE.equals(param.getKey())
                    || KEY_PARAM_TOP_P.equals(param.getKey()));
        } else {
            verifySamplingParams(postParams);
        }
    }

    /**
     * 校验将被发出的采样参数是否落在 MiMo 文档声明的取值范围内
     */
    private static void verifySamplingParams(List<HttpUtils.PostParam> postParams) {
        for (HttpUtils.PostParam param : postParams) {
            if (KEY_PARAM_TEMPERATURE.equals(param.getKey())) {
                verifySamplingRange(param, TEMPERATURE_MIN, TEMPERATURE_MAX);
            } else if (KEY_PARAM_TOP_P.equals(param.getKey())) {
                verifySamplingRange(param, TOP_P_MIN, TOP_P_MAX);
            }
        }
    }

    private static void verifySamplingRange(HttpUtils.PostParam param, float min, float max) {
        Object rawVal = param.getRawVal();
        if (!(rawVal instanceof Number)) {
            throw TisException.create("MiMo 采样参数 " + param.getKey()
                    + " 的值必须是数字，当前为：" + param.getValue());
        }
        float val = ((Number) rawVal).floatValue();
        if (val < min || val > max) {
            throw TisException.create("MiMo 的 " + param.getKey() + " 取值范围为 [" + min + ", " + max
                    + "]，当前值为 " + val + "，请在「高级采样」中调整");
        }
    }

    private ThinkingMode getThinkingMode() {
        return this.thinking == null ? ThinkingMode.disabled : this.thinking;
    }

    @Override
    protected TokenUsageSummary getTokenUsageSummary(JSONObject responseJson) {
        if (responseJson.containsKey("usage")) {
            JSONObject usage = responseJson.getJSONObject("usage");
            return new TokenUsageSummary(usage.getLongValue("prompt_tokens"), usage.getLongValue("completion_tokens"));
        }
        return null;
    }

    @Override
    protected boolean processResponseJson(LLMResponse response, JSONObject responseJson) {
        if (responseJson.containsKey("error")) {
            JSONObject errDetail = responseJson.getJSONObject("error");
            String errMessage = errDetail.getString("message");
            if (StringUtils.isNotEmpty(errMessage)) {
                response.setErrorMessage(errMessage);
                return false;
            }
        }
        response.setModel(this.getModel());
        return true;
    }

    @Override
    protected StringBuilder getResponseBodyContent(JSONObject responseJson) {
        if (responseJson.containsKey("choices")) {
            JSONArray choices = responseJson.getJSONArray("choices");
            if (!choices.isEmpty()) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                // thinking 开启时推理内容在 reasoning_content 中，content 为最终答案
                if (StringUtils.isEmpty(message.getString("content")) //
                        && StringUtils.isNotEmpty(message.getString("reasoning_content"))) {
                    logger.warn("MiMo response content is empty, only reasoning_content has been returned, "
                            + "finish_reason:{}", choice.getString("finish_reason"));
                }
                return new StringBuilder(StringUtils.defaultString(message.getString("content")));
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
        if (errBody.containsKey("error")) {
            JSONObject errDetail = errBody.getJSONObject("error");
            String errMessage = errDetail.getString("message");
            if (StringUtils.isNotEmpty(errMessage)) {
                throw TisException.create(errMessage);
            }
        }
    }

    @Override
    protected List<ConfigFileContext.Header> appendHeaders() {
        // MiMo 支持 api-key 与 Bearer 两种鉴权方式，此处采用 MiMo 原生的 api-key 请求头
        return List.of(new ConfigFileContext.Header("api-key", getApiKey()));
    }

    @Override
    public String getProviderName() {
        return "MiMo";
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.isNotEmpty(this.getApiKey());
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
    protected String getModel() {
        return StringUtils.isNotEmpty(this.model) ? this.model : DEFAULT_MODEL;
    }

    private String getApiKey() {
        return this.apiKey;
    }

    @Override
    protected URL getApiUrl() {
        try {
            return new URL((StringUtils.isNotEmpty(this.baseUrl) ? this.baseUrl : DEFAULT_BASE_URL) + URL_PATH);
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
            return "MiMo";
        }

        @Override
        protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            return this.verify(msgHandler, context, postFormVals);
        }

        @Override
        protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
            MimoLLMProvider mimo = postFormVals.newInstance();
            try {
                LLMResponse chat = mimo.chat(IAgentContext.createNull(), new UserPrompt("test", "hello"), null);
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
            if (tokens < 1 || tokens > MAX_COMPLETION_TOKENS_UPPER) {
                msgHandler.addFieldError(context, fieldName, "必须在：1至" + MAX_COMPLETION_TOKENS_UPPER + "之间");
                return false;
            }
            return true;
        }

        @Override
        public EndType getEndType() {
            return EndType.LLM_Mimo;
        }
    }
}
