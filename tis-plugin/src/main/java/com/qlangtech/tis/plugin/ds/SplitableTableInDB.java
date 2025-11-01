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
import com.google.common.collect.Sets;
import com.qlangtech.tis.datax.DataXJobInfo;
import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.plugin.ds.SplitTableStrategy.SplitTablePhysics2LogicNameConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-09-21 13:23
 **/
public class SplitableTableInDB extends TableInDB {
    private static final Logger logger = LoggerFactory.getLogger(SplitableTableInDB.class);
    public static final Pattern firstLogicTabNamePattern = Pattern.compile("\\(\\S+?\\)");
    //<key:逻辑表名,List<String> 物理表列表>
    public Map<String, SplitableDB> tabs = new HashMap<>();

    private final Pattern splitTabPattern;
    private final boolean prefixWildcardStyle;

    /**
     * @param id
     * @param splitTabPattern
     * @param prefixWildcardStyle 使用前缀匹配的样式，在flink-cdc表前缀通配匹配的场景中使用
     */
    public SplitableTableInDB(DBIdentity id, Pattern splitTabPattern, boolean prefixWildcardStyle) {
        super(id);
        this.splitTabPattern = Objects.requireNonNull(splitTabPattern, "splitTabPattern can not be null");
        this.prefixWildcardStyle = prefixWildcardStyle;
    }

    @Override
    public Function<String, String> getPhysicsTabName2LogicNameConvertor() {
        return new SplitTablePhysics2LogicNameConverter(this);
    }

    /**
     * @param jdbcUrl 可以标示是哪个分库的
     * @param tab
     */
    @Override
    public void add(String jdbcUrl, String tab) {
        Matcher matcher = this.splitTabPattern.matcher(tab);

        if (matcher.matches()) {
            String logicTabName = matcher.group(1);
            this.addPhysicsTab(jdbcUrl, logicTabName, tab);
        } else {
            this.addPhysicsTab(jdbcUrl, tab, tab);
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
            physicsTabs = new SplitableDB(logicTabName, this);
            tabs.put(logicTabName, physicsTabs);
        }
        physicsTabs.add(jdbcUrl, tab);
    }

    @Override
    public DataXJobInfo createDataXJobInfo(DataXJobSubmit.TableDataXEntity tabEntity, boolean shallRewrite2RegexPattern) {

        SplitableDB splitableDB = tabs.get(tabEntity.getSourceTableName());
        Objects.requireNonNull(splitableDB, "SourceTableName:" + tabEntity.getSourceTableName()
                + " relevant splitableDB can not be null,exist tabs:" + String.join(",", tabs.keySet()));

        List<String> matchedTabs = splitableDB.getTabsInDB(tabEntity.getDbIdenetity(), shallRewrite2RegexPattern);
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

    public Pair<Boolean, List<String>> rewritePhysicsTabs(String logicTabName, List<String> physicsTabs) {

        if (this.prefixWildcardStyle) {

            Matcher matcher = firstLogicTabNamePattern.matcher(this.splitTabPattern.pattern());
            if (matcher.find()) {
                final String newPatternForMatchFlinkBinlog = matcher.replaceFirst(logicTabName);
                logger.info("logicTabName:{},newPatternForMatchFlinkBinlog:{} with splitTabPattern:{}"
                        , logicTabName, newPatternForMatchFlinkBinlog, this.splitTabPattern.pattern());
                return Pair.of(true, Collections.singletonList(newPatternForMatchFlinkBinlog));
            } else {
                throw new IllegalStateException("firstLogicTabNamePattern:"
                        + firstLogicTabNamePattern + " can not find matched part in pattern:" + this.splitTabPattern.pattern());
            }

        }

        return Pair.of(false, physicsTabs);
    }

    public static class SplitableDB {
        private final String logicTabName;
        private final SplitableTableInDB splitableTableInDB;

        public SplitableDB(String logicTabName, SplitableTableInDB splitableTableInDB) {
            this.logicTabName = Objects.requireNonNull(logicTabName, "logicTabName can not be null");
            this.splitableTableInDB = Objects.requireNonNull(splitableTableInDB, "splitableTableInDB can not be null");
        }

        public String getLogicTabName() {
            return this.logicTabName;
        }

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

        /**
         * @param jdbcUrl
         * @param shallRewrite2RegexPattern 是否套吧表名替换成在flink-cdc表前缀通配匹配的场景中使用的正则式通配符样式
         * @return
         */
        public List<String> getTabsInDB(String jdbcUrl, boolean shallRewrite2RegexPattern) {

            if (shallRewrite2RegexPattern) {
                return this.splitableTableInDB.rewritePhysicsTabs(this.logicTabName, this.physicsTabInSplitableDB.get(jdbcUrl)).getRight();
            } else {
                return this.physicsTabInSplitableDB.get(jdbcUrl);
            }
        }

        /**
         * flink-cdc 端从binlog 中读取的是物理表，需要将物理表映射成逻辑表
         *
         * @return Pair<Boolean / 是否已经被重写成正则式Pattern样式 /, Set < String>>
         */
        public Pair<Boolean, Set<String>> rewrite2RegexPattern() {
            Set<String> rewriteRegexMatchPatterns = Sets.newHashSet();
            for (List<String> physicsTabs : this.physicsTabInSplitableDB.values()) {
                Pair<Boolean, List<String>> oneNodeOf = this.splitableTableInDB.rewritePhysicsTabs(this.logicTabName, physicsTabs);
                if (!oneNodeOf.getKey()) {
                    // 没有被重写
                    return Pair.of(false, Collections.emptySet());
                }
                rewriteRegexMatchPatterns.addAll(oneNodeOf.getValue());
            }
            return Pair.of(true, rewriteRegexMatchPatterns);
        }
    }
}
