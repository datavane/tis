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
package com.qlangtech.tis.async.message.client.consumer.impl;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.IMQConsumerStatusFactory;
import com.qlangtech.tis.async.message.client.consumer.IMQListenerFactory;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;

/**
 * MQ监听工厂
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class MQListenerFactory implements IMQListenerFactory, IMQConsumerStatusFactory, Describable<MQListenerFactory> {

    @Override
    public Descriptor<MQListenerFactory> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }
    // public static DescriptorExtensionList<AbstractMQListenerFactory, Descriptor<AbstractMQListenerFactory>> all() {
    // return TIS.get()
    // .<AbstractMQListenerFactory, Descriptor<AbstractMQListenerFactory>>getDescriptorList(AbstractMQListenerFactory.class);
    // }
}
