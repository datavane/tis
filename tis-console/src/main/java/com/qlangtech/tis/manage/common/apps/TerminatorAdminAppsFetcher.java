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

import java.util.ArrayList;
import java.util.List;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorAdminAppsFetcher extends NormalUserApplicationFetcher {

    public TerminatorAdminAppsFetcher(IUser user, Department department, RunContext context) {
        super(user, department, context);
    }

    @Override
    protected Criteria process(Criteria criteria) {
        // return criteria.andDptIdEqualTo(user.getDepartmentid());
        return criteria;
    }

    @Override
    public List<TriggerCrontab> getTriggerTabs(IUsrDptRelationDAO usrDptRelationDAO) {
        return getAllTriggerTabs(usrDptRelationDAO);
    }

    /**
     * @param usrDptRelationDAO
     * @return
     */
    public static List<TriggerCrontab> getAllTriggerTabs(IUsrDptRelationDAO usrDptRelationDAO) {
        UsrDptRelationCriteria ucriteria = new UsrDptRelationCriteria();
        ucriteria.createCriteria().andIsAutoDeploy();
        // 应用触发器一览
        return usrDptRelationDAO.selectAppDumpJob(ucriteria);
    }

    // @Override
    // public List<ApplicationApply> getAppApplyList(
    // IApplicationApplyDAO applicationApplyDAO) {
    // 
    // return super.getAppApplyList(applicationApplyDAO);
    // }
    @Override
    public List<Department> getDepartmentBelongs(RunContext runContext) {
        DepartmentCriteria criteria = new DepartmentCriteria();
        criteria.createCriteria().andIsLeaf();
        return runContext.getDepartmentDAO().selectByExample(criteria, 1, 500);
    }

    @Override
    protected void setApplicationApplyCriteria(com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApplyCriteria.Criteria criteria) {
    }

    @Override
    protected List<String> initAuthorityFuncList() {
        return new ArrayList<String>() {

            private static final long serialVersionUID = 0;

            @Override
            public boolean contains(Object o) {
                return true;
            }
        };
    }
}
