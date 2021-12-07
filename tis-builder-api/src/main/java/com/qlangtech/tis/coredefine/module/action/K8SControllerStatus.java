/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.coredefine.module.action;

import com.qlangtech.tis.coredefine.module.action.impl.FlinkJobDeploymentDetails;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-06 10:59
 **/
public class K8SControllerStatus {
    // k8s的RC是否已经创建
    private boolean k8sReplicationControllerCreated;
    private IDeploymentDetail rcDeployment;
    private FlinkJobDeploymentDetails flinJobDetail;

    public FlinkJobDeploymentDetails getFlinkJobDetail() {
        return flinJobDetail;
    }

    public void setFlinkJobDetail(FlinkJobDeploymentDetails flinJobDetail) {
        this.flinJobDetail = flinJobDetail;
    }

    // private MqConfigMeta mqConfig;
    public boolean isK8sReplicationControllerCreated() {
        return k8sReplicationControllerCreated;
    }

    public void setK8sReplicationControllerCreated(boolean k8sReplicationControllerCreated) {
        this.k8sReplicationControllerCreated = k8sReplicationControllerCreated;
    }

    public IDeploymentDetail getRcDeployment() {
        return rcDeployment;
    }

    public void setRcDeployment(IDeploymentDetail rcDeployment) {
        this.rcDeployment = rcDeployment;
    }
}
