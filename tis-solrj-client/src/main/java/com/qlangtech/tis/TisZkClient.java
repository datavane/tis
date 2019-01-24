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
package com.qlangtech.tis;

import com.qlangtech.tis.solr.common.cloud.ZkRepeatClientConnectionStrategy;
import org.apache.solr.common.cloud.ConnectionManager;
import org.apache.solr.common.cloud.OnReconnect;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.solr.common.cloud.SolrZooKeeper;
import org.apache.solr.common.cloud.TisSolrZkClient;
import org.apache.solr.common.cloud.ZkACLProvider;
import org.apache.solr.common.cloud.ZkClientConnectionStrategy;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Op;
import org.apache.zookeeper.OpResult;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisZkClient {

    private final List<OnReconnect> reconnectList;

    private final SolrZkClient zkclient;

    public TisZkClient(String zkServerAddress, int zkClientTimeout) {
        this(zkServerAddress, zkClientTimeout, new ArrayList<OnReconnect>());
    }

    /**
     * @param zkServerAddress
     * @param zkClientTimeout
     */
    public TisZkClient(String zkServerAddress, int zkClientTimeout, final List<OnReconnect> reconnectList) {
        // this.zkclient = ;
        this(new TisSolrZkClient(zkServerAddress, zkClientTimeout, zkClientTimeout, new ZkRepeatClientConnectionStrategy(), new OnReconnect() {

            @Override
            public void command() {
                for (OnReconnect re : reconnectList) {
                    re.command();
                }
            }
        }), reconnectList);
    }

    public TisZkClient(SolrZkClient zkclient, List<OnReconnect> reconnectList) {
        this.zkclient = zkclient;
        this.reconnectList = reconnectList;
    }

    public SolrZkClient getZK() {
        return this.zkclient;
    }

    public void addOnReconnect(OnReconnect conn) {
        this.reconnectList.add(conn);
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

    public Boolean exists(String path, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        return zkclient.exists(path, retryOnConnLoss);
    }

    public List<String> getChildren(String path, Watcher watcher, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        return zkclient.getChildren(path, watcher, retryOnConnLoss);
    }

    public byte[] getData(String path, Watcher watcher, Stat stat, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
        return zkclient.getData(path, watcher, stat, retryOnConnLoss);
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

    public void makePath(String path, byte[] data, CreateMode createMode, Watcher watcher, boolean failOnExists, boolean retryOnConnLoss) throws KeeperException, InterruptedException {
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

    // public void close() {
    // //zkclient.close();
    // }
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
