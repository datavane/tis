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
import com.qlangtech.tis.async.message.client.consumer.IAsyncMsgDeserialize;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class AbstractAsyncMsgDeserialize implements IAsyncMsgDeserialize, Describable<AbstractAsyncMsgDeserialize> {

    // public abstract String getName();
    // public static ExtensionList<AbstractAsyncMsgDeserialize> getDeserializeList() {
    // return TIS.get().getExtensionList(AbstractAsyncMsgDeserialize.class);
    // }
    @Override
    public Descriptor<AbstractAsyncMsgDeserialize> getDescriptor() {
        return TIS.get().getDescriptor(this.getClass());
    }
}
