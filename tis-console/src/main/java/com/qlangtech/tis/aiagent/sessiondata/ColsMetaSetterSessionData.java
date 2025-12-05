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

package com.qlangtech.tis.aiagent.sessiondata;

import com.qlangtech.tis.aiagent.core.ISessionData;
import com.qlangtech.tis.plugin.IdentityName;

/**
 * tdfs 同步到 mysql 分布式文件系统，需要确定tdfs端的列类型
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/12/3
 */
public class ColsMetaSetterSessionData implements ISessionData {
  private boolean hasValidSet;
  private final IdentityName pipeline;

  public ColsMetaSetterSessionData(IdentityName pipeline) {
    this.pipeline = pipeline;
  }

  public boolean isHasValidSet() {
    return hasValidSet;
  }

  public IdentityName getPipeline() {
    return pipeline;
  }

  public void setHasValidSet(boolean hasValidSet) {
    this.hasValidSet = hasValidSet;
  }
}
