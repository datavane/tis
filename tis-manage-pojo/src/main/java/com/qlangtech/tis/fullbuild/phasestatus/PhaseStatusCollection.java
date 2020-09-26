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
package com.qlangtech.tis.fullbuild.phasestatus;

import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.IndexBackFlowPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.JoinPhaseStatus;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public class PhaseStatusCollection {

    private DumpPhaseStatus dumpPhase;

    private JoinPhaseStatus joinPhase;

    private BuildPhaseStatus buildPhase;

    private IndexBackFlowPhaseStatus indexBackFlowPhaseStatus;

    /**
     * @return
     */
    public boolean isComplete() {
        return dumpPhase.isComplete() && joinPhase.isComplete() && buildPhase.isComplete() && indexBackFlowPhaseStatus.isComplete();
    }

    private Integer taskid;

    public PhaseStatusCollection(Integer taskid) {
        super();
        this.taskid = taskid;
        this.dumpPhase = new DumpPhaseStatus(taskid);
        this.joinPhase = new JoinPhaseStatus(taskid);
        this.buildPhase = new BuildPhaseStatus(taskid);
        this.indexBackFlowPhaseStatus = new IndexBackFlowPhaseStatus(taskid);
    }

    public Integer getTaskid() {
        return taskid;
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
