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

import java.rmi.RemoteException;
import java.util.List;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.trigger.rmi.JobDesc;

/*
 * monitor all the jon trigger run in job server
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Triggermonitor extends TriggerBasicScreen {

	private static final long serialVersionUID = 1L;

	@Override
	public void execute(Context context) throws Exception {
		Integer jobid = this.getInt("jobid");
		String indexname = this.getString("indexname");
		// this.enableChangeDomain(context);
		this.disableNavigationBar(context);
		List<JobDesc> joblist = getJobList(indexname, jobid);
		processJoblist(context, joblist);
		forward();
	}

	protected void forward() {
		getRundataInstance().forwardTo(getTemplateName());
	}

	protected void processJoblist(Context context, List<JobDesc> joblist) throws Exception {
		context.put("joblist", joblist);
	}

	private List<JobDesc> getJobList(String indexname, Integer jobid) throws RemoteException {
		return (jobid == null || indexname == null) ? this.getTriggerJobConsole().getAllJobsInServer()
				: this.getTriggerJobConsole().getJob(indexname, jobid.longValue());
	}

	@Override
	public boolean isEnableDomainView() {
		return false;
	}

	protected String getTemplateName() {
		return "triggermonitortemplate.vm";
	}
}
