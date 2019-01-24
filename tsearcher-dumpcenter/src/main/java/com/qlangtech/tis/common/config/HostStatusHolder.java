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
package com.qlangtech.tis.common.config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import com.qlangtech.tis.common.zk.NodeLifeManager;
import com.qlangtech.tis.common.zk.TerminatorZKException;
import com.qlangtech.tis.common.zk.TerminatorZkClient;

/*
 * 搜索服务对应的所有机器状态的Holder
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class HostStatusHolder extends ConcurrentHashMap<String, Boolean> {

    private static Log log = LogFactory.getLog(HostStatusHolder.class);

    private static final long serialVersionUID = -1006468707809294518L;

    /**
     * @uml.property  name="serviceConfig"
     * @uml.associationEnd
     */
    private IServiceConfig serviceConfig = null;

    /**
     * @uml.property  name="nodeLifeManager"
     * @uml.associationEnd
     */
    private NodeLifeManager nodeLifeManager = null;

    public HostStatusHolder(TerminatorZkClient zkClient, IServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
        this.nodeLifeManager = new NodeLifeManager(zkClient);
        this.initState();
    }

    public void initState() {
        Set<String> allIps = this.serviceConfig.getAllNodeIps();
        log.warn("初始化名为==>" + serviceConfig.getServiceName() + "的搜索服务的所有机器状态.");
        for (final String ip : allIps) {
            boolean isAlive = false;
            try {
                isAlive = this.nodeLifeManager.isAlive(ip, new Watcher() {

                    @Override
                    public void process(WatchedEvent event) {
                        String path = event.getPath();
                        EventType type = event.getType();
                        log.warn("机器节点可用状态变更，path ==> " + path + " EventType ==> " + type);
                        if (event.getType() == EventType.NodeCreated) {
                            log.warn("机器节点可用状态变更 [" + ip + "] ===> true ,自此 此节点可被访问.");
                            put(ip, true);
                        }
                        if (event.getType() == EventType.NodeDeleted) {
                            log.warn("机器节点可用状态变更 [" + ip + "] ===> false ,自此 此节点将不被访问.");
                            put(ip, false);
                        }
                        log.warn("重新绑定对znode ==> " + path + " 的Watcher.");
                        try {
                            HostStatusHolder.this.nodeLifeManager.getZkClient().exists(event.getPath(), this);
                        } catch (TerminatorZKException e) {
                            log.error(e, e);
                        }
                    }
                });
            } catch (TerminatorZKException e) {
                log.error("获取ip为[" + ip + "] 的机器可用状态时出现异常，系统默认此机器为不可用状态", e);
            }
            this.put(ip, isAlive);
        }
    }

    /**
     * 判断一台机器当前是否可用
     *
     * @param ip
     * @return
     */
    public boolean isAlive(String ip) {
        if (this.containsKey(ip)) {
            return this.get(ip);
        } else {
            return false;
        }
    }

    /**
     * @return
     * @uml.property  name="serviceConfig"
     */
    public IServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    /**
     * @param serviceConfig
     * @uml.property  name="serviceConfig"
     */
    public void setServiceConfig(IServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    /**
     * @return
     * @uml.property  name="nodeLifeManager"
     */
    public NodeLifeManager getNodeLifeManager() {
        return nodeLifeManager;
    }

    /**
     * @param nodeLifeManager
     * @uml.property  name="nodeLifeManager"
     */
    public void setNodeLifeManager(NodeLifeManager nodeLifeManager) {
        this.nodeLifeManager = nodeLifeManager;
    }
}
