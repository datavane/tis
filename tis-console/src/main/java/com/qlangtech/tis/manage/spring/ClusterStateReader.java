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
package com.qlangtech.tis.manage.spring;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.TISZkStateReader;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月13日 下午5:08:51
 */
public class ClusterStateReader extends EnvironmentBindService<TISZkStateReader> {

    private ZooKeeperGetter zooKeeperGetter;

    private static final Logger LOG = LoggerFactory.getLogger(ClusterStateReader.class);

    @Override
    protected TISZkStateReader createSerivce(RunEnvironment runtime) {
        try {
            TisZkClient zkClinet = zooKeeperGetter.getInstance(runtime);
            final TISZkStateReader zkStateReader = new TISZkStateReader(zkClinet.getZK());
            zkClinet.addOnReconnect(() -> {
                try {
                    zkStateReader.createClusterStateWatchersAndUpdate();
                } catch (KeeperException e) {
                    LOG.error("A ZK error has occurred", e);
                    throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "A ZK error has occurred", e);
                } catch (InterruptedException e) {
                    // Restore the interrupted status
                    Thread.currentThread().interrupt();
                    LOG.error("Interrupted", e);
                    throw new ZooKeeperException(SolrException.ErrorCode.SERVER_ERROR, "Interrupted", e);
                }
            });
            zkStateReader.createClusterStateWatchersAndUpdate();
            return zkStateReader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setZooKeeperGetter(ZooKeeperGetter zooKeeperGetter) {
        this.zooKeeperGetter = zooKeeperGetter;
    }
}
