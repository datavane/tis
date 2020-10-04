/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common.apps;

import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptRelationCriteria;
import com.qlangtech.tis.manage.common.IUser;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.TriggerCrontab;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-1-28
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
    criteria.createCriteria().andIsLeaf(true);
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
