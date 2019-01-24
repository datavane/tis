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

import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import com.qlangtech.tis.common.zk.TerminatorZKException;
import com.qlangtech.tis.common.zk.TerminatorZkClient;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GroupWatcher implements Watcher {

    private static Log log = LogFactory.getLog(GroupWatcher.class);

    /**
     * @uml.property  name="zkClient"
     * @uml.associationEnd
     */
    private TerminatorZkClient zkClient = null;

    /**
     * @uml.property  name="serviceConfig"
     * @uml.associationEnd
     */
    private IServiceConfig serviceConfig = null;

    private String groupName = null;

    private String groupPath = null;

    public void setGroupPath(String groupPath) {
        this.groupPath = groupPath;
    }

    public GroupWatcher(TerminatorZkClient zkClient, IServiceConfig serviceConfig, String groupName) {
        this.zkClient = zkClient;
        this.serviceConfig = serviceConfig;
        this.groupName = groupName;
    }

    @Override
    public void process(WatchedEvent event) {
    // String path = event.getPath();
    // EventType type = event.getType();
    // log.warn("["+serviceConfig.getServiceName()+"]GroupConfig结构有变化，对应的zookeeper的znode的path ==> " + path + " EventType ==> " + type);
    // if(type == EventType.NodeChildrenChanged){//main-tree/search4xx/0/子节点出现变化
    // try {
    // Thread.sleep(1000);
    // } catch (InterruptedException e1) {
    // try {
    // zkClient.getChildren(path,this);
    // } catch (TerminatorZKException e) {
    // log.error(e,e);
    // }
    // Thread.currentThread().interrupt();
    // }
    // List<String> hostList = null;
    // try {
    // if(!zkClient.exists(path)){
    // log.warn("path ==> " + path + " 的节点不存在.");
    // GroupConfig groupConfig = new GroupConfig(groupName);
    // serviceConfig.removeGroupConfig(groupConfig);
    // return ;
    // }
    // hostList = zkClient.getChildren(path, this);
    // } catch (TerminatorZKException e) {
    // log.error("获取path ==> " + path +" 的孩子节点失败.",e);
    // if(e.getCause() != null&&e.getCause() instanceof KeeperException.NoNodeException ){
    // //					GroupConfig groupConfig = new GroupConfig(groupName);
    // //					serviceConfig.removeGroupConfig(groupConfig);
    // }
    // return ;
    // }
    // GroupConfig groupConfig = new GroupConfig(groupName);
    // if(hostList == null || hostList.isEmpty()){
    // log.error("path ==>  " + path + "没有孩子节点，判断为误推送，忽略此次推送的信息.");
    // //serviceConfig.removeGroupConfig(groupConfig);
    // return;
    // }
    // for (String hostInfo : hostList) {
    // HostConfig hostConfig = HostInfoParser.toHostConfig(hostInfo);
    // groupConfig.addHostConfig(hostConfig);
    // }
    // serviceConfig.addGroupConfig(groupConfig);
    // }else if(type == EventType.NodeDeleted){
    // log.warn("path ==> " + path + "的znode节点被删除.");
    // GroupConfig groupConfig = new GroupConfig(groupName);
    // log.error("path ==>  " + path + "孩子节点被删除,删除组信息");
    // serviceConfig.removeGroupConfig(groupConfig);
    // try {
    // //zkClient.getChildren(path,this);
    // zkClient.exists(path, this);
    // }catch (Exception e) {
    // log.warn("path ==> " + path + "触发事件["+type+"]：",e);
    // 
    // }
    // return;
    // } else if (type == EventType.NodeCreated) {
    // try {
    // Thread.sleep(3000);// 等等ZK节点路径完整生成
    // } catch (InterruptedException e) {
    // log.warn("等待线程被Interrupt", e);
    // }
    // log.warn("path ==> " + path + "的组节点被创建！");
    // List<String> hostList = null;
    // try {// 继续Check一下
    // if (!zkClient.exists(path)) {
    // log.warn("path ==> " + path + " 的节点不存在.");
    // GroupConfig groupConfig = new GroupConfig(groupName);
    // serviceConfig.removeGroupConfig(groupConfig);
    // return;
    // }
    // hostList = zkClient.getChildren(path, this);
    // } catch (TerminatorZKException e) {
    // log.error("获取path ==> " + path + " 的孩子节点失败.", e);
    // if (e.getCause() != null
    // && e.getCause() instanceof KeeperException.NoNodeException) {
    // // GroupConfig groupConfig = new GroupConfig(groupName);
    // // serviceConfig.removeGroupConfig(groupConfig);
    // }
    // return;
    // }
    // GroupConfig groupConfig = new GroupConfig(groupName);
    // if (hostList == null || hostList.isEmpty()) {
    // log.error("path ==>  " + path + "没有孩子节点，判断为误推送，忽略此次推送的信息.");
    // // serviceConfig.removeGroupConfig(groupConfig);
    // return;
    // }
    // for (String hostInfo : hostList) {
    // HostConfig hostConfig = HostInfoParser.toHostConfig(hostInfo);
    // groupConfig.addHostConfig(hostConfig);
    // }
    // serviceConfig.addGroupConfig(groupConfig);
    // 
    // }
    // else {
    // log.warn("未知事件  path==> [" + path + "] type==>["+type+"].客户端GroupConfig不做任何变化！！");
    // //			GroupConfig groupConfig = new GroupConfig(groupName);
    // //			serviceConfig.removeGroupConfig(groupConfig);
    // try {
    // if(path==null){
    // path = groupPath;
    // }
    // zkClient.exists(path,this);
    // }catch (Exception e) {
    // log.warn("未知事件 path ==> " + path + "type==>["+type+"]后重新绑定Watcher出现错误！！",e);
    // }
    // }
    // if (this.serviceConfig.getHolder() != null) {
    // GroupConfig groupConfig = serviceConfig.getGroupConfig(groupName);
    // if (groupConfig != null) {
    // String groupName = groupConfig.getGroupName();
    // // for(String ip:groupConfig.keySet()){
    // this.serviceConfig.getHolder().loadIsAlive(groupName,
    // groupConfig.keySet());
    // // }
    // }
    // }
    }
}
