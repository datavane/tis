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
package com.qlangtech.tis.runtime.module.misc;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataXNameAware;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.runtime.module.action.IParamGetter;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface IControlMsgHandler extends IFieldErrorHandler, IMessageHandler, IParamGetter, IDataXNameAware {

    public static IControlMsgHandler namedContext(String collectionName) {
        return new IControlMsgHandler() {
            @Override
            public SSEEventWriter getEventStreamWriter() {
                throw new UnsupportedOperationException();
            }



            @Override
            public DataXName getCollectionName() {
                return new DataXName(collectionName, StoreResourceType.DataApp);
            }

            @Override
            public String getString(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getString(String key, String dftVal) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean getBoolean(String key) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addFieldError(Context context, String fieldName, String msg, Object... params) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value) {
                throw new UnsupportedOperationException();
            }
            @Override
            public BasicPipelineValidator getPipelineValidator(BizLogic logicType) {
                throw new UnsupportedOperationException();
            }
            @Override
            public void errorsPageShow(Context context) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addActionMessage(Context context, String msg) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setBizResult(Context context, Object result, boolean overwriteable) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addErrorMessage(Context context, String msg) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * 获得EventStream 类型的Writer，用于在UI流程中显示执行进度
     *
     * @return
     */
    public SSEEventWriter getEventStreamWriter();


}
