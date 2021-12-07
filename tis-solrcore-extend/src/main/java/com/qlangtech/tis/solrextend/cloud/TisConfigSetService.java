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
