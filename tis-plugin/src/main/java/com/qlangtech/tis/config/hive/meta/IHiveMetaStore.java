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

package com.qlangtech.tis.config.hive.meta;

import java.io.Closeable;
import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-03-12 13:18
 **/
public interface IHiveMetaStore extends Closeable {

    /**
     * 服务端版本
     *
     * @return
     */
    public String getServerVersion();

    void dropTable(String database, String tableName);

    public HiveTable getTable(String database, String tableName);

    public List<HiveTable> getTables(String database);

    /**
     * org.apache.hadoop.hive.metastore.IMetaStoreClient can be unwrap in client size
     *
     * @param <T>
     * @return
     */
    <T> T unwrapClient();

    /**
     * 取得org.apache.hadoop.hive.conf.HiveConf
     *
     * @param <HIVE_CONFIG>
     * @return
     */
    <HIVE_CONFIG> HIVE_CONFIG getHiveCfg();
}
