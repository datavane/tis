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
package com.qlangtech.tis.coredefine.module.action;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.job.DataXJobWorker.K8SWorkerCptType;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.util.DefaultDescriptorsJSON;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.DescriptorsMeta;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-08 15:19
 */
public class PluginDescMeta<T extends Describable<T>> {

  protected final DescriptorsJSON pluginDesc;

  private final Map<K8SWorkerCptType, List<T>> plugins = Maps.newHashMap();

  public PluginDescMeta(Collection<Descriptor<T>> descList) {
    this.pluginDesc = new DefaultDescriptorsJSON(descList);
  }

  /**
   * 添加某种类型的插件
   *
   * @param type
   * @param p
   */
  public void addTypedPlugins(K8SWorkerCptType type, List<T> p) {
    List<T> plugins = this.plugins.get(type);
    if (plugins == null) {
      plugins = Lists.newArrayList();
      this.plugins.put(type, plugins);
    }
    plugins.addAll(p);
  }

  public Map<String, Integer> getTypedPluginCount() {
    return this.plugins.entrySet().stream().collect(Collectors.toMap((e) -> e.getKey().token, (e) -> e.getValue().size()));
  }

  public DescriptorsMeta getPluginDesc() {
    return pluginDesc.getDescriptorsJSON();
  }
}
