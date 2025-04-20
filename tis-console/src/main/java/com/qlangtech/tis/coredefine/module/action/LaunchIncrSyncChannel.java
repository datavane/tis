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

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.job.DefaultSSERunnable;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteStep;
import com.qlangtech.tis.datax.job.ILaunchingOrchestrate.ExecuteSteps;
import com.qlangtech.tis.datax.job.ServerLaunchToken;
import com.qlangtech.tis.runtime.module.action.BasicModule;

import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-09-10 14:48
 **/
public abstract class LaunchIncrSyncChannel {
  private final DataXName appName;
  private final BasicModule module;
  private final Context context;
  private final ServerLaunchToken incrLaunchToken;
  private final TISK8sDelegate k8sClient;
  private final boolean isRelaunch;

  public LaunchIncrSyncChannel(DataXName appName, BasicModule module, Context context, ServerLaunchToken incrLaunchToken) {
    this(appName, module, context, incrLaunchToken, false);
  }

  /**
   * @param appName
   * @param module
   * @param context
   * @param incrLaunchToken
   * @param isRelaunch      是否是重新启动
   */
  public LaunchIncrSyncChannel(DataXName appName, BasicModule module, Context context, ServerLaunchToken incrLaunchToken, boolean isRelaunch) {
    appName.assetCheckDataAppType();
    this.appName = appName;
    this.module = module;
    this.context = context;
    this.incrLaunchToken = incrLaunchToken;
    this.k8sClient = TISK8sDelegate.getK8SDelegate(appName);
    this.isRelaunch = isRelaunch;
  }

//  public String getCollectionName() {
//    return appName.getPipelineName();
//  }

  protected abstract ILaunchingOrchestrate<FlinkJobDeployDTO> getFlinkJobWorkingOrchestrate(TISK8sDelegate k8sClient);

  /**
   * 开始执行启动流程
   */
  public void executeLaunch() {


    ILaunchingOrchestrate<FlinkJobDeployDTO> orchestrate = getFlinkJobWorkingOrchestrate(k8sClient);
    final ExecuteSteps executeSteps = orchestrate.createExecuteSteps(this);
    DefaultSSERunnable launchProcess = new DefaultSSERunnable(this.module, executeSteps, () -> {
      try {
        Thread.sleep(4000l);
      } catch (InterruptedException e) {
      }
    }) {
      @Override
      public void afterLaunched() {
        module.addActionMessage(context, "已经成功启动Flink增量实例");
      }
    };


    DefaultSSERunnable.execute(launchProcess, false
      , incrLaunchToken, () -> {
        incrLaunchToken.writeLaunchToken(isRelaunch, () -> {
          for (ExecuteStep execStep : executeSteps.getExecuteSteps()) {
            execStep.getSubJob().execSubJob(new FlinkJobDeployDTO(context, k8sClient));
          }
          return Optional.empty();
        });
        launchProcess.afterLaunched();
      });
  }

  class FlinkJobDeployDTO {
    final Context context;
    final TISK8sDelegate k8sClient;
    private final StringBuffer logger = new StringBuffer("flink sync app:" + appName);

    public FlinkJobDeployDTO(Context context, TISK8sDelegate k8sClient) {
      this.context = context;
      this.k8sClient = k8sClient;
    }

    public boolean hasErrors() {
      return context.hasErrors();
    }

    public void appendLog(String loggerMsg) {
      logger.append(loggerMsg);
    }
  }
}
