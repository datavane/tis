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

import com.qlangtech.tis.plugin.StoreResourceType;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-03-19 16:14
 **/
public class TriggersConfig {

    private final String dataXName;
    private final StoreResourceType resType;

    public TriggersConfig(String dataXName, StoreResourceType resType) {
        this.resType = Objects.requireNonNull(resType, "resType can not be null");
        if (StringUtils.isEmpty(dataXName)) {
            throw new IllegalArgumentException("param dataXName can not be empty");
        }
        this.dataXName = dataXName;
    }

    public StoreResourceType getResType() {
        return this.resType;
    }

    public String getDataXName() {
        return this.dataXName;
    }
}
