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
package com.qlangtech.tis.realtime.transfer.impl;

import com.qlangtech.tis.async.message.client.consumer.IAsyncMsgDeserialize;
import com.qlangtech.tis.async.message.client.consumer.IConsumerHandle;
import com.qlangtech.tis.async.message.client.consumer.IMQListener;
import com.qlangtech.tis.extension.TISExtension;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ConsumeAdapter implements InitializingBean {

    private IMQListener target;

    private IConsumerHandle consumerHandle;

    @Override
    public void afterPropertiesSet() throws Exception {
        final String mqType = "rockmq";
        final String deserializeType = "hession";
        target = loadPluginInstance(mqType, IMQListener.class);
        if (consumerHandle == null) {
            throw new IllegalStateException("consumerHandle have not");
        }
        target.setConsumerHandle(this.consumerHandle);
        IAsyncMsgDeserialize dType = this.loadPluginInstance(deserializeType, IAsyncMsgDeserialize.class);
        target.setDeserialize(dType);
    }

    public void setConsumerHandle(IConsumerHandle consumerHandle) {
        this.consumerHandle = consumerHandle;
    }

    private <T> T loadPluginInstance(String pluginName, Class<T> clazz) throws InstantiationException {
        if (StringUtils.isEmpty(pluginName)) {
            throw new IllegalArgumentException("param pluginName can not be null");
        }
        T result = null;
        Index<TISExtension, T> loadedListener = Index.load(TISExtension.class, clazz);
        for (final IndexItem<TISExtension, T> item : loadedListener) {
            if (pluginName.equals(item.annotation().name())) {
                result = item.instance();
            }
        }
        if (result == null) {
            StringBuffer typeBuffer = new StringBuffer();
            AtomicInteger count = new AtomicInteger();
            loadedListener.forEach((r) -> {
                count.incrementAndGet();
                typeBuffer.append(r.annotation().name()).append(",");
            });
            throw new IllegalStateException("plugin name:" + pluginName + "type:(" + clazz.getName() + ")in [" + typeBuffer + "] size:" + count.get());
        }
        return result;
    }
    // @Override
    // public IAsyncMsgDeserialize getDeserialize() {
    // return null;
    // }
    // 
    // @Override
    // public IConsumerHandle getConsumerHandle() {
    // return null;
    // }
    // 
    // @Override
    // public void start() throws MQConsumeException {
    // 
    // }
}
