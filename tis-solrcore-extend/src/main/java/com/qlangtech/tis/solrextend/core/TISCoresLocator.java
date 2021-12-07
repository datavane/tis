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
package com.qlangtech.tis.solrextend.core;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.IRepositoryResource;
import com.qlangtech.tis.solrextend.cloud.TISPluginClassLoader;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.CorePropertiesLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-02-05 07:14
 */
public class TISCoresLocator extends CorePropertiesLocator {
    private static final Logger logger = LoggerFactory.getLogger(TISCoresLocator.class);

    public TISCoresLocator(Path coreDiscoveryRoot) {
        super(coreDiscoveryRoot);
    }

    @Override
    public List<CoreDescriptor> discover(CoreContainer cc) {
        try {
            List<CoreDescriptor> cores = super.discover(cc);
            Set<String> collections = cores.stream().map((c) -> (c.getCloudDescriptor().getCollectionName())).collect(Collectors.toSet());
            // 启动过程中将core相关的插件资源下载
            List<IRepositoryResource> resources = Lists.newArrayList();
            for (String c : collections) {
                resources.addAll(TISPluginClassLoader.getSchemaRelevantResource(c));
            }
            ComponentMeta coreComponent = TIS.getCoreComponent(resources);
            coreComponent.synchronizePluginsFromRemoteRepository();
            logger.info("synchronizePluginsFromRemoteRepository {},load resource size {}"
                    , collections.stream().collect(Collectors.joining(",")), resources.size());
            return cores;
        } finally {
            TIS.permitInitialize = true;
        }
    }
}
