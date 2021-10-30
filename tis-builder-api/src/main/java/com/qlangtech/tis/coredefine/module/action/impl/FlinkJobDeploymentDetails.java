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

import com.qlangtech.tis.config.flink.IFlinkClusterConfig;
import com.qlangtech.tis.coredefine.module.action.IDeploymentDetail;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-25 12:37
 **/
public class FlinkJobDeploymentDetails implements IDeploymentDetail {
    private final IFlinkClusterConfig clusterCfg;

    public FlinkJobDeploymentDetails(IFlinkClusterConfig clusterCfg) {
        this.clusterCfg = clusterCfg;
    }

    public IFlinkClusterConfig getClusterCfg() {
        return this.clusterCfg;
    }

    @Override
    public void accept(IDeploymentDetailVisitor visitor) {
        visitor.visit(this);
    }
}
