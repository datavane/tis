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
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-07-11 08:03
 **/
public abstract class AdapterPluginContext implements IPluginContext, IControlMsgHandler {
    private final IPluginContext pluginContext;
    private final IControlMsgHandler msgHandler;

    public AdapterPluginContext(IPluginContext pluginContext) {
        this.pluginContext = pluginContext;
        this.msgHandler = (IControlMsgHandler) pluginContext;
    }

    @Override
    public void executeBizLogic(BizLogic logicType, Context context, Object param) throws Exception {
        pluginContext.executeBizLogic(logicType, context, param);
    }

    @Override
    public Pair<Boolean, IPluginItemsProcessor> getPluginItems(IUploadPluginMeta pluginMeta, Context context, int pluginIndex, JSONArray itemsArray, boolean verify, PropValRewrite propValRewrite) {
        return pluginContext.getPluginItems(pluginMeta, context, pluginIndex, itemsArray, verify, propValRewrite);
    }

    @Override
    public List<IUploadPluginMeta> parsePluginMeta(String[] plugins, boolean useCache) {
        return Collections.emptyList();
    }

    @Override
    public JSONObject getJSONPostContent() {
        return pluginContext.getJSONPostContent();
    }

    @Override
    public PrintWriter getEventStreamWriter() {
        return msgHandler.getEventStreamWriter();
    }

    @Override
    public String getString(String key) {
        return msgHandler.getString(key);
    }

    @Override
    public String getString(String key, String dftVal) {
        return msgHandler.getString(key, dftVal);
    }

    @Override
    public boolean getBoolean(String key) {
        return msgHandler.getBoolean(key);
    }

    @Override
    public void addFieldError(Context context, String fieldName, String msg, Object... params) {
        msgHandler.addFieldError(context, fieldName, msg, params);
    }

    @Override
    public boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value) {
        return msgHandler.validateBizLogic(logicType, context, fieldName, value);
    }

    @Override
    public String getExecId() {
        return pluginContext.getExecId();
    }

    @Override
    public boolean isCollectionAware() {
        return pluginContext.isCollectionAware();
    }

    @Override
    public boolean isDataSourceAware() {
        return pluginContext.isDataSourceAware();
    }

    @Override
    public void addDb(Descriptor.ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context, boolean shallUpdateDB) {
        pluginContext.addDb(dbDesc, dbName, context, shallUpdateDB);
    }

    @Override
    public DataXName getCollectionName() {
        return pluginContext.getCollectionName();
    }

    @Override
    public void errorsPageShow(Context context) {
        pluginContext.errorsPageShow(context);
    }

    @Override
    public void addActionMessage(Context context, String msg) {
        pluginContext.addActionMessage(context, msg);
    }

    @Override
    public void setBizResult(Context context, Object result, boolean overwriteable) {
        pluginContext.setBizResult(context, result, overwriteable);
    }

    @Override
    public void addErrorMessage(Context context, String msg) {
        pluginContext.addErrorMessage(context, msg);
    }

    @Override
    public String getRequestHeader(String key) {
        return pluginContext.getRequestHeader(key);
    }
}
