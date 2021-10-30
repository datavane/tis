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
