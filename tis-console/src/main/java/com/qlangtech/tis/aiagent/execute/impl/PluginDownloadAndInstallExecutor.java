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

package com.qlangtech.tis.aiagent.execute.impl;

import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.execute.StepExecutor;
import com.qlangtech.tis.aiagent.plan.DescribableImpl;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.coredefine.module.action.PluginFilter;
import com.qlangtech.tis.coredefine.module.action.PluginWillInstall;
import com.qlangtech.tis.extension.model.UpdateCenter;
import com.qlangtech.tis.extension.model.UpdateSite;
import com.qlangtech.tis.extension.util.TextFile;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/18
 */
public class PluginDownloadAndInstallExecutor implements StepExecutor {
  @Override
  public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {

    UpdateCenter center = TIS.get().getUpdateCenter();
    final List<UpdateSite.Plugin> availables = center.getPlugins(UpdateSite::getAllPlugins);
    try {
      if (CollectionUtils.isEmpty(availables)) {
        for (UpdateSite usite : center.getSiteList()) {
          TextFile textFile = usite.getDataLoadFaildFile();
          if (textFile.exists()) {
            throw TisException.create(textFile.read());
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    Set<PluginWillInstall> pluginsInstall = parsePluginWillInstalls(plan.getSourceType(), plan.readerExtendPoints.values(), availables);
    pluginsInstall.addAll(parsePluginWillInstalls(plan.getTargetType(), plan.writerExtendPoints.values(), availables));

    plan.checkDescribableImplHasSet();
    Boolean success = false;
    try {
      if (CollectionUtils.isNotEmpty(pluginsInstall)) {
        Future<Boolean> installResult = PluginWillInstall.installPlugins(new ArrayList<>(pluginsInstall));
        success = installResult.get();
        if (!success) {
          throw TisException.create("install plugins:"
            + pluginsInstall.stream().map(PluginWillInstall::getName).collect(Collectors.joining(",")) + " faild");
        }
      }
    } catch (TisException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }


    /**
     * 需要等待安装完毕
     */
    return true;
  }

  private Set<PluginWillInstall> parsePluginWillInstalls(
    IEndTypeGetter.EndType endType, Collection<DescribableImpl> extendPoints, List<UpdateSite.Plugin> availables) {
    if (endType == null) {
      throw new IllegalArgumentException("param endType can not be null");
    }
    Set<PluginWillInstall> pluginsInstall = Sets.newHashSet();

    PluginFilter filter = PluginFilter.create(
      endType.getVal(), extendPoints);

    List<UpdateSite.Plugin> filterAvailables
      = availables.stream().filter((plugin) -> {
      return !(filter.filter(Optional.empty(), plugin));
    }).collect(Collectors.toList());

    List<String> impls = null;

    outter:
    for (DescribableImpl ep : extendPoints) {
      for (UpdateSite.Plugin plugin : filterAvailables) {
        impls = plugin.extendPoints.get(ep.getExtendPointClassName());
        if (CollectionUtils.isNotEmpty(impls)) {
          for (String pluginImpl : impls) {
            ep.addImpl(pluginImpl);
          }
          if (plugin.getInstalled() == null) {
            // 扩展实现的插件还没有安装，需要进行安装
            pluginsInstall.add(new PluginWillInstall(plugin.getDisplayName()));
          }
        }
      }
    }
    return pluginsInstall;
  }

  /**
   * 执行完毕之后需要校验
   *
   * @param step 要验证的步骤
   * @return
   */
  @Override
  public ValidationResult validate(TaskStep step) {
    return ValidationResult.success();
  }

  @Override
  public TaskStep.StepType getSupportedType() {
    return TaskStep.StepType.PLUGIN_INSTALL;
  }
}
