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
package com.qlangtech.tis.plugin.ontology;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.IDescribableManipulate;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.ds.manipulate.ManipulateItemsProcessor;
import com.qlangtech.tis.plugin.manipulate.BasicManipuldateProcessor;
import com.qlangtech.tis.plugin.manipulate.ManipulatePluginCacheRegister;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.util.IPluginContext;

import java.util.Optional;

/**
 * DefaultOntologyDomain 的伴生操作基类。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/21
 * @see com.qlangtech.tis.datax.DefaultDataXProcessorManipulate
 * @see OntologyDomain
 */
public abstract class OntologyDomainManipulate extends BasicManipuldateProcessor<OntologyDomainManipulate> {

    private static ManipulatePluginCacheRegister<OntologyDomainManipulate> ontologyCacheRegister;

    public static ManipulatePluginCacheRegister.TemplateManipulateStore<OntologyDomainManipulate>
    getManipulateStore(String domainName, boolean forceFresh) {
        if (ontologyCacheRegister == null) {
            ontologyCacheRegister = new ManipulatePluginCacheRegister<>(
                    domain -> TIS.getPluginStore(OntologyDomain.getStoreKey(domain, OntologyDomainManipulate.class)));
        }
        return ontologyCacheRegister.getOrLoad(domainName, forceFresh);
    }

    @Override
    protected IPluginStore<OntologyDomainManipulate> loadPluginStore(IPluginContext pluginContext,
                                                                     ManipulateItemsProcessor itemsProcessor) {
        OntologyPluginMeta meta = OntologyPluginMeta.createPluginMeta(itemsProcessor.getPluginMeta());
        KeyedPluginStore.Key<OntologyDomainManipulate> key =
                OntologyDomain.getStoreKey(meta.getDomain(), OntologyDomainManipulate.class);
        return TIS.getPluginStore(key);
    }

    @Override
    protected void afterManipuldateProcess(IPluginContext pluginContext, Optional<Context> context,
                                           ManipulateItemsProcessor itemsProcessor) {

        OntologyPluginMeta meta = OntologyPluginMeta.createPluginMeta(itemsProcessor.getPluginMeta());
        String domainName = meta.getDomain();
        ManipulatePluginCacheRegister.TemplateManipulateStore<OntologyDomainManipulate> memStore =
                getManipulateStore(domainName, false);
        if (itemsProcessor.isDeleteProcess()) {
            memStore.remove(this);
        } else {
            memStore.replace(this);
        }
    }

    protected static class BasicDesc extends BasicManipuldateProcessor.BasicDesc<OntologyDomainManipulate>
            implements IEndTypeGetter, IDescribableManipulate.IManipulateStorable {
        public BasicDesc() {
            super();
        }
    }
}
