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

package com.qlangtech.tis.plugin.llm.log;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.reader.ObjectReaderImplJSONP;
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.trigger.util.JsonUtil;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/27
 */
public class DefaultExecuteLog extends BasicExecuteLog implements ExecuteLog {
    private List<HttpUtils.PostParam> postParams;
    private JSONObject responseJson;
    private JSONObject errBody;
    private final Logger logger;

     DefaultExecuteLog(UserPrompt prompt, IAgentContext context, Logger logger) {
        super(prompt, context);
        this.logger = Objects.requireNonNull(logger, "logger can not be null");
    }

    @Override
    public void setPostParams(List<HttpUtils.PostParam> postParams) {
        super.setPostParams(postParams);
        this.postParams = postParams;
    }

    @Override
    public void setError(JSONObject errBody) {
        super.setError(errBody);
        this.errBody = errBody;
    }

    @Override
    public void setResponse(JSONObject responseJson) {
        this.responseJson = responseJson;
    }

    @Override
    public void summary() {
        super.summary();
        StringBuilder summary = new StringBuilder();
        summary.append("\nparams---------------------------------------------------------------\n");
        summary.append(JsonUtil.toString(postParams, true));
        summary.append("\nuser prompt----------------------------------------------------------\n");
        summary.append(prompt.getPrompt());
        summary.append("\nresponse--------------------------------------------------------------\n");
        if (responseJson != null) {
            summary.append(JsonUtil.toString(responseJson, true));
        } else {
            summary.append("none");
        }
        if (errBody != null) {
            summary.append("\nerror--------------------------------------------------------------\n");
            summary.append(JsonUtil.toString(errBody, true));
        }
        logger.info(summary.toString());
    }
}
