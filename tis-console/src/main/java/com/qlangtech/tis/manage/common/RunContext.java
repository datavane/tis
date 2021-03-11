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

import com.qlangtech.tis.ISolrZKClientGetter;
import com.qlangtech.tis.manage.biz.dal.dao.*;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;
import org.apache.solr.common.cloud.TISZkStateReader;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface RunContext extends ISolrZKClientGetter {

  public IApplicationDAO getApplicationDAO();

  public IGroupInfoDAO getGroupInfoDAO();

  public IServerGroupDAO getServerGroupDAO();

  public ISnapshotDAO getSnapshotDAO();

  public ISnapshotViewDAO getSnapshotViewDAO();

  public IUploadResourceDAO getUploadResourceDAO();

  public IBizFuncAuthorityDAO getBizFuncAuthorityDAO();


  IUsrDptRelationDAO getUsrDptRelationDAO();

  public IDepartmentDAO getDepartmentDAO();

  public IAppTriggerJobRelationDAO getAppTriggerJobRelationDAO();

  public IFuncRoleRelationDAO getFuncRoleRelationDAO();

  public IRoleDAO getRoleDAO();

  public IFuncDAO getFuncDAO();

  // add for implement authority system20130124 end
  public IResourceParametersDAO getResourceParametersDAO();


  public TISZkStateReader getZkStateReader();

  // 全量构建需要的一些dao
  public IWorkflowDAOFacade getWorkflowDAOFacade();
}
