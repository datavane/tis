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

package com.qlangtech.tis.plugin;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.Descriptor.ParseDescribable;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.util.IPluginContext;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-12-07 18:28
 **/
public interface IPluginStore<T extends Describable> extends IRepositoryResource, IPluginStoreSave<T> {

    /**
     * 不需要持久化的pluginStore，例如JobTrigger
     *
     * @param <T>
     * @return
     * @see com.qlangtech.tis.plugin.trigger.JobTrigger
     */
    public static <T extends Describable> IPluginStore<T> noSaveStore() {
        return new IPluginStore<T>() {
            @Override
            public T getPlugin() {
                return null;
            }

            @Override
            public List<T> getPlugins() {
                return Collections.emptyList();
            }

            @Override
            public void cleanPlugins() {

            }

            @Override
            public List<Descriptor<T>> allDescriptor() {
                return Collections.emptyList();
            }

            @Override
            public T find(String name, boolean throwNotFoundErr) {
                return null;
            }

            @Override
            public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional<Context> context,
                                               List<ParseDescribable<T>> dlist, boolean update) {

                dlist.stream().forEach((plugin) -> {
                    plugin.getSubFormInstances().forEach((p) -> {
                        if (!(p instanceof ManipuldateProcessor)) {
                            throw new IllegalStateException("instance of " + p.getClass().getName() + " must be type "
                                    + "of " + ManipuldateProcessor.class.getSimpleName());
                        }
                        ((ManipuldateProcessor) p).manipuldateProcess(pluginContext, context);
                    });
                });

                return new SetPluginsResult(true, false);
            }

            @Override
            public void copyConfigFromRemote() {

            }

            @Override
            public long getWriteLastModifyTimeStamp() {
                return 0;
            }

            @Override
            public XmlFile getTargetFile() {
                return null;
            }
        };
    }

    public T getPlugin();

    public List<T> getPlugins();

    public void cleanPlugins();

    public List<Descriptor<T>> allDescriptor();


    public default T find(String name) {
        return find(name, true);
    }

    public T find(String name, boolean throwNotFoundErr);

    interface Recyclable {
        // 是否已经是脏数据了，已经在PluginStore中被替换了
        boolean isDirty();
    }

    interface RecyclableController extends Recyclable {
        /**
         * 标记已经失效
         */
        void signDirty();
    }

    interface AfterPluginSaved {
        /**
         * Plugin 保存执行回调执行
         */
        void afterSaved(IPluginContext pluginContext, Optional<Context> context);
    }

    interface AfterPluginDeleted {
        /**
         * 插件被删除之后执行
         *
         * @param pluginContext
         * @param context
         */
        void afterDeleted(IPluginContext pluginContext, Optional<Context> context);
    }

    interface BeforePluginSaved {
        /**
         * Plugin 保存执行千回调执行
         */
        void beforeSaved(IPluginContext pluginContext, Optional<Context> context);
    }

    /**
     * 不需要持久化的plugin进行提交处理
     */
    interface ManipuldateProcessor {
        /**
         * 执行处理
         */
        void manipuldateProcess(IPluginContext pluginContext, Optional<Context> context);
    }

    interface AfterPluginVerified {
        /**
         * Plugin 验证成功执行回调执行
         */
        void afterVerified(IPluginStoreSave pluginStore);
    }
}
