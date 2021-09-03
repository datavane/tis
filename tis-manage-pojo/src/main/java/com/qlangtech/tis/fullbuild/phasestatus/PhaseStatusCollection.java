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
package com.qlangtech.tis.fullbuild.phasestatus;

import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.IndexBackFlowPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public class PhaseStatusCollection implements IPhaseStatusCollection {

    private DumpPhaseStatus dumpPhase;

    private JoinPhaseStatus joinPhase;

    private BuildPhaseStatus buildPhase;

    private IndexBackFlowPhaseStatus indexBackFlowPhaseStatus;
    private ExecutePhaseRange executePhaseRange;

    /**
     * @return
     */
    public boolean isComplete() {
        return (!executePhaseRange.contains(FullbuildPhase.FullDump) || dumpPhase.isComplete())
                && (!executePhaseRange.contains(FullbuildPhase.JOIN) || joinPhase.isComplete())
                && (!executePhaseRange.contains(FullbuildPhase.BUILD) || buildPhase.isComplete())
                && (!executePhaseRange.contains(FullbuildPhase.IndexBackFlow) || indexBackFlowPhaseStatus.isComplete());
    }

    public void flushStatus2Local() {
        if (executePhaseRange.contains(FullbuildPhase.FullDump)) {
            dumpPhase.writeStatus2Local();
        }
        if (executePhaseRange.contains(FullbuildPhase.JOIN)) {
            joinPhase.writeStatus2Local();
        }
        if (executePhaseRange.contains(FullbuildPhase.BUILD)) {
            buildPhase.writeStatus2Local();
        }
        if (executePhaseRange.contains(FullbuildPhase.IndexBackFlow)) {
            indexBackFlowPhaseStatus.writeStatus2Local();
        }
    }

    private Integer taskid;

    public PhaseStatusCollection(Integer taskid, ExecutePhaseRange executePhaseRange) {
        super();
        this.executePhaseRange = executePhaseRange;
        this.taskid = taskid;
        this.dumpPhase = new DumpPhaseStatus(taskid);
        this.joinPhase = new JoinPhaseStatus(taskid);
        this.buildPhase = new BuildPhaseStatus(taskid);
        this.indexBackFlowPhaseStatus = new IndexBackFlowPhaseStatus(taskid);
    }

    public Integer getTaskid() {
        return this.taskid;
    }

    public DumpPhaseStatus getDumpPhase() {
        return this.dumpPhase;
    }

    public void setDumpPhase(DumpPhaseStatus dumpPhase) {
        this.dumpPhase = dumpPhase;
    }

    public JoinPhaseStatus getJoinPhase() {
        return this.joinPhase;
    }

    public void setJoinPhase(JoinPhaseStatus joinPhase) {
        this.joinPhase = joinPhase;
    }

    public BuildPhaseStatus getBuildPhase() {
        return this.buildPhase;
    }

    public IndexBackFlowPhaseStatus getIndexBackFlowPhaseStatus() {
        return this.indexBackFlowPhaseStatus;
    }

    public void setBuildPhase(BuildPhaseStatus buildPhase) {
        this.buildPhase = buildPhase;
    }

    public void setIndexBackFlowPhaseStatus(IndexBackFlowPhaseStatus indexBackFlowPhaseStatus) {
        this.indexBackFlowPhaseStatus = indexBackFlowPhaseStatus;
    }
}
