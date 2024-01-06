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

    public static final int maxCpuCoresLimit = 8;
    public static final int maxCpuCoresRequest = 4;

    private int replicaCount = 1;
    /**
     * 副本（pods）数目是否可变
     */
    private boolean replicaScalable;
    private IReplicaScalaLog rcScalaLog;

    private Specification cpuRequest;

    private Specification cpuLimit;

    private Specification memoryRequest;

    private Specification memoryLimit;

    public IReplicaScalaLog getRcScalaLog() {
        return rcScalaLog;
    }

    public void setRcScalaLog(IReplicaScalaLog rcScalaLog) {
        this.rcScalaLog = rcScalaLog;
    }

    public boolean isReplicaScalable() {
        return replicaScalable;
    }

    public void setReplicaScalable(boolean replicaScalable) {
        this.replicaScalable = replicaScalable;
    }

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

    public String toJavaMemorySpec() {
        return "-Xms" + (int) (this.getMemoryRequest().normalizeMemory() * 0.8)
                + "m -Xmx" + (int) (this.getMemoryLimit().normalizeMemory() * 0.8) + "m";
    }

    public static void main(String[] args) {
        System.out.println((int) (104 * 0.8));
    }
}
