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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.PluginWrapper;
import com.qlangtech.tis.extension.model.UpdateCenter;
import com.qlangtech.tis.extension.model.UpdateSite;
import com.qlangtech.tis.install.InstallState;
import com.qlangtech.tis.install.InstallUtil;
import com.qlangtech.tis.maven.plugins.tpi.PluginClassifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/19
 */
public class PluginWillInstall {

  private static final Logger logger = LoggerFactory.getLogger(PluginWillInstall.class);

  private String name;
  private Optional<PluginClassifier> classifier;
  private boolean multiClassifier;

  public static List<PluginWillInstall> parse(JSONArray pluginsInstall) {
    List<PluginWillInstall> willBeInstall = Lists.newArrayList();
    PluginWillInstall pi = null;
    for (int i = 0; i < pluginsInstall.size(); i++) {
      JSONObject willInstall = pluginsInstall.getJSONObject(i);
      String pluginName = willInstall.getString("name");

      Optional<PluginClassifier> classifier = Optional.empty();
      String c = null;
      if (StringUtils.isNotEmpty(c = willInstall.getString("selectedClassifier"))) {
        classifier = Optional.of(PluginClassifier.create(c));
      }
      pi = new PluginWillInstall(pluginName, classifier, willInstall.getBooleanValue("multiClassifier"));
      willBeInstall.add(pi);
//      if (willInstall.getBooleanValue("multiClassifier") && !classifier.isPresent()) {
//        this.addFieldError(context, pluginName, "请选择安装的版本");
//        continue;
//      }
    }
    return willBeInstall;
  }


  private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

  /**
   *
   * @param pluginsInstall
   */
  public static Future<Boolean> installPlugins(List<PluginWillInstall> pluginsInstall) {


    long start = System.currentTimeMillis();
    final boolean dynamicLoad = true;
    UUID correlationId = UUID.randomUUID();
    UpdateCenter updateCenter = TIS.get().getUpdateCenter();
    List<Future<UpdateCenter.UpdateCenterJob>> installJobs = new ArrayList<>();
    PluginWillInstall willInstall = null;
    String pluginName = null;
    UpdateSite.Plugin plugin = null;
    List<Pair<UpdateSite.Plugin, Optional<PluginClassifier>>> coords = Lists.newArrayList();
    Optional<PluginClassifier> classifier;
    //  String c = null;
    List<PluginWrapper> batch = new ArrayList<>();
    for (int i = 0; i < pluginsInstall.size(); i++) {
      willInstall = pluginsInstall.get(i);
      pluginName = willInstall.getName();
      classifier = willInstall.getSelectedClassifier();
//      if (StringUtils.isNotEmpty(c = willInstall.getString("selectedClassifier"))) {
//        classifier = Optional.of(PluginClassifier.create(c));
//      }
//      if (willInstall.getBooleanValue("multiClassifier") && !classifier.isPresent()) {
//        this.addFieldError(context, pluginName, "请选择安装的版本");
//        continue;
//      }

      if (StringUtils.isEmpty(pluginName)) {
        throw new IllegalStateException("plugin name can not empty");
      }
      plugin = updateCenter.getPlugin(pluginName);

      coords.add(Pair.of(plugin, classifier));
    }

//    if (this.hasErrors(context)) {
//      return;
//    }

    for (Pair<UpdateSite.Plugin, Optional<PluginClassifier>> coord : coords) {
      /***********
       * 校验证书是否有效
       ***********/
      coord.getLeft().validateLicense();

      Future<UpdateCenter.UpdateCenterJob> installJob
        = coord.getLeft().deploy(dynamicLoad, correlationId, coord.getRight(), batch);
      installJobs.add(installJob);
    }

    if (dynamicLoad) {
      installJobs.add(updateCenter.addJob(updateCenter.new CompleteBatchJob(batch, start, correlationId)));
    }

    final TIS tis = TIS.get();

    //TODO: 每个安装流程都要进来
    if (true || !tis.getInstallState().isSetupComplete()) {


      tis.setInstallState(InstallState.INITIAL_PLUGINS_INSTALLING);
      updateCenter.persistInstallStatus();

      return executorService.submit(new Callable<Boolean>() {
        @Override
        public Boolean call() throws Exception {
          boolean failures = false;
          INSTALLING:
          while (true) {
            try {
              updateCenter.persistInstallStatus();
              Thread.sleep(500);
              failures = false;
              for (Future<UpdateCenter.UpdateCenterJob> jobFuture : installJobs) {
                if (!jobFuture.isDone() && !jobFuture.isCancelled()) {
                  continue INSTALLING;
                }
                UpdateCenter.UpdateCenterJob job = jobFuture.get();
                if (job instanceof UpdateCenter.InstallationJob && ((UpdateCenter.InstallationJob) job).status instanceof UpdateCenter.DownloadJob.Failure) {
                  failures = true;
                }
              }
            } catch (Exception e) {
              logger.warn("Unexpected error while waiting for initial plugin set to install.", e);
            }
            break;
          }
          updateCenter.persistInstallStatus();
          if (!failures) {
            try {
              // 为了使Assemble 节点有时间初始化
              Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            // 为了让Assemble等节点的uberClassLoader重新加载一次，需要主动向Assemble等节点发送一个指令
            //   notifyPluginUpdate2AssembleNode(TIS.KEY_ACTION_CLEAN_TIS + "=true", "TIS");
            InstallUtil.proceedToNextStateFrom(InstallState.INITIAL_PLUGINS_INSTALLING);
          }
          return !failures;
        }
      });
    }

    return executorService.submit(() -> false);
  }


  public PluginWillInstall(String name, Optional<PluginClassifier> selectedClassifier, boolean multiClassifier) {
    this.name = name;
    this.classifier = selectedClassifier;
    this.multiClassifier = multiClassifier;
  }

  public PluginWillInstall(String name) {
    this(name, Optional.empty(), false);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Optional<PluginClassifier> getSelectedClassifier() {
    return this.classifier;
  }

  public boolean isPresentPluginClassifier() {
    return this.getSelectedClassifier().isPresent();
  }

  public void setSelectedClassifier(Optional<PluginClassifier> selectedClassifier) {
    this.classifier = selectedClassifier;
  }

  public boolean isMultiClassifier() {
    return this.multiClassifier;
  }

  public void setMultiClassifier(boolean multiClassifier) {
    this.multiClassifier = multiClassifier;
  }
}
