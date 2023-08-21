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

package com.qlangtech.tis.utils;

import com.qlangtech.tis.plugin.IdentityName;

import java.util.List;
import java.util.ServiceLoader;

/**
 * 为了在插件工程中可以调用console中获取db库列表功能
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-08-19 16:20
 **/
public abstract class DBsGetter {
    private static DBsGetter dbsGetter;

    public static DBsGetter getInstance() {
        if (dbsGetter == null) {
            ServiceLoader<DBsGetter> loader = ServiceLoader.load(DBsGetter.class);
            for (DBsGetter getter : loader) {
                return (dbsGetter = getter);
            }
            throw new IllegalStateException("can not load instance of " + DBsGetter.class.getSimpleName());
        }
        return dbsGetter;
    }

    /**
     * 根据Desc Name 查找 dbName 列表
     *
     * @param extendClass
     * @return
     */
    public abstract List<IdentityName> getExistDbs(String... extendClass);
}
