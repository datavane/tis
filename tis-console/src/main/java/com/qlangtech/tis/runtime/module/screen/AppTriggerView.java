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
package com.qlangtech.tis.runtime.module.screen;

import java.util.Arrays;
import java.util.List;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelationCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.trigger.module.screen.BasicAppTriggerView;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppTriggerView extends BasicAppTriggerView {

    /**
     */
    private static final long serialVersionUID = 1L;

    @Override
    @Func(PermissionConstant.TRIGGER_JOB_LIST)
    public void execute(Context context) throws Exception {
        // 列出所有的定时任务
        super.execute(context);
        context.put("enable_aselect_app_domain", true);
        context.put("readonly", true);
        AppTriggerJobRelationCriteria criteria = new AppTriggerJobRelationCriteria();
        AppDomainInfo appdomain = this.getAppDomain();
        criteria.createCriteria().andAppIdEqualTo(appdomain.getAppid()).andJobTypeBetween((byte) 1, (byte) 2);
        List<AppTriggerJobRelation> triggerlist = this.getAppTriggerJobRelationDAO().selectByExample(criteria, 1, 20);
        TriggerCrontab triggerFunc = new TriggerCrontab();
        triggerFunc.setAppId(appdomain.getAppid());
        triggerFunc.setAppName(appdomain.getAppName());
        for (AppTriggerJobRelation trigger : triggerlist) {
            if (trigger.getJobType() == (byte) 2) {
                // 增量
                triggerFunc.setIcrontab(trigger.getCrontab());
                triggerFunc.setIjobId(trigger.getJobId().intValue());
                // triggerFunc.setAppName(trigger.getProjectName());
                triggerFunc.setIstop("Y".equalsIgnoreCase(trigger.getIsStop()));
            // triggerFunc.setAppName(trigger.getProjectName());
            // triggerFunc.setAppId(this.getAppDomain().getAppid());
            } else if (trigger.getJobType() == (byte) 1) {
                // 全量
                // triggerFunc.setAppId(this.getAppDomain().getAppid());
                triggerFunc.setFcrontab(trigger.getCrontab());
                triggerFunc.setFjobId(trigger.getJobId().intValue());
                triggerFunc.setFstop("Y".equalsIgnoreCase(trigger.getIsStop()));
            } else {
                throw new IllegalStateException("trigger type :" + trigger.getJobType());
            }
        }
        context.put("triggerlist", Arrays.asList(triggerFunc));
        this.forward("appListTemplate.vm");
    }
}
