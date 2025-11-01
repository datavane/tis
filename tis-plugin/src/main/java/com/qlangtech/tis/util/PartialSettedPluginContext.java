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

package com.qlangtech.tis.util;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.manage.common.ILoginUser;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.runtime.module.misc.FormVaildateType;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/10/9
 */
public class PartialSettedPluginContext implements IPluginContext, IControlMsgHandler {

    private DataXName collectionName;
    private IFieldErrorHandler fieldErrorHandler;
    private IMessageHandler messageHandler;

    private IPluginContext targetRuntimeContext;

//    /**
//     * @param fieldErrorHandler
//     * @param messageHandler
//     * @return
//     * @see // DefaultMessageHandler
//     */
//    public PartialSettedPluginContext setMessageAndFieldErrorHandler(IFieldErrorHandler fieldErrorHandler, IMessageHandler messageHandler) {
//        this.fieldErrorHandler = fieldErrorHandler;
//        this.messageHandler = messageHandler;
//        return this;
//    }

    public PartialSettedPluginContext setTargetRuntimeContext(IPluginContext targetRuntimeContext) {
        this.targetRuntimeContext = Objects.requireNonNull(targetRuntimeContext, "param targetRuntimeContext can not be null");
        this.fieldErrorHandler = (IFieldErrorHandler) targetRuntimeContext;
        this.messageHandler = targetRuntimeContext;
        return this;
    }

    @Override
    public void errorsPageShow(Context context) {
        messageHandler.errorsPageShow(context);
    }

    @Override
    public void addActionMessage(Context context, String msg) {
        messageHandler.addActionMessage(context, msg);
    }

    @Override
    public void setBizResult(Context context, Object result) {
        messageHandler.setBizResult(context, result);
    }

    @Override
    public void setBizResult(Context context, Object result, boolean overwriteable) {
        messageHandler.setBizResult(context, result, overwriteable);
    }

    @Override
    public void addErrorMessage(Context context, String msg) {
        messageHandler.addErrorMessage(context, msg);
    }


    private Optional<String> execId;

    private ILoginUser loginUser;

    @Override
    public JSONObject getJSONPostContent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ILoginUser getLoginUser() {
        return this.loginUser;
    }

    @Override
    public void executeBizLogic(IFieldErrorHandler.BizLogic logicType, Context context, Object param) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public BasicPipelineValidator getPipelineValidator(BizLogic logicType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IUploadPluginMeta> parsePluginMeta(String[] plugins, boolean useCache) {
        return Collections.emptyList();
    }

    @Override
    public Pair<Boolean, IPluginItemsProcessor> getPluginItems(
            IUploadPluginMeta pluginMeta, Context context, int pluginIndex, JSONArray itemsArray, FormVaildateType verify, PropValRewrite propValRewrite) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getExecId() {
        return execId.orElse(null);
    }

    @Override
    public boolean isCollectionAware() {
        return collectionName.getType() == StoreResourceType.DataApp;
    }

    @Override
    public boolean isDataSourceAware() {
        return collectionName.getType() == StoreResourceType.DataBase;
    }

    @Override
    public void addDb(Descriptor.ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context,
                      boolean shallUpdateDB) {
        Objects.requireNonNull(this.targetRuntimeContext, "targetRuntimeContext can not be null")
                .addDb(dbDesc, dbName, context, shallUpdateDB);
    }

    @Override
    public String getRequestHeader(String key) {
        return null;
    }

    @Override
    public DataXName getCollectionName() {
        return collectionName;
    }

    public PartialSettedPluginContext setCollectionName(DataXName collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public PartialSettedPluginContext setExecId(Optional<String> execId) {
        this.execId = execId;
        return this;
    }

    public PartialSettedPluginContext setLoginUser(ILoginUser loginUser) {
        this.loginUser = Objects.requireNonNull(loginUser, "loginUser can not be null");
        return this;
    }

    @Override
    public SSEEventWriter getEventStreamWriter() {
        return null;
    }

    @Override
    public String getString(String key) {
        return "";
    }

    @Override
    public String getString(String key, String dftVal) {
        return "";
    }

    @Override
    public boolean getBoolean(String key) {
        return false;
    }

    @Override
    public void addFieldError(Context context, String fieldName, String msg, Object... params) {
        fieldErrorHandler.addFieldError(context, fieldName, msg, params);
    }

    @Override
    public boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value) {

        return fieldErrorHandler.validateBizLogic(logicType, context, fieldName, value);
    }

}
