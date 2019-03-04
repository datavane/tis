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
package com.qlangtech.tis.manage.common.apps;

import java.util.List;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationApplyDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApply;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelation;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AppsFetcher implements IAppsFetcher {

    protected final RunContext context;

    protected final IUser user;

    protected final Department dpt;

    private final List<String> authorityFuncList;

    private static final Log log = LogFactory.getLog(AppsFetcher.class);

    public static IAppsFetcher create(IUser user, RunContext context, boolean maxMach) {
        if (true || maxMach) {
            UsrDptRelation usr = user.getUsr();
            // context.getUsrDptRelationDAO()
            // .loadFromWriteDB(user.getId());
            Department dpt = new Department();
            // dpt.setDptId( usr.getDptId());
            // dpt.setFullName(usr.getDptName());
            dpt.setDptId(123);
            dpt.setFullName("dpt");
            return new AdminAppsFetcher(user, dpt, context);
        }
        // TUser user = UserUtils.getUser(DefaultFilter.getReqeust());
        Assert.assertNotNull("user can not be null", user);
        Assert.assertNotNull("context can not be null", context);
        // context.getUsrDptRelationDAO().loadFromWriteDB(user.getId());
        UsrDptRelation usr = user.getUsr();
        Department dpt = new Department();
        dpt.setDptId(usr.getDptId());
        dpt.setFullName(usr.getDptName());
        Assert.assertNotNull("dpt.getDptId() can not be null", dpt.getDptId());
        // if (user.getAppsFetcher() == null) {
        if (dpt.getDptId().equals(Config.getDptTisId())) {
            // TIS管理者
            return // user.setAppsFetcher(
            new AdminAppsFetcher(user, dpt, // );
            context);
        } else if (usr.isExtraDptRelation()) {
            return new NormalUserWithExtraDptsApplicationFetcher(user, dpt, context);
        } else {
            // 部门普通使用者
            return // user.setAppsFetcher(
            new NormalUserApplicationFetcher(user, dpt, // );
            context);
        }
    }

    public static IAppsFetcher create(IUser user, RunContext context) {
        return create(user, context, false);
    }

 
    @Override
    public boolean hasGrantAuthority(String permissionCode) {
        return this.authorityFuncList.contains(permissionCode);
    }

    protected final IApplicationDAO getApplicationDAO() {
        return context.getApplicationDAO();
    }

    protected AppsFetcher(IUser user, Department department, RunContext context) {
        super();
        this.context = context;
        this.user = user;
        this.dpt = department;
        this.authorityFuncList = this.initAuthorityFuncList();
        if (user != null && department != null && context != null) {
            log.warn("userid:" + user.getId() + ",name:" + user.getName() + ",class:" + this.getClass().getSimpleName() + ",this.authorityFuncList.class:" + this.authorityFuncList.getClass().getSimpleName() + ",this.authorityFuncList.size:" + this.authorityFuncList.size());
        }
    }

    protected abstract List<String> initAuthorityFuncList();

  
    @Override
    public abstract List<Application> getApps(CriteriaSetter setter);


    @Override
    public abstract int count(CriteriaSetter setter);


    @Override
    public abstract int update(Application app, CriteriaSetter setter);


    @Override
    public abstract List<Department> getDepartmentBelongs(RunContext runcontext);

    @Override
    public abstract List<TriggerCrontab> getTriggerTabs(IUsrDptRelationDAO usrDptRelationDAO);

  
    @Override
    public abstract List<ApplicationApply> getAppApplyList(IApplicationApplyDAO applicationApplyDAO);

    public static interface CriteriaSetter {

        void set(ApplicationCriteria.Criteria criteria);
    }
}
