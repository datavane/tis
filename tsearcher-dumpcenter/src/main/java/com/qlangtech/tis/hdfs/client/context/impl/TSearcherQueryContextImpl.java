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
package com.qlangtech.tis.hdfs.client.context.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.common.config.ServiceConfigSupport;
import com.qlangtech.tis.common.zk.OnReconnect;
import com.qlangtech.tis.common.zk.TerminatorZkClient;
import com.qlangtech.tis.exception.TerminatorInitException;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.hdfs.client.router.GroupRouter;
import com.qlangtech.tis.hdfs.client.status.SolrCoreStatusHolder;
import com.qlangtech.tis.trigger.util.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TSearcherQueryContextImpl implements ServiceConfigSupport, InitializingBean, TSearcherQueryContext {

    static final Log logger = LogFactory.getLog(TSearcherQueryContextImpl.class);

    public static final int DEFAULT_ZK_TIMEOUT = 300000;

    private int zkTimeout = DEFAULT_ZK_TIMEOUT;

    protected String serviceName = null;

    private String zkAddress;

    // private String shardKey = "id";
    private GroupRouter groupRouter;

    private IServiceConfig serviceConfig = null;

    private TerminatorZkClient zkClient = null;

    private final List<ServiceConfigChangeListener> serviceConfigChangeListener = new ArrayList<ServiceConfigChangeListener>();

    public static interface ServiceConfigChangeListener {

        public void onChange(IServiceConfig config);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
    }

    private void init() throws TerminatorInitException {
        try {
            // initZookeeperClient();
            initCoreInClusterArchitView();
            // initGroupRouter();
            inihostStatusHolder();
        } catch (Exception e) {
            throw new TerminatorInitException("Load ServiceConfig Object From ZK Have Error: ", e);
        }
    }

    /**
     * 触发服务配置更新事件
     */
    public void fireServiceConfigChange() {
        this.onServiceConfigChange(this.getServiceConfig());
    }

    /**
     */
    public TSearcherQueryContextImpl() {
        super();
    }

    public final IServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(IServiceConfig serviceConfig) {
        Assert.assertNotNull("param serviceConfig can not be null", serviceConfig);
        this.serviceConfig = serviceConfig;
        for (ServiceConfigChangeListener listener : serviceConfigChangeListener) {
            listener.onChange(serviceConfig);
        }
    }

    @Override
    public final void onServiceConfigChange(IServiceConfig serviceConfig) {
        this.setServiceConfig(serviceConfig);
    }

    @Override
    public void addCoreConfigChangeListener(ServiceConfigChangeListener listener) {
        this.serviceConfigChangeListener.add(listener);
    }

    // private void initZookeeperClient() {
    // 
    // if (zkAddress == null) {
    // throw new IllegalArgumentException("zkaddress can not be null");
    // }
    // 
    // if (zkClient == null) {
    // zkClient = TerminatorZkClient.create(zkAddress, zkTimeout, null,
    // true);
    // }
    // }
    private void initCoreInClusterArchitView() {
        // if (this.canConnectToZK) {
        logger.warn("Connect ZK SUC，Load ServiceConfig From ZK");
        try {
            this.getServiceConfig().checkBySelf();
        } catch (Exception e) {
            throw new TerminatorInitException("Load ServiceConfig Object From ZK Have Error: ", e);
        }
    // if (this.getServiceConfig() != null) {
    // try {
    // ServiceConfig.backUp2LocalFS(this.getServiceConfig());
    // } catch (IOException e) {
    // throw new TerminatorInitException(
    // "从ZK上装载ServiceConfig对象持久化到本地文件系统作为备份失败", e);
    // }
    // }
    // zkClient.setOnReconnect(new OnReconnect() {
    // public void onReconnect(TerminatorZkClient zkClient) {
    // try {
    // serviceConfig.initConfig();
    // } catch (TerminatorZKException e) {
    // logger.warn("[" + serviceName
    // + "]和ZK断开后重新连接ZK，并重新初始化seviceConfig出现错误:", e);
    // }
    // 
    // }
    // 
    // @Override
    // public String getReconnectName() {
    // return "ServiceConfigEvent";
    // }
    // });
    }

    private SolrCoreStatusHolder hostStatusHolder;

    private final void inihostStatusHolder() {
        // if (canConnectToZK) {
        hostStatusHolder = new SolrCoreStatusHolder(zkClient, serviceConfig);
        // } else {
        // hostStatusHolder = new SolrCoreStatusHolder(zkClient, serviceConfig)
        // {
        // private static final long serialVersionUID = -7034391383912579505L;
        // 
        // public void initState() {
        // }
        // 
        // public boolean isAlive(String ip) {
        // return true;
        // }
        // };
        // }
        zkClient.setOnReconnect(new OnReconnect() {

            public void onReconnect(TerminatorZkClient zkClient) {
                hostStatusHolder.loadIsAlive();
                logger.warn("zk server reconnect event occure");
            }

            @Override
            public String getReconnectName() {
                return "SolrHostStatusHoldEvent";
            }
        });
    }

    public final SolrCoreStatusHolder getHostStatusHolder() {
        return hostStatusHolder;
    }

    public final GroupRouter getGroupRouter() {
        return groupRouter;
    }

    public final void setGroupRouter(GroupRouter groupRouter) {
        this.groupRouter = groupRouter;
    }

    public final String getServiceName() {
        return serviceName;
    }

    public final void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public final void setZkAddress(String zkAddress) {
        this.zkAddress = zkAddress;
    }

    // public final String getShardKey() {
    // return shardKey;
    // }
    // 
    // public final void setShardKey(String shardKey) {
    // this.shardKey = shardKey;
    // }
    public TerminatorZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(TerminatorZkClient zkClient) {
        this.zkClient = zkClient;
    }

    // @Override
    public Set<String> getGroupNameSet() {
        return Collections.emptySet();
    }
}
