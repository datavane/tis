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

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.cloud.DocCollection;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.ZkStateReader;
import com.qlangtech.tis.common.TerminatorConstant;
import com.qlangtech.tis.common.zk.TerminatorZKException;
import com.qlangtech.tis.common.zk.TerminatorZkClient;
import com.qlangtech.tis.hdfs.client.status.SolrCoreStatusHolder;

/*
 * 对应于一个搜索服务的集群节点配置
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ServiceConfig implements Serializable, IServiceConfig {

    private static Log log = LogFactory.getLog(ServiceConfig.class);

    private static final long serialVersionUID = -3167055235327688049L;

    private String serviceName;

    private transient TerminatorZkClient zkClient;

    private transient ServiceConfigSupport serviceConfigSupport;

    private transient SolrCoreStatusHolder holder;

    private static ZkStateReader zkStateReader;

    // public DocRouter getRouter() {
    // return ZkStateReader.getCollectionLive(zkStateReader, serviceName)
    // .getRouter();
    // }
    public SolrCoreStatusHolder getHolder() {
        return holder;
    }

    public void setHolder(SolrCoreStatusHolder holder) {
        this.holder = holder;
    }

    public ServiceConfig(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * group 的数量是否大于0 baisui add
     *
     * @return
     */
    @Override
    public boolean hasAnyGroup() {
        return getGroupSize() > 0;
    }

    @Override
    public int getGroupSize() {
        return this.getGroupNameSet().size();
    }

    public ServiceConfig(String serviceName, TerminatorZkClient zkClient, ServiceConfigSupport serviceConfigSupport) throws TerminatorZKException {
        this(serviceName);
        this.zkClient = zkClient;
        this.serviceConfigSupport = serviceConfigSupport;
        this.initConfig();
    }

    // /**
    // * 备份序列化对象到本地文件系统
    // *
    // * @param dir
    // * @throws IOException
    // */
    // public static void backUp2LocalFS(ServiceConfig serviceConfig)
    // throws IOException {
    // ObjectOutputStream out = null;
    // try {
    // File file = new File("serviceConfig.bak");
    // log.warn("备份ServiceConfig对象到本地文件系统 ==> " + file.getAbsolutePath());
    // if (file.exists()) {
    // file.delete();
    // }
    // file.createNewFile();
    // FileOutputStream fileOut = new FileOutputStream(file);
    // out = new ObjectOutputStream(fileOut);
    // out.writeObject(serviceConfig);
    // out.flush();
    // } catch (Throwable e) {
    // log.warn("ignor this error", e);
    // } finally {
    // if (out != null)
    // out.close();
    // }
    // }
    // /**
    // * 从本地文件系统中读取备份的ServiceConfig对像
    // *
    // * @return
    // * @throws IOException
    // * @throws ClassNotFoundException
    // */
    // public static ServiceConfig loadFromLocalFS() throws IOException,
    // ClassNotFoundException {
    // ObjectInputStream input = null;
    // try {
    // File file = new File("serviceConfig.bak");
    // if (!file.exists()) {
    // throw new RuntimeException("找不到本地备份文件 ==> "
    // + file.getAbsolutePath());
    // }
    // FileInputStream fileInput = new FileInputStream(file);
    // input = new ObjectInputStream(fileInput);
    // return (ServiceConfig) input.readObject();
    // } catch (Throwable e) {
    // log.warn("ignore this error", e);
    // } finally {
    // IOUtils.closeQuietly(input);
    // }
    // 
    // return new ServiceConfig("");
    // }
    public synchronized void initConfig() throws TerminatorZKException {
        // getZkStateReader();
        // Map<String, Slice> groupSlice = zkStateReader.getClusterState()
        // .getActiveSlicesMap(serviceName);
        // 
        // for (String groupName : groupList) {
        // GroupConfig groupConfig = new GroupConfig(groupName);
        // this.addGroupConfig(groupConfig);
        // 
        // GroupWatcher groupWatcher = new GroupWatcher(zkClient, this,
        // groupName);
        // 
        // String groupPath = TerminatorZKUtils.contactZnodePaths(servicePath,
        // groupName);
        // groupWatcher.setGroupPath(groupPath);
        // List<String> hostList = zkClient.getChildren(groupPath,
        // groupWatcher);
        // for (String hostInfo : hostList) {
        // HostConfig hostConfig = HostInfoParser.toHostConfig(hostInfo);
        // groupConfig.addHostConfig(hostConfig);
        // }
        // }
        // 
        // // ===========================================================
        // 
        // log.warn(" init ServicViewConfig!!!");
        // String servicePath = TerminatorZKUtils.getMainPath(serviceName);
        // Watcher serviceWatcher = new ServiceWatcher(serviceName, zkClient,
        // serviceConfigSupport);
        // try {
        // if (!zkClient.exists(servicePath, serviceWatcher)) {
        // log.warn("CenterNode may be not Publish Service View to ZK,So the Service
        // Client must be waitting util CenterNode publish the Service View");
        // // return ;
        // }
        // } catch (Exception e) {
        // log.warn(" init ServicViewConfig have Error:", e);
        // return;
        // }
        // 
        // List<String> groupList = null;
        // try {
        // groupList = zkClient.getChildren(servicePath, serviceWatcher);
        // 
        // } catch (Exception e) {
        // log.warn("Get zk path["
        // + servicePath
        // +
        // "]'s children have problem,may be tsearcher server have not Created,Please
        // ignore the error!!");
        // }
        // if (groupList == null) {
        // log.warn(" Zk path[" + servicePath
        // + "]'s children is null ,so do noting!");
        // return;
        // }
        // 
        // for (String groupName : groupList) {
        // GroupConfig groupConfig = new GroupConfig(groupName);
        // this.addGroupConfig(groupConfig);
        // 
        // GroupWatcher groupWatcher = new GroupWatcher(zkClient, this,
        // groupName);
        // 
        // String groupPath = TerminatorZKUtils.contactZnodePaths(servicePath,
        // groupName);
        // groupWatcher.setGroupPath(groupPath);
        // List<String> hostList = zkClient.getChildren(groupPath,
        // groupWatcher);
        // for (String hostInfo : hostList) {
        // HostConfig hostConfig = HostInfoParser.toHostConfig(hostInfo);
        // groupConfig.addHostConfig(hostConfig);
        // }
        // }
        log.warn("ServiceConfig's structure :\n{\n" + this.toString() + "\n}\n");
    }

    /**
     * @throws TerminatorZKException
     */
    public ZkStateReader getZkStateReader() {
        if (zkStateReader == null) {
            synchronized (ServiceConfig.class) {
                if (zkStateReader == null) {
                    zkStateReader = new ZkStateReader(this.zkClient);
                    try {
                        zkStateReader.createClusterStateWatchersAndUpdate();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return zkStateReader;
    }

    /**
     * 判断该搜索服务是否是单索引(无索引切分)搜索
     *
     * @return
     */
    public boolean isSingle() {
        return getDocCollection().getSlices().size() > 1;
    }

    public DocCollection getDocCollection() {
        return this.getZkStateReader().getClusterState().getCollection(this.getServiceName());
    }

    /**
     * 获取该搜索服务对应的所有的分组名称
     *
     * @return
     */
    @Override
    public synchronized Set<String> getGroupNameSet() {
        return getDocCollection().getSlicesMap().keySet();
    }

    public Set<String> keySet() {
        return getGroupNameSet();
    }

    /**
     * 获取该搜索服务的所有机器节点的IP
     *
     * @return
     */
    @Override
    public Set<String> getAllNodeIps() {
        // return ipSet;
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized Map<String, List<String>> getAllCoreIpMap() {
        // return ipMap;
        throw new UnsupportedOperationException();
    }

    /**
     * 获取单索引的coreName
     *
     * @return
     */
    @Override
    public String getSingleCoreName() {
        return serviceName + TerminatorConstant.CORENAME_SEPERATOR + TerminatorConstant.SINGLE_CORE_GROUP_NAME;
    }

    /**
     * 获取该搜索服务的搜有coreName
     *
     * @return
     */
    @Override
    public Set<String> getCoreNameSet() {
        Set<String> groupNameSet = this.getGroupNameSet();
        Set<String> coreNameSet = new HashSet<String>(groupNameSet.size());
        for (String groupName : groupNameSet) {
            coreNameSet.add(serviceName + TerminatorConstant.HSF_VERSION_SEPERATOR + groupName);
        }
        return coreNameSet;
    }

    // public void addGroupConfig(GroupConfig groupConfig) {
    // this.put(groupConfig.getGroupName(), groupConfig);
    // }
    // public void removeGroupConfig(GroupConfig groupConfig) {
    // this.remove(groupConfig.getGroupName());
    // }
    @Override
    public GroupConfig getGroupConfig(String groupName) {
        Slice slice = this.getDocCollection().getSlice(groupName);
        HostConfig hostConfig = null;
        GroupConfig groupConfig = new GroupConfig(groupName);
        for (Replica replica : slice.getReplicas()) {
            hostConfig = new HostConfig(replica.getNodeName());
            groupConfig.addHostConfig(hostConfig);
        }
        return groupConfig;
    }

    /**
     * @return
     * @uml.property name="serviceName"
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName
     * @uml.property name="serviceName"
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public synchronized int getGroupNum() {
        return getDocCollection().getSlicesMap().size();
    }

    @Override
    public void checkBySelf() {
        if (getGroupNum() == 0) {
            // throw new
            // RuntimeException("ServiceConfig配置对象理论上来讲有问题,没有任何的GroupConfig信息.");
            log.warn("CenterNode have not publish ZK cluster view,So it does not support search query!!!!!");
        }
    }

    public String toString() {
        // throw new UnsupportedOperationException();
        return this.getDocCollection().toString();
    }
}
