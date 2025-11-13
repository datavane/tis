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

package com.qlangtech.tis.plugin.datax.format.guesstype;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/12
 */
public class TargetTabsEntities {
    private final List<FocusWildcardTabName> focusTabs;
    private final Map<FocusWildcardTabName, KafkaLogicalTableName> logical2PhyicalTabRegister = Maps.newHashMap();

    /**
     * 关注点可以是普通order_detail,或者order_detail* 这样的通配符形式的样式
     * 匹配需要大小写忽略：Sets.newTreeSet(String.CASE_INSENSITIVE_ORDER);
     *
     * @param focusTabs 可以是 order_detail,或者order_detail*
     */
    public TargetTabsEntities(String[] focusTabs) {
        this.focusTabs = Lists.newArrayList(focusTabs).stream().sorted(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.length() - o2.length();
            }
        }).map(FocusWildcardTabName::new).collect(Collectors.toList());
    }

    /**
     * 不能带通配符，逻辑表名
     *
     * @return
     */
    public List<FocusWildcardTabName> getLogicalTableEntites() {
        return Collections.unmodifiableList(this.focusTabs);
    }

    /**
     * 解析逻辑表名,通过物理表名获得逻辑表名实例，所用场景是 从kafka消息体上获得表名属性
     * ，如果上游数据源分库分表的，那表名格式应该是：`order_detail_001`,`order_detail_002`,`order_detail_003` 这样的，就需要将物理表名转化成逻辑表名
     *
     * @param physicalTableName 物理表名
     * @return
     */
    public KafkaLogicalTableName parseLogicalTable(String physicalTableName) {
        for (FocusWildcardTabName tabName : this.focusTabs) {
            if (tabName.isMatch(physicalTableName)) {
                KafkaLogicalTableName logicalTableName = logical2PhyicalTabRegister.get(tabName);
                if (logicalTableName == null) {
                    logicalTableName = new KafkaLogicalTableName(tabName.getLogicalTableName());
                    logical2PhyicalTabRegister.put(tabName, logicalTableName);
                }
                logicalTableName.addPhysicalTableName(physicalTableName);
                return logicalTableName;
            }
        }
        throw new IllegalStateException("tabName:" + physicalTableName + " relevant kafkaLogicalTableName can not be null,this.focusTabs:"
                + this.focusTabs.stream().map(String::valueOf).collect(Collectors.joining(",")));
    }
}
