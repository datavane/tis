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
package com.qlangtech.tis.dataplatform.spring;

import com.qlangtech.tis.manage.common.Config;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.cloud.ZkStateReader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import java.util.Collections;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年2月15日
 */
public class CloudSolrClientFactory implements FactoryBean<ZkStateReader>, InitializingBean {

    private CloudSolrClient solrClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        Optional<String> empty = Optional.empty();
        CloudSolrClient.Builder clientBuilder = new CloudSolrClient.Builder(Collections.singletonList(Config.getZKHost()), empty);
        clientBuilder.withConnectionTimeout(40000);
        clientBuilder.withSocketTimeout(40000);
        this.solrClient = clientBuilder.build();
    // solrClient = new CloudSolrClient(TSearcherConfigFetcher.get().getZkAddress());
    // solrClient.connect();
    }

    @Override
    public ZkStateReader getObject() throws Exception {
        return solrClient.getZkStateReader();
    // return this.solrClient;
    }

    @Override
    public Class<?> getObjectType() {
        return ZkStateReader.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
