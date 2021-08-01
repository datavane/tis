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

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-27 09:53
 **/
public class HorizontalpodAutoscaler {
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
