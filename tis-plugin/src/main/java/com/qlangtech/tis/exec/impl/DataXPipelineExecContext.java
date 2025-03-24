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

package com.qlangtech.tis.exec.impl;

import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.exec.AbstractExecContext;
import com.qlangtech.tis.plugin.StoreResourceType;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2023/11/18
 */
public class DataXPipelineExecContext extends AbstractExecContext {


    private final String dataXName;


    public DataXPipelineExecContext(String dataXName, Long triggerTimestamp) {
        super(triggerTimestamp);
        if (StringUtils.isEmpty(dataXName)) {
            throw new IllegalArgumentException("param dataXName can not be null");
        }
        this.dataXName = dataXName;

    }


    @Override
    public StoreResourceType getResType() {
        return StoreResourceType.DataApp;
    }


    @Override
    public IDataxProcessor getProcessor() {

        StoreResourceType resType = Objects.requireNonNull(getResType(), "resType can not be null");
//        switch (resType) {
//            case DataApp:
        return DataxProcessor.load(null, resType, this.dataXName);
//            case DataFlow:
//                if (StringUtils.isEmpty(this.getWorkflowName())) {
//                    throw new IllegalStateException("proper workflowName can not be empty");
//                }
//                return DataxProcessor.load(null, resType, this.getWorkflowName());
//            default:
//                throw new IllegalStateException("illegal resType:" + resType);
//        }
    }

    @Override
    public Integer getWorkflowId() {
        return null;
    }

    @Override
    public String getWorkflowName() {
        return null;
    }

    @Override
    public String identityValue() {
        StoreResourceType resType = Objects.requireNonNull(getResType(), "resType can not be null");
//        switch (resType) {
//            case DataApp:
        return resType.getType() + "_" + this.getIndexName();
//            case DataFlow:
//                if (StringUtils.isEmpty(this.getWorkflowName())) {
//                    throw new IllegalStateException("proper workflowName can not be empty");
//                }
//                return resType.getType() + "_" + this.getWorkflowName();
//            default:
//                throw new IllegalStateException("illegal resType:" + resType);
//        }
    }


//    public void setWorkflowName(String workflowName) {
//       // this.workflowName = workflowName;
//        throw new UnsupportedOperationException();
//    }


    @Override
    public String getIndexName() {
        return this.dataXName;
    }

    @Override
    public boolean hasIndexName() {
        return StringUtils.isNotEmpty(this.dataXName);
    }


}
