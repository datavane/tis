package com.qlangtech.tis.plugin.ds;

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

import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.DataXJobInfo;
import com.qlangtech.tis.datax.DataXJobSubmit;

import java.util.Collections;
import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-12-17 18:45
 **/
public abstract class TableInDB {

    public static TableInDB create() {
        return new DftTableInDB();
    }

    /**
     * @param jdbcUrl 可以标示是哪个分库的
     * @param tab
     */
    public abstract void add(String jdbcUrl, String tab);

//    /**
//     * 取得匹配的物理表
//     *
//     * @param jdbcUrl
//     * @param tab
//     * @return
//     */
//    public abstract List<String> getMatchedTabs(String jdbcUrl, String tab);

    public abstract List<String> getTabs();

    public abstract boolean contains(String tableName);

    public abstract boolean isEmpty();

    public abstract DataXJobInfo createDataXJobInfo(DataXJobSubmit.TableDataXEntity tabEntity);

    private static class DftTableInDB extends TableInDB {
        private List<String> tabs = Lists.newArrayList();

        @Override
        public void add(String jdbcUrl, String tab) {
            this.tabs.add(tab);
        }

        @Override
        public DataXJobInfo createDataXJobInfo(DataXJobSubmit.TableDataXEntity tabEntity) {
            return DataXJobInfo.create(tabEntity.getFileName(), Collections.emptyList());
        }
//        @Override
//        public List<String> getMatchedTabs(String jdbcUrl, String tab) {
//            return Collections.singletonList(tab);
//        }

        @Override
        public List<String> getTabs() {
            return this.tabs;
        }

        @Override
        public boolean contains(String tableName) {
            return this.tabs.contains(tableName);
        }

        @Override
        public boolean isEmpty() {
            return this.tabs.isEmpty();
        }
    }
}
