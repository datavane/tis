/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.transfer;

import com.qlangtech.tis.async.message.client.consumer.IMQListener;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.realtime.yarn.rpc.TopicInfo;
import com.qlangtech.tis.spring.LauncherResourceUtils;
import com.qlangtech.tis.spring.LauncherResourceUtils.AppLauncherResource;
import com.qlangtech.tis.spring.ResourceXmlApplicationContext;
import com.qlangtech.tis.sql.parser.DBNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月31日
 */
public abstract class BasicTransferTool {

    private ResourceXmlApplicationContext appContext;

    // 本地保存监听的collection下的topic的状态
    protected final Map<String, TopicInfo> /* collection */
            collectionFocusTopicInfo = new HashMap<>();

    private final URLClassLoader classLoader;

    private final long timestamp;

    private static final Logger logger = LoggerFactory.getLogger(BasicTransferTool.class);

    public BeanFactory getSpringBeanFactory() {
        return this.appContext.getBeanFactory();
    }

    /**
     * @param classLoader
     * @param
     */
    public BasicTransferTool(URLClassLoader classLoader, long timestamp) {
        this.timestamp = timestamp;
        this.classLoader = classLoader;
    }

    protected final AtomicBoolean initialized = new AtomicBoolean(false);

    private AppLauncherResource launcherResource;

    protected List<String> getIndexNames() {
        if (launcherResource == null) {
            throw new IllegalStateException("method startService has not been execute");
        }
        return launcherResource.getIndexNames();
    }

    public void startService(String collectionName) throws Exception {
        if (initialized.compareAndSet(false, true)) {
            final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.classLoader);
                if (StringUtils.isBlank(collectionName)) {
                    throw new IllegalStateException("includesCollectionNames set can not be empty");
                }
                AppLauncherResource launcherResource = getLauncherResource(collectionName);
                Resource[] resources = launcherResource.getResource().toArray(new Resource[]{});
                logger.info("load extra resource:{}", Arrays.stream(resources).map(r -> r.toString()).collect(Collectors.joining(",")));
                this.appContext = new ResourceXmlApplicationContext(resources) {

                    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
                        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
                        // 将依赖的DB字段注入
                        DBNode.registerDependencyDbsFacadeConfig(collectionName, timestamp, factory);
                        // for (Map.Entry<String, DBConfig> dbCfg : facadeBbConfigs.entrySet()) {
                        // SpringDBRegister dbRegister = new SpringDBRegister(dbCfg.getKey(), dbCfg.getValue(), factory);
                        // dbRegister.visitFirst();
                        // }
                        super.prepareBeanFactory(beanFactory);
                    }
                };
                Assert.assertNotNull(this.appContext);
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        }
    }

    private void addCollectionFocuseTag(String collection, String topic, Set<String> tags) {
        TopicInfo topicInfo = collectionFocusTopicInfo.get(collection);
        if (topicInfo == null) {
            synchronized (collectionFocusTopicInfo) {
                topicInfo = collectionFocusTopicInfo.get(collection);
                if (topicInfo == null) {
                    topicInfo = new TopicInfo();
                    collectionFocusTopicInfo.put(collection, topicInfo);
                }
            }
        }
        topicInfo.addTag(topic, tags);
    }

    protected List<IOnsListenerStatus> getAllTransferChannel() {
        if (this.appContext == null) {
            throw new IllegalStateException("appContext has not been initialize");
        }
        Map<String, MQListenerFactory> consumeListeners = this.appContext.getBeansOfType(MQListenerFactory.class);
        List<IOnsListenerStatus> incrChannels = new ArrayList<>();
        for (MQListenerFactory l : consumeListeners.values()) {
            for (IMQListener mqListener : l.getMqListeners()) {
                add2Channels(incrChannels, mqListener.getTopic(), (BasicRMListener) mqListener.getConsumerHandle());
            }
        }
        if (incrChannels.size() < 1) {
            throw new IllegalStateException("incrChannels is empty");
        }
        return incrChannels;
    }

    private void add2Channels(List<IOnsListenerStatus> incrChannels, String topic, BasicRMListener listener) {
        incrChannels.add(listener);
        this.addCollectionFocuseTag(listener.getCollectionName(), topic, listener.getTableFocuse());
    }

    protected AppLauncherResource getLauncherResource(String includesCollectionName) throws IOException {
        if (launcherResource == null) {
            launcherResource = LauncherResourceUtils.getAppResource(Collections.singleton(includesCollectionName)
                    , "classpath*:com/qlangtech/tis/realtime/transfer/" + includesCollectionName + "/app-context*.xml", this.classLoader);
        }
        return launcherResource;
    }

    // private static final Set<String> includesCollectionNames = Collections.emptySet();

//    protected AppLauncherResource getLauncherResource() throws IOException {
//        return getLauncherResource(includesCollectionNames);
//    }
}
