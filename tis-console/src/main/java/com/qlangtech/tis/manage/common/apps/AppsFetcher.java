/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common.apps;

import com.qlangtech.tis.manage.biz.dal.dao.IApplicationApplyDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.*;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-1-28
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
            return new TerminatorAdminAppsFetcher(user, dpt, context);
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
        if (// dpt.getDptId().equals(Config.getDptTerminatorId())
        true) {
            return // user.setAppsFetcher(
            new // );
            TerminatorAdminAppsFetcher(// );
            user, // );
            dpt, context);
        } else if (usr.isExtraDptRelation()) {
            return new NormalUserWithExtraDptsApplicationFetcher(user, dpt, context);
        } else {
            // 部门普通使用者
            return // user.setAppsFetcher(
            new // );
            NormalUserApplicationFetcher(// );
            user, // );
            dpt, context);
        }
    }

    public static IAppsFetcher create(IUser user, RunContext context) {
        return create(user, context, false);
    }

    /*
   * (non-Javadoc)
   *
   * @see
   * com.taobao.terminator.manage.common.apps.IAppsFetcher#hasGrantAuthority
   * (java.lang.String)
   */
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

    /*
   * (non-Javadoc)
   *
   * @see
   * com.taobao.terminator.manage.common.apps.IAppsFetcher#getApps(com.taobao
   * .terminator.manage.common.apps.AppsFetcher.CriteriaSetter)
   */
    @Override
    public abstract List<Application> getApps(CriteriaSetter setter);

    /*
   * (non-Javadoc)
   *
   * @see
   * com.taobao.terminator.manage.common.apps.IAppsFetcher#count(com.taobao
   * .terminator.manage.common.apps.AppsFetcher.CriteriaSetter)
   */
    @Override
    public abstract int count(CriteriaSetter setter);

    /*
   * (non-Javadoc)
   *
   * @see
   * com.taobao.terminator.manage.common.apps.IAppsFetcher#update(com.taobao
   * .terminator.manage.biz.dal.pojo.Application,
   * com.taobao.terminator.manage.common.apps.AppsFetcher.CriteriaSetter)
   */
    @Override
    public abstract int update(Application app, CriteriaSetter setter);

    /*
   * (non-Javadoc)
   *
   * @see com.taobao.terminator.manage.common.apps.IAppsFetcher#
   * getDepartmentBelongs (com.taobao.terminator.manage.common.RunContext)
   */
    @Override
    public abstract List<Department> getDepartmentBelongs(RunContext runcontext);

    /*
   * (non-Javadoc)
   *
   * @see
   * com.taobao.terminator.manage.common.apps.IAppsFetcher#getTriggerTabs(
   * com.taobao.terminator.manage.biz.dal.dao.IUsrDptRelationDAO)
   */
    @Override
    public abstract List<TriggerCrontab> getTriggerTabs(IUsrDptRelationDAO usrDptRelationDAO);

    /*
   * (non-Javadoc)
   *
   * @see
   * com.taobao.terminator.manage.common.apps.IAppsFetcher#getAppApplyList
   * (com.taobao.terminator.manage.biz.dal.dao.IApplicationApplyDAO)
   */
    @Override
    public abstract List<ApplicationApply> getAppApplyList(IApplicationApplyDAO applicationApplyDAO);

    public static interface CriteriaSetter {

        void set(ApplicationCriteria.Criteria criteria);
    }
}
