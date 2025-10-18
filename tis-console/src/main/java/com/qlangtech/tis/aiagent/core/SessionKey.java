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

package com.qlangtech.tis.aiagent.core;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/14
 */
public class SessionKey {
  private final String requestId;

  public static SessionKey create() {
    return create("tis_session_" + System.currentTimeMillis());
  }

  public static SessionKey create(String requestKey) {
    return new SessionKey(requestKey);
  }

  public SessionKey(String requestId) {
    if (StringUtils.isEmpty(requestId)) {
      throw new IllegalArgumentException("requestId can not be empty");
    }
    this.requestId = requestId;
  }

  public String getSessionKey() {
    return this.requestId;
  }

  @Override
  public String toString() {
    return this.getSessionKey();
  }
}
