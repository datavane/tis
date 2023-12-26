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

package com.qlangtech.tis.datax.job;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-12-20 09:20
 **/
public interface IRegisterApp {
    /**
     * 注册新app
     *
     * @param domain   : format: IP:PORT
     * @param appName
     * @param password
     */
    public void registerApp(String domain, String appName, String password) throws PowerjobOrchestrateException;

    public class RegisterAppResult {
        private final boolean success;
        private final boolean passwordInvalid;
        private final String message;

        public RegisterAppResult(boolean success, boolean passwordInvalid, String message) {
            this.success = success;
            this.passwordInvalid = passwordInvalid;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isPasswordInvalid() {
            return passwordInvalid;
        }

        public String getMessage() {
            return message;
        }
    }

}
