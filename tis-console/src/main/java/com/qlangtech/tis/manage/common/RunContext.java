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

import com.qlangtech.tis.ISolrZKClientGetter;
import com.qlangtech.tis.manage.biz.dal.dao.IAppPackageDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IAppTriggerJobRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationExtendDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IBizFuncAuthorityDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IGlobalAppResourceDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IGroupInfoDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRdsDbDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRdsTableDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerGroupDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerJoinGroupDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotViewDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrApplyDptRecordDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptExtraRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface RunContext extends ISolrZKClientGetter {

    public IApplicationDAO getApplicationDAO();

    public IAppPackageDAO getAppPackageDAO();

    public IGroupInfoDAO getGroupInfoDAO();

    // public IServerDAO getServerDAO();
    public IServerGroupDAO getServerGroupDAO();

    public ISnapshotDAO getSnapshotDAO();

    public ISnapshotViewDAO getSnapshotViewDAO();

    public IUploadResourceDAO getUploadResourceDAO();

    // public AdminUserService getAuthService();
    public IBizFuncAuthorityDAO getBizFuncAuthorityDAO();

    // 组织服务
    // public OrgService getOrgService();
    // 通过应用查找这个应用下所有组中的服务器（每个组只出一个服务器）
    public IServerJoinGroupDAO getServerJoinGroupDAO();

    public IUsrApplyDptRecordDAO getUsrApplyDptRecordDAO();

    public IUsrDptRelationDAO getUsrDptRelationDAO();

    public IUsrDptExtraRelationDAO getUsrDptExtraRelationDAO();

    public IDepartmentDAO getDepartmentDAO();

    public IGlobalAppResourceDAO getGlobalAppResourceDAO();

    public IAppTriggerJobRelationDAO getAppTriggerJobRelationDAO();

   

    // add for implement authority system20130124
    public IFuncRoleRelationDAO getFuncRoleRelationDAO();

    public IRoleDAO getRoleDAO();

    public IFuncDAO getFuncDAO();

    // add for implement authority system20130124 end
    public IResourceParametersDAO getResourceParametersDAO();

    // 聚石塔相关dao
    // public IIsvDAO getIsvDAO();
    public IRdsDbDAO getRdsDbDAO();

    public IRdsTableDAO getRdsTableDAO();

    public IApplicationExtendDAO getApplicationExtendDAO();

    public TISZkStateReader getZkStateReader();
}
