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
package com.qlangtech.tis.plugin.workshop.widget;

import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;

/**
 * Workshop Widget 插件类目注册
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/9/9
 */
@TISExtension
public class WorkshopWidgetHeteroEnum {

    public static final String WORKSHOP_WIDGET_IDENTITY = "workshop-widget";

    @TISExtension
    public static final HeteroEnum<IWorkshopWidget> WORKSHOP_WIDGET = new HeteroEnum<>(
            IWorkshopWidget.class,
            WORKSHOP_WIDGET_IDENTITY,
            "Workshop Widget",
            Selectable.Single,
            false) {

        @SuppressWarnings("all")
        @Override
        public IPluginStore<IWorkshopWidget> getPluginStore(
                com.qlangtech.tis.util.IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            // Widget 实例按 module 隔离持久化，由 WorkshopEditorService 提供实际存储
            return IPluginStore.noSaveStore(pluginMeta);
        }
    };
}