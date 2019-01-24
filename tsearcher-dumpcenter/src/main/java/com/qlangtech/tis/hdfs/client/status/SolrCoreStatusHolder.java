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
package com.qlangtech.tis.hdfs.client.status;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import com.qlangtech.tis.common.config.HostStatusHolder;
import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.common.config.ServiceConfig;
import com.qlangtech.tis.common.zk.CoreLifeManager;
import com.qlangtech.tis.common.zk.SolrCoreZKUtils;
import com.qlangtech.tis.common.zk.TerminatorZKException;
import com.qlangtech.tis.common.zk.TerminatorZkClient;

/*
 * @description
 * @since 2012-8-28 下午05:53:21
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SolrCoreStatusHolder extends ConcurrentHashMap<String, Boolean> {

    private static Log log = LogFactory.getLog(HostStatusHolder.class);

    private static final long serialVersionUID = -1006468707809294518L;

    private IServiceConfig serviceConfig = null;

    private CoreLifeManager lifeManager;

    public SolrCoreStatusHolder(TerminatorZkClient zkClient, IServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        this.lifeManager = new CoreLifeManager(zkClient);
        this.init();
    }

    public void init() {
        this.serviceConfig.setHolder(this);
        this.loadIsAlive();
    }

    public void loadIsAlive(String groupName, Set<String> ips) {
        if (groupName == null) {
            return;
        }
        String serviceName = serviceConfig.getServiceName();
        if (serviceName == null) {
            return;
        }
        if (ips == null || ips.size() == 0) {
            return;
        }
        for (String ip : ips) {
            this.initSolrCoreIsAlive(groupName, serviceName, ip);
        }
    }

    public void initSolrCoreIsAlive(final String groupName, final String serviceName, final String ip) {
        boolean isAlive = false;
        final String ipAppendCoreName = ip + "-" + serviceName + "-" + groupName;
        try {
            isAlive = this.lifeManager.isAlive(ip, serviceName + "-" + groupName, new Watcher() {

                @Override
                public void process(WatchedEvent event) {
                    String path = event.getPath();
                    EventType type = event.getType();
                    log.warn("机器节点可用状态变更，path ==> " + path + " EventType ==> " + type);
                    if (event.getType() == EventType.NodeCreated) {
                        log.warn("机器节点可用状态变更 [" + ipAppendCoreName + "] ===> true ,自此 此节点可被访问.");
                        put(ipAppendCoreName, true);
                    }
                    if (event.getType() == EventType.NodeDeleted) {
                        log.warn("机器节点可用状态变更 [" + ipAppendCoreName + "] ===> false ,自此 此节点将不被访问.");
                        put(ipAppendCoreName, false);
                    }
                    log.warn("重新绑定对znode ==> " + path + " 的Watcher.");
                    try {
                        if (path == null) {
                            path = SolrCoreZKUtils.getSolrCoreStatusPath(ip, serviceName + "-" + groupName);
                        }
                        SolrCoreStatusHolder.this.lifeManager.getZkClient().exists(path, this);
                    } catch (TerminatorZKException e) {
                        log.error(e, e);
                    }
                }
            });
        } catch (TerminatorZKException e) {
            log.error("获取ip为[" + ip + "] 的机器可用状态时出现异常，系统默认此机器为不可用状态", e);
        }
        this.put(ipAppendCoreName, isAlive);
    }

    public void loadIsAlive() {
    // Map<String, List<String>> allIps = this.serviceConfig.getAllCoreIpMap();
    // String serviceName = serviceConfig.getServiceName();
    // log.warn("Init ServiceName ==>" + serviceConfig.getServiceName()
    // + " All Search Index Service.");
    // for (final Map.Entry<String, List<String>> entry : allIps.entrySet()) {
    // String groupName = entry.getKey();
    // List<String> list = entry.getValue();
    // for (final String ip : list) {
    // this.initSolrCoreIsAlive(groupName, serviceName, ip);
    // }
    // }
    }

    public boolean isAlive(String ipAppendCoreName) {
        if (this.containsKey(ipAppendCoreName)) {
            return this.get(ipAppendCoreName);
        } else {
            return false;
        }
    }

    public IServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public CoreLifeManager getNodeLifeManager() {
        return lifeManager;
    }

    public void setNodeLifeManager(CoreLifeManager lifeManager) {
        this.lifeManager = lifeManager;
    }
}
