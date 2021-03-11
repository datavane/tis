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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.manage.biz.dal.dao.*;
import com.qlangtech.tis.manage.spring.ClusterStateReader;
import com.qlangtech.tis.manage.spring.ZooKeeperGetter;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import org.apache.solr.common.cloud.TISZkStateReader;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RunContextImpl implements RunContext {

  private final IApplicationDAO applicationDAO;
  private final IGroupInfoDAO groupInfoDAO;

  private final IServerDAO serverDAO;

  private final IServerGroupDAO serverGroupDAO;

  private final ISnapshotDAO snapshotDAO;

  private final ISnapshotViewDAO snapshotViewDAO;

  private final IUploadResourceDAO uploadResourceDAO;


  private final IAppTriggerJobRelationDAO appTriggerJobRelationDAO;

  // private final RpcCoreManage rpcCoreManage;
  private final IFuncDAO funcDAO;

  private final IFuncRoleRelationDAO funcRoleRelationDAO;

  private final IRoleDAO roleDAO;

  private final IResourceParametersDAO resourceParametersDAO;

  private final ZooKeeperGetter zooKeeperGetter;

  private final ClusterStateReader clusterStateReader;

  private final IWorkflowDAOFacade comDfireTisWorkflowDAOFacade;

  private final IUsrDptRelationDAO usrDptRelationDAO;

  private final IDepartmentDAO departmentDAO;

  private IBizFuncAuthorityDAO bizFuncAuthorityDAO;

  public RunContextImpl(
    IApplicationDAO applicationDAO, // AdminUserService
    IGroupInfoDAO groupInfoDAO, // AdminUserService
    IServerDAO serverDAO, // AdminUserService
    IServerGroupDAO serverGroupDAO, // AdminUserService
    ISnapshotDAO snapshotDAO, // AdminUserService
    ISnapshotViewDAO snapshotViewDAO, // OrgService orgService,
    IUploadResourceDAO uploadResourceDAO, // RpcCoreManage rpcCoreManage,
    IBizFuncAuthorityDAO bizFuncAuthorityDAO, // RpcCoreManage rpcCoreManage,
    IUsrDptRelationDAO usrDptRelationDAO, // RpcCoreManage rpcCoreManage,
    IDepartmentDAO departmentDAO, // RpcCoreManage rpcCoreManage,
    IAppTriggerJobRelationDAO appTriggerJobRelationDAO, // IIsvDAO isvDAO,
    IFuncDAO funcDAO, // IIsvDAO isvDAO,
    IFuncRoleRelationDAO funcRoleRelationDAO, // IIsvDAO isvDAO,
    IRoleDAO roleDAO, // IIsvDAO isvDAO,
    IResourceParametersDAO resourceParametersDAO, // IIsvDAO isvDAO,
    ZooKeeperGetter zooKeeperGetter //
    , ClusterStateReader clusterStateReader, IWorkflowDAOFacade workflowDAOFacade) {
    super();

    this.applicationDAO = applicationDAO;
    // this.bizDomainDAO = bizDomainDAO;
    this.groupInfoDAO = groupInfoDAO;
    this.serverDAO = serverDAO;
    this.serverGroupDAO = serverGroupDAO;
    this.snapshotDAO = snapshotDAO;
    this.snapshotViewDAO = snapshotViewDAO;
    this.uploadResourceDAO = uploadResourceDAO;
    this.bizFuncAuthorityDAO = bizFuncAuthorityDAO;
    this.usrDptRelationDAO = usrDptRelationDAO;
    this.departmentDAO = departmentDAO;
    this.appTriggerJobRelationDAO = appTriggerJobRelationDAO;
    this.funcDAO = funcDAO;
    this.funcRoleRelationDAO = funcRoleRelationDAO;
    this.roleDAO = roleDAO;
    this.resourceParametersDAO = resourceParametersDAO;
    this.zooKeeperGetter = zooKeeperGetter;
    this.clusterStateReader = clusterStateReader;
    this.comDfireTisWorkflowDAOFacade = workflowDAOFacade;
  }

  @Override
  public TISZkStateReader getZkStateReader() {
    return this.clusterStateReader.getInstance();
  }

  @Override
  public ITISCoordinator getSolrZkClient() {
    return zooKeeperGetter.getInstance();
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


  public IUploadResourceDAO getUploadResourceDAO() {
    return uploadResourceDAO;
  }

  @Override
  public IBizFuncAuthorityDAO getBizFuncAuthorityDAO() {
    return bizFuncAuthorityDAO;
  }

  public ISnapshotViewDAO getSnapshotViewDAO() {
    return snapshotViewDAO;
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

  public IUsrDptRelationDAO getUsrDptRelationDAO() {
    return this.usrDptRelationDAO;
  }

  public IDepartmentDAO getDepartmentDAO() {
    return this.departmentDAO;
  }


  @Override
  public IWorkflowDAOFacade getWorkflowDAOFacade() {
    return this.comDfireTisWorkflowDAOFacade;
  }
}
