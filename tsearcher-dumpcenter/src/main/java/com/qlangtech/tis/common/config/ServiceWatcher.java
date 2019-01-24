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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import com.qlangtech.tis.common.zk.TerminatorZKException;
import com.qlangtech.tis.common.zk.TerminatorZKUtils;
import com.qlangtech.tis.common.zk.TerminatorZkClient;

/*
 * 监听搜索服务节点结构信息变更
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ServiceWatcher implements Watcher {

    private static Log log = LogFactory.getLog(ServiceWatcher.class);

    /**
     * @uml.property name="zkClient"
     * @uml.associationEnd
     */
    private TerminatorZkClient zkClient = null;

    private String serviceName = null;

    /**
     * @uml.property name="serviceConfigSupport"
     * @uml.associationEnd
     */
    private ServiceConfigSupport serviceConfigSupport;

    public ServiceWatcher(String serviceName, TerminatorZkClient zkClient, ServiceConfigSupport serviceConfigSupport) {
        this.serviceName = serviceName;
        this.zkClient = zkClient;
        this.serviceConfigSupport = serviceConfigSupport;
        this.checkFields();
    }

    private void checkFields() {
    }

    @Override
    public void process(WatchedEvent event) {
    // String path = event.getPath();
    // EventType type = event.getType();
    // 
    // log.warn("ServiceConfig结构有变化，对应的zookeeper的znode的path ==> " + path
    // + "EventType ==> " + type);
    // log.warn("现有的ServiceConfig的结构如下:\n {\n"
    // + serviceConfigSupport.getServiceConfig().toString() + "\n}\n");
    // 
    // if (type == EventType.NodeChildrenChanged) {
    // log.warn("ZK通知重新加载ServiceConfig对象,业务[" + serviceName + "]的组数发生变化");
    // 
    // List<String> groupList = null;
    // try {
    // groupList = zkClient.getChildren(path, this);
    // } catch (TerminatorZKException e) {
    // log.error("获取path ==> " + path + " 的孩子节点失败,忽略此次改动.", e);
    // if (e.getCause() != null
    // && e.getCause() instanceof KeeperException.NoNodeException) {
    // // ServiceConfig serviceConfig = new
    // // ServiceConfig(serviceName);
    // // serviceConfigSupport.onServiceConfigChange(serviceConfig);
    // }
    // return;
    // }
    // 
    // if (groupList == null || groupList.isEmpty()) {
    // log.error("path ==>  " + path
    // + "没有孩子节点，可能是ZKMainTree的子节点全部被删除 ,该错误不可容忍！！");
    // // return;
    // }
    // 
    // List<GroupConfig> groupConfigList = new ArrayList<GroupConfig>();
    // boolean isOk = true;
    // if (groupList != null)
    // for (String groupName : groupList) {
    // GroupConfig groupConfig = new GroupConfig(groupName);
    // groupConfigList.add(groupConfig);
    // String groupPath = TerminatorZKUtils.contactZnodePaths(
    // path, groupName);
    // List<String> hostList = null;
    // try {
    // GroupWatcher groupWatcher = new GroupWatcher(zkClient,
    // serviceConfigSupport.getServiceConfig(),
    // groupName);
    // // groupWatcher没有groupPath没有初始化,可能导致重启zk时不断报异常
    // groupWatcher.setGroupPath(groupPath);
    // 
    // hostList = zkClient
    // .getChildren(groupPath, groupWatcher);
    // } catch (TerminatorZKException e) {
    // log.error("获取path ==> " + groupPath + " 的孩子节点失败.", e);
    // isOk = false;
    // }
    // for (String hostInfo : hostList) {
    // HostConfig hostConfig = HostInfoParser
    // .toHostConfig(hostInfo);
    // groupConfig.addHostConfig(hostConfig);
    // }
    // }
    // 
    // if (!isOk) {
    // log.error("由于遍历znode节点过程中的异常，此次推送无效.");
    // } else {
    // ServiceConfig serviceConfig = new ServiceConfig(serviceName);
    // // for(GroupConfig groupConfig : groupConfigList){
    // // serviceConfig.addGroupConfig(groupConfig);
    // // }
    // serviceConfigSupport.onServiceConfigChange(serviceConfig);
    // log.warn("新的ServiceConfig的结构如下:\n {\n"
    // + serviceConfig.toString() + "\n}\n");
    // }
    // } else if (type == EventType.NodeDeleted) {
    // log.error("应用被删除,很可能是手工操作ZK失误,为了保险起见不重新更新ServiceConfig,只是继续监控路径["
    // + path + "]");
    // try {
    // zkClient.exists(path, this);
    // } catch (TerminatorZKException e) {
    // log.error("获取path ==> " + path + " 的孩子节点失败,忽略此次改动.", e);
    // if (e.getCause() != null
    // && e.getCause() instanceof KeeperException.NoNodeException) {
    // // ServiceConfig serviceConfig = new
    // // ServiceConfig(serviceName);
    // // serviceConfigSupport.onServiceConfigChange(serviceConfig);
    // }
    // return;
    // }
    // // ServiceConfig serviceConfig = new ServiceConfig(serviceName);
    // // serviceConfigSupport.onServiceConfigChange(serviceConfig);
    // } else if (type == EventType.NodeCreated) {
    // 
    // log.warn("[" + serviceName + "]节点被创建，重新加载ServiceConfig对象.");
    // try {
    // Thread.sleep(3000);// 等等ZK节点路径完整生成
    // } catch (InterruptedException e) {
    // log.warn("等待线程被Interrupt", e);
    // }
    // List<String> groupList = null;
    // try {
    // groupList = zkClient.getChildren(path, this);
    // } catch (TerminatorZKException e) {
    // log.error("获取path ==> " + path + " 的孩子节点失败,忽略此次改动.", e);
    // if (e.getCause() != null
    // && e.getCause() instanceof KeeperException.NoNodeException) {
    // ServiceConfig serviceConfig = new ServiceConfig(serviceName);
    // serviceConfigSupport.onServiceConfigChange(serviceConfig);
    // }
    // return;
    // }
    // 
    // if (groupList == null || groupList.isEmpty()) {
    // log.error("path ==>  " + path
    // + "没有孩子节点，可能是ZKMain被删除 ，需要暂停客户端的导入.");
    // // return;
    // }
    // 
    // List<GroupConfig> groupConfigList = new ArrayList<GroupConfig>();
    // boolean isOk = true;
    // if (groupList != null)
    // for (String groupName : groupList) {
    // GroupConfig groupConfig = new GroupConfig(groupName);
    // groupConfigList.add(groupConfig);
    // String groupPath = TerminatorZKUtils.contactZnodePaths(
    // path, groupName);
    // List<String> hostList = null;
    // try {
    // GroupWatcher groupWatcher = new GroupWatcher(zkClient,
    // serviceConfigSupport.getServiceConfig(),
    // groupName);
    // groupWatcher.setGroupPath(groupPath);
    // hostList = zkClient
    // .getChildren(groupPath, groupWatcher);
    // } catch (TerminatorZKException e) {
    // log.error("获取path ==> " + groupPath + " 的孩子节点失败.", e);
    // isOk = false;
    // }
    // for (String hostInfo : hostList) {
    // HostConfig hostConfig = HostInfoParser
    // .toHostConfig(hostInfo);
    // groupConfig.addHostConfig(hostConfig);
    // }
    // }
    // 
    // if (!isOk) {
    // log.error("由于遍历znode节点过程中的异常，此次推送无效.");
    // } else {
    // ServiceConfig serviceConfig = new ServiceConfig(serviceName);
    // for (GroupConfig groupConfig : groupConfigList) {
    // // serviceConfig.addGroupConfig(groupConfig);
    // }
    // serviceConfigSupport.onServiceConfigChange(serviceConfig);
    // log.warn("新的ServiceConfig的结构如下:\n {\n"
    // + serviceConfig.toString() + "\n}\n");
    // }
    // 
    // } else {
    // log.warn("不识别的事件类型 path ==> " + path + "  ==> " + type
    // + ",不做任何处理，重新绑定Watcher监听对象.");
    // try {
    // if (path == null) {
    // path = TerminatorZKUtils.getMainPath(serviceName);
    // }
    // zkClient.exists(path, this);
    // } catch (TerminatorZKException e) {
    // log.error("路径[" + path + "]被事件[" + type + "]通知后重新绑定出错", e);
    // if (e.getCause() != null
    // && e.getCause() instanceof KeeperException.NoNodeException) {
    // // ServiceConfig serviceConfig = new
    // // ServiceConfig(serviceName);
    // // serviceConfigSupport.onServiceConfigChange(serviceConfig);
    // }
    // }
    // }
    }
}
