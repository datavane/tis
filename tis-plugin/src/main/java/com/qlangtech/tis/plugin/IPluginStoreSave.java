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
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.util.IPluginContext;

import java.util.List;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IPluginStoreSave<T extends Describable> extends IRepositoryTargetFile {

    /**
     * 不执行任何保存功能
     */
     IPluginStoreSave noneSave = new IPluginStoreSave() {
        @Override
        public SetPluginsResult setPlugins(IPluginContext pluginContext, Optional optional, List dlist, boolean update) {
            return new SetPluginsResult(true, false);
        }

        @Override
        public XmlFile getTargetFile() {
            throw new UnsupportedOperationException();
        }
    };


    default SetPluginsResult setPlugins(IPluginContext pluginContext
            , Optional<Context> context, List<Descriptor.ParseDescribable<T>> dlist) {
        return this.setPlugins(pluginContext, context, dlist, false);
    }

    /**
     * @param context
     * @param dlist
     * @param update  whether the process is update or create
     * @return
     */
    SetPluginsResult setPlugins(IPluginContext pluginContext
            , Optional<Context> context, List<Descriptor.ParseDescribable<T>> dlist, boolean update);
}
