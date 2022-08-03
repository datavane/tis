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

package com.qlangtech.tis.plugin.datax;

import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.util.*;

/**
 * 在增量构建流程中针对 SelectedTab 属性进行扩展
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-07-30 11:53
 * @see com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory
 **/
public abstract class IncrSourceSelectedTabExtend implements Describable<IncrSourceSelectedTabExtend>, IdentityName {

    public static final String HETERO_ENUM_IDENTITY = "incrSelectedExtend";

    @FormField(identity = true, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
    public String tabName;

    @Override
    public String identityValue() {
        return this.tabName;
    }

    public void setName(String name) {
        this.tabName = name;
    }

    @TISExtension
    public static final HeteroEnum<IncrSourceSelectedTabExtend> INCR_SELECTED_TAB_EXTEND = new HeteroEnum<IncrSourceSelectedTabExtend>(//
            IncrSourceSelectedTabExtend.class, //
            HETERO_ENUM_IDENTITY, //
            "Incr Selected Extend", //
            Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            final String dataxName = pluginMeta.getDataXName();// (pluginMeta.getExtraParam(DataxUtils.DATAX_NAME));
//            if (StringUtils.isEmpty(dataxName)) {
//                throw new IllegalArgumentException(
//                        "plugin extra param 'DataxUtils.DATAX_NAME'" + DataxUtils.DATAX_NAME + " can not be null");
//            }
            //  return com.qlangtech.tis.manage.IAppSource.getPluginStore(pluginContext, dataxName);
            return IncrSourceSelectedTabExtend.getPluginStore(pluginContext, dataxName);
        }
    };

    private static PluginStore<IncrSourceSelectedTabExtend> getPluginStore(IPluginContext pluginContext, String appname) {
        KeyedPluginStore.AppKey key = new KeyedPluginStore.AppKey(pluginContext, false, appname, IncrSourceSelectedTabExtend.class);
        return pluginStore.get(key);
    }

    private static final transient Memoizer<KeyedPluginStore.AppKey, KeyedPluginStore<IncrSourceSelectedTabExtend>> pluginStore
            = new Memoizer<KeyedPluginStore.AppKey, KeyedPluginStore<IncrSourceSelectedTabExtend>>() {
        @Override
        public KeyedPluginStore<IncrSourceSelectedTabExtend> compute(KeyedPluginStore.AppKey key) {
            return new KeyedPluginStore(key);
        }
    };
}
