/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.datax.impl;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataXPluginMeta;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.util.IPluginContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * datax Reader
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:48
 */
public abstract class DataxReader implements Describable<DataxReader>, IDataxReader {
    public static final String HEAD_KEY_REFERER = "Referer";
    public static final ThreadLocal<DataxReader> dataxReaderThreadLocal = new ThreadLocal<>();

    public static DataxReader getThreadBingDataXReader() {
        DataxReader reader = dataxReaderThreadLocal.get();
        return reader;
    }

    /**
     * save
     *
     * @param appname
     */
    public static KeyedPluginStore<DataxReader> getPluginStore(IPluginContext pluginContext, String appname) {
        KeyedPluginStore<DataxReader> pluginStore
                = new KeyedPluginStore(new AppKey(pluginContext, appname, DataxReader.class)
                , new PluginStore.IPluginProcessCallback<DataxReader>() {
            @Override
            public void process(DataxReader reader) {

                List<PluginFormProperties> subFieldFormPropertyTypes = reader.getDescriptor().getSubPluginFormPropertyTypes();
                if (subFieldFormPropertyTypes.size() > 0) {
                    // 加载子字段
                    subFieldFormPropertyTypes.forEach((pt) -> {
                        pt.accept(new PluginFormProperties.IVisitor() {
                            @Override
                            public Void visit(SuFormProperties props) {

                                try {
                                    SubFieldFormAppKey<DataxReader> subFieldKey
                                            = new SubFieldFormAppKey<>(pluginContext, appname, props, DataxReader.class);
                                    KeyedPluginStore<DataxReader> subFieldStore = KeyedPluginStore.getPluginStore(subFieldKey);
                                    DataxReader subFieldReader = subFieldStore.getPlugin();
                                    if (subFieldReader == null) {
                                        return null;
                                    }
                                    props.subFormField.set(reader, props.subFormField.get(subFieldReader));
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException("get subField:" + props.getSubFormFieldName(), e);
                                }


                                return null;
                            }
                        });
                    });
                }
            }

        });

        return pluginStore;
    }

    public interface IDataxReaderGetter {
        DataxReader get(String appName);
    }

    /**
     * 测试用
     */
    public static IDataxReaderGetter dataxReaderGetter;

    /**
     * load
     *
     * @param appName
     * @return
     */
    public static DataxReader load(IPluginContext pluginContext, String appName) {
        DataxReader appSource = null;
        if (dataxReaderGetter != null) {
            appSource = dataxReaderGetter.get(appName);
            DataxReader.dataxReaderThreadLocal.set(appSource);
            return appSource;
        }
        appSource = getPluginStore(pluginContext, appName).getPlugin();
        Objects.requireNonNull(appSource, "appName:" + appName + " relevant appSource can not be null");
        DataxReader.dataxReaderThreadLocal.set(appSource);
        return appSource;
    }

    private static final Pattern DATAX_UPDATE_PATH = Pattern.compile("/x/(" + ValidatorCommons.pattern_identity + ")/update");

    public static class AppKey<TT extends Describable> extends KeyedPluginStore.Key<TT> {
        public AppKey(IPluginContext pluginContext, String appname, Class<TT> clazz) {
            super(IFullBuildContext.NAME_APP_DIR, calAppName(pluginContext, appname), clazz);
        }

        private static KeyedPluginStore.KeyVal calAppName(IPluginContext pluginContext, String appname) {
            if (pluginContext == null) {
                return new KeyedPluginStore.KeyVal(appname);
            }
            String referer = pluginContext.getRequestHeader(HEAD_KEY_REFERER);
            Matcher configPathMatcher = DATAX_UPDATE_PATH.matcher(referer);
            boolean inUpdateProcess = configPathMatcher.find();
            if (inUpdateProcess && !pluginContext.isCollectionAware()) {
                throw new IllegalStateException("pluginContext.isCollectionAware() must be true");
            }
            return (pluginContext != null && inUpdateProcess)
                    ? new KeyedPluginStore.KeyVal(appname, pluginContext.getExecId()) : new KeyedPluginStore.KeyVal(appname);
        }
    }

    public static class SubFieldFormAppKey<TT extends Describable> extends AppKey<TT> {
        public final SuFormProperties subfieldForm;

        public SubFieldFormAppKey(IPluginContext pluginContext, String appname, SuFormProperties subfieldForm, Class<TT> clazz) {
            super(pluginContext, appname, clazz);
            this.subfieldForm = subfieldForm;
        }

        @Override
        protected String getSerializeFileName() {
            return super.getSerializeFileName() + "." + this.subfieldForm.getSubFormFieldName();
        }
    }


    @Override
    public final Descriptor<DataxReader> getDescriptor() {
        Descriptor<DataxReader> descriptor = TIS.get().getDescriptor(this.getClass());
        Class<BaseDataxReaderDescriptor> expectClass = getExpectDescClass();
        if (!(expectClass.isAssignableFrom(descriptor.getClass()))) {
            throw new IllegalStateException(descriptor.getClass() + " must implement the Descriptor of " + expectClass.getName());
        }
        return descriptor;
    }

    protected <TT extends DataxReader.BaseDataxReaderDescriptor> Class<TT> getExpectDescClass() {
        return (Class<TT>) BaseDataxReaderDescriptor.class;
    }

    public static abstract class BaseDataxReaderDescriptor extends Descriptor<DataxReader> {

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = new HashMap<>();
            eprops.put("rdbms", this.isRdbms());
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
         * 像Mysql会有明确的表名，而OSS没有明确的表名,RDBMS 关系型数据库 应该都为true
         *
         * @return
         */
        public boolean hasExplicitTable() {
            return this.isRdbms();
        }

        /**
         * 是否可以选择多个表，像Mysql这样的 ,RDBMS 关系型数据库 应该都为true
         *
         * @return
         */
        public abstract boolean isRdbms();
    }
}
