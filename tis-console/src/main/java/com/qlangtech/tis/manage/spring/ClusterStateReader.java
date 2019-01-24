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
package com.qlangtech.tis.manage.spring;

import org.apache.solr.common.SolrException;
import org.apache.solr.common.cloud.OnReconnect;
import org.apache.solr.common.cloud.TISZkStateReader;
import org.apache.solr.common.cloud.ZooKeeperException;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ClusterStateReader extends EnvironmentBindService<TISZkStateReader> {

    private ZooKeeperGetter zooKeeperGetter;

    private static final Logger LOG = LoggerFactory.getLogger(ClusterStateReader.class);

    @Override
    protected TISZkStateReader createSerivce(RunEnvironment runtime) {
        try {
            TisZkClient zkClinet = zooKeeperGetter.getInstance(runtime);
            final TISZkStateReader zkStateReader = new TISZkStateReader(zkClinet.getZK());
            zkClinet.addOnReconnect(new OnReconnect() {

                @Override
                public void command() {
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
