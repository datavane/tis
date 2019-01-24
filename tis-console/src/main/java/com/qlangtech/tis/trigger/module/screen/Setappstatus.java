/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.trigger.module.screen;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import junit.framework.Assert;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.UserUtils;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;
import com.qlangtech.tis.manage.common.apps.IAppsFetcher;
import com.qlangtech.tis.trigger.biz.dal.dao.TriggerJob;
import com.qlangtech.tis.trigger.biz.dal.pojo.AppTrigger;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Setappstatus extends TriggerBasicScreen {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        String appName = this.getString("app");
        Assert.assertNotNull(appName);
        Application application = getApplication(this.getRequest(), this, appName);
        // 
        AppTrigger appTrigger = getJobMetaDataDAO().queryJob(appName);
        if (appTrigger.getFullTrigger() == null && appTrigger.getIncTrigger() == null) {
            this.addErrorMessage(context, "还没有设定全量或者增量任务");
            return;
        }
        final TriggerJob fulltriggerJob = appTrigger.getFullTrigger();
        final TriggerJob incrTriggerJob = appTrigger.getIncTrigger();
        StringBuffer result = new StringBuffer("您确定要");
        if (ManageUtils.isInPauseState(fulltriggerJob != null, (fulltriggerJob != null && fulltriggerJob.isStop()), incrTriggerJob != null, (incrTriggerJob != null && incrTriggerJob.isStop()))) {
            result.append("开启");
            context.put("appset2stop", false);
        } else {
            result.append("停止");
            context.put("appset2stop", true);
        }
        // if (fulltriggerJob != null) {
        // if (fulltriggerJob.isStop()) {
        // result.append("开启");
        // context.put("appset2stop", false);
        // } else {
        // result.append("停止");
        // context.put("appset2stop", true);
        // }
        // } else if (incrTriggerJob != null) {
        // if (incrTriggerJob.isStop()) {
        // result.append("开启");
        // context.put("appset2stop", false);
        // } else {
        // result.append("停止");
        // context.put("appset2stop", true);
        // }
        // }
        result.append(application.getProjectName() + "的定时任务吗？");
        context.put("notice", result.toString());
    // this.getTriggerJobDAO().selectByPrimaryKey(jobId)
    }

    // private static boolean isInPauseState(boolean hasfulldump,
    // boolean fulljobStop, boolean hasincrdump, boolean incrjobStop) {
    // if (hasfulldump) {
    // return fulljobStop;
    // // if (fulljobStop) {
    // // return true;
    // // } else {
    // // return false;
    // // }
    // } else if (hasincrdump) {
    // return incrjobStop;
    // // if (incrjobStop) {
    // // return true;
    // // } else {
    // // return false;
    // // }
    // }
    // // 默认为停止状态
    // return true;
    // }
    public static Application getApplication(HttpServletRequest request, RunContext context, final String appName) {
        IAppsFetcher fetcher = UserUtils.getAppsFetcher(request, context);
        List<Application> applist = fetcher.getApps(new CriteriaSetter() {

            @Override
            public void set(Criteria criteria) {
                criteria.andProjectNameEqualTo(appName);
            }
        });
        // List<Application> applist = context.getApplicationDAO()
        // .selectByExample(appcriteria, 1, 1);
        Application application = null;
        for (Application app : applist) {
            application = app;
            break;
        }
        Assert.assertNotNull(application);
        return application;
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }
}
