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

package com.qlangtech.tis.extension;

import com.qlangtech.tis.plugin.IPluginStore;

import java.util.Optional;

/**
 * 支持插件操作，例如数据管道、数据源头克隆
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-07-09 15:23
 **/
public interface IDescribableManipulate<T extends Describable<T>> {
    public Class<T> getManipulateExtendPoint();

    /**
     * 支持操作行为的持久化
     *
     * @return
     */
    Optional<IPluginStore<T>> getManipulateStore();


    /**
     * 标识操作插件是可以持久化的，例如：ExportTISPipelineToDolphinscheduler
     */
    interface IManipulateStorable {
        default boolean isManipulateStorable() {
            return false;
        }
    }
}
