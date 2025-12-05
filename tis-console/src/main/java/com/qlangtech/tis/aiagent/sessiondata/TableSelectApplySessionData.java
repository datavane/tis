/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.aiagent.sessiondata;

import com.qlangtech.tis.aiagent.core.ISessionData;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/24
 */
public class TableSelectApplySessionData implements ISessionData {
  /**
   * 表选择确认，为true则说明表学则流程已经成功了
   */
  private boolean tableSelectConfirm;
  private List<String> selectedTabs;

  public boolean isTableSelectConfirm() {
    return tableSelectConfirm;
  }

  public void setTableSelectConfirm(boolean tableSelectConfirm) {
    this.tableSelectConfirm = tableSelectConfirm;
  }

  public void setTableSelected(List<String> selectedTabs) {
    this.selectedTabs = Objects.requireNonNull(selectedTabs, "selectedTabs can not be null");
  }

  public List<String> getSelectedTabs() {
    return this.selectedTabs;
  }
}
