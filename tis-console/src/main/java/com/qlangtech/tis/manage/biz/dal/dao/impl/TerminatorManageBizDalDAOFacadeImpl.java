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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationExtendDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRdsDbDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRdsTableDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ITerminatorManageBizDalDAOFacade;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrApplyDptRecordDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TerminatorManageBizDalDAOFacadeImpl implements ITerminatorManageBizDalDAOFacade {

    private final IUsrDptRelationDAO usrDptRelationDAO;

    private final IApplicationDAO applicationDAO;

    private final IDepartmentDAO departmentDAO;

    private final IFuncRoleRelationDAO funcRoleRelationDAO;

    private final IRoleDAO roleDAO;

    private final IFuncDAO funcDAO;

    private final IUsrApplyDptRecordDAO usrApplyDptRecordDAO;

    // 聚石塔相关DAO
    // private final IIsvDAO isvDAO;
    private final IRdsDbDAO rdsDbDAO;

    private final IRdsTableDAO rdsTableDAO;

    private final IApplicationExtendDAO applicationExtendDAO;

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

    public IUsrApplyDptRecordDAO getUsrApplyDptRecordDAO() {
        return this.usrApplyDptRecordDAO;
    }

    // //聚石塔相关表
    // public IIsvDAO getIsvDAO() {
    // return this.isvDAO;
    // }
    // 
    public IRdsDbDAO getRdsDbDAO() {
        return this.rdsDbDAO;
    }

    public IRdsTableDAO getRdsTableDAO() {
        return this.rdsTableDAO;
    }

    public IApplicationExtendDAO getApplicationExtendDAO() {
        return this.applicationExtendDAO;
    }

    public TerminatorManageBizDalDAOFacadeImpl(// IIsvDAO isvDAO,
    IUsrDptRelationDAO usrDptRelationDAO, // IIsvDAO isvDAO,
    IApplicationDAO applicationDAO, // IIsvDAO isvDAO,
    IDepartmentDAO departmentDAO, // IIsvDAO isvDAO,
    IFuncRoleRelationDAO funcRoleRelationDAO, // IIsvDAO isvDAO,
    IRoleDAO roleDAO, // IIsvDAO isvDAO,
    IFuncDAO funcDAO, // IIsvDAO isvDAO,
    IUsrApplyDptRecordDAO usrApplyDptRecordDAO, IRdsDbDAO rdsDbDAO, IRdsTableDAO rdsTableDAO, IApplicationExtendDAO applicationExtendDAO) {
        this.usrDptRelationDAO = usrDptRelationDAO;
        this.applicationDAO = applicationDAO;
        this.departmentDAO = departmentDAO;
        this.funcRoleRelationDAO = funcRoleRelationDAO;
        this.roleDAO = roleDAO;
        this.funcDAO = funcDAO;
        this.usrApplyDptRecordDAO = usrApplyDptRecordDAO;
        // this.isvDAO = isvDAO;
        this.rdsDbDAO = rdsDbDAO;
        this.rdsTableDAO = rdsTableDAO;
        this.applicationExtendDAO = applicationExtendDAO;
    }
}
