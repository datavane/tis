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
package com.qlangtech.tis.plugin.ontology.impl.binding;

import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.util.IPluginContext;

/**
 * 切换 OT 绑定数据源（典型场景：建模阶段 binding=MySQL，同步落 Doris 后切换到 Doris）。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/9
 * @see DefaultBindingSwitcher
 */
public interface OntologyBindingSwitcher {

    /**
     * 校验是否可切换，返回校验报告（兼容差异列表）。
     */
    BindingSwitchReport validate(IPluginContext ctx,OntologyObjectType ot, DataSourceFactory targetDs);

    /**
     * 执行切换：把 binding.dbName 改为 newDsName 并持久化。
     * 下游图存储 / 索引同步由 IPluginStore.afterSaved 钩子承担（详见 06 文档），本方法不直接调用。
     */
    void switchBinding(OntologyObjectType ot, DataSourceFactory tagetDS, IPluginContext ctx);
}
