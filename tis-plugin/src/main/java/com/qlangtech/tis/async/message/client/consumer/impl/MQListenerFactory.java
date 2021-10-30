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
package com.qlangtech.tis.async.message.client.consumer.impl;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.IConsumerHandle;
import com.qlangtech.tis.async.message.client.consumer.IMQConsumerStatusFactory;
import com.qlangtech.tis.async.message.client.consumer.IMQListenerFactory;
import com.qlangtech.tis.datax.IDataXPluginMeta;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 监听数据源工厂，类似监听MySql，PG中数据源所对应的Flink SourceFunction 封装
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class MQListenerFactory implements IMQListenerFactory, IMQConsumerStatusFactory, Describable<MQListenerFactory> {

    @Override
    public Descriptor<MQListenerFactory> getDescriptor() {
        Descriptor<MQListenerFactory> descriptor = TIS.get().getDescriptor(this.getClass());
        Class<BaseDescriptor> expectClass = getExpectDescClass();
        if (!(expectClass.isAssignableFrom(descriptor.getClass()))) {
            throw new IllegalStateException(descriptor.getClass() + " must implement the Descriptor of " + expectClass.getName());
        }
        return descriptor;
    }

    protected <TT extends BaseDescriptor> Class<TT> getExpectDescClass() {
        return (Class<TT>) BaseDescriptor.class;
    }

    public void setConsumerHandle(IConsumerHandle consumerHandle) {
        throw new UnsupportedOperationException();
    }


    public static abstract class BaseDescriptor extends Descriptor<MQListenerFactory> {

        @Override
        public final Map<String, Object> getExtractProps() {
            Map<String, Object> eprops = new HashMap<>();
            Optional<IDataXPluginMeta.EndType> targetType = this.getTargetType();
            eprops.put(IDataXPluginMeta.END_TARGET_TYPE, targetType.isPresent() ? targetType.get().getVal() : "all");
            return eprops;
        }

        /**
         * 取得服务对象，如果这个Plugin是MySqlCDC的话,则返回 EndType.MySQL, 如果全部匹配的话，则返回empty
         *
         * @return
         */
        public abstract Optional<IDataXPluginMeta.EndType> getTargetType();
    }
}
