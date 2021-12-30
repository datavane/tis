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
package com.qlangtech.tis.datax.impl;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataXPluginMeta;
import com.qlangtech.tis.datax.IDataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.util.IPluginContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:48
 */
public abstract class DataxWriter implements Describable<DataxWriter>, IDataxWriter {

    /**
     * save
     *
     * @param appname
     */
    public static KeyedPluginStore<DataxWriter> getPluginStore(IPluginContext context, String appname) {
//        KeyedPluginStore<DataxWriter> pluginStore = new KeyedPluginStore();
//        return pluginStore;
        return TIS.dataXWriterPluginStore.get(createDataXWriterKey(context, appname));
    }

    public static void cleanPluginStoreCache(IPluginContext context, String appname) {
        TIS.dataXWriterPluginStore.clear(createDataXWriterKey(context, appname));
    }

    private static KeyedPluginStore.AppKey createDataXWriterKey(IPluginContext context, String appname) {
        return new KeyedPluginStore.AppKey(context, false, appname, DataxWriter.class);
    }


    public interface IDataxWriterGetter {
        DataxWriter get(String appName);
    }

    /**
     * 测试用
     */
    public static IDataxWriterGetter dataxWriterGetter;

    /**
     * load
     *
     * @param appName
     * @return
     */
    public static DataxWriter load(IPluginContext context, String appName) {
        if (dataxWriterGetter != null) {
            return dataxWriterGetter.get(appName);
        }
        DataxWriter appSource = getPluginStore(context, appName).getPlugin();
        Objects.requireNonNull(appSource, "appName:" + appName + " relevant appSource can not be null");
        return appSource;
    }


//    public static class AppKey extends KeyedPluginStore.Key<DataxWriter> {
//        public AppKey(String dataxName) {
//            super(IFullBuildContext.NAME_APP_DIR, dataxName, DataxWriter.class);
//        }
//    }


    @Override
    public BaseDataxWriterDescriptor getWriterDescriptor() {
        return (BaseDataxWriterDescriptor) getDescriptor();
    }

    @Override
    public final Descriptor<DataxWriter> getDescriptor() {

        Descriptor<DataxWriter> descriptor = TIS.get().getDescriptor((Class<Describable>) this.getOwnerClass());
        Class<BaseDataxWriterDescriptor> expectClazz = getExpectDescClass();
        if (!(expectClazz.isAssignableFrom(descriptor.getClass()))) {
            throw new IllegalStateException(descriptor.getClass() + " must implement the Descriptor of " + expectClazz.getName());
        }
        return descriptor;
    }

    protected <TT extends BaseDataxWriterDescriptor> Class<TT> getExpectDescClass() {
        return (Class<TT>) BaseDataxWriterDescriptor.class;
    }


    public static abstract class BaseDataxWriterDescriptor extends Descriptor<DataxWriter> {

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = new HashMap<>();
            eprops.put("supportMultiTable", this.isSupportMultiTable());
            eprops.put("rdbms", this.isRdbms());
            eprops.put("createDDL", this.isSupportTabCreate());
            if (this.getEndType() != null) {
                eprops.put("endType", this.getEndType().getVal());
            }
            return eprops;
        }

        /**
         * 如果返回null则说明不支持增量同步功能
         *
         * @return
         */
        protected IDataXPluginMeta.EndType getEndType() {
            return null;
        }

        /**
         * reader 中是否可以选择多个表，例如像elastic这样的writer中对于column的设置比较复杂，
         * 需要在writer plugin页面中完成，所以就不能支持在reader中选择多个表了
         *
         * @return
         */
        public boolean isSupportMultiTable() {
            return true;
        }

        /**
         * 是否可以选择多个表，像Mysql这样的 ,RDBMS 关系型数据库 应该都为true
         *
         * @return
         */
        public abstract boolean isRdbms();

        /**
         * 是否支持自动创建
         *
         * @return
         */
        public boolean isSupportTabCreate() {
            return false;
        }
    }
}
