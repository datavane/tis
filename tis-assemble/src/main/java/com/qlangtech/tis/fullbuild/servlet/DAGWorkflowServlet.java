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
package com.qlangtech.tis.fullbuild.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.datax.ActorSystemStatus;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.DataXJobSubmitAkkaClusterSupport;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.NodeStatus;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.WorkflowRuntimeStatus;
import com.qlangtech.tis.job.common.JobParams;
import com.qlangtech.tis.manage.common.IAjaxResult;
import com.qlangtech.tis.workflow.pojo.DagNodeExecution;
import org.apache.commons.io.IOUtils;
import com.qlangtech.tis.fullbuild.IFullBuildContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/2/12
 */
public class DAGWorkflowServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DataXJobSubmitAkkaClusterSupport akkaClusterSupport =
                (DataXJobSubmitAkkaClusterSupport) DataXJobSubmit.getDataXJobSubmit();
        final String method = req.getParameter("method");
        JSONObject result = new JSONObject();
        boolean success = false;
        breakSwitch:
        switch (method) {
            case "queryWorkflowStatus": {
                WorkflowRuntimeStatus workflowRuntimeStatus = getWorkflowStatus(req, akkaClusterSupport);
                if (workflowRuntimeStatus != null) {
                    result.put(IAjaxResult.KEY_BIZRESULT, workflowRuntimeStatus);
                    success = true;
                }
                break;
            }
            case "getNodeExecutionDetail": {
                WorkflowRuntimeStatus workflowRuntimeStatus = getWorkflowStatus(req, akkaClusterSupport);
                if (workflowRuntimeStatus != null) {
                    Long nodeId = Long.parseLong(req.getParameter("nodeId"));
                    for (NodeStatus nodeStaus : workflowRuntimeStatus.getNodes()) {
                        if (Objects.equals(nodeStaus.getNodeId(), nodeId)) {
                            DagNodeExecution nodeExecution = nodeStaus.convert();
                            result.put(IAjaxResult.KEY_BIZRESULT, nodeExecution);
                            success = true;
                            break breakSwitch;
                        }
                    }
                }
                break;
            }
            case "queryActorSystemStatus": {
                ActorSystemStatus statusInfo = akkaClusterSupport.queryActorSystemStatus();
                result.put(IAjaxResult.KEY_BIZRESULT, statusInfo);
                success = true;
                break;
            }
            default: {
                throw new IllegalStateException("illegal method:" + method);
            }
        }
        result.put(IAjaxResult.KEY_SUCCESS, success);
        IOUtils.write(JSON.toJSONString(result, true), resp.getWriter());
    }

    private static WorkflowRuntimeStatus getWorkflowStatus( //
                                                            HttpServletRequest req,
                                                            DataXJobSubmitAkkaClusterSupport akkaClusterSupport) {
        Integer instanceId = Integer.parseInt(req.getParameter(JobParams.KEY_TASK_ID));
        DataXName dataXName = DataXName.createDataXPipeline(req.getHeader(IFullBuildContext.KEY_APP_NAME));// new
        // DataXName(req.getParameter(StoreResourceType.DATAX_NAME),
        // StoreResourceType.parse(req.getParameter(StoreResourceType.KEY_STORE_RESOURCE_TYPE)));
        return akkaClusterSupport.doQueryWorkflowStatus(dataXName, instanceId);
    }
}
