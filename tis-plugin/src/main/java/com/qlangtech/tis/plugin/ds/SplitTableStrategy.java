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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.DataXJobInfo;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.TISExtensible;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-12-17 21:20
 **/
@TISExtensible
public abstract class SplitTableStrategy implements Describable<SplitTableStrategy>, Serializable {
    public static final Pattern PATTERN_PHYSICS_TABLE = Pattern.compile("(\\S+)_(\\d+)");

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

    public static class SplitableTableInDB extends TableInDB {
        //<key:逻辑表名,List<String> 物理表列表>
        public Map<String, SplitableDB> tabs = new HashMap<>();

        private final Pattern splitTabPattern;

        public SplitableTableInDB(DBIdentity id, Pattern splitTabPattern) {
            super(id);
            this.splitTabPattern = splitTabPattern;
        }

        @Override
        public Function<String, String> getPhysicsTabName2LogicNameConvertor() {
            return new SplitTablePhysics2LogicNameConverter(this);
        }

        //        @Override
//        public List<String> getMatchedTabs(Optional<String> jdbcUrl, String tab) {
//
//            SplitableDB splitableDB = tabs.get(tab);
//            Objects.requireNonNull(splitableDB, "tab:" + tab + " relevant splitableDB can not be null");
//
//            if (jdbcUrl.isPresent()) {
//                return splitableDB.getTabsInDB(jdbcUrl.get());
//            } else {
//                Set<String> mergeAllTabs = new HashSet<>();
//                for (Map.Entry<String, List<String>> entry : splitableDB.physicsTabInSplitableDB.entrySet()) {
//                    mergeAllTabs.addAll(entry.getValue());
//                }
//                return Lists.newArrayList(mergeAllTabs);
//            }
//        }

        /**
         * @param jdbcUrl 可以标示是哪个分库的
         * @param tab
         */
        @Override
        public void add(String jdbcUrl, String tab) {
            Matcher matcher = PATTERN_PHYSICS_TABLE.matcher(tab);

            if (matcher.matches()) {
                String logicTabName = matcher.group(1);
                addPhysicsTab(jdbcUrl, logicTabName, tab);
            } else {
                addPhysicsTab(jdbcUrl, tab, tab);
                //  tabs.put(tab, (new SplitableDB()).add(jdbcUrl, tab));
            }
        }

        /**
         * @param jdbcUrl
         * @param logicTabName
         * @param tab          物理表名
         */
        private void addPhysicsTab(String jdbcUrl, String logicTabName, String tab) {
            SplitableDB physicsTabs = tabs.get(logicTabName);
            if (physicsTabs == null) {
                physicsTabs = new SplitableDB();
                tabs.put(logicTabName, physicsTabs);
            }
            physicsTabs.add(jdbcUrl, tab);
        }

        @Override
        public DataXJobInfo createDataXJobInfo(DataXJobSubmit.TableDataXEntity tabEntity) {

            SplitableDB splitableDB = tabs.get(tabEntity.getSourceTableName());
            Objects.requireNonNull(splitableDB, "SourceTableName:" + tabEntity.getSourceTableName() + " relevant splitableDB can not be null");

            List<String> matchedTabs = splitableDB.getTabsInDB(tabEntity.getDbIdenetity());
            if (CollectionUtils.isEmpty(matchedTabs)) {
                throw new IllegalStateException("jdbcUrl:" + tabEntity.getDbIdenetity() + " relevant matchedTabs can not be empty");
            }
            // 目前将所有匹配的表都在一个datax 单独进程中去执行，后期可以根据用户的配置单一个单独的dataX进程中执行部分split表以提高导入速度
            return DataXJobInfo.create(tabEntity.getFileName(), tabEntity, matchedTabs);
        }

        @Override
        public List<String> getTabs() {
            return Lists.newArrayList(tabs.keySet());
        }

        @Override
        public boolean contains(String tableName) {
            return this.tabs.containsKey(tableName);
        }


        @Override
        public boolean isEmpty() {
            return this.tabs.isEmpty();
        }
    }

    public static class SplitableDB {
        // key:jdbcUrl ,val:physicsTab
        public Map<String, List<String>> physicsTabInSplitableDB = Maps.newHashMap();

        public SplitableDB add(String jdbcUrl, String tab) {
            List<String> tabs = physicsTabInSplitableDB.get(jdbcUrl);
            if (tabs == null) {
                tabs = Lists.newArrayList();
                physicsTabInSplitableDB.put(jdbcUrl, tabs);
            }
            tabs.add(tab);
            return this;
        }

        public List<String> getTabsInDB(String jdbcUrl) {
            return this.physicsTabInSplitableDB.get(jdbcUrl);
        }
    }

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
