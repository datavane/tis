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
package com.qlangtech.tis.async.message.client.consumer;

import com.qlangtech.tis.realtime.transfer.DTO;
import java.io.IOException;

/**
 * 将消息中间件中的数据反序列化
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IAsyncMsgDeserialize {

    DTO deserialize(byte[] content) throws IOException;
}
