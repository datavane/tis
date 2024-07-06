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

package com.qlangtech.tis.plugin.ds;

import com.qlangtech.tis.runtime.module.misc.IMessageHandler;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/12/2
 */
public class AdapterSelectedTab implements ISelectedTab {
    private final ISelectedTab target;

    public AdapterSelectedTab(ISelectedTab target) {
        this.target = target;
    }

    @Override
    public List<IColMetaGetter> overwriteCols(IMessageHandler pluginCtx) {
        return target.overwriteCols(pluginCtx);
    }

    @Override
    public String getName() {
        return target.getName();
    }

    @Override
    public String getWhere() {
        return target.getWhere();
    }

    @Override
    public boolean isAllCols() {
        return target.isAllCols();
    }

    @Override
    public List<CMeta> getCols() {
        return target.getCols();
    }

    @Override
    public Set<String> acceptedCols() {
        return target.acceptedCols();
    }

    @Override
    public List<String> getPrimaryKeys() {
        return target.getPrimaryKeys();
    }
}
