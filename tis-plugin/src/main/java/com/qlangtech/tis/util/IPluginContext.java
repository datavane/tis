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
import com.qlangtech.tis.datax.IDataXNameAware;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.extension.impl.SuFormProperties.SuFormGetterContext;
import com.qlangtech.tis.extension.model.UpdateSite.Data;
import com.qlangtech.tis.manage.common.ILoginUser;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler.BizLogic;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.runtime.module.misc.IPostContent;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IPluginContext extends IMessageHandler, IDataXNameAware, IPostContent {

    static final ThreadLocal<IPluginContext> pluginContextThreadLocal = new ThreadLocal<>();

    public static void setPluginContext(IPluginContext pluginContext) {
        pluginContextThreadLocal.set(pluginContext);
    }


    public static IPluginContext getThreadLocalInstance() {
        return pluginContextThreadLocal.get();
    }


    public static IPluginContext namedContext(DataXName collectionName) {
        return namedContext(collectionName, Optional.empty());
    }

    public static IPluginContext namedContext(String collectionName) {
        return namedContext(DataXName.createDataXPipeline(collectionName), Optional.empty());
    }

    public static IPluginContext namedContext(String collectionName, Optional<String> execId) {
        return namedContext(collectionName, execId);
    }

    public static IPluginContext namedContext(DataXName collectionName, Optional<String> execId) {
        if ((collectionName) == null) {
            throw new IllegalArgumentException("param collectionName can not be empty");
        }
        PartialSettedPluginContext context = new PartialSettedPluginContext();
        return context.setCollectionName(collectionName).setExecId(execId);
    }


    ILoginUser getLoginUser();

    /**
     * 执行更新流程客户端会保存一个ExecId的UUID
     *
     * @return
     */
    String getExecId();


    public void executeBizLogic(BizLogic logicType, Context context, Object param) throws Exception;

    /**
     * 是否和数据源相关
     *
     * @return
     */
    boolean isDataSourceAware();


    /**
     * TIS default implements: PluginAction.addDb()
     * 向数据库中新添加一条db的记录
     *
     * @param dbName
     * @param context
     */
    void addDb(Descriptor.ParseDescribable<DataSourceFactory> dbDesc, String dbName, Context context, boolean shallUpdateDB);


}
