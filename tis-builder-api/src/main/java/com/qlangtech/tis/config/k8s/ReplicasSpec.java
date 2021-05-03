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
package com.qlangtech.tis.config.k8s;

import com.qlangtech.tis.coredefine.module.action.Specification;
import org.apache.commons.lang.StringUtils;

/**
 * 发布实例(ReplicationController,RepliaSet,Deployment)时的pod规格
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ReplicasSpec {

    private int replicaCount = 1;

    private Specification cpuRequest;

    private Specification cpuLimit;

    private Specification memoryRequest;

    private Specification memoryLimit;




    public int getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(int replicaCount) {
        this.replicaCount = replicaCount;
    }

    public boolean isSpecificationsDiff(ReplicasSpec s) {
        return isNotEqula(this.getCpuLimit(), s.getCpuLimit()) || isNotEqula(this.getCpuRequest(), s.getCpuRequest()) || isNotEqula(this.getMemoryLimit(), s.getMemoryLimit()) || isNotEqula(this.getMemoryRequest(), s.getMemoryRequest());
    }

    private boolean isNotEqula(Specification s1, Specification s2) {
        return s1.getVal() != s2.getVal() || !StringUtils.equals(s1.getUnit(), s2.getUnit());
    }

    public Specification getCpuRequest() {
        return cpuRequest;
    }

    public void setCpuRequest(Specification cpuRequest) {
        this.cpuRequest = cpuRequest;
    }

    public Specification getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(Specification cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public Specification getMemoryRequest() {
        return memoryRequest;
    }

    public void setMemoryRequest(Specification memoryRequest) {
        this.memoryRequest = memoryRequest;
    }

    public Specification getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Specification memoryLimit) {
        this.memoryLimit = memoryLimit;
    }


}
