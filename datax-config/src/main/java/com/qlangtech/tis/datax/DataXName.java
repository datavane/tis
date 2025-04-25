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

package com.qlangtech.tis.datax;

import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-04-20 14:08
 **/
public class DataXName {

    public static DataXName createDataXPipeline(String pipelineName) {
        return new DataXName(pipelineName, StoreResourceType.DataApp);
    }

    public boolean isDataAppType() {
        return this.getType() == StoreResourceType.DataApp;
    }

    public boolean isDataFlowType() {
        return this.getType() == StoreResourceType.DataFlow;
    }

    public StoreResourceType assetCheckDataAppType() {
//        if (this.getType() != StoreResourceType.DataApp) {
//            throw new IllegalStateException("dataXName type must be :" + StoreResourceType.DataApp + " but is :" + this.getType());
//        }
//        return this.getType();

        return this.assetCheckType(StoreResourceType.DataApp);
    }

    public StoreResourceType assetCheckType(StoreResourceType resType) {
        if (this.getType() != resType) {
            throw new IllegalStateException("dataXName type must be :" + resType + " but is :" + this.getType());
        }
        return this.getType();
    }

    private final String pipelineName;
    private final StoreResourceType type;

    public DataXName(String pipelineName, StoreResourceType type) {
        this.pipelineName = pipelineName;
        this.type = Objects.requireNonNull(type, "target type can not be null");
    }

    public String getPipelineName() {
        return this.pipelineName;
    }

    public StoreResourceType getType() {
        return this.type;
    }

    @Override
    public String toString() {
        // 为了兼容之前老的使用，这里的名称就只用this.pipelineName
        return this.pipelineName;
    }
}
