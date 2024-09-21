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

import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.TISExtensible;
import com.qlangtech.tis.plugin.ds.SplitableTableInDB.SplitableDB;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-12-17 21:20
 **/
@TISExtensible
public abstract class SplitTableStrategy implements Describable<SplitTableStrategy>, Serializable {
    public static final Pattern PATTERN_PHYSICS_TABLE = Pattern.compile("(\\S+)_(\\d+)");

    /**
     * 取得节点描述信息
     *
     * @return
     */
    public abstract String getNodeDesc();


    public abstract boolean isSplittable();

    public abstract TableInDB createTableInDB(DBIdentity dbId);

    /**
     * 逻辑表名称，没有数据库没有采用分表策略则直接发挥tabName ，如果采用分表则在扩展类中自定义扩展<br/>
     *
     * @param tabName
     * @return Pair<String, EntityName> firstKey:jdbcUrl
     */
    public abstract DBPhysicsTable getMatchedPhysicsTable(DataSourceFactory dsFactory, String jdbcUrl, EntityName tabName);


    /**
     * 取得对应的物理表集合
     *
     * @param dsFactory
     * @param tabEntity 逻辑表
     * @return
     */
    public final List<String> getAllPhysicsTabs(DataSourceFactory dsFactory, DataXJobSubmit.TableDataXEntity tabEntity) {
        return getAllPhysicsTabs(dsFactory, tabEntity.getDbIdenetity(), tabEntity.getSourceTableName());
    }

    public abstract List<String> getAllPhysicsTabs(DataSourceFactory dsFactory, String jdbcUrl, String sourceTableName);

    public static class DBPhysicsTable {
        private final String jdbcUrl;
        private final EntityName physicsTab;

        public DBPhysicsTable(String jdbcUrl, EntityName physicsTab) {
            this.jdbcUrl = jdbcUrl;
            this.physicsTab = physicsTab;
        }

        public String getJdbcUrl() {
            return this.jdbcUrl;
        }

        public EntityName getPhysicsTab() {
            return this.physicsTab;
        }
    }

    /**
     * 物理表映射逻辑表
     */
    public static class SplitTablePhysics2LogicNameConverter implements Function<String, String>, Serializable {
        private final Map<String, String> physics2LogicTabNameConverter;

        public SplitTablePhysics2LogicNameConverter(SplitableTableInDB splitTabInDB) {
            this.physics2LogicTabNameConverter = Maps.newHashMap();
            for (Map.Entry<String, SplitableDB> dbEntry : splitTabInDB.tabs.entrySet()) {
                for (Map.Entry<String, List<String>> logicEntry : dbEntry.getValue().physicsTabInSplitableDB.entrySet()) {
                    for (String physicsTabName : logicEntry.getValue()) {
                        this.physics2LogicTabNameConverter.put(physicsTabName, dbEntry.getKey());
                    }
                }
            }
        }

        @Override
        public String apply(String physicsName) {
            String logicalTabName = this.physics2LogicTabNameConverter.get(physicsName);
            if (logicalTabName == null) {
                throw new IllegalStateException("physics tabName:" + physicsName
                        + " can not find relevant logicalTabName,repo size:" + this.physics2LogicTabNameConverter.size());
            }
            return logicalTabName;
        }
    }
}
