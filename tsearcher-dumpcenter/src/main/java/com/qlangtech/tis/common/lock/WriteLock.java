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
package com.qlangtech.tis.common.lock;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import static org.apache.zookeeper.CreateMode.EPHEMERAL_SEQUENTIAL;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import java.io.IOException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

/*
 * A <a href="package.html">protocol to implement an exclusive write lock or to elect a leader</a>. <p/> You invoke  {@link #lock()}  to  start the process of grabbing the lock; you may get the lock then or it may be  some time later. <p/> You can register a listener so that you are invoked  when you get the lock; otherwise you can ask if you have the lock by calling  {@link #isOwner()} 
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class WriteLock extends ProtocolSupport {

    private static final Logger LOG = Logger.getLogger(WriteLock.class);

    /**
     * @uml.property  name="dir"
     */
    private final String dir;

    /**
     * @uml.property  name="id"
     */
    private String id;

    /**
     * @uml.property  name="idName"
     * @uml.associationEnd
     */
    private ZNodeName idName;

    private String ownerId;

    private String lastChildId;

    private byte[] data = { 0x12, 0x34 };

    /**
     * @uml.property  name="callback"
     * @uml.associationEnd
     */
    private LockListener callback;

    /**
     * @uml.property  name="zop"
     * @uml.associationEnd
     */
    private LockZooKeeperOperation zop;

    public static void main(String[] args) throws Exception {
        final CountDownLatch connectedSignal = new CountDownLatch(1);
        Watcher w = new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                KeeperState zkState = event.getState();
                if (zkState == KeeperState.SyncConnected) {
                    connectedSignal.countDown();
                }
            }
        };
        final ZooKeeper zk = new ZooKeeper("10.232.15.46:2181", 3000, w);
        connectedSignal.await();
        for (int i = 0; i < 10; i++) {
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    String id = null;
                    try {
                        id = zk.create("/a", null, Ids.OPEN_ACL_UNSAFE, EPHEMERAL_SEQUENTIAL);
                    } catch (KeeperException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    System.out.println(id);
                }
            };
            new Thread(run).start();
        }
        Thread.sleep(1000000);
    /*	WriteLock lock = new WriteLock(zk, "/dislock", Ids.OPEN_ACL_UNSAFE);
		boolean isLock = lock.lock();
		if(isLock)
			System.out.println();
		lock.unlock();*/
    }

    /**
     * zookeeper contructor for writelock
     * @param zookeeper zookeeper client instance
     * @param dir the parent path you want to use for locking
     * @param acls the acls that you want to use for all the paths,
     * if null world read/write is used.
     */
    public WriteLock(ZooKeeper zookeeper, String dir, List<ACL> acl) {
        super(zookeeper);
        this.dir = dir;
        if (acl != null) {
            setAcl(acl);
        }
        this.zop = new LockZooKeeperOperation();
    }

    /**
     * zookeeper contructor for writelock with callback
     * @param zookeeper the zookeeper client instance
     * @param dir the parent path you want to use for locking
     * @param acl the acls that you want to use for all the paths
     * @param callback the call back instance
     */
    public WriteLock(ZooKeeper zookeeper, String dir, List<ACL> acl, LockListener callback) {
        this(zookeeper, dir, acl);
        this.callback = callback;
    }

    /**
     * return the current locklistener
     * @return the locklistener
     */
    public LockListener getLockListener() {
        return this.callback;
    }

    /**
     * register a different call back listener
     * @param callback the call back instance
     */
    public void setLockListener(LockListener callback) {
        this.callback = callback;
    }

    /**
     * Removes the lock or associated znode if
     * you no longer require the lock. this also
     * removes your request in the queue for locking
     * in case you do not already hold the lock.
     * @throws RuntimeException throws a runtime exception
     * if it cannot connect to zookeeper.
     */
    public synchronized void unlock() throws RuntimeException {
        if (!isClosed() && id != null) {
            // this process when closing if we cannot reconnect to ZK
            try {
                ZooKeeperOperation zopdel = new ZooKeeperOperation() {

                    public boolean execute() throws KeeperException, InterruptedException {
                        zookeeper.delete(id, -1);
                        return Boolean.TRUE;
                    }
                };
                zopdel.execute();
            } catch (InterruptedException e) {
                LOG.warn("Caught: " + e, e);
                // set that we have been interrupted.
                Thread.currentThread().interrupt();
            } catch (KeeperException.NoNodeException e) {
            // do nothing
            } catch (KeeperException e) {
                LOG.warn("Caught: " + e, e);
                throw (RuntimeException) new RuntimeException(e.getMessage()).initCause(e);
            } finally {
                if (callback != null) {
                    callback.lockReleased();
                }
                id = null;
            }
        }
    }

    /**
     * the watcher called on
     * getting watch while watching
     * my predecessor
     */
    private class LockWatcher implements Watcher {

        public void process(WatchedEvent event) {
            // lets either become the leader or watch the new/updated node
            LOG.debug("Watcher fired on path: " + event.getPath() + " state: " + event.getState() + " type " + event.getType());
            try {
                lock();
            } catch (Exception e) {
                LOG.warn("Failed to acquire lock: " + e, e);
            }
        }
    }

    /**
     * a zoookeeper operation that is mainly responsible
     * for all the magic required for locking.
     */
    private class LockZooKeeperOperation implements ZooKeeperOperation {

        /**
         * find if we have been created earler if not create our node
         *
         * @param prefix the prefix node
         * @param zookeeper teh zookeeper client
         * @param dir the dir paretn
         * @throws KeeperException
         * @throws InterruptedException
         */
        private void findPrefixInChildren(String prefix, ZooKeeper zookeeper, String dir) throws KeeperException, InterruptedException {
            List<String> names = zookeeper.getChildren(dir, false);
            for (String name : names) {
                if (name.startsWith(prefix)) {
                    id = name;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found id created last time: " + id);
                    }
                    break;
                }
            }
            if (id == null) {
                id = zookeeper.create(dir + "/" + prefix, data, getAcl(), EPHEMERAL_SEQUENTIAL);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Created id: " + id);
                }
            }
        }

        /**
         * the command that is run and retried for actually
         * obtaining the lock
         * @return if the command was successful or not
         */
        public boolean execute() throws KeeperException, InterruptedException {
            do {
                if (id == null) {
                    long sessionId = zookeeper.getSessionId();
                    String prefix = "x-" + sessionId + "-";
                    // lets try look up the current ID if we failed
                    // in the middle of creating the znode
                    findPrefixInChildren(prefix, zookeeper, dir);
                    idName = new ZNodeName(id);
                }
                if (id != null) {
                    List<String> names = zookeeper.getChildren(dir, false);
                    if (names.isEmpty()) {
                        LOG.warn("No children in: " + dir + " when we've just " + "created one! Lets recreate it...");
                        // lets force the recreation of the id
                        id = null;
                    } else {
                        // lets sort them explicitly (though they do seem to come back in order ususally :)
                        SortedSet<ZNodeName> sortedNames = new TreeSet<ZNodeName>();
                        for (String name : names) {
                            sortedNames.add(new ZNodeName(dir + "/" + name));
                        }
                        ownerId = sortedNames.first().getName();
                        SortedSet<ZNodeName> lessThanMe = sortedNames.headSet(idName);
                        if (!lessThanMe.isEmpty()) {
                            ZNodeName lastChildName = lessThanMe.last();
                            lastChildId = lastChildName.getName();
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("watching less than me node: " + lastChildId);
                            }
                            Stat stat = zookeeper.exists(lastChildId, new LockWatcher());
                            if (stat != null) {
                                return Boolean.FALSE;
                            } else {
                                LOG.warn("Could not find the" + " stats for less than me: " + lastChildName.getName());
                            }
                        } else {
                            if (isOwner()) {
                                if (callback != null) {
                                    callback.lockAcquired();
                                }
                                return Boolean.TRUE;
                            }
                        }
                    }
                }
            } while (id == null);
            return Boolean.FALSE;
        }
    }

    /**
     * Attempts to acquire the exclusive write lock returning whether or not it was
     * acquired. Note that the exclusive lock may be acquired some time later after
     * this method has been invoked due to the current lock owner going away.
     */
    public synchronized boolean lock() throws KeeperException, InterruptedException {
        if (isClosed()) {
            return false;
        }
        ensurePathExists(dir);
        return (Boolean) retryOperation(zop);
    }

    /**
     * return the parent dir for lock
     * @return  the parent dir used for locks.
     * @uml.property  name="dir"
     */
    public String getDir() {
        return dir;
    }

    /**
     * Returns true if this node is the owner of the
     *  lock (or the leader)
     */
    public boolean isOwner() {
        return id != null && ownerId != null && id.equals(ownerId);
    }

    /**
     * return the id for this lock
     * @return  the id for this lock
     * @uml.property  name="id"
     */
    public String getId() {
        return this.id;
    }
}
