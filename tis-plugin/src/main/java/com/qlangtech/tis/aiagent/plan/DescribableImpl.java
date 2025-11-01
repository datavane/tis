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

package com.qlangtech.tis.aiagent.plan;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/9/19
 */
public class DescribableImpl {
    /**
     * 扩展点
     */
    private final Class<? extends Describable> extendPoint;
    /**
     * 详细实现插件类
     */
    private List<String> impls = Lists.newArrayList();
    private Descriptor _descriptor;
    private final Optional<IEndTypeGetter.EndType> endType;

    // private boolean _implPluginHasInstalled;
    public DescribableImpl(Class<? extends Describable> extendPoint, Optional<IEndTypeGetter.EndType> endType) {
        this.extendPoint = extendPoint;
        this.endType = endType;
    }

    public Optional<IEndTypeGetter.EndType> getEndType() {
        return this.endType;
    }

    public Class<? extends Describable> getExtendPoint() {
        return this.extendPoint;
    }

    public String getExtendPointClassName() {
        return this.extendPoint.getName();
    }


    public Descriptor getImplDesc() {
        if (CollectionUtils.isEmpty(this.impls)) {
            throw new IllegalStateException("prop impl can not be empty");
        }
        if (_descriptor == null) {
            for (String impl : this.impls) {
                Descriptor d = TIS.get().getDescriptor(impl);
                if (endType.isPresent()) {
                    if ((d instanceof IEndTypeGetter)
                            && (((IEndTypeGetter) d).getEndType() == endType.get())) {
                        //  this.endType = endType;
                        this._descriptor = d;
                        return d;
                    }
                } else {
                    //this.endType = Optional.empty();
                    this._descriptor = d;
                    return d;
                }
            }

        }
        return this._descriptor;
    }


    public DescribableImpl addImpl(String impl) {
        this.impls.add(impl);
        return this;
    }

    public DescribableImpl setDescriptor(Descriptor desc) {
        this._descriptor = Objects.requireNonNull(desc, "desc can not be null");
        // this.endType = endType;
        this.impls = Collections.singletonList(desc.getId());
        return this;
    }

    public List<String> getImpls() {
        return this.impls;
    }


}
