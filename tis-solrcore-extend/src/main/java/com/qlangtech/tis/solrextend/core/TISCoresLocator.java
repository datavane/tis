/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
