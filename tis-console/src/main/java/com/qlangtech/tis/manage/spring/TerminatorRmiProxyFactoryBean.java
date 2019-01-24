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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.server.RMIClientSocketFactory;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.cloud.OnReconnect;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import com.qlangtech.tis.TisZkClient;
//import com.qlangtech.tis.trigger.biz.dal.dao.JobConstant;
//import com.qlangtech.tis.trigger.socket.Constant;
import com.qlangtech.tis.trigger.zk.AbstractWatcher;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorRmiProxyFactoryBean extends RmiProxyFactoryBean {

    private final TisZkClient zkgetter;

    public TerminatorRmiProxyFactoryBean(final TisZkClient zookeeper) {
        super();
        // this.zkaddress = String.valueOf(zookeeper.getRemoteAddress());
        // this.zookeeper = zookeeper;
        this.zkgetter = zookeeper;
        // this.serviceAddressGetter = new
        // TriggerServiceAddressGetter(zkgetter);
        this.setRegistryClientSocketFactory(new RMIClientSocketFactory() {

            @Override
            public Socket createSocket(String host, int port) throws IOException {
                return new TerminatorSocket(host, port);
            }
        });
        zookeeper.addOnReconnect(new OnReconnect() {

            @Override
            public void command() {
                afterPropertiesSet();
            }
        });
    }

    private class TerminatorSocket extends Socket {

        public TerminatorSocket(String host, int port) throws UnknownHostException, IOException {
            super(host, port);
        }
    }

    @Override
    public void afterPropertiesSet() {
        try {
          //  final String parentPath = Constant.TRIGGER_SERVER + JobConstant.DOMAIN_TERMINAOTR;
            // final Watcher serverAddressWatcher = new Watcher() {
            // @Override
            // public void process(WatchedEvent event) {
            // 
            // if (EventType.None.equals(event.getType())) {
            // return;
            // }
            // 
            // Watcher w = this;
            // if (event.getType() == EventType.NodeCreated) {
            // afterPropertiesSet();
            // } else {
            // try {
            // zkgetter.getData(parentPath, w, new Stat(), true);
            // } catch (Exception e) {
            // throw new IllegalStateException(e);
            // }
            // }
            // }
            // };
            // List<String>
//            List<String> ipList = this.zkgetter.getChildren(parentPath, null, true);
//            Collections.shuffle(ipList);
//            String ipaddress = null;
//            for (String path : ipList) {
//                ipaddress = new String(this.zkgetter.getData(parentPath + "/" + path, new AbstractWatcher() {
//
//                    @Override
//                    protected void process(Watcher watcher) throws KeeperException, InterruptedException {
//                        afterPropertiesSet();
//                    }
//                }, new Stat(), true));
//                break;
//            }
//            if (StringUtils.isEmpty(ipaddress)) {
//                throw new IllegalStateException("ipaddress can not be null");
//            }
//            InetAddress remoteAddress = InetAddress.getByName(ipaddress);
//            if (!remoteAddress.isReachable(2000)) {
//                throw new IllegalStateException("address:" + ipaddress + " is not reachable");
//            }
//            final MessageFormat format = new MessageFormat("rmi://{0}:9997/consoleTriggerJobService");
//            this.setServiceUrl(format.format(new Object[] { new String(ipaddress) }));
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.setCacheStub(false);
        super.afterPropertiesSet();
    }

    // private static final String TRIGGER_SERVER =
    // "/terminator-lock/trigger_server";
    // private String getServiceAddress(
    // final TriggerServiceAddressGetter.Callback callback) {
    // final SolrZkClient zookeeper = zkgetter;
    // try {
    // 
    // if (zookeeper.exists(TRIGGER_SERVER, true) == null) {
    // throw new IllegalStateException("path:" + TRIGGER_SERVER
    // + " in zk is null," + zookeeper);
    // }
    // 
    // return new String(zookeeper.getData(TRIGGER_SERVER, new Watcher() {
    // @Override
    // public void process(WatchedEvent event) {
    // 
    // callback.execute(zookeeper);
    // 
    // }
    // 
    // }, new Stat(), true));
    // // (TRIGGER_SERVER, false,
    // // new Stat()));
    // 
    // if (callback == null) {
    // 
    // } else {
    // final Watcher serverAddressWatcher = new Watcher() {
    // @Override
    // public void process(WatchedEvent event) {
    // if (EventType.NodeDataChanged == event.getType()
    // || EventType.NodeCreated == event.getType()) {
    // // callback.execute(zookeeper);
    // } else {
    // try {
    // zookeeper.exists(TRIGGER_SERVER, this);
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // }
    // };
    // 
    // return new String(zookeeper.getData(TRIGGER_SERVER,
    // serverAddressWatcher, new Stat()));
    // }
    // 
    // } catch (IllegalStateException e) {
    // throw e;
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // }
    // public ZooKeeper getZookeeper() {
    // return zookeeper;
    // }
    public static void main(String[] args) {
    }
}
