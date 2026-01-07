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
import com.google.common.collect.Sets;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.extension.AIPromptEnhance;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.TISExtensible;
import com.qlangtech.tis.plugin.ds.SplitableTableInDB.SplitableDB;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.groovy.runtime.metaclass.ConcurrentReaderHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-12-17 21:20
 * @see NoneSplitTableStrategy
 **/
@TISExtensible
@AIPromptEnhance(prompt = "若用户未提及分表、多节点、表后缀等关键词，默认选择 id = 'off'")
public abstract class SplitTableStrategy implements Describable<SplitTableStrategy>, Serializable {
    public static final Pattern PATTERN_PHYSICS_TABLE = Pattern.compile("(\\S+?)(_\\d+)?");

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

    public static class UnrecognizedPhysicsTabs2LogicName implements Serializable {
        private final String logicTabName;
        private final Pattern[] testPattern;

        private static UnrecognizedPhysicsTabs2LogicName create(String logicTabName, Set<String> regexPattern) {
            return new UnrecognizedPhysicsTabs2LogicName(logicTabName, regexPattern.stream().map((regex) -> Pattern.compile(regex)).toArray(Pattern[]::new));
        }

        public UnrecognizedPhysicsTabs2LogicName(String logicTabName, Pattern[] testPattern) {
            this.logicTabName = logicTabName;
            this.testPattern = Objects.requireNonNull(testPattern, "testPattern can not be null");
            if (testPattern.length < 1) {
                throw new IllegalArgumentException("testPattern array length can not small than 1");
            }
        }

        /**
         * 测试是否匹配
         *
         * @param physicsTabName
         * @return
         */
        public boolean test(String physicsTabName) {
            for (Pattern p : testPattern) {
                if (p.matcher(physicsTabName).matches()) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 物理表映射逻辑表
     */
    public static class SplitTablePhysics2LogicNameConverter implements Function<String, String>, Serializable {
        private static final Logger logger = LoggerFactory.getLogger(SplitTablePhysics2LogicNameConverter.class);
        private final ConcurrentMap<String, String> physics2LogicTabNameConverter;
        /**
         * 用于当使用正则识别新进入的表，该表在增量任务启动之后创建的需要重新识别
         */
        private final Set<UnrecognizedPhysicsTabs2LogicName> unrecognizedPhysicsTabs2LogicName;

        public SplitTablePhysics2LogicNameConverter(SplitableTableInDB splitTabInDB) {
            this.physics2LogicTabNameConverter = Maps.newConcurrentMap();
            this.unrecognizedPhysicsTabs2LogicName = Sets.newHashSet();
            Pair<Boolean, Set<String>> regexPattern = null;
            for (Map.Entry<String /**逻辑表名*/, SplitableDB> dbEntry : splitTabInDB.tabs.entrySet()) {
                regexPattern = dbEntry.getValue().rewrite2RegexPattern();
                if (regexPattern.getKey()) {
                    unrecognizedPhysicsTabs2LogicName.add(UnrecognizedPhysicsTabs2LogicName.create(dbEntry.getKey(), regexPattern.getRight()));//   regexPattern.getRight();
                }

                for (Map.Entry<String, List<String>> logicEntry : dbEntry.getValue().physicsTabInSplitableDB.entrySet()) {
                    for (String physicsTabName : logicEntry.getValue()) {
                        this.physics2LogicTabNameConverter.put(physicsTabName, dbEntry.getKey());
                    }
                }
            }
        }

        @Override
        public String apply(final String physicsName) {
            String logicalTabName = this.physics2LogicTabNameConverter.get(physicsName);
            if (logicalTabName == null) {

                for (UnrecognizedPhysicsTabs2LogicName recognize : unrecognizedPhysicsTabs2LogicName) {
                    if (recognize.test(physicsName)) {
                        logger.info("physicsName:" + physicsName + " has been regonized by unrecognized PhysicsTabs2LogicName to logicName:" + recognize.logicTabName);
                        this.physics2LogicTabNameConverter.putIfAbsent(physicsName, recognize.logicTabName);
                        return recognize.logicTabName;
                    }
                }

                throw new IllegalStateException("physics tabName:" + physicsName
                        + " can not find relevant logicalTabName,repo size:" + this.physics2LogicTabNameConverter.size()
                        + ",unrecognizedPhysicsTabs2LogicName size:" + unrecognizedPhysicsTabs2LogicName.size());
            }
            return logicalTabName;
        }
    }
}
