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

package com.qlangtech.tis.plugin;

import com.qlangtech.tis.config.k8s.ReplicasSpec;
import com.qlangtech.tis.coredefine.module.action.Specification;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtensible;

import java.util.Optional;

/**
 * 内存规格
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-03-21 22:48
 **/
@TISExtensible
public abstract class MemorySpecification implements Describable<MemorySpecification> {

    public static final Integer MEMORY_REQUEST_DEFAULT = 1024;
    public static final String FIELD_MEMORY_LIMIT = "memoryLimit";
    public static final String FIELD_MEMORY_REQUEST = "memoryRequest";

    public abstract Integer getMemoryRequest();

    public abstract Integer getMemoryLimit();

    /**
     * 取得内存规格参数,
     *
     * @return
     */
    public final String getJavaMemorySpec() {
//        ReplicasSpec replicSpec = new ReplicasSpec();
//        replicSpec.setMemoryLimit(Specification.parse(this.getMemoryLimit() + Specification.MEMORY_UNIT_MEGABYTE));
//        replicSpec.setMemoryRequest(Specification.parse(this.getMemoryRequest() + Specification.MEMORY_UNIT_MEGABYTE));
        return this.getMemorySpec().toJavaMemorySpec(Optional.empty());
    }

    public final ReplicasSpec getMemorySpec() {
        ReplicasSpec replicSpec = new ReplicasSpec();
        replicSpec.setMemoryLimit(Specification.parse(this.getMemoryLimit() + Specification.MEMORY_UNIT_MEGABYTE));
        replicSpec.setMemoryRequest(Specification.parse(this.getMemoryRequest() + Specification.MEMORY_UNIT_MEGABYTE));
        return replicSpec;
    }

    protected abstract static class BasicMemorySpecificationDescriptor extends Descriptor<MemorySpecification> {
        public BasicMemorySpecificationDescriptor() {
            super();
        }
    }
}
