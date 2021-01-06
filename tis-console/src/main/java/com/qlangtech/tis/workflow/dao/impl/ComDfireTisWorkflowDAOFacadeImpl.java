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
package com.qlangtech.tis.workflow.dao.impl;

import com.qlangtech.tis.workflow.dao.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ComDfireTisWorkflowDAOFacadeImpl implements IWorkflowDAOFacade {

  private final IWorkFlowDAO workFlowDAO;

  private final IWorkFlowBuildHistoryDAO workFlowBuildHistoryDAO;

  private final IDatasourceTableDAO datasourceTableDAO;

  private final IDatasourceDbDAO datasourceDbDAO;


  public IWorkFlowDAO getWorkFlowDAO() {
    return this.workFlowDAO;
  }

  public IWorkFlowBuildHistoryDAO getWorkFlowBuildHistoryDAO() {
    return this.workFlowBuildHistoryDAO;
  }

  public IDatasourceTableDAO getDatasourceTableDAO() {
    return this.datasourceTableDAO;
  }

  public IDatasourceDbDAO getDatasourceDbDAO() {
    return this.datasourceDbDAO;
  }

  public ComDfireTisWorkflowDAOFacadeImpl(IWorkFlowDAO workFlowDAO, IWorkFlowBuildHistoryDAO workFlowBuildHistoryDAO
    , IDatasourceTableDAO datasourceTableDAO, IDatasourceDbDAO datasourceDbDAO) {
    this.workFlowDAO = workFlowDAO;
    this.workFlowBuildHistoryDAO = workFlowBuildHistoryDAO;
    this.datasourceTableDAO = datasourceTableDAO;
    this.datasourceDbDAO = datasourceDbDAO;
  }
}
