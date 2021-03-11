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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.manage.biz.dal.dao.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TerminatorManageBizDalDAOFacadeImpl implements ITISManageBizDalDAOFacade {

  private final IUsrDptRelationDAO usrDptRelationDAO;

  private final IApplicationDAO applicationDAO;

  private final IDepartmentDAO departmentDAO;

  private final IFuncRoleRelationDAO funcRoleRelationDAO;

  private final IRoleDAO roleDAO;

  private final IFuncDAO funcDAO;

  public IUsrDptRelationDAO getUsrDptRelationDAO() {
    return this.usrDptRelationDAO;
  }

  public IApplicationDAO getApplicationDAO() {
    return this.applicationDAO;
  }

  public IDepartmentDAO getDepartmentDAO() {
    return this.departmentDAO;
  }

  public IFuncRoleRelationDAO getFuncRoleRelationDAO() {
    return this.funcRoleRelationDAO;
  }

  public IRoleDAO getRoleDAO() {
    return this.roleDAO;
  }

  public IFuncDAO getFuncDAO() {
    return this.funcDAO;
  }


  public TerminatorManageBizDalDAOFacadeImpl(// IIsvDAO isvDAO,
                                             IUsrDptRelationDAO usrDptRelationDAO, // IIsvDAO isvDAO,
                                             IApplicationDAO applicationDAO, // IIsvDAO isvDAO,
                                             IDepartmentDAO departmentDAO, // IIsvDAO isvDAO,
                                             IFuncRoleRelationDAO funcRoleRelationDAO, // IIsvDAO isvDAO,
                                             IRoleDAO roleDAO, // IIsvDAO isvDAO,
                                             IFuncDAO funcDAO // IIsvDAO isvDAO,
  ) {
    this.usrDptRelationDAO = usrDptRelationDAO;
    this.applicationDAO = applicationDAO;
    this.departmentDAO = departmentDAO;
    this.funcRoleRelationDAO = funcRoleRelationDAO;
    this.roleDAO = roleDAO;
    this.funcDAO = funcDAO;

  }
}
