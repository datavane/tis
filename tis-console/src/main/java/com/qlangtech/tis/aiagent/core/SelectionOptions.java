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

import com.qlangtech.tis.extension.util.PluginExtraProps;

import java.util.List;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/6
 */
public class SelectionOptions implements ISessionData {
  private final Integer selectedIndex;
  private final List<PluginExtraProps.CandidatePlugin> candidatePlugins;

  private static final Integer KEY_HAS_NOT_SELECTED_TOKEN = -1;

  /**
   * 创建
   *
   * @param candidatePlugins
   * @return
   */
  public static SelectionOptions createUnSelectedOptions(List<PluginExtraProps.CandidatePlugin> candidatePlugins) {
    return new SelectionOptions(KEY_HAS_NOT_SELECTED_TOKEN, candidatePlugins);
  }

  public SelectionOptions(Integer selectedIndex, List<PluginExtraProps.CandidatePlugin> candidatePlugins) {
    this.selectedIndex = selectedIndex;
    this.candidatePlugins = candidatePlugins;
  }

  public Integer getSelectedIndex() {
    return this.selectedIndex;
  }

  public PluginExtraProps.CandidatePlugin getSelectPluginDesc() {
    PluginExtraProps.CandidatePlugin selected = candidatePlugins.get(selectedIndex);
    if (selected.getInstalledPluginDescriptor(true) == null) {
      throw new IllegalStateException(selected.getDisplayName()
        + " of extendpoint:" + selected.getHetero().getExtensionPoint().getName() + " have not been installed");
    }
    return selected;
  }

  public List<PluginExtraProps.CandidatePlugin> getCandidatePlugins() {
    return this.candidatePlugins;
  }

  public ISessionData setSelectedIndex(Integer selectedIndex) {
    // this.candidatePlugins.get(selectedIndex).
    return new SelectionOptions(selectedIndex, this.getCandidatePlugins());
  }

  /**
   * 用户还没有做出选择
   *
   * @return
   */
  public boolean hasSelectedOpt() {
    return this.selectedIndex > KEY_HAS_NOT_SELECTED_TOKEN;
  }
}
