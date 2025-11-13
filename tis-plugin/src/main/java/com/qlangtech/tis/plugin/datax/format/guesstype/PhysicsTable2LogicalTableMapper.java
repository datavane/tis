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

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 实现物理表映射到逻辑表的映射
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2025/11/13
 * @see TargetTabsEntities
 * @see FocusWildcardTabName
 * @see KafkaLogicalTableName
 */
public class PhysicsTable2LogicalTableMapper {
    private final Map<String/**物理表名*/, KafkaLogicalTableName> physicsTable2LogicalTable = Maps.newHashMap();
    private final TargetTabsEntities targetTabsEntities;

    public PhysicsTable2LogicalTableMapper(TargetTabsEntities targetTabsEntities) {
        this.targetTabsEntities = Objects.requireNonNull(targetTabsEntities
                , "targetTabsEntities can not be null");
    }

    /**
     * 通过物理表名找到对应的逻辑表实体
     *
     * @param tabName 物理表名
     * @return
     */
    public KafkaLogicalTableName parseLogicalTableName(String tabName) {
        if (StringUtils.isEmpty(tabName)) {
            throw new IllegalArgumentException("param tableName can not be null");
        }
        KafkaLogicalTableName logicalTabName = physicsTable2LogicalTable.get(tabName);
        if (logicalTabName == null) {
            physicsTable2LogicalTable.put(tabName
                    // 从注册表中查看对应的逻辑表应该叫什么名字
                    , Objects.requireNonNull(targetTabsEntities.parseLogicalTable(tabName)
                            , "logicalTabName can not be null"));
        }
        return logicalTabName;
    }
}
