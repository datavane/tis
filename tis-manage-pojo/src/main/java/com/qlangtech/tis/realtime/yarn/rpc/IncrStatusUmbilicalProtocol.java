/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.yarn.rpc;

import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildSharedPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus.TableDumpStatus;

/**
 * 增量子节点会实时将自己的状态信息汇报给master节点
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月7日
 */
public interface IncrStatusUmbilicalProtocol {

    public static final long versionID = 0L;

    public PingResult ping();

    /**
     * 增量执行子节点向服务端节点发送，子节点执行状态
     *
     * @param upateCounter
     */
    public MasterJob reportStatus(UpdateCounterMap upateCounter);

    /**
     * 增量节点启动的时候，向服务端汇报本地的情况，例如:监听的topic是下的tag是什么等等
     *
     * @param launchReportInfo
     */
    public void nodeLaunchReport(LaunchReportInfo launchReportInfo);

    // /**
    // * 报告查询节点的状态信息
    // * @param upateCounter
    // */
    // public void reportQueryNodeStatus(UpdateCounterMap upateCounter);
    /**
     * 报告表dump状态
     */
    public void reportDumpTableStatus(TableDumpStatus tableDumpStatus);

    /**
     * 报告索引build階段執行狀態
     *
     * @param buildStatus
     */
    public void reportBuildIndexStatus(BuildSharedPhaseStatus buildStatus);
}
