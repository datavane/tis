/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.exec.impl;

import com.google.common.collect.Lists;
import com.qlangtech.tis.ajax.AjaxResult;
import com.qlangtech.tis.assemble.ExecResult;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.exec.*;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BasicPhaseStatus;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.HttpUtils.PostParam;
import com.qlangtech.tis.order.center.IParamContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 执行进度可跟踪的执行器
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月23日
 */
public abstract class TrackableExecuteInterceptor implements IExecuteInterceptor, ITaskPhaseInfo {

    private static final Logger log = LoggerFactory.getLogger(TrackableExecuteInterceptor.class);

    public static final Map<Integer, PhaseStatusCollection> /*** taskid*/
            taskPhaseReference = new HashMap<>();


    /**
     * 标记当前任务的ID
     *
     * @return
     */
    @Override
    @SuppressWarnings("all")
    public <T extends BasicPhaseStatus<?>> T getPhaseStatus(IExecChainContext execContext, FullbuildPhase phase) {
        PhaseStatusCollection phaseStatusCollection = taskPhaseReference.get(execContext.getTaskId());
        switch (phase) {
            case FullDump:
                return (T) phaseStatusCollection.getDumpPhase();
            case JOIN:
                return (T) phaseStatusCollection.getJoinPhase();
            case BUILD:
                return (T) phaseStatusCollection.getBuildPhase();
            case IndexBackFlow:
                return (T) phaseStatusCollection.getIndexBackFlowPhaseStatus();
            default:
                throw new IllegalStateException(phase + " is illegal has not any match status");
        }
    }

    @Override
    public final ExecuteResult intercept(ActionInvocation invocation) throws Exception {
        IExecChainContext execChainContext = invocation.getContext();
        int taskid = execChainContext.getTaskId();
        log.info("phase:" + FullbuildPhase.desc(this.getPhase()) + " start ,taskid:" + taskid);
        // 开始执行一个新的phase需要通知console
        // final int phaseId = createNewPhase(taskid, FullbuildPhase.getFirst(this.getPhase()));
        ExecuteResult result = null;
        try {
            result = this.execute(execChainContext);
            if (!result.isSuccess()) {
                log.error("taskid:" + taskid + ",phase:" + FullbuildPhase.desc(this.getPhase()) + " faild,reason:" + result.getMessage());
            }
        } catch (Exception e) {
            // }
            throw e;
        }
        if (result.isSuccess()) {
            return invocation.invoke();
        } else {
            log.error("full build job is failed");
            // StringUtils.EMPTY);
            return result;
        }
    }

    /**
     * 执行
     *
     * @param execChainContext
     * @return
     * @throws Exception
     */
    protected abstract ExecuteResult execute(IExecChainContext execChainContext) throws Exception;

    /**
     * 创建新的Task执行结果
     */
    public static class IntegerAjaxResult extends AjaxResult<Integer> {
    }

    public static class CreateNewTaskResult {

        private int taskid;

        private Application app;

        public CreateNewTaskResult() {
            super();
        }

        public int getTaskid() {
            return taskid;
        }

        public void setTaskid(int taskid) {
            this.taskid = taskid;
        }

        public void setApp(Application app) {
            this.app = app;
        }

        public Application getApp() {
            return app;
        }
    }

}
