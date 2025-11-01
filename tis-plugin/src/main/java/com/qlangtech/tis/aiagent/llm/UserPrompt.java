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

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/1
 */
public class UserPrompt {
    //摘要
    private final String abstractInfo;

    private final String prompt;

    public UserPrompt(String abstractInfo, String prompt) {
        this.abstractInfo = abstractInfo;
        this.prompt = prompt;
    }

    public UserPrompt setNewPrompt(String prompt) {
        return new UserPrompt(this.getAbstractInfo(), prompt);
    }

    public UserPrompt setAbstract(String abstractInfo) {
        return new UserPrompt(abstractInfo, this.prompt);
    }

    public String getAbstractInfo() {
        return abstractInfo;
    }

    public String getPrompt() {
        return prompt;
    }
}
