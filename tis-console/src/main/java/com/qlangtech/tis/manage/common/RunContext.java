/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.manage.common;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.cloud.ISolrZKClientGetter;
import com.qlangtech.tis.dao.ICommonDAOContext;
import com.qlangtech.tis.manage.biz.dal.dao.IAppTriggerJobRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IBizFuncAuthorityDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IDepartmentDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IFuncRoleRelationDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IGroupInfoDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IRoleDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IServerGroupDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotDAO;
import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotViewDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUploadResourceDAO;
import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;


/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface RunContext extends ISolrZKClientGetter, ICommonDAOContext {

  default void createPipeline(Context context, String dataxName) throws Exception {
    throw new UnsupportedOperationException();
  }

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


//  public TISZkStateReader getZkStateReader();

  // 全量构建需要的一些dao

}
