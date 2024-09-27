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

package com.qlangtech.tis.manage.common;

import com.opensymphony.xwork2.config.ConfigurationManager;
import org.apache.struts2.dispatcher.mapper.ActionMapping;
import org.apache.struts2.dispatcher.mapper.DefaultActionMapper;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-09-27 11:44
 **/
public class TerminatorDefaultActionMapper extends DefaultActionMapper {
  public TerminatorDefaultActionMapper() {
    super();
    this.setAllowDynamicMethodCalls(Boolean.TRUE.toString());
    this.setAlwaysSelectFullNamespace(Boolean.TRUE.toString());
  }

  @Override
  protected void extractMethodName(ActionMapping mapping, ConfigurationManager configurationManager) {
    // super.extractMethodName(mapping, configurationManager);
  }
}
