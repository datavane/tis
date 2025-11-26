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

package com.qlangtech.tis.datax;

import java.util.List;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/25
 */
public interface IManipulateStatus {
    public ManipulateStateSummary manipulateStatusSummary();


    public static ManipulateState create(String message) {
        return new ManipulateState.MessageState(message);
    }

    public static class ManipulateStateSummary {
        private List<ManipulateState> status;
        private String summary;
        private boolean activate;

        public ManipulateStateSummary(List<ManipulateState> status, String summary, boolean activate) {
            this.status = status;
            this.summary = summary;
            this.activate = activate;
        }

        public List<ManipulateState> getStatus() {
            return this.status;
        }

        public String getSummary() {
            return this.summary;
        }

        public boolean isActivate() {
            return this.activate;
        }
    }

    public abstract class ManipulateState {
        public abstract String getStat();

        private static class MessageState extends ManipulateState {
            private final String message;

            public MessageState(String message) {
                this.message = message;
            }

            @Override
            public String getStat() {
                return message;
            }
        }
    }
}
