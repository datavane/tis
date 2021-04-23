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

package com.qlangtech.tis.datax.job;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.config.k8s.ReplicasSpec;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;

/**
 * DataX 任务执行容器
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-23 17:49
 **/
public abstract class DataXJobWorker implements Describable<DataXJobWorker> {

    private ReplicasSpec replicasSpec;
    // 是否支持弹性伸缩容量
    private HorizontalpodAutoscaler hpa;

    protected boolean supportHPA() {
        return this.hpa != null;
    }

    public ReplicasSpec getReplicasSpec() {
        return replicasSpec;
    }

    public void setReplicasSpec(ReplicasSpec replicasSpec) {
        this.replicasSpec = replicasSpec;
    }

    /**
     * 服务是否已经启动
     *
     * @return
     */
    public abstract boolean inService();

    /**
     * 启动服务
     */
    public abstract void launchService();

    @Override
    public Descriptor<DataXJobWorker> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }

    public static class HorizontalpodAutoscaler {
        private int minPod;
        private int maxPod;
        private int cpuAverageUtilization;

        public int getMinPod() {
            return minPod;
        }

        public void setMinPod(int minPod) {
            this.minPod = minPod;
        }

        public int getMaxPod() {
            return maxPod;
        }

        public void setMaxPod(int maxPod) {
            this.maxPod = maxPod;
        }

        public int getCpuAverageUtilization() {
            return cpuAverageUtilization;
        }

        public void setCpuAverageUtilization(int cpuAverageUtilization) {
            this.cpuAverageUtilization = cpuAverageUtilization;
        }
    }
}
