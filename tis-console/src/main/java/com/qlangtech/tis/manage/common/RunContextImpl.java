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
package com.qlangtech.tis.manage.common;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.manage.biz.dal.dao.*;
import com.qlangtech.tis.manage.spring.ClusterStateReader;
import com.qlangtech.tis.manage.spring.ZooKeeperGetter;
import com.qlangtech.tis.workflow.dao.IComDfireTisWorkflowDAOFacade;
import org.apache.solr.common.cloud.TISZkStateReader;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RunContextImpl implements RunContext {

    private final IAppPackageDAO appPackageDAO;

    private final IApplicationDAO applicationDAO;

    // private final IBizDomainDAO bizDomainDAO;
    private final IGroupInfoDAO groupInfoDAO;

    private final IServerDAO serverDAO;

    private final IServerGroupDAO serverGroupDAO;

    private final ISnapshotDAO snapshotDAO;

    private final ISnapshotViewDAO snapshotViewDAO;

    private final IUploadResourceDAO uploadResourceDAO;

    // private final AdminUserService authService;
    // baisui add 20120413
    private final IServerJoinGroupDAO serverJoinGroupDAO;

    // private final OrgService orgService;
    private final IGlobalAppResourceDAO globalAppResourceDAO;

    private final IAppTriggerJobRelationDAO appTriggerJobRelationDAO;

    // private final RpcCoreManage rpcCoreManage;
    private final IFuncDAO funcDAO;

    private final IFuncRoleRelationDAO funcRoleRelationDAO;

    private final IRoleDAO roleDAO;

    private final IResourceParametersDAO resourceParametersDAO;

    // 聚石塔相关DAO
    // private final IIsvDAO isvDAO;
    private final IRdsDbDAO rdsDbDAO;

    private final IRdsTableDAO rdsTableDAO;

    private final IApplicationExtendDAO applicationExtendDAO;

    private final ZooKeeperGetter zooKeeperGetter;

    private final ClusterStateReader clusterStateReader;

    private final IComDfireTisWorkflowDAOFacade comDfireTisWorkflowDAOFacade;

    private final IUsrDptRelationDAO usrDptRelationDAO;

    private final IUsrDptExtraRelationDAO usrDptExtraRelationDAO;

    private final IDepartmentDAO departmentDAO;

    private final IUsrApplyDptRecordDAO usrApplyDptRecordDAO;

    private IBizFuncAuthorityDAO bizFuncAuthorityDAO;

    public RunContextImpl(// IBizDomainDAO bizDomainDAO,
    IAppPackageDAO appPackageDAO, // IBizDomainDAO bizDomainDAO,
    IApplicationDAO applicationDAO, // AdminUserService
    IGroupInfoDAO groupInfoDAO, // AdminUserService
    IServerDAO serverDAO, // AdminUserService
    IServerGroupDAO serverGroupDAO, // AdminUserService
    ISnapshotDAO snapshotDAO, // AdminUserService
    ISnapshotViewDAO snapshotViewDAO, // OrgService orgService,
    IUploadResourceDAO uploadResourceDAO, // RpcCoreManage rpcCoreManage,
    IServerJoinGroupDAO serverJoinGroupDAO, // RpcCoreManage rpcCoreManage,
    IBizFuncAuthorityDAO bizFuncAuthorityDAO, // RpcCoreManage rpcCoreManage,
    IUsrDptRelationDAO usrDptRelationDAO, // RpcCoreManage rpcCoreManage,
    IDepartmentDAO departmentDAO, // RpcCoreManage rpcCoreManage,
    IGlobalAppResourceDAO globalAppResourceDAO, // RpcCoreManage rpcCoreManage,
    IAppTriggerJobRelationDAO appTriggerJobRelationDAO, // IIsvDAO isvDAO,
    IFuncDAO funcDAO, // IIsvDAO isvDAO,
    IFuncRoleRelationDAO funcRoleRelationDAO, // IIsvDAO isvDAO,
    IRoleDAO roleDAO, // IIsvDAO isvDAO,
    IResourceParametersDAO resourceParametersDAO, // IIsvDAO isvDAO,
    IUsrDptExtraRelationDAO usrDptExtraRelationDAO, IUsrApplyDptRecordDAO usrApplyDptRecordDAO, IRdsDbDAO rdsDbDAO, IRdsTableDAO rdsTableDAO, IApplicationExtendDAO applicationExtendDAO, ZooKeeperGetter zooKeeperGetter, ClusterStateReader clusterStateReader, IComDfireTisWorkflowDAOFacade comDfireTisWorkflowDAOFacade) {
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
        // this.rpcCoreManage = rpcCoreManage;
        this.usrApplyDptRecordDAO = usrApplyDptRecordDAO;
        // add for implementing authority system
        this.funcDAO = funcDAO;
        this.funcRoleRelationDAO = funcRoleRelationDAO;
        this.roleDAO = roleDAO;
        this.resourceParametersDAO = resourceParametersDAO;
        this.usrDptExtraRelationDAO = usrDptExtraRelationDAO;
        // 聚石塔相关DAO
        // this.isvDAO = isvDAO;
        this.rdsDbDAO = rdsDbDAO;
        this.rdsTableDAO = rdsTableDAO;
        this.applicationExtendDAO = applicationExtendDAO;
        this.zooKeeperGetter = zooKeeperGetter;
        this.clusterStateReader = clusterStateReader;
        this.comDfireTisWorkflowDAOFacade = comDfireTisWorkflowDAOFacade;
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

    // public OrgService getOrgService() {
    // return orgService;
    // }
    @Override
    public IRoleDAO getRoleDAO() {
        return this.roleDAO;
    }

    // @Override
    // public RpcCoreManage getRpcCoreManage() {
    // return this.rpcCoreManage;
    // }
    @Override
    public IAppTriggerJobRelationDAO getAppTriggerJobRelationDAO() {
        return appTriggerJobRelationDAO;
    }

    // @Override
    // public AdminUserService getAuthService() {
    // return this.authService;
    // }
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

    // @Override
    // public IBizDomainDAO getBizDomainDAO() {
    // return bizDomainDAO;
    // }
    @Override
    public IBizFuncAuthorityDAO getBizFuncAuthorityDAO() {
        return bizFuncAuthorityDAO;
    }

    // @Override
    // public IServerDAO getServerDAO() {
    // return serverDAO;
    // }
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

    public IUsrDptRelationDAO getUsrDptRelationDAO() {
        return this.usrDptRelationDAO;
    }

    public IDepartmentDAO getDepartmentDAO() {
        return this.departmentDAO;
    }

    public IUsrApplyDptRecordDAO getUsrApplyDptRecordDAO() {
        return this.usrApplyDptRecordDAO;
    }

    // 聚石塔相关表
    // public IIsvDAO getIsvDAO() {
    // return this.isvDAO;
    // }
    public IRdsDbDAO getRdsDbDAO() {
        return this.rdsDbDAO;
    }

    public IRdsTableDAO getRdsTableDAO() {
        return this.rdsTableDAO;
    }

    public IApplicationExtendDAO getApplicationExtendDAO() {
        return this.applicationExtendDAO;
    }

    @Override
    public IComDfireTisWorkflowDAOFacade getWorkflowDAOFacade() {
        return this.comDfireTisWorkflowDAOFacade;
    }
}
