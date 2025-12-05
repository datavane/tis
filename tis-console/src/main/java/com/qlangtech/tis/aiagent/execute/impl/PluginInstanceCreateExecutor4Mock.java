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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.AgentContext;
import com.qlangtech.tis.aiagent.core.RequestKey;
import com.qlangtech.tis.aiagent.plan.TaskPlan;
import com.qlangtech.tis.aiagent.plan.TaskStep;
import com.qlangtech.tis.aiagent.sessiondata.ColsMetaSetterSessionData;
import com.qlangtech.tis.coredefine.module.action.DataxAction;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.valve.AjaxValve;
import com.qlangtech.tis.plugin.IDataXEndTypeGetter;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ds.DataTypeMeta;
import com.qlangtech.tis.util.PartialSettedPluginContext;

import java.util.Map;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/12/3
 */
public class PluginInstanceCreateExecutor4Mock extends BasicStepExecutor {
  @Override
  public boolean execute(TaskPlan plan, TaskStep step, AgentContext context) {

    Context ctx = plan.getRuntimeContext(false);
    IdentityName primaryFieldVal = IdentityName.create("mysql_mysql");
    PartialSettedPluginContext pluginCtx = createPluginContext(plan,
            DataXName.createDataXPipeline(primaryFieldVal.identityValue()));
    IDataxProcessor process = DataxProcessor.load(pluginCtx, pluginCtx.getTISDataXName());
    plan.getSourceEnd().setProcessor((IAppSource) process);
    plan.getSourceEnd().setEndTypeMeta((IDataXEndTypeGetter) ((DataxReader) process.getReader(null)).getDescriptor());
    plan.getTargetEnd().setEndTypeMeta((IDataXEndTypeGetter) ((DataxWriter) process.getWriter(null)).getDescriptor());
    //    Map<String, Object> colsMetaViewBiz = DataTypeMeta.createViewBiz( //
    //      DataTypeMeta.IMultiItemsView.unknow(), DataxAction.getTableMapper(pluginCtx, process));
    //
    //    if (!DataxAction.validateAndSaveTableMapper( //
    //      plan.getControlMsgHandler(), ctx, primaryFieldVal.identityValue(), new JSONObject(colsMetaViewBiz))) {
    //      RequestKey requestKey = RequestKey.create();
    //
    //      context.sendOpenColsMetaSetter(requestKey, primaryFieldVal, AjaxValve.ActionExecResult.create(ctx),
    //      colsMetaViewBiz);
    //
    //      ColsMetaSetterSessionData colsMeta = context.waitForUserPost(requestKey,
    //      ColsMetaSetterSessionData::isHasValidSet);
    //    }

    return true;
  }

  @Override
  public ValidationResult validate(TaskStep step) {
    return ValidationResult.success();
  }

  @Override
  public TaskStep.StepType getSupportedType() {
    return TaskStep.StepType.PLUGIN_CREATE;
  }
}
