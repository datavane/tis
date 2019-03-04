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

import static com.qlangtech.tis.trigger.biz.dal.dao.JobConstant.DOMAIN_TERMINAOTR;
import static com.qlangtech.tis.trigger.biz.dal.dao.JobConstant.JOB_INCREASE_DUMP;
import static com.qlangtech.tis.trigger.biz.dal.dao.JobConstant.JOB_TYPE_FULL_DUMP;
import java.util.Date;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.quartz.CronExpression;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.trigger.biz.dal.dao.ITriggerBizDalDAOFacade;
import com.qlangtech.tis.trigger.biz.dal.pojo.TriggerJob;
import com.qlangtech.tis.trigger.biz.dal.pojo.TriggerJobCriteria;
import com.qlangtech.tis.trigger.module.screen.Setappstatus;
import com.qlangtech.tis.trigger.module.screen.TriggerBasicModule;

/* 
 * 配置全量构建任务，触发时间相关
 * 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TriggerAction extends TriggerBasicModule {

    private static final long serialVersionUID = 1L;

    private static final int CRONTAB_EXPRESS = 1;

    private static final int HOUR_OF_DAY_EXPRESS = 2;

    private static final int MINUTE_OF_HOUR_EXPRESS = 3;

    /**
     * 设置全量crontab
     *
     * @param nav
     * @param context
     */
    @Func(PermissionConstant.TRIGGER_JOB_SET)
    public void doSetFullDumpCrontab(Context context) {
        this.setDumpCrontab(context, true);
    }

    /**
     * 暂停定时程序
     */
    @Func(PermissionConstant.TRIGGER_JOB_PAUSE_RUN_SET)
    public void doPause(Context context) throws Exception {
        String appname = this.getString("app");
        Assert.assertNotNull(appname);
        this.getTriggerJobConsole().pause(Setappstatus.getApplication(this.getRequest(), this, appname).getProjectName());
        this.addActionMessage(context, "应用:" + appname + "定时任务停止成功");
    }

    /**
     * 重新启动定时程序
     *
     * @param context
     */
    @Func(PermissionConstant.TRIGGER_JOB_PAUSE_RUN_SET)
    public void doResume(Context context) throws Exception {
        String appname = this.getString("app");
        Assert.assertNotNull(appname);
        this.getTriggerJobConsole().resume(Setappstatus.getApplication(this.getRequest(), this, appname).getProjectName());
        this.addActionMessage(context, "应用:" + appname + "定时任务启动成功");
    }

    /**
     * 取得单个应用app状态,ajax
     *
     * @param context
     * @throws Exception
     */
    // @Func(PermissionConstant.PERMISSION_CORE_CONFIG_RESOURCE_MANAGE)
    public void doGetAppTrigger(Context context) throws Exception {
        final Integer appid = this.getInt("appid");
        Assert.assertNotNull(appid);
        // BizFuncAuthorityCriteria criteria = AppList
        // .createBizFuncAuthorityCriteria(context, this,
        // new BizFuncAuthorityCriteriaSetter() {
        // @Override
        // public void set(Criteria criteria) {
        // criteria.andAppIdEqualTo(appid);
        // }
        // });
        // 
        // // 列出所有的定时任务,某个应用下的定时任务，正常情况下应该只有一条
        // List<BizFuncAuthority> authlist = this.getBizFuncAuthorityDAO()
        // .selectAppDumpJob(criteria);
        UsrDptRelationCriteria criteria = new UsrDptRelationCriteria();
        criteria.createCriteria().andAppIdEqual(appid);
        for (TriggerCrontab auth : this.getUsrDptRelationDAO().selectAppDumpJob(criteria)) {
            JSONObject result = new JSONObject();
            result.put("fullcron", auth.getFcrontab());
            result.put("fjobId", auth.getFjobId());
            result.put("inccron", auth.getIcrontab());
            result.put("ijobId", auth.getIjobId());
            result.put("ispause", ManageUtils.isInPauseState(auth));
            this.writeJson(new StringBuffer(result.toString()));
            return;
        }
    }

    /**
     * 设置增量crontab
     *
     * @param nav
     * @param context
     */
    @Func(PermissionConstant.TRIGGER_JOB_SET)
    public void doSetIncreaseDumpCrontab(Context context) {
        this.setDumpCrontab(context, false);
    }

    /**
     * 更新增量dump crontab
     *
     * @param nav
     * @param context
     */
    @Func(PermissionConstant.TRIGGER_JOB_SET)
    public void doUpdateIncreaseDumpCrontab(Context context) {
        updateCrontab(context, false);
    }

    /**
     * 更新全量
     *
     * @param nav
     * @param context
     */
    @Func(PermissionConstant.TRIGGER_JOB_SET)
    public void doUpdateFullDumpCrontab(Context context) {
        updateCrontab(context, true);
    }

    private void updateCrontab(Context context, boolean fulldump) {
        Integer jobid = this.getInt("jobid");
        Assert.assertNotNull(jobid);
        final String crontab = parseCrontab(fulldump, context);
        if (crontab == null) {
            return;
        }
        if (this.updateJob(context, jobid, crontab, fulldump ? JOB_TYPE_FULL_DUMP : JOB_INCREASE_DUMP)) {
            this.addActionMessage(context, "更新" + (fulldump ? "全量" : "增量") + "定时任务表达式:“" + crontab + "”成功");
        }
    }

    private String parseCrontab(boolean fulldump, Context context) {
        Integer inputType = this.getInt("inputtype");
        if (inputType == null) {
            this.addErrorMessage(context, "请选择表达式类型");
            return null;
        }
        String crontab = null;
        if (inputType == CRONTAB_EXPRESS) {
            crontab = this.getString("crontab");
            if (StringUtils.isEmpty(crontab)) {
                this.addErrorMessage(context, "CronTab表达式不能为空");
                return null;
            }
            if (!CronExpression.isValidExpression(crontab)) {
                this.addErrorMessage(context, "Crontab:" + crontab + "表达式不正确");
                return null;
            }
        } else if (!fulldump && inputType == MINUTE_OF_HOUR_EXPRESS) {
            // 增量任务
            Integer minute = this.getInt("minute");
            if (minute < 0 || minute > 59) {
                this.addErrorMessage(context, "分钟数必须在0～59之间");
                return null;
            }
            crontab = "0 0/" + minute + " * * * ?";
        } else if (fulldump && inputType == HOUR_OF_DAY_EXPRESS) {
            // 全量任务
            Integer hour = this.getInt("hours");
            if (hour > 23 || hour < 0) {
                this.addErrorMessage(context, "小时数必须在0～23之间");
                return null;
            }
            crontab = "0 0 " + hour + " * * ?";
        } else {
            throw new IllegalArgumentException("full dump set,illegal input type value: " + inputType);
        }
        return crontab;
    }

    /**
     * 创建job
     *
     * @param context
     * @param crontab
     * @param jobtype
     */
    public static boolean createJob(Integer appid, Context context, String crontab, byte jobtype, BasicModule basicModule, ITriggerBizDalDAOFacade triggerContext) {
        // final Integer appid = this.getInt("appid");
        Assert.assertNotNull(triggerContext);
        Assert.assertNotNull(basicModule);
        Assert.assertNotNull(appid);
        Application application = basicModule.getApplicationDAO().loadFromWriteDB(appid);
        Assert.assertNotNull(application);
        AppTriggerJobRelationCriteria criteria = new AppTriggerJobRelationCriteria();
        criteria.createCriteria().andAppIdEqualTo(appid).andJobTypeEqualTo(jobtype);
        if (basicModule.getAppTriggerJobRelationDAO().countByExample(criteria) > 0) {
            basicModule.addErrorMessage(context, "已经设置了DUMP，不能重复操作");
            return false;
        }
        TriggerJob triggerJob = new TriggerJob();
        triggerJob.setCrontab(crontab);
        triggerJob.setGmtCreate(new Date());
        triggerJob.setDomain(DOMAIN_TERMINAOTR);
        // 新增一条JOB记录
        Long jobid = triggerContext.getTriggerJobDAO().insertSelective(triggerJob);
        // 添加一条job和app的关联记录
        AppTriggerJobRelation triggerRelation = new AppTriggerJobRelation();
        triggerRelation.setCrontab(crontab);
        triggerRelation.setGmtCreate(new Date());
        triggerRelation.setGmtModified(new Date());
        triggerRelation.setJobType(jobtype);
        triggerRelation.setJobId(jobid);
        triggerRelation.setAppId(appid);
        triggerRelation.setProjectName(application.getProjectName());
        basicModule.getAppTriggerJobRelationDAO().insertSelective(triggerRelation);
        return true;
    }

    /**
     * 更新job
     *
     * @param context
     * @param jobid
     * @param crontab
     * @param jobtype
     * @return
     */
    private boolean updateJob(Context context, Integer jobid, String crontab, byte jobtype) {
        final Integer appid = this.getInt("appid");
        Assert.assertNotNull(appid);
        TriggerJob record = new TriggerJob();
        record.setCrontab(crontab);
        TriggerJobCriteria triggerCriteria = new TriggerJobCriteria();
        triggerCriteria.createCriteria().andJobIdEqualTo(new Long(jobid)).andDomainEqualTo(DOMAIN_TERMINAOTR).andCrontabNotEqualTo(crontab);
        // 新增一条JOB记录
        this.getTriggerJobDAO().updateByExampleSelective(record, triggerCriteria);
        // 添加一条job和app的关联记录
        AppTriggerJobRelation triggerRelation = new AppTriggerJobRelation();
        triggerRelation.setCrontab(crontab);
        // triggerRelation.setGmtCreate(new Date());
        // triggerRelation.setJobType(jobtype);
        // triggerRelation.setJobId(jobid);
        // triggerRelation.setAppId(appid);
        AppTriggerJobRelationCriteria triggerRelationCriteria = new AppTriggerJobRelationCriteria();
        triggerRelationCriteria.createCriteria().andJobIdEqualTo(new Long(jobid)).andJobTypeEqualTo(jobtype).andAppIdEqualTo(appid).andCrontabNotEqualTo(crontab);
        this.getAppTriggerJobRelationDAO().updateByExampleSelective(triggerRelation, triggerRelationCriteria);
        // this.getAppTriggerJobRelationDAO().insertSelective(triggerRelation);
        return true;
    }

    private void setDumpCrontab(Context context, boolean fulldump) {
        final String crontab = parseCrontab(fulldump, context);
        if (crontab == null) {
            return;
        }
        if (createJob(this.getInt("appid"), context, crontab, fulldump ? JOB_TYPE_FULL_DUMP : JOB_INCREASE_DUMP, this, this.getTriggerBizDalDaoFacade())) {
            this.addActionMessage(context, "设置" + (fulldump ? "全量" : "增量") + "定时任务表达式:“" + crontab + "”成功");
        }
    }
    // private TriggerJobConsole triggerJobConsole;
    // 
    // public TriggerJobConsole getTriggerJobConsole() {
    // return triggerJobConsole;
    // }
    // 
    // @Autowired
    // public void setTriggerJobConsole(TriggerJobConsole triggerJobConsole) {
    // this.triggerJobConsole = triggerJobConsole;
    // }
}
