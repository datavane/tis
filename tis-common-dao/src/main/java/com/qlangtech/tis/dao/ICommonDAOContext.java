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

package com.qlangtech.tis.dao;

import com.qlangtech.tis.manage.biz.dal.dao.IApplicationDAO;
import com.qlangtech.tis.workflow.dao.IWorkFlowBuildHistoryDAO;
import com.qlangtech.tis.workflow.dao.IWorkFlowDAO;
import com.qlangtech.tis.workflow.dao.IWorkflowDAOFacade;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/9
 */
public interface ICommonDAOContext {
    public IApplicationDAO getApplicationDAO();

    public IWorkflowDAOFacade getWorkflowDAOFacade();

    default IWorkFlowBuildHistoryDAO getTaskBuildHistoryDAO() {
        return this.getWorkflowDAOFacade().getWorkFlowBuildHistoryDAO();
    }

    default IWorkFlowDAO getWorkFlowDAO() {
        return this.getWorkflowDAOFacade().getWorkFlowDAO();
    }
}
