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
