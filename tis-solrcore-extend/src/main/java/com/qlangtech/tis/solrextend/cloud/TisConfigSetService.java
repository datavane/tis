/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
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

    // private final ZkController zkController;
    private static final long currentSchemaModificationVersion = 1;

    public TisConfigSetService(SolrResourceLoader loader) {
        super(loader, false);
    // this.zkController = zkController;
    }

    // @Override
    // public String configName(CoreDescriptor desc) {
    // return "collection " + desc.getCloudDescriptor().getCollectionName();
    // }
    @Override
    protected Long getCurrentSchemaModificationVersion(String configSet, SolrConfig solrConfig, String schemaFile) {
        return currentSchemaModificationVersion;
    }

    @Override
    public String configSetName(CoreDescriptor cd) {
        return cd.getCloudDescriptor().getCollectionName();
    // throw new UnsupportedOperationException();
    }

    @Override
    protected SolrResourceLoader createCoreResourceLoader(CoreDescriptor cd) {
        // try {
        // // for back compat with cores that can create collections without
        // // the collections API
        // if (!zkController.getZkClient().exists(ZkStateReader.COLLECTIONS_ZKNODE + "/" + cd.getCollectionName(),
        // true)) {
        // CreateCollectionCmd.createCollectionZkNode(zkController.getSolrCloudManager().getDistribStateManager(),
        // cd.getCollectionName(), cd.getCloudDescriptor().getParams());
        // }
        // } catch (KeeperException e) {
        // SolrException.log(log, null, e);
        // } catch (InterruptedException e) {
        // Thread.currentThread().interrupt();
        // SolrException.log(log, null, e);
        // }
        String collectionName = cd.getCloudDescriptor().getCollectionName();
        // String configName = zkController.getZkStateReader().readConfigName(cd.getCollectionName());
        return new TisSolrResourceLoader(cd.getInstanceDir(), parentLoader.getClassLoader(), // ,cd.getSubstitutableProperties(), zkController
        collectionName);
    // String configName =
    // zkController.getZkStateReader().readConfigName(desc.getCollectionName());
    // zkController.createCollectionZkNode(desc.getCloudDescriptor());
    // String collectionName =
    // desc.getCloudDescriptor().getCollectionName();
    // return new TisSolrResourceLoader(desc.getInstanceDir(), configName,
    // parentLoader.getClassLoader(), desc.getSubstitutableProperties(),
    // zkController, collectionName);
    }
    // protected SolrConfig createSolrConfig(CoreDescriptor cd, SolrResourceLoader loader) {
    // String name = cd.getConfigName();
    // try {
    // return new TisSolrConfig(loader, cd, null);
    // } catch (Exception e) {
    // String resource;
    // if (loader instanceof ZkSolrResourceLoader) {
    // resource = name;
    // } else {
    // resource = loader.getConfigDir() + name;
    // }
    // throw new SolrException(ErrorCode.SERVER_ERROR, "Error loading solr config from " + resource, e);
    // }
    // // return SolrConfig.readFromResourceLoader(loader, cd.getConfigName());
    // }
}
