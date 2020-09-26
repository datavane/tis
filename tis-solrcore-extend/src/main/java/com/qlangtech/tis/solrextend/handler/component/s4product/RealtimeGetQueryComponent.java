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
package com.qlangtech.tis.solrextend.handler.component.s4product;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.cloud.CloudDescriptor;
import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RealtimeGetQueryComponent extends SearchComponent {

    public static final String NAME = "RealtimeGetQuery";

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
    }

    @Override
    public void process(ResponseBuilder rb) throws IOException {
    }

    @Override
    public int distributedProcess(ResponseBuilder rb) throws IOException {
        if (rb.stage < ResponseBuilder.STAGE_GET_FIELDS)
            return ResponseBuilder.STAGE_GET_FIELDS;
        if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
            return createSubRequests(rb);
        }
        return ResponseBuilder.STAGE_DONE;
    }

    public int createSubRequests(ResponseBuilder rb) throws IOException {
        SolrParams params = rb.req.getParams();
        CloudDescriptor cloudDescriptor = rb.req.getCore().getCoreDescriptor().getCloudDescriptor();
        ZkController zkController = rb.req.getCore().getCoreContainer().getZkController();
        String collection = cloudDescriptor.getCollectionName();
        for (Slice slice : zkController.getClusterState().getCollection(cloudDescriptor.getCollectionName()).getActiveSlices()) {
            String shard = slice.getName();
            // String shardIdList = StrUtils.join(entry.getValue(), ',');
            ShardRequest sreq = new ShardRequest();
            sreq.purpose = 1;
            // sreq.shards = new String[]{shard}; // TODO: would be nice if this
            // would
            // work...
            sreq.shards = sliceToShards(rb, collection, shard);
            sreq.actualShards = sreq.shards;
            SolrQuery squery = new SolrQuery();
            squery.set(ShardParams.SHARDS_QT, "/select");
            String fields = params.get(CommonParams.FL);
            if (fields != null) {
                squery.set(CommonParams.FL, fields);
            }
            squery.set("distrib", false);
            squery.setQuery(params.get(CommonParams.Q));
            sreq.params = squery;
            // sreq.params // TODO: how to avoid hardcoding this and hit the
            // same
            // handler?
            sreq.params.set("distrib", false);
            rb.addRequest(this, sreq);
        }
        return ResponseBuilder.STAGE_DONE;
    }

    private String[] sliceToShards(ResponseBuilder rb, String collection, String slice) {
        // seems either form may be filled in rb.slices?
        String lookup = collection + '_' + slice;
        for (int i = 0; i < rb.slices.length; i++) {
            if (lookup.equals(rb.slices[i]) || slice.equals(rb.slices[i])) {
                return new String[] { rb.shards[i] };
            }
        }
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Can't find shard '" + lookup + "'");
    }

    @Override
    public String getDescription() {
        return NAME;
    }
}
