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

import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.KeyedPluginStore.AppKey;
import com.qlangtech.tis.plugin.KeyedPluginStore.Key;
import com.qlangtech.tis.plugin.KeyedPluginStore.PluginClassCategory;
import com.qlangtech.tis.datax.StoreResourceType;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-04-08 14:55
 **/
public class TransformerRuleKey extends KeyedPluginStore.AppKey {
    private final String tableName;

    /**
     * IPluginContext pluginContext, StoreResourceType resourceType, String appname, Class<TT> clazz
     *
     * @param pluginContext
     * @param resourceType
     * @param appname
     * @param tableName
     * @param pluginClass
     */
    public TransformerRuleKey(IPluginContext pluginContext, StoreResourceType resourceType, String appname, String tableName, Class pluginClass) {
        // IFullBuildContext.NAME_APP_DIR
        super(AppKey.calAppName(pluginContext, appname, Optional.of("transformer")), resourceType, new PluginClassCategory(pluginClass));
        this.tableName = tableName;
    }

    public static Key createStoreKey(IPluginContext pluginContext, StoreResourceType resourceType, String appname, String tableName) {
        Key key = new TransformerRuleKey(pluginContext, resourceType, appname, tableName, HeteroEnum.TRANSFORMER_RULES.extensionPoint);
        return key;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tableName);
    }

    @Override
    public String getSerializeFileName() {
        return this.getSubDirPath() + File.separator + tableName;
    }
}
