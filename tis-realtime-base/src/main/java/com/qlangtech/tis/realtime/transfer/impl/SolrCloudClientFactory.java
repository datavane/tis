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
package com.qlangtech.tis.realtime.transfer.impl;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.cloud.*;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.solrj.extend.TisCloudSolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-08-13 12:47
 */
public class SolrCloudClientFactory implements ITisCloudClientFactory {

    @Override
    public ITisCloudClient create() {
        return new SolrCloudClient();
    }

    @Override
    public int getTypeCode() {
        return REAL_TIME_ROCKETMQ;
    }

    static class SolrCloudClient implements ITisCloudClient {

        final TisCloudSolrClient solrClient;

        private final ITISCoordinator coordinator;

        public SolrCloudClient() {
            this.solrClient = new // 
                    TisCloudSolrClient(//
                    Config.getZKHost(), 5000, /* socketTimeout */
                    5000, /* connTimeout */
                    200, /* maxConnectionsPerHost */
                    200);
            this.coordinator = new ITISCoordinator() {
                @Override
                public boolean shallConnect2RemoteIncrStatusServer() {
                    return true;
                }

                private TisZkClient tisZkClient;

                @Override
                public List<String> getChildren(String zkPath, Watcher watcher, boolean b) {
                    try {
                        return solrClient.getZkClient().getChildren(zkPath, watcher, b);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void addOnReconnect(IOnReconnect onReconnect) {
                    if (tisZkClient == null) {
                        synchronized (SolrCloudClient.this) {
                            if (tisZkClient == null) {
                                tisZkClient = new TisZkClient(Config.getZKHost(), 30000);
                            }
                        }
                    }
                    tisZkClient.addOnReconnect(onReconnect);
                }

                @Override
                public byte[] getData(String s, Watcher o, Stat stat, boolean b) {
                    try {
                        return solrClient.getZkClient().getData(s, o, stat, b);
                    } catch (Exception e) {
                        throw new IllegalStateException("zkpath:" + s, e);
                    }
                }
            };
        }

        @Override
        public void add(String collection, ICloudInputDocument doc, long timeVersion) throws CloudServerException {
            try {
                SolrInputDocument addDoc = doc.unwrap();
                this.solrClient.add(collection, addDoc, timeVersion);
            } catch (SolrServerException e) {
                throw new CloudServerException("doc add faild", e);
            }
        }

        @Override
        public Object getDocById(String collection, String pk, String shareId) throws CloudServerException {
            try {
                return this.solrClient.getById(collection, pk, shareId);
            } catch (SolrServerException e) {
                throw new CloudServerException("collection:" + collection + ",pk:" + pk + ",shardid:" + shareId, e);
            }
        }

        @Override
        public ITISCoordinator getCoordinator() {
            return this.coordinator;
        }
    }
}
