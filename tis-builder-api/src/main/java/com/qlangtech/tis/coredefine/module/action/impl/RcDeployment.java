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
package com.qlangtech.tis.coredefine.module.action.impl;

import com.qlangtech.tis.config.k8s.ReplicasSpec;
import com.qlangtech.tis.coredefine.module.action.IDeploymentDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行时RC
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-09-02 12:32
 */
public class RcDeployment extends ReplicasSpec implements IDeploymentDetail {

    // 创建时间
    private long creationTimestamp;

    private ReplicationControllerStatus status;

    private List<PodStatus> pods = new ArrayList<>();

    // 环境变量
    final Map<String, String> envs = new HashMap<>();

    private String dockerImage;

    public ReplicationControllerStatus getStatus() {
        return status;
    }

    public void setStatus(ReplicationControllerStatus status) {
        this.status = status;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public void addPod(PodStatus podStat) {
        this.pods.add(podStat);
    }

    public List<PodStatus> getPods() {
        return pods;
    }

    public void setDockerImage(String dockerImage) {
        this.dockerImage = dockerImage;
    }

    public void addEnv(String key, String val) {
        this.envs.put(key, val);
    }

    public Map<String, String> getEnvs() {
        return this.envs;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    @Override
    public void accept(IDeploymentDetailVisitor visitor) {
        visitor.visit(this);
    }

    public static class ReplicationControllerStatus {

        private Integer availableReplicas;

        private Integer fullyLabeledReplicas;

        private Long observedGeneration;

        // 已经有的副本
        private Integer readyReplicas;

        // 目标副本
        private Integer replicas;

        public Integer getAvailableReplicas() {
            return availableReplicas;
        }

        public void setAvailableReplicas(Integer availableReplicas) {
            this.availableReplicas = availableReplicas;
        }

        public Integer getFullyLabeledReplicas() {
            return fullyLabeledReplicas;
        }

        public void setFullyLabeledReplicas(Integer fullyLabeledReplicas) {
            this.fullyLabeledReplicas = fullyLabeledReplicas;
        }

        public Long getObservedGeneration() {
            return observedGeneration;
        }

        public void setObservedGeneration(Long observedGeneration) {
            this.observedGeneration = observedGeneration;
        }

        public Integer getReadyReplicas() {
            return readyReplicas;
        }

        public void setReadyReplicas(Integer readyReplicas) {
            this.readyReplicas = readyReplicas;
        }

        public Integer getReplicas() {
            return replicas;
        }

        public void setReplicas(Integer replicas) {
            this.replicas = replicas;
        }
    }

    public static class PodStatus {

        private String name;

        private String phase;

        // pod启动次数
        private int restartCount;

        private long startTime;

        public String getName() {
            return name;
        }

        public int getRestartCount() {
            return restartCount;
        }

        public void setRestartCount(int restartCount) {
            this.restartCount = restartCount;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhase() {
            return phase;
        }

        public void setPhase(String phase) {
            this.phase = phase;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }
    }
}
