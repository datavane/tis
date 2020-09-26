/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.realtime.transfer;

import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.IConsumerHandle;
import com.qlangtech.tis.async.message.client.consumer.IMQListener;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.plugin.PluginStore;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MQListenerFactory implements InitializingBean {

    private List<IMQListener> mqListeners;

    private String collection;

    private IConsumerHandle consumer;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.mqListeners = Lists.newArrayList();
        if (StringUtils.isEmpty(this.collection)) {
            throw new IllegalStateException("collection name have not be set");
        }
        PluginStore<com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory> mqFactory = TIS.getPluginStore(this.collection, com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory.class);
        List<com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory> mqListenerFactory = mqFactory.getPlugins();
        if (this.consumer == null) {
            throw new IllegalStateException("consume can not be null");
        }
        if (mqListenerFactory.size() < 1) {
            throw new IllegalStateException("mqListenerFactory size can not small than 1");
        }
        for (com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory listenerFactory : mqListenerFactory) {
            IMQListener imqListener = listenerFactory.create();
            imqListener.setConsumerHandle(consumer);
            this.mqListeners.add(imqListener);
            /**
             * ***************************************
             *  启动监听
             * *****************************************
             */
            if (!Config.isTestMock()) {
                imqListener.start();
            }
        }
    }

    public List<IMQListener> getMqListeners() {
        return this.mqListeners;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setConsumer(IConsumerHandle consumer) {
        this.consumer = consumer;
    }
}
