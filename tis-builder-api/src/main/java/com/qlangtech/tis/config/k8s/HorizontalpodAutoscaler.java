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

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-27 09:53
 **/
public class HorizontalpodAutoscaler {
    private Integer minPod;
    private Integer maxPod;
    private Integer cpuAverageUtilization;

    public static HorizontalpodAutoscaler getDft() {
        HorizontalpodAutoscaler dft = new HorizontalpodAutoscaler();
        dft.setCpuAverageUtilization(10);
        dft.setMinPod(1);
        dft.setMaxPod(10);
        return dft;
    }

    public Integer getMinPod() {
        return this.minPod;
    }

    public void setMinPod(Integer minPod) {
        this.minPod = minPod;
    }

    public Integer getMaxPod() {
        return maxPod;
    }

    public void setMaxPod(Integer maxPod) {
        this.maxPod = maxPod;
    }

    public Integer getCpuAverageUtilization() {
        return this.cpuAverageUtilization;
    }

    public void setCpuAverageUtilization(Integer cpuAverageUtilization) {
        this.cpuAverageUtilization = cpuAverageUtilization;
    }
}
