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
package com.qlangtech.tis.solrextend.handler.normal;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.ReplicationHandler;
import org.apache.solr.handler.IndexFetcher.IndexFetchResult;

/**
 * solr原生的 副本拷贝机制不可控，先将这个废弃掉，有啥问题后面再调整
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisReplicationHandler extends ReplicationHandler {

    // @Override
    // @SuppressWarnings("all")
    // public void inform(SolrCore core) {
    //
    // }
    @Override
    public IndexFetchResult doFetch(SolrParams solrParams, boolean forceReplication) {
        return IndexFetchResult.INDEX_FETCH_SUCCESS;
    // return true;
    }
    // @Override
    // public String getDescription() {
    //
    // return super.getDescription();
    // }
    //
    // @Override
    // public NamedList getStatistics() {
    //
    // return super.getStatistics();
    // }
    //
    // @Override
    // @SuppressWarnings("all")
    // public void init(NamedList args) {
    //
    // // super.init(args);
    // }
    //
    // @Override
    // public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
    // throw new UnsupportedOperationException();
    // }
}
