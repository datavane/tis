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

package com.qlangtech.tis.plugin.ontology.impl.linker;

import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IdentityName;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/3
 */
public abstract class LinkResources extends OneStepOfMultiSteps {

    @Override
    protected final Class<? extends OneStepOfMultiSteps.BasicDesc> getBasicDescClass() {
        return BasicLinkResourceDesc.class;
    }

    /**
     * 提取连接可使用的唯一名称，用于鉴别连接的唯一名称，防止重复创建连接
     *
     * @return
     */
    public abstract IdentityName getLinkIdentityName();

    public abstract List<ObjectLinkInfo> getLinks();

    /**
     * join 的节点 left 作为 source 端，right 作为 target 端；同时携带连接列与 cardinality，便于 ChatBI/SQL 生成阶段直接使用。
     */
    public record ObjectLinkInfo(String source, String sourceField,
                                 String target, String targetField,
                                 Cardinality cardinality) {

        public boolean contain(String objType) {
            return StringUtils.equals(objType, this.source) || StringUtils.equals(objType, this.target);
        }
    }

    @TISExtension
    public static class BasicLinkResourceDesc extends OneStepOfMultiSteps.BasicDesc {

        @Override
        public final String getDisplayName() {
            return "第二步";
        }

        @Override
        public Step getStep() {
            return Step.Step2;
        }

        @Override
        public Optional<BasicDesc> nextPluginDesc(OneStepOfMultiSteps current) {
            return Optional.empty();
        }

        @Override
        public boolean isFinalStep() {
            return true;
        }

        @Override
        public String getStepDescription() {
            return "Link resources";
        }
    }
}