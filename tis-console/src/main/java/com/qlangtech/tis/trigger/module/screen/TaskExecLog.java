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
import junit.framework.Assert;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.trigger.biz.dal.dao.JobConstant;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskExecLogCriteria;

/*
 * 显示所有执行日志
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TaskExecLog extends TriggerBasicScreen {

    /**
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        this.getRundata().setLayout("blank");
        Integer taskid = this.getInt("taskid");
        Assert.assertNotNull(taskid);
        TaskExecLogCriteria criteria = new TaskExecLogCriteria();
        criteria.createCriteria().andTaskIdEqualTo(new Long(taskid)).andDomainEqualTo(JobConstant.DOMAIN_TERMINAOTR);
        criteria.setOrderByClause("exec_log_id desc");
        List<com.qlangtech.tis.trigger.biz.dal.pojo.TaskExecLog> taskList = this.getTaskExecLogDAO().selectByExampleWithoutBLOBs(criteria);
        context.put("loglist", taskList);
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }
}
