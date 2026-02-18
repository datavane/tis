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

import com.google.common.collect.Lists;
import com.qlangtech.tis.ajax.AjaxResult;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.job.common.JobCommon;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.manage.common.HttpUtils.PostParam;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;
import com.qlangtech.tis.realtime.yarn.rpc.IncrRateControllerCfgDTO;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import org.apache.commons.lang.StringUtils;

import static com.qlangtech.tis.manage.common.HttpUtils.postJSON;
import static com.qlangtech.tis.manage.common.HttpUtils.soapRemote;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import com.qlangtech.tis.exec.IExecChainContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-09 15:45
 **/
public class TaskSoapUtils {

    private static final Logger logger = LoggerFactory.getLogger(TaskSoapUtils.class);
    public static final MessageFormat WORKFLOW_CONFIG_URL_FORMAT =
            new MessageFormat(Config.getConfigRepositoryHost() + "/config/config.ajax?action={0}&event_submit_{1" +
                    "}=true&handler={2}{3}");

    /**
     * @param dataXName
     * @param lastModified 本地最终更新时间，会与服务端的时间戳进行比较
     */
    public static IncrRateControllerCfgDTO getIncrRateLimitCfg(DataXName dataXName, Long lastModified) {
        String url = createFullbuild_workflow_actionURL("do_get_rate_controller");
        List<PostParam> params = Lists.newArrayList();
        params.add(new PostParam(IncrRateControllerCfgDTO.KEY_PIPELINE, dataXName.getPipelineName()));
        params.add(new PostParam(IncrRateControllerCfgDTO.KEY_LAST_MODIFIED, lastModified));

        AjaxResult<IncrRateControllerCfgDTO> result = null;
        try {
            result = soapRemote(url, params, IncrRateControllerCfgDTO.class, false);
        } catch (TisException e) {
            //throw new RuntimeException(e);
            logger.warn(e.getMessage());
            return null;
        }
        if (result == null || !result.isSuccess()) {
            // throw new IllegalStateException("remote apply faild,error:" + String.join(" ", result.getErrormsg()));
            return null;
        }
        return result.getBizresult();
    }

    //    public static void feedbackAsynTaskStatus(int taskid, String subTaskName, boolean success) {
    //        String url = createFullbuild_workflow_actionURL("do_feedback_asyn_task_status");
    //
    //        //        WORKFLOW_CONFIG_URL_FORMAT
    //        //                .format(new Object[]{"fullbuild_workflow_action", "do_feedback_asyn_task_status",
    //        //                StringUtils.EMPTY,
    //        //                        StringUtils.EMPTY});
    //        List<PostParam> params = Lists.newArrayList();
    //        params.add(new PostParam(IParamContext.KEY_REQUEST_DISABLE_TRANSACTION, true));
    //        params.add(new PostParam(JobCommon.KEY_TASK_ID, taskid));
    //        params.add(new PostParam(IParamContext.KEY_ASYN_JOB_NAME, subTaskName));
    //        params.add(new PostParam(IParamContext.KEY_ASYN_JOB_SUCCESS, success));
    //
    //        soapRemote(url, params, CreateNewTaskResult.class, false);
    //    }

    private static String createFullbuild_workflow_actionURL(String methodName) {
        return WORKFLOW_CONFIG_URL_FORMAT.format(new Object[]{"fullbuild_workflow_action", methodName,
                StringUtils.EMPTY, StringUtils.EMPTY});
    }

    public static void createTaskComplete(int taskid, ExecResult execResult, PEWorkflowDAG dag) {
        if (execResult == null) {
            throw new IllegalArgumentException("param execResult can not be null");
        }
        String url = createFullbuild_workflow_actionURL("do_task_complete");

        List<PostParam> params = Lists.newArrayList(//
                new PostParam("execresult", String.valueOf(execResult.getValue())), //
                new PostParam(JobCommon.KEY_TASK_ID, String.valueOf(taskid)) //
                , new PostParam(PEWorkflowDAG.KEY_DAG, Objects.requireNonNull(dag, "dag can not be null")));

        postJSON(url, params, CreateNewTaskResult.class, true);
    }

    /**
     * 取得当前工作流 执行状态
     *
     * @param taskId
     * @return
     */
    public static WorkFlowBuildHistory getWFStatus(Integer taskId) {
        if (taskId == null || taskId < 1) {
            throw new IllegalArgumentException("param taskId can not be empty");
        }
        String url = createFullbuild_workflow_actionURL("do_get_wf");
        //        IExecChainContext.WORKFLOW_CONFIG_URL_POST_FORMAT
        //                .format(new Object[]{"fullbuild_workflow_action", "do_get_wf"});
        List<PostParam> params = Lists.newArrayList();
        params.add(new PostParam(JobCommon.KEY_TASK_ID, taskId));

        AjaxResult<WorkFlowBuildHistory> result = soapRemote(url, params, WorkFlowBuildHistory.class, true);
        return result.getBizresult();
    }
}
