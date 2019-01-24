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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import junit.framework.Assert;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationApplyDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApply;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApplyCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.ManageUtils;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NormalUserApplicationFetcher extends AppsFetcher {

    private final AbandonRepeatList belongDpt = new AbandonRepeatList();

    private List<Integer> dptids;

    public NormalUserApplicationFetcher(IUser user, Department department, RunContext context) {
        super(user, department, context);
        processDepartment(department, context);
    }

    protected void processDepartment(Department department, RunContext context) {
        processBelongDepartment(context.getDepartmentDAO().loadFromWriteDB(department.getDptId()), context);
        postCreateBelongDpt(belongDpt);
        this.dptids = new ArrayList<Integer>(belongDpt.addIds);
    }

    private static class AbandonRepeatList extends ArrayList<Department> {

        private static final long serialVersionUID = 1L;

        private final Set<Integer> addIds = new HashSet<Integer>();

        @Override
        public boolean add(Department e) {
            if (addIds.contains(e.getDptId())) {
                return false;
            }
            addIds.add(e.getDptId());
            return super.add(e);
        }

        @Override
        public boolean addAll(Collection<? extends Department> c) {
            for (Department dpt : c) {
                this.add(dpt);
            }
            return true;
        }
    }

    protected void postCreateBelongDpt(List<Department> belongDpt) {
    }

    @Override
    public final List<ApplicationApply> getAppApplyList(IApplicationApplyDAO applicationApplyDAO) {
        ApplicationApplyCriteria query = new ApplicationApplyCriteria();
        query.setOrderByClause("app_id desc");
        return applicationApplyDAO.selectByExample(query);
    }

    protected void setApplicationApplyCriteria(ApplicationApplyCriteria.Criteria criteria) {
        List<Department> dpts = this.getDepartmentBelongs(context);
        List<Integer> dptlist = new ArrayList<Integer>();
        for (Department dpt : dpts) {
            dptlist.add(dpt.getDptId());
        }
        criteria.andDptIdIn(dptlist);
    }

    private void processBelongDepartment(Department department, RunContext context) {
        if (true) {
            return;
        }
        Assert.assertNotNull("department can not be null", department);
        Assert.assertNotNull("context can not be null", context);
        // 部门是叶子节点吗？
        if (department.getLeaf()) {
            belongDpt.add(department);
        } else {
            DepartmentCriteria query = new DepartmentCriteria();
            query.createCriteria().andParentIdEqualTo(department.getDptId());
            for (Department dpt : context.getDepartmentDAO().selectByExample(query)) {
                processBelongDepartment(dpt, context);
            }
        }
    }

    // NormalUserApplicationFetcher(TUser user, RunContext context) {
    // super(user, context);
    // }
    @Override
    public final List<Application> getApps(CriteriaSetter setter) {
        ApplicationCriteria criteria = createCriteria(setter);
        criteria.setOrderByClause("app_id desc");
        return this.getApplicationDAO().selectByExample(criteria, 1, 500);
    }

    @Override
    public final int count(CriteriaSetter setter) {
        ApplicationCriteria criteria = createCriteria(setter);
        return this.getApplicationDAO().countByExample(criteria);
    }

    private ApplicationCriteria createCriteria(CriteriaSetter setter) {
        ApplicationCriteria criteria = new ApplicationCriteria();
        setter.set(process(criteria.createCriteria()));
        return criteria;
    }

    @Override
    public int update(Application app, CriteriaSetter setter) {
        ApplicationCriteria criteria = createCriteria(setter);
        return this.getApplicationDAO().updateByExampleSelective(app, criteria);
    }

    protected ApplicationCriteria.Criteria process(ApplicationCriteria.Criteria criteria) {
        // } else {
        return criteria.andDptIdIn(dptids);
    // }
    }

    @Override
    public List<Department> getDepartmentBelongs(RunContext runcontext) {
        return this.belongDpt;
    }

    @Override
    public List<TriggerCrontab> getTriggerTabs(IUsrDptRelationDAO usrDptRelationDAO) {
        UsrDptRelationCriteria ucriteria = new UsrDptRelationCriteria();
        // if (this.dpt.getLeaf()) {
        ucriteria.createCriteria().andDptIdIn(dptids).andIsAutoDeploy();
        // 应用触发器一览
        return usrDptRelationDAO.selectAppDumpJob(ucriteria);
    }

    @SuppressWarnings("all")
    @Override
    protected List<String> initAuthorityFuncList() {
        // .selectFuncListByUsrid(user.getId()));
        return new ArrayList<String>() {

            @Override
            public boolean contains(Object o) {
                return true;
            }
        };
    // }
    // try {
    // // 线上环境
    // List<String> authorityFuncList = new ArrayList<String>();
    // SimpleSSOUser ssoUser;
    // 
    // ssoUser = SimpleUserUtil
    // .findUser(ServletActionContext.getRequest());
    // String userId = ssoUser.getId().toString();
    // UserPermissionCondition userPermissionCondition = new
    // UserPermissionCondition();
    // userPermissionCondition.setUserId(userId);// 接入SSO登录后可获得,
    // // simpleSSOUser.getId()
    // List<String> appNames = new ArrayList<String>();
    // appNames.add("terminatorconsole");// 在ACL后台配置的应用英文名
    // userPermissionCondition.setAppNames(appNames);
    // List<Permission> results;
    // 
    // results = AclServiceProvider.getUserPermissionService()
    // .findPermissionByUser(userPermissionCondition);
    // for (Permission permission : results) {
    // authorityFuncList.add(permission.getName());
    // }
    // return authorityFuncList;
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    }
}
