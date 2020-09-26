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
package com.qlangtech.tis.solrj.io.stream.expr;

import org.apache.solr.client.solrj.impl.ExtendCloudSolrClient;
import org.apache.solr.client.solrj.io.stream.expr.StreamFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class StreamFactoryWithClient extends StreamFactory {

    private static final long serialVersionUID = 1L;

    private transient ExtendCloudSolrClient extendClient;

    public StreamFactoryWithClient withExtendClient(ExtendCloudSolrClient extendClient) {
        this.extendClient = extendClient;
        return this;
    }

    public ExtendCloudSolrClient getExtendClient() {
        return extendClient;
    }
}
