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
import com.qlangtech.tis.manage.common.ILoginUser;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
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
public class PartialSettedPluginContext implements IPluginContext {

    private DataXName collectionName;

    private Optional<String> execId;

    private ILoginUser loginUser;

    @Override
    public JSONObject getJSONPostContent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ILoginUser getLoginUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void executeBizLogic(IFieldErrorHandler.BizLogic logicType, Context context, Object param) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IUploadPluginMeta> parsePluginMeta(String[] plugins, boolean useCache) {
        return Collections.emptyList();
    }

    @Override
    public Pair<Boolean, IPluginItemsProcessor> getPluginItems(
            IUploadPluginMeta pluginMeta, Context context, int pluginIndex, JSONArray itemsArray, boolean verify, PropValRewrite propValRewrite) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getExecId() {
        return execId.orElse(null);
    }

    @Override
    public boolean isCollectionAware() {
        return true;
    }

    @Override
    public boolean isDataSourceAware() {
        return false;
    }

    @Override
    public void addDb(Descriptor.ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context,
                      boolean shallUpdateDB) {
    }

    @Override
    public String getRequestHeader(String key) {
        return null;
    }

    @Override
    public DataXName getCollectionName() {
        return collectionName;
    }

    @Override
    public void errorsPageShow(Context context) {

    }

    @Override
    public void addActionMessage(Context context, String msg) {

    }

    @Override
    public void setBizResult(Context context, Object result, boolean overwriteable) {

    }

    @Override
    public void addErrorMessage(Context context, String msg) {

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
}
