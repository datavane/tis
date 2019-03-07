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
package com.qlangtech.tis.trigger.socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.qlangtech.tis.trigger.TriggerJobManage;
import com.qlangtech.tis.trigger.httpserver.impl.NullTriggerContext;

import junit.framework.Assert;

/*
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public final class TriggerJob implements Job {

	private static final Log log = LogFactory.getLog(TriggerJob.class);

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		JobDataMap data = context.getJobDetail().getJobDataMap();

		JobSchedule schedule = (JobSchedule) data.get(TriggerJobManage.JOB_SCHEDULE);
		TriggerJobManage triggerJobServer = (TriggerJobManage) data.get(TriggerJobManage.JOB_TRIGGER_SERVER);
		Assert.assertNotNull(schedule);
		// Assert.assertNotNull(conn);
		log.info("job was fire with trigger:" + schedule.getIndexName() + ",crontab:" + schedule.getCrobexp());
		// 执行全量任务开始
		try {
			triggerJobServer.triggerFullDump(schedule.getIndexName(), new NullTriggerContext());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new JobExecutionException(e);
		}
	}
}
