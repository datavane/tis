/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis;

import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.solr.common.cloud.ZkRepeatClientConnectionStrategy;
import com.qlangtech.tis.solrj.util.ZkUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.*;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisZkClient implements ITISCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(TisZkClient.class);
    private final List<OnReconnect> reconnectList;

    @Override
    public TisZkClient unwrap() {
        return this;
    }

    @Override
    public void create(String path, byte[] data, boolean persistent, boolean sequential) {
        //throw new UnsupportedOperationException();
        CreateMode createMode = null;
        if (persistent) {
            createMode = sequential ? CreateMode.PERSISTENT_SEQUENTIAL : CreateMode.PERSISTENT;
        } else {
            createMode = sequential ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.EPHEMERAL;
        }
        try {
            zkclient.create(path, data, createMode, true);
        } catch (Exception e) {
            throw new RuntimeException("path:" + path, e);
        }
    }

    public static ITISCoordinator create() {
        if (Config.isStandaloneMode()) {
            logger.info("create ITISCoordinator with Standalone Mode");
            return new ITISCoordinator() {
                private final String DEFAULT_CHILD1_PATH = "child001";

                @Override
                public boolean shallConnect2RemoteIncrStatusServer() {
                    return true;
                }

                @Override
                public List<String> getChildren(String zkPath, Watcher watcher, boolean b) {
                    if (ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH.equals(zkPath)) {
                        return Collections.singletonList(DEFAULT_CHILD1_PATH);
                    }
                    throw new IllegalStateException("zkPath:" + zkPath + " is illegal");
                }

                @Override
                public void addOnReconnect(IOnReconnect onReconnect) {

                }

                @Override
                public byte[] getData(String s, Watcher o, Stat stat, boolean b) {
                    if (StringUtils.equals(s
                            , ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH + ZkUtils.PATH_SPLIT + DEFAULT_CHILD1_PATH)) {
                        return (Config.getAssembleHost() + ":" + ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PORT).getBytes(TisUTF8.get());
                    }
                    throw new IllegalStateException("zkPath:" + s + " is illegal");
                }

                @Override
                public void create(String path, byte[] data, boolean persistent, boolean sequential) {

                }

                @Override
                public boolean exists(String path, boolean watch) {
                    return true;
                }

                @Override
                public <T> T unwrap() {
                    return null;
                }
            };
        } else {
            logger.info("create ITISCoordinator with Distribute Mode");
            return new TisZkClient(Config.getZKHost(), 60000);
        }
    }

    @Override
    public boolean shallConnect2RemoteIncrStatusServer() {
        return true;
    }

    private final SolrZkClient zkclient;

    private TisZkClient(String zkServerAddress, int zkClientTimeout) {
        this(zkServerAddress, zkClientTimeout, new ArrayList<>());
    }

    /**
     * @param zkServerAddress
     * @param zkClientTimeout
     */
    private TisZkClient(String zkServerAddress, int zkClientTimeout, final List<OnReconnect> reconnectList) {
        // this.zkclient = ;
        this(new TisSolrZkClient(zkServerAddress, zkClientTimeout, zkClientTimeout, new ZkRepeatClientConnectionStrategy(), new OnReconnect() {

            @Override
            public void command() {
                try {
                    for (OnReconnect re : reconnectList) {
                        re.command();
                    }
                } catch (KeeperException.SessionExpiredException e) {
                    throw new RuntimeException(e);
                }
            }
        }), reconnectList);
    }

    private TisZkClient(SolrZkClient zkclient, List<OnReconnect> reconnectList) {
        this.zkclient = zkclient;
        this.reconnectList = reconnectList;
    }

    public SolrZkClient getZK() {
        return this.zkclient;
    }

    private void addOnReconnect(OnReconnect conn) {
        this.reconnectList.add(conn);
    }

    @Override
    public void addOnReconnect(IOnReconnect onReconnect) {
        this.addOnReconnect(new OnReconnect() {

            @Override
            public void command() throws KeeperException.SessionExpiredException {
                onReconnect.command();
            }
        });
    }

    public int getZkClientTimeout() {
        return zkclient.getZkClientTimeout();
    }

    public boolean equals(Object obj) {
        return zkclient.equals(obj);
    }

    public ConnectionManager getConnectionManager() {
        return zkclient.getConnectionManager();
    }

    public ZkClientConnectionStrategy getZkClientConnectionStrategy() {
        return zkclient.getZkClientConnectionStrategy();
    }

    public boolean isConnected() {
        return zkclient.isConnected();
    }

    public void delete(String path, int version, boolean retryOnConnLoss) throws InterruptedException, KeeperException {
        zkclient.delete(path, version, retryOnConnLoss);
    }

    public Stat exists(String path, Watcher watcher, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        return zkclient.exists(path, watcher, retryOnConnLoss);
    }

    public boolean exists(String path, boolean retryOnConnLoss) {
        try {
            return zkclient.exists(path, retryOnConnLoss);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getChildren(String path, Watcher watcher, boolean retryOnConnLoss) {
        try {
            return zkclient.getChildren(path, watcher, retryOnConnLoss);
        } catch (Exception e) {
            throw new RuntimeException(path, e);
        }
    }

    public byte[] getData(String path, Watcher watcher, Stat stat, boolean retryOnConnLoss) {
        try {
            return zkclient.getData(path, watcher, stat, retryOnConnLoss);
        } catch (Exception e) {
            throw new RuntimeException(path, e);
        }
    }

    public Stat setData(String path, byte[] data, int version, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        return zkclient.setData(path, data, version, retryOnConnLoss);
    }

    public String create(String path, byte[] data, CreateMode createMode, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        return zkclient.create(path, data, createMode, retryOnConnLoss);
    }

    public void makePath(String path, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        zkclient.makePath(path, retryOnConnLoss);
    }

    public void makePath(String path, boolean failOnExists, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        zkclient.makePath(path, failOnExists, retryOnConnLoss);
    }

    public void makePath(String path, File file, boolean failOnExists, boolean retryOnConnLoss) throws IOException, KeeperException, InterruptedException {
        zkclient.makePath(path, file, failOnExists, retryOnConnLoss);
    }

    public void makePath(String path, File file, boolean retryOnConnLoss) throws IOException, KeeperException, InterruptedException {
        zkclient.makePath(path, file, retryOnConnLoss);
    }

    public void makePath(String path, CreateMode createMode, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        zkclient.makePath(path, createMode, retryOnConnLoss);
    }

    public void makePath(String path, byte[] data, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        zkclient.makePath(path, data, retryOnConnLoss);
    }

    public void makePath(String path, byte[] data, CreateMode createMode, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        zkclient.makePath(path, data, createMode, retryOnConnLoss);
    }

    public void makePath(String path, byte[] data, CreateMode createMode, Watcher watcher, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        zkclient.makePath(path, data, createMode, watcher, retryOnConnLoss);
    }

    public void makePath(String path, byte[] data, CreateMode createMode
            , Watcher watcher, boolean failOnExists, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        zkclient.makePath(path, data, createMode, watcher, failOnExists, retryOnConnLoss);
    }

    public void makePath(String zkPath, CreateMode createMode, Watcher watcher, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        zkclient.makePath(zkPath, createMode, watcher, retryOnConnLoss);
    }

    public Stat setData(String path, byte[] data, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        return zkclient.setData(path, data, retryOnConnLoss);
    }

    public Stat setData(String path, File file, boolean retryOnConnLoss) throws IOException, KeeperException, InterruptedException {
        return zkclient.setData(path, file, retryOnConnLoss);
    }

    public List<OpResult> multi(Iterable<Op> ops, boolean retryOnConnLoss) throws InterruptedException, KeeperException {
        return zkclient.multi(ops, retryOnConnLoss);
    }

    public void printLayout(String path, int indent, StringBuilder string) throws KeeperException, InterruptedException {
        zkclient.printLayout(path, indent, string);
    }

    public void printLayoutToStdOut() throws KeeperException, InterruptedException {
        zkclient.printLayoutToStdOut();
    }

    public boolean isClosed() {
        return zkclient.isClosed();
    }

    public SolrZooKeeper getSolrZooKeeper() {
        return zkclient.getSolrZooKeeper();
    }

    public void clean(String path) throws InterruptedException, KeeperException {
        zkclient.clean(path);
    }

    public String getZkServerAddress() {
        return zkclient.getZkServerAddress();
    }

    public ZkACLProvider getZkACLProvider() {
        return zkclient.getZkACLProvider();
    }
}
