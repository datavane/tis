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
import com.koubei.web.tag.pager.Pager;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelationCriteria;
import com.qlangtech.tis.trigger.biz.dal.dao.JobConstant;
import com.qlangtech.tis.trigger.biz.dal.pojo.TaskCriteria;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TaskList extends TriggerBasicScreen {

    private static final long serialVersionUID = 1L;

    @Override
    public void execute(Context context) throws Exception {
        this.disableNavigationBar(context);
        Integer jobid = this.getInt("jobid");
        Assert.assertNotNull(jobid);
        Integer page = this.getInt("page");
        page = (page == null) ? 1 : page;
        // this.getTriggerJobDAO().loadFromWriteDB(new Long(jobid));
        AppTriggerJobRelationCriteria acriteria = new AppTriggerJobRelationCriteria();
        acriteria.createCriteria().andJobIdEqualTo(new Long(jobid));
        List<AppTriggerJobRelation> joblist = this.getAppTriggerJobRelationDAO().selectByExample(acriteria);
        Assert.assertTrue("joblist the size must at least have one", joblist.size() > 0);
        for (AppTriggerJobRelation job : joblist) {
            context.put("triggerjob", job);
            break;
        }
        TaskCriteria criteria = new TaskCriteria();
        criteria.createCriteria().andDomainEqualTo(JobConstant.DOMAIN_TERMINAOTR).andJobIdEqualTo(new Long(jobid)).andRuntimeEqualTo(this.getAppDomain().getRunEnvironment().getKeyName());
        criteria.setOrderByClause("task_id desc");
        Pager pager = createPager();
        pager.setTotalCount(this.getTaskDAO().countByExample(criteria));
        pager.setCurPage(page);
        context.put("pager", pager);
        context.put("tasklist", this.getTaskDAO().selectByExample(criteria, page, PAGE_SIZE));
    }

    @Override
    protected StringBuffer getPagerUrl() {
        return new StringBuffer("?jobid=" + this.getInt("jobid"));
    }

    @Override
    public boolean isEnableDomainView() {
        return false;
    }
}
