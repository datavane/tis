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
package com.qlangtech.tis.solrextend.cloud;

import org.apache.solr.core.ConfigSetService;
import org.apache.solr.core.CoreDescriptor;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisConfigSetService extends ConfigSetService {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final long currentSchemaModificationVersion = 1;

    public TisConfigSetService(SolrResourceLoader loader) {
        super(loader, false);
    }

    @Override
    protected Long getCurrentSchemaModificationVersion(String configSet, SolrConfig solrConfig, String schemaFile) {
        return currentSchemaModificationVersion;
    }

    @Override
    public String configSetName(CoreDescriptor cd) {
        return cd.getCloudDescriptor().getCollectionName();
    }

    @Override
    protected SolrResourceLoader createCoreResourceLoader(CoreDescriptor coreDesc) {
        String collectionName = coreDesc.getCloudDescriptor().getCollectionName();
        return new TisSolrResourceLoader(coreDesc.getInstanceDir(), parentLoader.getClassLoader(), collectionName);
    }
}
