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
package com.qlangtech.tis.manage.common;

import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppDomainInfo {

    private final Integer dptid;

    private final Integer appid;

    private final RunEnvironment runEnvironment;

    // private final RunContext context;
    private IApplicationDAO applicationDao;

    private String appName;

 

   

    public AppDomainInfo(Integer bizid, Integer appid, RunEnvironment runEnvironment, RunContext context) {
        this(bizid, appid, runEnvironment, context.getApplicationDAO());
    }

    public AppDomainInfo(Integer bizid, Integer appid, RunEnvironment runEnvironment, IApplicationDAO applicationDao) {
        super();
        judgeNull(applicationDao);
        this.dptid = bizid;
        this.appid = appid;
        this.runEnvironment = runEnvironment;
        // this.context = context;
        this.applicationDao = applicationDao;
    // return application.getProjectName();
    }

    public AppDomainInfo(Integer bizid, Integer appid, Integer runEnvironment, RunContext context) {
        this(bizid, appid, RunEnvironment.getEnum(runEnvironment.shortValue()), context);
    }

    public AppDomainInfo(Integer bizid, Integer appid, Integer runEnvironment, IApplicationDAO applicationDao) {
        this(bizid, appid, RunEnvironment.getEnum(runEnvironment.shortValue()), applicationDao);
    }

    public String getAppName() {
        if (StringUtils.isEmpty(this.appName)) {
            this.appName = this.applicationDao.selectByPrimaryKey(this.getAppid()).getProjectName();
        }
        return this.appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * 创建于应用无关的当前环境
     *
     * @param runEnvironment
     * @return
     */
    public static AppDomainInfo createAppNotAware(RunEnvironment runEnvironment) {
        return new EnvironmentAppDomainInfo(runEnvironment);
    }

    public static class EnvironmentAppDomainInfo extends AppDomainInfo {

        public EnvironmentAppDomainInfo(RunEnvironment runEnvironment) {
            super(-1, -1, runEnvironment, (IApplicationDAO) null);
        }

        @Override
        public String getAppName() {
            return StringUtils.EMPTY;
        }

        @Override
        protected void judgeNull(IApplicationDAO context) {
        }
    }

    public AppDomainInfo(Application app, Integer runEnvironment, RunContext context) {
        this(app.getDptId(), app.getAppId(), runEnvironment, context);
    }

    public AppDomainInfo(Application app, Integer runEnvironment, IApplicationDAO applicationDao) {
        this(app.getDptId(), app.getAppId(), runEnvironment, applicationDao);
    }

    protected void judgeNull(IApplicationDAO applicationDao) {
        // context) {
        if (applicationDao == null) {
            throw new IllegalArgumentException("context can not be null");
        }
    }

    // public Integer getRunId() {
    // return runEnvironment.getId().intValue();
    // }
    // 
    // public short getShortRunId() {
    // return runEnvironment.getId();
    // }
    // 
    // public String getRunEnvir() {
    // 
    // return getRunEnvir(this.runEnvironment);
    // }
    // public static String getRunEnvir(int runEnvironment) {
    // return RunEnvironment.getEnum((short) runEnvironment).getDescribe();
    // }
    public RunEnvironment getRunEnvironment() {
        return runEnvironment;
    }

    // public Integer getBizid() {
    // return dptid;
    // }
    public Integer getDptid() {
        return dptid;
    }

    public Integer getAppid() {
        return appid;
    }
}
