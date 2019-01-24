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
package com.qlangtech.tis.trigger.module.action;

import java.util.List;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.trigger.module.screen.Triggermonitor;
import com.qlangtech.tis.trigger.rmi.JobDesc;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TriggermonitorAjax extends Triggermonitor {

    private static final long serialVersionUID = 1L;

    public void doTaskSchedule(Context context) throws Exception {
        this.execute(context);
    }

    /**
     * @param context
     * @throws Exception
     */
    public void doGetRecentErrorJobs(Context context) throws Exception {
        context.put("biz_result", this.getTaskDAO().getRecentExecuteJobs(this.getAppDomain().getRunEnvironment().getKeyName()));
    }

    @Override
    protected void processJoblist(Context context, List<JobDesc> joblist) throws Exception {
        StringBuffer execResult = new StringBuffer();
        execResult.append("{");
        execResult.append("\"result\":[");
        execResult.append("{\"jobid\":0,").append("\"triggerdate\":\"\"}\n");
        int i = 0;
        for (JobDesc job : joblist) {
            i++;
            if (job.getPreviousFireTime() == null) {
                continue;
            }
            execResult.append(",");
            execResult.append("{\"jobid\":").append(job.getJobid()).append(",").append("\"triggerdate\":\"").append(ManageUtils.formatDateYYYYMMdd(job.getPreviousFireTime())).append("\"}\n");
        // if (i < joblist.size()) {
        // 
        // }
        }
        execResult.append("]}");
        writeJson(execResult);
    }

    @Override
    protected void forward() {
    }
}
