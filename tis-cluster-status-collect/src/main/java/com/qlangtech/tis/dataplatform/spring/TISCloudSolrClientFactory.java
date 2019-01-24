/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.dataplatform.spring;

import java.util.Collections;
import java.util.Optional;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.common.cloud.ZkStateReader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISCloudSolrClientFactory implements FactoryBean<ZkStateReader>, InitializingBean {

	private CloudSolrClient solrClient;

	@Override
	public void afterPropertiesSet() throws Exception {
		Optional<String> empty = Optional.empty();

		CloudSolrClient.Builder clientBuilder = new CloudSolrClient.Builder(
				Collections.singletonList(TSearcherConfigFetcher.get().getZkAddress()), empty);
		clientBuilder.withConnectionTimeout(40000);
		clientBuilder.withSocketTimeout(40000);

		this.solrClient = clientBuilder.build();
		// solrClient.setZkClientTimeout(40000);
		// solrClient.setZkConnectTimeout(40000);
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
