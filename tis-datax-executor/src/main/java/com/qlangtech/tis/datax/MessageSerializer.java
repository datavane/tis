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

package com.qlangtech.tis.datax;

import com.alibaba.fastjson.JSON;
import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.curator.framework.recipes.queue.QueueSerializer;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-06 15:33
 **/
public class MessageSerializer implements QueueSerializer<CuratorTaskMessage> {

    @Override
    public byte[] serialize(CuratorTaskMessage item) {
        return JSON.toJSONString(item, false).getBytes(TisUTF8.get());
    }

    @Override
    public CuratorTaskMessage deserialize(byte[] bytes) {
        return JSON.parseObject(new String(bytes), CuratorTaskMessage.class);
    }
}
