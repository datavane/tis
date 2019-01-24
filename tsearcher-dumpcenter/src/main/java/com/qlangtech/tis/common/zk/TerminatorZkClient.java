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
package com.qlangtech.tis.common.zk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.cloud.SolrZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import com.qlangtech.tis.solr.common.cloud.ZkRepeatClientConnectionStrategy;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorZkClient extends SolrZkClient {

    private static Log log = LogFactory.getLog(TerminatorZkClient.class);

    private static int zkTime = 3000;

    /**
     * @uml.property name="ephemeralSet"
     */
    private Set<String> ephemeralSet = Collections.synchronizedSet(new HashSet<String>());

    // private OnReconnect onReconnect = new OnReconnect() {
    // public void onReconnect(TerminatorZkClient zkClient) {
    // 
    // }o
    // };
    private final List<OnReconnect> reconnectList;

    public String createUnsafeSequential(String path) throws KeeperException, InterruptedException {
        return super.create(path, new byte[0], CreateMode.PERSISTENT_SEQUENTIAL, false);
    // return zookeeper.create(path, , ZooDefs.Ids.OPEN_ACL_UNSAFE,
    // );
    }

    // public static TerminatorZkClient create(){
    // if (TSearcherConfigFetcher.isRunInVersion2Environment()) {
    // // 是二套环境应用
    // return new TerminatorZkClientReadOnly(zkAddress,
    // zkTimeout, null, true);
    // } else {
    // return new TerminatorZkClient(zkAddress, zkTimeout,
    // null, true);
    // }
    // }
    /**
     * @return
     * @uml.property name="ephemeralSet"
     */
    public Set<String> getEphemeralSet() {
        return ephemeralSet;
    }

    /**
     * @param ephemeralSet
     * @uml.property name="ephemeralSet"
     */
    public void setEphemeralSet(Set<String> ephemeralSet) {
        this.ephemeralSet = ephemeralSet;
    }

    /**
     * @uml.property name="zkState"
     */
    private KeeperState zkState;

    /**
     * @uml.property name="zookeeper"
     */
    // protected ZooKeeper zookeeper;
    /**
     * @uml.property name="zkAddress"
     */
    private String zkAddress;

    /**
     * @uml.property name="zkClientTimeout"
     */
    private int zkClientTimeout;

    /**
     * @uml.property name="connectedSignal"
     */
    private CountDownLatch connectedSignal;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private static TerminatorZkClient zkClient;

    /**
     * 取得zk
     *
     * @return
     */
    public static TerminatorZkClient getZK() {
        if (zkClient == null) {
            throw new IllegalStateException("zkClient can not be null");
        }
        return zkClient;
    }

    public static TerminatorZkClient create(String zkAddress, int zkClientTimeout, final OnReconnect onReconnect, boolean watchState) {
        if (zkClient == null) {
            synchronized (TerminatorZkClient.class) {
                if (zkClient == null) {
                    final List<OnReconnect> reconnectList = new ArrayList<>();
                    // TerminatorZkClient result = null;
                    org.apache.solr.common.cloud.OnReconnect reconnect = new org.apache.solr.common.cloud.OnReconnect() {

                        @Override
                        public void command() {
                            try {
                                onReconnect.onReconnect(null);
                                for (OnReconnect conn : reconnectList) {
                                    conn.onReconnect(null);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    };
                    zkClient = new TerminatorZkClient(zkAddress, zkClientTimeout, reconnect, reconnectList);
                }
            }
        }
        log.warn("start create TerminatorZkClient");
        return zkClient;
    // } catch (TerminatorZKException e) {
    // throw new RuntimeException(e);
    // // if (result != null) {
    // // result.close();
    // // }
    // // log.error(e.getMessage(), e);
    // // try {
    // // result = new MockTerminatorZkClient();
    // // } catch (TerminatorZKException e1) {
    // // throw new RuntimeException(e1);
    // // }
    // }
    }

    // protected TerminatorZkClient(String zkAddress, int zkClientTimeout,
    // OnReconnect onReconnect, boolean watchState)
    // throws TerminatorZKException {
    // this.zkAddress = zkAddress;
    // this.zkClientTimeout = zkClientTimeout;
    // this.zkState = KeeperState.Disconnected;
    // this.connectedSignal = new CountDownLatch(1);
    // 
    // this.registOnReconnect(onReconnect);
    // this.connect();
    // 
    // /*
    // * if(watchState){ ScheduledExecutorService scheduler =
    // * Executors.newSingleThreadScheduledExecutor();
    // * scheduler.scheduleAtFixedRate(new ZkStateListener(this), 3000, 3000,
    // * TimeUnit.MILLISECONDS); }
    // */
    // }
    // private TerminatorZkClient(String zkAddress, int zkClientTimeout)
    // throws TerminatorZKException {
    // this(zkAddress, zkClientTimeout, null, false);
    // }
    // private TerminatorZkClient(String zkAddress) throws TerminatorZKException
    // {
    // this(zkAddress, zkTime, null, false);
    // }
    static final int DEFAULT_CLIENT_CONNECT_TIMEOUT = 30000;

    protected TerminatorZkClient(String zkServerAddress, int zkClientTimeout, org.apache.solr.common.cloud.OnReconnect onReonnect, List<OnReconnect> reconnectlist) {
        super(zkServerAddress, zkClientTimeout, new ZkRepeatClientConnectionStrategy(), onReonnect);
        this.reconnectList = reconnectlist;
    }

    public void addEphemeral(String path) {
        ephemeralSet.add(path);
    }

    protected void reset() {
        connectedSignal = new CountDownLatch(1);
    }

    /**
     * 创建Znode,并给该Znode赋值，如果该Znode已经存在，则只给该znode赋值
     *
     * @param path
     * @param bytes
     * @param isPersistent
     * @throws TerminatorZKException
     */
    public void create(String path, byte[] bytes, boolean isPersistent) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        CreateMode createMode = isPersistent ? CreateMode.PERSISTENT : CreateMode.EPHEMERAL;
        try {
            this.create(path, bytes, createMode, !isPersistent);
        } catch (Exception e) {
            throw new TerminatorZKException(e);
        }
    // try {
    // zookeeper.create(path, bytes, Ids.OPEN_ACL_UNSAFE, createMode);
    // } catch (KeeperException e) {
    // if (e instanceof KeeperException.NoNodeException) {
    // throw new TerminatorZKException("The node [" + e.getPath()
    // + "]'s parent node doesn't exist,can't create it. ", e);
    // } else if (e instanceof KeeperException.NodeExistsException) {
    // this.setData(path, bytes);
    // } else {
    // throw new TerminatorZKException("Other error", e);
    // }
    // } catch (InterruptedException e) {
    // Thread.currentThread().interrupt();
    // throw new TerminatorZKException(e);
    // } catch (Throwable e) {
    // throw new TerminatorZKException(e);
    // }
    }

    public boolean createPathIfAbsent(String path, byte[] bytes, boolean isPersistent) {
        CreateMode createMode = isPersistent ? CreateMode.PERSISTENT : CreateMode.EPHEMERAL;
        path = TerminatorZKUtils.normalizePath(path);
        try {
            // zookeeper.create(path, bytes, Ids.OPEN_ACL_UNSAFE, createMode);
            super.create(path, bytes, createMode, !isPersistent);
        } catch (KeeperException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    /**
     * 创建Znode,并给该Znode赋值，如果该Znode已经存在，则只给该znode赋值
     *
     * @param path
     * @param bytes
     * @param isPersistent
     * @throws TerminatorZKException
     */
    public void create(String path, byte[] bytes, boolean isPersistent, boolean reconnectRecreate) throws TerminatorZKException {
        create(path, bytes, isPersistent);
        if (reconnectRecreate && !isPersistent) {
            addEphemeral(path);
        }
    }

    public boolean createPathIfAbsent(String path, byte[] bytes, boolean isPersistent, boolean reconnectRecreate) {
        if (createPathIfAbsent(path, bytes, isPersistent)) {
            if (reconnectRecreate && !isPersistent) {
                addEphemeral(path);
            }
            return true;
        }
        return false;
    }

    /**
     * 创建Znode(持久化Znode)并给该Znode赋值
     *
     * @param path
     * @param bytes
     * @throws TerminatorZKException
     */
    public void create(String path, byte[] bytes) throws TerminatorZKException {
        this.create(path, bytes, true);
    }

    /**
     * 递归创建znode并给该Znode赋值
     *
     * @param path
     * @param bytes
     * @throws TerminatorZKException
     */
    public void rcreate(String path, byte[] bytes) throws TerminatorZKException {
        this.rcreatePath(path);
        this.setData(path, bytes);
    }

    /**
     * 递归创建Znode路径，不赋值
     *
     * @param path
     * @return
     * @throws TerminatorZKException
     */
    public String rcreatePath(String path) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        if (this.exists(path))
            return path;
        String[] splits = path.substring(1).split(TerminatorZKUtils.SEPARATOR);
        String _p = "";
        for (String split : splits) {
            _p = _p + TerminatorZKUtils.SEPARATOR + split;
            if (!this.exists(_p)) {
                this.createPath(_p);
            }
        }
        return path;
    }

    /**
     * 递归创建,但是末尾的path根据参数isPersistent决定是持久节点还是临时节点
     *
     * @param path
     * @param isPersistent
     * @return
     * @throws TerminatorZKException
     */
    public String rcreatePath(String path, boolean isPersistent) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        if (this.exists(path))
            return path;
        String[] splits = path.substring(1).split(TerminatorZKUtils.SEPARATOR);
        String _p = "";
        for (int i = 0; i < splits.length; i++) {
            _p = _p + TerminatorZKUtils.SEPARATOR + splits[i];
            if (i < splits.length - 1) {
                if (!this.exists(_p)) {
                    this.createPath(_p);
                }
            }
            if (i == splits.length - 1) {
                if (!this.exists(_p)) {
                    this.createPath(_p, isPersistent);
                }
            }
        }
        return path;
    }

    /**
     * 创建持久化的Znode的Path,如果存在，则返回
     *
     * @param path
     * @throws TerminatorZKException
     */
    public void createPath(String path) throws TerminatorZKException {
        this.createPath(path, true);
    }

    /**
     * 创建Znode的Path，如果存在，则返回
     *
     * @param path
     * @param isPersistent
     * @throws TerminatorZKException
     */
    public void createPath(String path, boolean isPersistent) throws TerminatorZKException {
        CreateMode createMode = isPersistent ? CreateMode.PERSISTENT : CreateMode.EPHEMERAL;
        path = TerminatorZKUtils.normalizePath(path);
        try {
            if (!this.exists(path)) {
                this.create(path, "".getBytes(), createMode, !isPersistent);
            // .create(path, null, Ids.OPEN_ACL_UNSAFE, createMode);
            }
        } catch (KeeperException e) {
            if (e instanceof KeeperException.NodeExistsException) {
            // do nothing
            } else if (e instanceof KeeperException.NoNodeException) {
                throw new TerminatorZKException("The node [" + e.getPath() + "]'s parent node doesn't exist,can't create it. ", e);
            } else {
                throw new TerminatorZKException("Other error", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TerminatorZKException(e);
        } catch (Throwable e) {
            throw new TerminatorZKException(e);
        }
    }

    /**
     * 创建Path，如果没有该node,如果已经存在则返回false
     *
     * @param path
     * @param isPersistent
     * @throws TerminatorZKException
     */
    public boolean createPathIfAbsent(String path, boolean isPersistent) {
        CreateMode createMode = isPersistent ? CreateMode.PERSISTENT : CreateMode.EPHEMERAL;
        path = TerminatorZKUtils.normalizePath(path);
        try {
            // zookeeper.create(path, null, Ids.OPEN_ACL_UNSAFE, createMode);
            super.create(path, null, createMode, !isPersistent);
        } catch (KeeperException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Throwable e) {
            return false;
        }
        return true;
    }

    /**
     * 判断该Znode节点是否存在
     *
     * @param path
     * @return
     * @throws TerminatorZKException
     */
    public boolean exists(String path) throws TerminatorZKException {
        return this.exists(path, (Watcher) null);
    }

    /**
     * 判断该Znode节点是否存在
     *
     * @param path
     * @param watcher
     * @return
     * @throws TerminatorZKException
     */
    public boolean exists(String path, Watcher watcher) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        if (watcher instanceof TerminatorWatcher) {
            ((TerminatorWatcher) watcher).setWatchType(TerminatorWatcher.WatcherType.EXIST_TYPE);
            ((TerminatorWatcher) watcher).setZkClient(this);
        }
        try {
            Stat stat = super.exists(path, watcher, true);
            return stat != null;
        } catch (KeeperException e) {
            throw new TerminatorZKException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TerminatorZKException(e);
        } catch (Throwable e) {
            throw new TerminatorZKException(e);
        }
    }

    /**
     * 删除Znode节点
     *
     * @param path
     * @return
     * @throws TerminatorZKException
     */
    public boolean delete(String path) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        try {
            // zookeeper.delete(path, -1);
            super.delete(path, -1, false);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TerminatorZKException("UnKnown", e);
        } catch (KeeperException e) {
            if (e instanceof KeeperException.NoNodeException) {
                throw new TerminatorZKException("Node does not exist,path is [" + e.getPath() + "].", e);
            } else if (e instanceof KeeperException.NotEmptyException) {
                throw new TerminatorZKException("The node has children,can't delete it.", e);
            } else {
                throw new TerminatorZKException("UnKnown.", e);
            }
        } catch (Throwable e) {
            throw new TerminatorZKException(e);
        }
    }

    /**
     * 递归删除该Znode节点
     *
     * @param path
     * @throws TerminatorZKException
     */
    public void rdelete(String path) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        try {
            // zookeeper.delete(, -1);
            super.delete(TerminatorZKUtils.normalizePath(path), -1, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TerminatorZKException(e);
        } catch (KeeperException e) {
            if (e instanceof KeeperException.NotEmptyException) {
                List<String> children = null;
                try {
                    children = super.getChildren(path, null, false);
                // children = zookeeper.getChildren(path, false);
                } catch (KeeperException e1) {
                    if (e1 instanceof KeeperException.NoNodeException) {
                        throw new TerminatorZKException("Node does not exist,path is [" + e.getPath() + "].", e);
                    }
                } catch (InterruptedException e1) {
                    throw new TerminatorZKException(e);
                }
                for (String child : children) {
                    String _path = path + TerminatorZKUtils.SEPARATOR + child;
                    this.rdelete(_path);
                }
                this.rdelete(path);
            } else if (e instanceof KeeperException.NoNodeException) {
                throw new TerminatorZKException("Node does not exist,path is [" + e.getPath() + "].", e);
            }
        }
    }

    public byte[] getData(String path) throws TerminatorZKException {
        return this.getData(path, (Watcher) null);
    }

    /**
     * 获取相应znode节点的数据
     *
     * @param path
     * @param watcher
     * @return
     * @throws TerminatorZKException
     */
    public byte[] getData(String path, Watcher watcher) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        if (watcher instanceof TerminatorWatcher) {
            ((TerminatorWatcher) watcher).setWatchType(TerminatorWatcher.WatcherType.GETDATA_TYPE);
            ((TerminatorWatcher) watcher).setZkClient(this);
        }
        byte[] data = null;
        try {
            // data = zookeeper.getData(path, watcher, null);
            data = super.getData(path, watcher, new Stat(), true);
        } catch (KeeperException e) {
            if (e instanceof KeeperException.NoNodeException) {
                throw new TerminatorZKException("Node does not exist,path is [" + e.getPath() + "].", e);
            } else {
                throw new TerminatorZKException(e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TerminatorZKException(e);
        }
        return data;
    }

    /**
     * 获取相应znode节点的孩子节点，单纯的获取孩子节点，不做任何的Watch
     *
     * @param path
     * @return
     * @throws TerminatorZKException
     */
    public List<String> getChildren(String path) throws TerminatorZKException {
        return this.getChildren(path, (Watcher) null);
    }

    /**
     * 获取相应的znode节点的孩子节点，并设置Watcher监听，用于监听该节点的还在节点的变更
     *
     * @param path
     * @param watcher
     * @return
     * @throws TerminatorZKException
     */
    public List<String> getChildren(String path, Watcher watcher) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        if (watcher instanceof TerminatorWatcher) {
            ((TerminatorWatcher) watcher).setWatchType(TerminatorWatcher.WatcherType.GETCHILDREN_TYPE);
            ((TerminatorWatcher) watcher).setZkClient(this);
        }
        // List<String> children = null;
        try {
            return super.getChildren(path, watcher, true);
        // children = zookeeper.getChildren(path, watcher);
        } catch (KeeperException e) {
            if (e instanceof KeeperException.NoNodeException) {
                throw new TerminatorZKException("Node does not exist,path is [" + e.getPath() + "].", e);
            } else {
                throw new TerminatorZKException(e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TerminatorZKException(e);
        }
    // return children;
    }

    /**
     * 给znode节点赋值
     *
     * @param path
     * @param bytes
     * @throws TerminatorZKException
     */
    public void setData(String path, byte[] bytes) throws TerminatorZKException {
        this.setData(path, bytes, -1);
    }

    public void setData(String path, byte[] bytes, int version) throws TerminatorZKException {
        path = TerminatorZKUtils.normalizePath(path);
        try {
            // zookeeper.setData(path, bytes, version);
            super.setData(path, bytes, false);
        } catch (KeeperException e) {
            if (e instanceof KeeperException.NoNodeException) {
                this.rcreate(path, bytes);
            } else if (e instanceof KeeperException.BadVersionException) {
                throw new TerminatorZKException("Bad Version,path [" + e.getPath() + "] version [" + version + "],the given version does not match the node's version", e);
            } else {
                throw new TerminatorZKException("May be value(byte[]) is larger than 1MB.", e);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TerminatorZKException(e);
        }
    }

    // /**
    // * 以文件目录的树状结构相似path的下的孩子结构
    // *
    // * @param path
    // * @return
    // * @throws Exception
    // */
    // public String showFolder(String path) throws Exception {
    // StringBuilder sb = new StringBuilder();
    // this.showFolder(path, sb, 0);
    // return sb.toString();
    // }
    // /**
    // * 以目录的层次结构显示znode的层次结构
    // *
    // * @param basePath
    // * @param sb
    // * @param n
    // * @throws Exception
    // */
    // private void showFolder(String basePath, StringBuilder sb, int n)
    // throws Exception {
    // for (int i = 0; i < n; i++) {
    // sb.append("   |");
    // }
    // sb.append("---");
    // String name = null;
    // if (basePath.equals("/") || basePath.equals("")) {
    // name = "[ZK-ROOT]";
    // basePath = "";
    // } else {
    // name = basePath.substring(basePath.lastIndexOf("/") + 1) + "";
    // byte[] bs = zookeeper.getData(basePath, false, null);
    // if (bs == null) {
    // name = name + " [empty] ";
    // }
    // }
    // sb.append(name).append("\n");
    // 
    // List<String> children = zookeeper.getChildren(
    // TerminatorZKUtils.normalizePath(basePath), false);
    // if (children != null && !children.isEmpty()) {
    // for (String child : children) {
    // this.showFolder(basePath + "/" + child, sb, n + 1);
    // }
    // }
    // }
    /**
     * 注册重新连接后的事件处理器
     *
     * @param onReconnect
     */
    public void registOnReconnect(OnReconnect onReconnect) {
        if (onReconnect != null) {
            this.reconnectList.add(onReconnect);
        // this.onReconnect = onReconnect;
        }
    }

    // /**
    // * 判断当前Zookeeper对象是否可用(链接是否断掉)
    // *
    // * @return
    // */
    // public boolean isAlive() {
    // return this.getState().isAlive();
    // }
    // 
    // public States getState() {
    // return zookeeper.getState();
    // }
    // 
    // public void close() {
    // try {
    // zookeeper.close();
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // zookeeper = null;
    // }
    public void setOnReconnect(OnReconnect onReconnect) {
        if (onReconnect != null)
            this.reconnectList.add(onReconnect);
    }

    /**
     * @return
     * @uml.property name="zkState"
     */
    public KeeperState getZkState() {
        return zkState;
    }

    /**
     * @param zkState
     * @uml.property name="zkState"
     */
    public void setZkState(KeeperState zkState) {
        this.zkState = zkState;
    }

    /**
     * @return
     * @uml.property name="zookeeper"
     */
    public ZooKeeper getZookeeper() {
        return super.getSolrZooKeeper();
    }

    /**
     * @return
     * @uml.property name="zkAddress"
     */
    public String getZkAddress() {
        return zkAddress;
    }

    /**
     * @param zkAddress
     * @uml.property name="zkAddress"
     */
    public void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    /**
     * @return
     * @uml.property name="zkClientTimeout"
     */
    public int getZkClientTimeout() {
        return zkClientTimeout;
    }

    /**
     * @param zkClientTimeout
     * @uml.property name="zkClientTimeout"
     */
    public void setZkClientTimeout(int zkClientTimeout) {
        this.zkClientTimeout = zkClientTimeout;
    }

    /**
     * @return
     * @uml.property name="connectedSignal"
     */
    public CountDownLatch getConnectedSignal() {
        return connectedSignal;
    }

    /**
     * @param connectedSignal
     * @uml.property name="connectedSignal"
     */
    public void setConnectedSignal(CountDownLatch connectedSignal) {
        this.connectedSignal = connectedSignal;
    }

    public static void main(String[] arg) throws Exception {
        // 10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181/tis/main
        TerminatorZkClient client = TerminatorZkClient.create("10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181", 30000, new OnReconnect() {

            @Override
            public String getReconnectName() {
                return null;
            }

            @Override
            public void onReconnect(TerminatorZkClient zkClient) throws Exception {
            }
        }, false);
        client.rcreatePath("/tis/main");
        client.close();
    }
}
