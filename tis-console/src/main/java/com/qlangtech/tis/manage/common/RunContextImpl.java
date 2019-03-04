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

import org.apache.solr.common.cloud.TISZkStateReader;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.manage.biz.dal.dao.IAppPackageDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IAppTriggerJobRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;

import com.qlangtech.tis.manage.biz.dal.dao.IBizFuncAuthorityDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IGlobalAppResourceDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IGroupInfoDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerGroupDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerJoinGroupDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotViewDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrApplyDptRecordDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptExtraRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.spring.ClusterStateReader;
import com.qlangtech.tis.manage.spring.ZooKeeperGetter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RunContextImpl implements RunContext {

	private final IAppPackageDAO appPackageDAO;

	private final IApplicationDAO applicationDAO;

	private final IGroupInfoDAO groupInfoDAO;

	private final IServerDAO serverDAO;

	private final IServerGroupDAO serverGroupDAO;

	private final ISnapshotDAO snapshotDAO;

	private final ISnapshotViewDAO snapshotViewDAO;

	private final IUploadResourceDAO uploadResourceDAO;

	private final IServerJoinGroupDAO serverJoinGroupDAO;

	private IBizFuncAuthorityDAO bizFuncAuthorityDAO;

	private final IGlobalAppResourceDAO globalAppResourceDAO;

	private final IAppTriggerJobRelationDAO appTriggerJobRelationDAO;

	private final IFuncDAO funcDAO;

	private final IFuncRoleRelationDAO funcRoleRelationDAO;

	private final IRoleDAO roleDAO;

	private final IResourceParametersDAO resourceParametersDAO;

	private final ZooKeeperGetter zooKeeperGetter;

	private final ClusterStateReader clusterStateReader;

	public RunContextImpl(IAppPackageDAO appPackageDAO, IApplicationDAO applicationDAO,

			IGroupInfoDAO groupInfoDAO, IServerDAO serverDAO, IServerGroupDAO serverGroupDAO, ISnapshotDAO snapshotDAO,
			ISnapshotViewDAO snapshotViewDAO, IUploadResourceDAO uploadResourceDAO,
			IServerJoinGroupDAO serverJoinGroupDAO, IBizFuncAuthorityDAO bizFuncAuthorityDAO,
			IUsrDptRelationDAO usrDptRelationDAO, IDepartmentDAO departmentDAO,
			IGlobalAppResourceDAO globalAppResourceDAO, IAppTriggerJobRelationDAO appTriggerJobRelationDAO,
			IFuncDAO funcDAO, IFuncRoleRelationDAO funcRoleRelationDAO, IRoleDAO roleDAO,
			IResourceParametersDAO resourceParametersDAO, IUsrDptExtraRelationDAO usrDptExtraRelationDAO,
			IUsrApplyDptRecordDAO usrApplyDptRecordDAO, ZooKeeperGetter zooKeeperGetter,
			ClusterStateReader clusterStateReader) {
		super();
		this.appPackageDAO = appPackageDAO;
		this.applicationDAO = applicationDAO;
		// this.bizDomainDAO = bizDomainDAO;
		this.groupInfoDAO = groupInfoDAO;
		this.serverDAO = serverDAO;
		this.serverGroupDAO = serverGroupDAO;
		this.snapshotDAO = snapshotDAO;
		this.snapshotViewDAO = snapshotViewDAO;
		this.uploadResourceDAO = uploadResourceDAO;
		// this.authService = authService;
		this.serverJoinGroupDAO = serverJoinGroupDAO;
		// this.orgService = orgService;
		this.bizFuncAuthorityDAO = bizFuncAuthorityDAO;
		this.usrDptRelationDAO = usrDptRelationDAO;
		this.departmentDAO = departmentDAO;
		this.globalAppResourceDAO = globalAppResourceDAO;
		this.appTriggerJobRelationDAO = appTriggerJobRelationDAO;

		this.usrApplyDptRecordDAO = usrApplyDptRecordDAO;
		// add for implementing authority system
		this.funcDAO = funcDAO;
		this.funcRoleRelationDAO = funcRoleRelationDAO;
		this.roleDAO = roleDAO;
		this.resourceParametersDAO = resourceParametersDAO;
		this.usrDptExtraRelationDAO = usrDptExtraRelationDAO;

		this.zooKeeperGetter = zooKeeperGetter;
		this.clusterStateReader = clusterStateReader;
	}

	@Override
	public TISZkStateReader getZkStateReader() {
		return this.clusterStateReader.getInstance();
	}

	@Override
	public TisZkClient getSolrZkClient() {
		return zooKeeperGetter.getInstance();
	}

	@Override
	public IUsrDptExtraRelationDAO getUsrDptExtraRelationDAO() {
		return this.usrDptExtraRelationDAO;
	}

	@Override
	public IResourceParametersDAO getResourceParametersDAO() {
		return this.resourceParametersDAO;
	}

	@Override
	public IFuncDAO getFuncDAO() {
		return this.funcDAO;
	}

	@Override
	public IFuncRoleRelationDAO getFuncRoleRelationDAO() {
		return this.funcRoleRelationDAO;
	}

	@Override
	public IRoleDAO getRoleDAO() {
		return this.roleDAO;
	}

	@Override
	public IAppTriggerJobRelationDAO getAppTriggerJobRelationDAO() {
		return appTriggerJobRelationDAO;
	}

	@Override
	public IGlobalAppResourceDAO getGlobalAppResourceDAO() {
		return this.globalAppResourceDAO;
	}

	public IUploadResourceDAO getUploadResourceDAO() {
		return uploadResourceDAO;
	}

	public IServerJoinGroupDAO getServerJoinGroupDAO() {
		return serverJoinGroupDAO;
	}

	@Override
	public IBizFuncAuthorityDAO getBizFuncAuthorityDAO() {
		return bizFuncAuthorityDAO;
	}

	public ISnapshotViewDAO getSnapshotViewDAO() {
		return snapshotViewDAO;
	}

	@Override
	public IAppPackageDAO getAppPackageDAO() {
		return appPackageDAO;
	}

	@Override
	public IApplicationDAO getApplicationDAO() {
		return applicationDAO;
	}

	@Override
	public IGroupInfoDAO getGroupInfoDAO() {
		return groupInfoDAO;
	}

	@Override
	public IServerGroupDAO getServerGroupDAO() {
		return serverGroupDAO;
	}

	@Override
	public ISnapshotDAO getSnapshotDAO() {
		return snapshotDAO;
	}

	private final IUsrDptRelationDAO usrDptRelationDAO;

	private final IUsrDptExtraRelationDAO usrDptExtraRelationDAO;

	private final IDepartmentDAO departmentDAO;

	private final IUsrApplyDptRecordDAO usrApplyDptRecordDAO;

	public IUsrDptRelationDAO getUsrDptRelationDAO() {
		return this.usrDptRelationDAO;
	}

	public IDepartmentDAO getDepartmentDAO() {
		return this.departmentDAO;
	}

	public IUsrApplyDptRecordDAO getUsrApplyDptRecordDAO() {
		return this.usrApplyDptRecordDAO;
	}

}
