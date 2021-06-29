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

/**
 *
 */
package com.qlangtech.tis.trigger;

import com.qlangtech.tis.trigger.impl.NullTriggerContext;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author 百岁（baisui@taobao.com）
 * @date 2012-6-25
 */
public final class QuartzTriggerJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(QuartzTriggerJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap data = context.getJobDetail().getJobDataMap();

        JobSchedule schedule = (JobSchedule) data.get(TriggerJobManage.JOB_SCHEDULE);
        TriggerJobManage triggerJobServer = (TriggerJobManage) data
                .get(TriggerJobManage.JOB_TRIGGER_SERVER);
        Objects.requireNonNull(schedule, "schedule can not be null");

        log.info("job was fire with trigger:" + schedule.getIndexName() + ",crontab:"
                + schedule.getCrobexp());
        // conn.trigger(//context.getTrigger(),
        // schedule.getJobid());

        // 执行全量任务开始
        try {
            triggerJobServer.triggerFullDump(schedule.getIndexName(), ExecType.FULLBUILD, new NullTriggerContext());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }

    }
}
