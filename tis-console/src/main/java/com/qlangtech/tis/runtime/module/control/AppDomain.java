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
package com.qlangtech.tis.runtime.module.control;

import org.apache.commons.lang.StringUtils;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.CheckAppDomainExistValve;
import com.qlangtech.tis.pubhook.common.Nullable;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.runtime.module.action.ChangeDomainAction;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AppDomain extends BasicModule {

    /**
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param groupName
     */
    public AppDomain() {
        super(StringUtils.EMPTY);
    }

    public void execute(Context context) throws Exception {
        AppDomainInfo appDomain = getAppDomain();
        if (appDomain == null) {
            // 跳转到指定上下文的页面
            // .withTarget("changedomain");
            getRundataInstance().redirectTo("changedomain");
            return;
        }
        Department department = null;
        // final Department department = this.getDepartmentDAO().loadFromWriteDB(
        // appDomain.getDptid());
        // Application app = this.getApplicationDAO().loadFromWriteDB(
        // appDomain.getAppid());
        Application app = new Application();
        app.setProjectName(appDomain.getAppName());
        // 校验是否选择了当前应用？
        if ((appDomain instanceof Nullable) || !shallSelectApp(department, app)) {
            appDomain = CheckAppDomainExistValve.createNull();
            context.put(ChangeDomainAction.SELECT_APP_NAME, getNotSelectDomainCaption());
        } else {
            boolean shallnotShowEnvironment = (context.get("shallnotShowEnvironment") != null) && (Boolean) context.get("shallnotShowEnvironment");
            context.put(ChangeDomainAction.SELECT_APP_NAME, getAppDesc(appDomain, department, app, shallnotShowEnvironment));
        }
        context.put("dptid", appDomain.getDptid());
        context.put("appid", appDomain.getAppid());
        context.put("runid", appDomain.getRunEnvironment().getId());
    }

    protected String getNotSelectDomainCaption() {
        return "您尚未选择应用";
    }

    protected boolean shallSelectApp(com.qlangtech.tis.manage.biz.dal.pojo.Department department, Application app) {
        // return department != null && app != null;
        return true;
    }

    protected String getAppDesc(AppDomainInfo appDomain, Department department, Application app, boolean shallnotShowEnvironment) {
        return getAppDescribe(app);
    }

    public static String getAppDescribe(Application app) {
        return app.getProjectName();
    // if (StringUtils.contains(app.getDptName(), "-")) {
    // return StringUtils.substringAfterLast(app.getDptName(), "-") + "["
    // + app.getProjectName() + "]";
    // } else {
    // return app.getDptName() + "[" + app.getProjectName() + "]";
    // }
    }
}
