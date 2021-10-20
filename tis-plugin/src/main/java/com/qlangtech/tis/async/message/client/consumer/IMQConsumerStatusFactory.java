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
package com.qlangtech.tis.async.message.client.consumer;

/**
 * 监听mq group comsumer的偏移量
 *
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-08-29 16:27
 */
public interface IMQConsumerStatusFactory {

    default IMQConsumerStatus createConsumerStatus() {
        throw new UnsupportedOperationException();
    }

    interface IMQConsumerStatus {
        // 想了一想这个接口暂时没有什么用，先不用
        // long getTotalDiff();
        // 
        // void close();
    }
}
