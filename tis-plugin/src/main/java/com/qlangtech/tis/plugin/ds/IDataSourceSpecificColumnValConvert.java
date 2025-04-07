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

package com.qlangtech.tis.plugin.ds;

/**
 * 用于转换特定的数据源中的类型，例如，sqlserver JDBC驱动中的转换 microsoft.sql.DateTimeOffset 类型的值，因为这个类型只和特定的驱动绑定，需要转成jdk通用的类型
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2025-04-07 14:24
 **/
public interface IDataSourceSpecificColumnValConvert {
    /**
     * 转换microsoft.sql.DateTimeOffset 的值
     *
     * @param input
     * @return
     */
    <T> T convert(Object input, Class<T> clazz);
}
