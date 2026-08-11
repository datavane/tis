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

package com.qlangtech.tis.coredefine.module.action;

import java.util.List;

/**
 * Flink Job DAG 简化拓扑信息（供前端 G6 拓扑图使用）
 */
public class JobTopologyInfo {

    private boolean available = false;
    private String errMsg;

    private List<TopologyNode> nodes;
    private List<TopologyEdge> edges;

    public static class TopologyNode {
        private String id;
        private String name;
        private int parallelism;
        private String state;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getParallelism() { return parallelism; }
        public void setParallelism(int parallelism) { this.parallelism = parallelism; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
    }

    public static class TopologyEdge {
        private String source;
        private String target;

        public TopologyEdge() {}
        public TopologyEdge(String source, String target) {
            this.source = source;
            this.target = target;
        }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getTarget() { return target; }
        public void setTarget(String target) { this.target = target; }
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public String getErrMsg() { return errMsg; }
    public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
    public List<TopologyNode> getNodes() { return nodes; }
    public void setNodes(List<TopologyNode> nodes) { this.nodes = nodes; }
    public List<TopologyEdge> getEdges() { return edges; }
    public void setEdges(List<TopologyEdge> edges) { this.edges = edges; }
}