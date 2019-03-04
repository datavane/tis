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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;

import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ITisManageBizDalDAOFacade;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrApplyDptRecordDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisManageBizDalDAOFacadeImpl implements ITisManageBizDalDAOFacade {

	private final IUsrDptRelationDAO usrDptRelationDAO;

	private final IApplicationDAO applicationDAO;

	private final IDepartmentDAO departmentDAO;

	private final IFuncRoleRelationDAO funcRoleRelationDAO;

	private final IRoleDAO roleDAO;

	private final IFuncDAO funcDAO;

	private final IUsrApplyDptRecordDAO usrApplyDptRecordDAO;

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

	public TisManageBizDalDAOFacadeImpl(IUsrDptRelationDAO usrDptRelationDAO, IApplicationDAO applicationDAO,
			IDepartmentDAO departmentDAO, IFuncRoleRelationDAO funcRoleRelationDAO, IRoleDAO roleDAO, IFuncDAO funcDAO,
			IUsrApplyDptRecordDAO usrApplyDptRecordDAO // IIsvDAO isvDAO,
	) {
		this.usrDptRelationDAO = usrDptRelationDAO;
		this.applicationDAO = applicationDAO;
		this.departmentDAO = departmentDAO;
		this.funcRoleRelationDAO = funcRoleRelationDAO;
		this.roleDAO = roleDAO;
		this.funcDAO = funcDAO;
		this.usrApplyDptRecordDAO = usrApplyDptRecordDAO;
		// this.isvDAO = isvDAO;
	}
}
