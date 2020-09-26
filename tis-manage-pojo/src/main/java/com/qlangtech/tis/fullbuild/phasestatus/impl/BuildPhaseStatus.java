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
package com.qlangtech.tis.fullbuild.phasestatus.impl;

import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.fullbuild.phasestatus.IProcessDetailStatus;
import org.apache.commons.lang.StringUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月26日
 */
public class BuildPhaseStatus extends BasicPhaseStatus<BuildSharedPhaseStatus> {

    @JSONField(serialize = false)
    public final Map<String, BuildSharedPhaseStatus> /* sharedName */
    nodeBuildStatus = new HashMap<>();

    private final ProcessDetailStatusImpl<BuildSharedPhaseStatus> processDetailStatus;

    public BuildPhaseStatus(int taskid) {
        super(taskid);
        this.processDetailStatus = new ProcessDetailStatusImpl<BuildSharedPhaseStatus>(nodeBuildStatus) {

            @Override
            protected BuildSharedPhaseStatus createMockStatus() {
                BuildSharedPhaseStatus s = new BuildSharedPhaseStatus();
                s.setSharedName(StringUtils.EMPTY);
                s.setWaiting(true);
                return s;
            }
        };
    }

    @Override
    protected FullbuildPhase getPhase() {
        return FullbuildPhase.BUILD;
    }

    @Override
    public boolean isShallOpen() {
        return shallOpenView(nodeBuildStatus.values());
    }

    @Override
    public IProcessDetailStatus<BuildSharedPhaseStatus> getProcessStatus() {
        return this.processDetailStatus;
    }

    /**
     * 取得某个shared build执行任务状态
     *
     * @param sharedName
     * @return
     */
    public BuildSharedPhaseStatus getBuildSharedPhaseStatus(String sharedName) {
        if (StringUtils.isEmpty(sharedName)) {
            throw new IllegalArgumentException("param sharedName can not be null");
        }
        BuildSharedPhaseStatus nBuildStatus = nodeBuildStatus.get(sharedName);
        if (nBuildStatus == null) {
            nBuildStatus = new BuildSharedPhaseStatus();
            nBuildStatus.setSharedName(sharedName);
            nBuildStatus.setTaskid(this.getTaskId());
            nodeBuildStatus.put(sharedName, nBuildStatus);
        }
        return nBuildStatus;
    }

    protected boolean writeStatus2Local() {
        return super.writeStatus2Local();
    }

    @Override
    protected Collection<BuildSharedPhaseStatus> getChildStatusNode() {
        return this.nodeBuildStatus.values();
    }
}
