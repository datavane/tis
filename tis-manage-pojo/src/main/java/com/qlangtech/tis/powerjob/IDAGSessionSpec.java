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

package com.qlangtech.tis.powerjob;

import com.qlangtech.tis.datax.LifeCycleHook;
import com.qlangtech.tis.powerjob.model.PEWorkflowDAG;
import com.qlangtech.tis.powerjob.model.WorkflowNodeType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/30
 */
public interface IDAGSessionSpec {


    public StringBuffer buildSpec(Consumer<Pair<String, String>> dptConsumer);

    public StringBuffer buildSpec();

    default IDAGSessionSpec getDpt(String id, LifeCycleHook execRole) {
        return getDpt(id, WorkflowNodeType.TASK, execRole, (node) -> {
        });
    }

    default IDAGSessionSpec getDpt(String id, LifeCycleHook execRole,
                                   Consumer<PEWorkflowDAG.Node> newAddedNodeConsumer) {
        return getDpt(id, WorkflowNodeType.TASK, execRole, newAddedNodeConsumer);
    }

    default IDAGSessionSpec getDpt(String id, WorkflowNodeType nodeType, LifeCycleHook execRole) {
        return getDpt(id, nodeType, execRole, (node) -> {
        });
    }

    public IDAGSessionSpec getDpt(String id, WorkflowNodeType nodeType, LifeCycleHook execRole,
                                  Consumer<PEWorkflowDAG.Node> newAddedNodeConsumer);

    public IDAGSessionSpec setMilestone();

    void addDpt(IDAGSessionSpec dpt);
}
