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

package com.qlangtech.tis.extension;

import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.ds.CMeta;

import java.util.Objects;
import java.util.Optional;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/10/15
 */
public class ElementPluginDesc {
    /**
     * Example: the descriptor for cols element of selectedTab
     *
     * @see SelectedTab
     * @see CMeta
     */
    private final Descriptor elementDesc;

    /**
     * The host plugin class for the cols element
     */
    public static Optional<ElementPluginDesc> create(Descriptor elementDesc) {
        return Optional.of(new ElementPluginDesc(elementDesc));
    }


    /**
     * @param elementDesc
     */
    private ElementPluginDesc(Descriptor elementDesc) {
        this.elementDesc = Objects.requireNonNull(elementDesc, "elementDesc can not be null");
    }

    public PluginExtraProps getFieldExtraDescs() {
        return this.elementDesc.fieldExtraDescs;
    }

    public Descriptor getElementDesc() {
        return this.elementDesc;
    }

    @Override
    public String toString() {
        return elementDesc.clazz.getSimpleName() + ",extraDescs:" + String.join(",", getFieldExtraDescs().keySet());
    }
}
