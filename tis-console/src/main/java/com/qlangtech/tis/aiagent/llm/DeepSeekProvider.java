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
import com.qlangtech.tis.manage.common.ConfigFileContext;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek大模型Provider实现
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/17
 */
public class DeepSeekProvider implements LLMProvider {
  private static final Logger logger = LoggerFactory.getLogger(DeepSeekProvider.class);

  private static final String DEFAULT_API_URL = "https://api.deepseek.com/chat/completions";
  private static final String DEFAULT_MODEL = "deepseek-chat";

  private final LLMConfig config;

  public DeepSeekProvider(LLMConfig config) {
    this.config = config;
    if (config.getApiUrl() == null) {
      config.setApiUrl(DEFAULT_API_URL);
    }
    if (config.getModel() == null) {
      config.setModel(DEFAULT_MODEL);
    }
  }

  @Override
  public LLMResponse chat(String prompt, String systemPrompt, double temperature) {


    try {

      List<HttpUtils.PostParam> postParams = new ArrayList<>();
      postParams.add(new HttpUtils.PostParam("model", config.getModel()));
      postParams.add(new HttpUtils.PostParam("temperature", temperature));
      postParams.add(new HttpUtils.PostParam("max_tokens", config.getMaxTokens() > 0 ? config.getMaxTokens() : 4000));

      JSONArray messages = new JSONArray();
      if (systemPrompt != null && !systemPrompt.isEmpty()) {
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
      }

      JSONObject userMessage = new JSONObject();
      userMessage.put("role", "user");
      userMessage.put("content", prompt);
      messages.add(userMessage);

      postParams.add(new HttpUtils.PostParam("messages", messages));

      //  httpPost.setEntity(new StringEntity(requestBody.toJSONString(), "UTF-8"));


      return HttpUtils.post(new URL(config.getApiUrl()), postParams, new PostFormStreamProcess<LLMResponse>() {
        @Override
        public ContentType getContentType() {
          return ContentType.JSON;
        }

        @Override
        public LLMResponse p(int status, InputStream stream, Map headerFields) throws IOException {
          LLMResponse response = new LLMResponse();
          String responseStr = IOUtils.toString(stream, TisUTF8.get());

          JSONObject responseJson = JSON.parseObject(responseStr);

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
            response.setTotalTokens(usage.getLongValue("total_tokens"));
          }

          response.setModel(config.getModel());
          return response;
        }

        @Override
        public List<ConfigFileContext.Header> getHeaders() {
          List<ConfigFileContext.Header> headers = new ArrayList<>(super.getHeaders());
          headers.add(new ConfigFileContext.Header("Authorization", "Bearer " + config.getApiKey()));
          return headers;
        }
      });
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }


//    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//      HttpPost httpPost = new HttpPost(config.getApiUrl());
//      httpPost.setHeader("Content-Type", "application/json");
//      httpPost.setHeader("Authorization", "Bearer " + config.getApiKey());
//
//      JSONObject requestBody = new JSONObject();
//      requestBody.put("model", config.getModel());
//      requestBody.put("temperature", temperature);
//      requestBody.put("max_tokens", config.getMaxTokens() > 0 ? config.getMaxTokens() : 4000);
//
//      JSONArray messages = new JSONArray();
//      if (systemPrompt != null && !systemPrompt.isEmpty()) {
//        JSONObject systemMessage = new JSONObject();
//        systemMessage.put("role", "system");
//        systemMessage.put("content", systemPrompt);
//        messages.add(systemMessage);
//      }
//
//      JSONObject userMessage = new JSONObject();
//      userMessage.put("role", "user");
//      userMessage.put("content", prompt);
//      messages.add(userMessage);
//
//      requestBody.put("messages", messages);
//
//      httpPost.setEntity(new StringEntity(requestBody.toJSONString(), "UTF-8"));
//
//      HttpResponse httpResponse = httpClient.execute(httpPost);
//      HttpEntity entity = httpResponse.getEntity();
//
//
//    } catch (Exception e) {
//      logger.error("DeepSeek API call failed", e);
//      response.setSuccess(false);
//      response.setErrorMessage(e.getMessage());
//    }
//
//    return response;
  }

  @Override
  public LLMResponse chatJson(String prompt, String systemPrompt, String jsonSchema) {
    String enhancedPrompt = prompt + "\n\n请严格按照以下JSON Schema格式返回结果：\n" + jsonSchema;

    LLMResponse response = chat(enhancedPrompt, systemPrompt, 0.1);

    if (response.isSuccess() && response.getContent() != null) {
      try {
        String content = response.getContent();
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
          String jsonStr = content.substring(start, end);
          JSONObject jsonContent = JSON.parseObject(jsonStr);
          response.setJsonContent(jsonContent);
        }
      } catch (Exception e) {
        logger.error("Failed to parse JSON response", e);
        response.setSuccess(false);
        response.setErrorMessage("JSON parse failed: " + e.getMessage());
      }
    }

    return response;
  }

  @Override
  public String getProviderName() {
    return "DeepSeek";
  }

  @Override
  public boolean isAvailable() {
    return config.getApiKey() != null && !config.getApiKey().isEmpty();
  }

  @Override
  public LLMConfig getConfig() {
    return config;
  }
}
