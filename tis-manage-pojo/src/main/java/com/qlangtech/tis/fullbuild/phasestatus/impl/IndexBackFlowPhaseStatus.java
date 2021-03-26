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
package com.qlangtech.tis.fullbuild.phasestatus.impl;

import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.fullbuild.phasestatus.IChildProcessStatusVisitor;
import com.qlangtech.tis.fullbuild.phasestatus.IProcessDetailStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.IndexBackFlowPhaseStatus.NodeBackflowStatus;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 索引回流执行状态
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public class IndexBackFlowPhaseStatus extends BasicPhaseStatus<NodeBackflowStatus> {

    @JSONField(serialize = false)
    public final Map<String, NodeBackflowStatus> /* nodeName */ nodesStatus = new HashMap<>();

    public IndexBackFlowPhaseStatus(int taskid) {
        super(taskid);
    }

    @Override
    protected FullbuildPhase getPhase() {
        return FullbuildPhase.IndexBackFlow;
    }

    @Override
    public boolean isShallOpen() {
        return shallOpenView(nodesStatus.values());
    }

    /**
     * 取得某個节点的索引回流状态對象
     */
    public NodeBackflowStatus getNode(String nodeName) {
        NodeBackflowStatus nodeBackflowStatus = this.nodesStatus.get(nodeName);
        if (nodeBackflowStatus == null) {
            nodeBackflowStatus = new NodeBackflowStatus(nodeName);
            this.nodesStatus.put(nodeName, nodeBackflowStatus);
        }
        return nodeBackflowStatus;
    }

    @Override
    public IProcessDetailStatus<NodeBackflowStatus> getProcessStatus() {
        return new IProcessDetailStatus<NodeBackflowStatus>() {
            @Override
            public Collection<NodeBackflowStatus> getDetails() {
                if (nodesStatus.isEmpty()) {
                    NodeBackflowStatus mock = new NodeBackflowStatus(StringUtils.EMPTY);
                    mock.setWaiting(true);
                    return Collections.singleton(mock);
                }
                return nodesStatus.values();
            }

            @Override
            public int getProcessPercent() {
                int allrow = 0;
                for (NodeBackflowStatus s : nodesStatus.values()) {
                    allrow += s.getAllSize();
                }
                if (allrow < 1) {
                    return 0;
                }
                double weight = 0;
                double percent = 0;
                for (Map.Entry<String, NodeBackflowStatus> entry : nodesStatus.entrySet()) {
                    weight = (entry.getValue().getReaded() * 100) / allrow;
                    percent += entry.getValue().getPercent() * weight;
                }
                return (int) (percent / 100);
            }

            @Override
            public void detailVisit(IChildProcessStatusVisitor visitor) {
                for (NodeBackflowStatus s : nodesStatus.values()) {
                    visitor.visit(s);
                }
            }
        };
    }

    @Override
    protected Collection<NodeBackflowStatus> getChildStatusNode() {
        return nodesStatus.values();
    }

    /**
     * 单个replic的回流执行状态
     */
    public static class NodeBackflowStatus extends AbstractChildProcessStatus {

        private final String nodeName;

        // 单位字节
        int allSize;

        // 已经读了多少字节
        int readed;

        /**
         * @param nodeName
         */
        public NodeBackflowStatus(String nodeName) {
            super();
            this.nodeName = nodeName;
        }

        // public boolean isWaiting() {
        // return waiting;
        // }
        // 
        // public void setWaiting(boolean waiting) {
        // this.waiting = waiting;
        // }
        public long getAllSize() {
            return allSize;
        }

        public void setAllSize(int allSize) {
            this.allSize = allSize;
        }

        public long getReaded() {
            return readed;
        }

        public void setReaded(int readed) {
            this.readed = readed;
        }

        @Override
        public String getAll() {
            return FileUtils.byteCountToDisplaySize(this.allSize);
        }

        @Override
        public String getProcessed() {
            return FileUtils.byteCountToDisplaySize(this.readed);
        }

        @Override
        public int getPercent() {
            if (this.allSize < 1) {
                return 0;
            }
            return (int) (((this.readed * 1f) / this.allSize) * 100);
        }

        @Override
        public String getName() {
            return this.nodeName;
        }
    }
}
