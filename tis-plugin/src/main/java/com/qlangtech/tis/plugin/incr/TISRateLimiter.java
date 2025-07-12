/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.incr;

import com.qlangtech.tis.extension.Describable;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-07-06 14:59
 **/
public abstract class TISRateLimiter implements Describable<TISRateLimiter> {

    /**
     * @return // @see org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy
     */
    public abstract <RateLimiterStrategy> RateLimiterStrategy getStrategy();


    public abstract boolean supportRateLimiter();

    public interface IResettableRateLimiter {
        /**
         * 重新设置新的流控阀值，可以运行期动态调整流量
         *
         * @param permitsPerSecond
         */
        void resetRate(Integer permitsPerSecond);

        /**
         * 重新啟動增量消息
         */
        void resumeConsume();

        /**
         * 停止增量接收消息
         */
        void pauseConsume();
    }
}
