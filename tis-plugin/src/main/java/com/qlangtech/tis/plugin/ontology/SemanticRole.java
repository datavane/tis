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
package com.qlangtech.tis.plugin.ontology;

/**
 * 属性的语义角色（ChatBI 用）。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/9
 */
public enum SemanticRole {
    Unknown(0, "未识别的语义角色，作为默认兜底"),
    Identifier(1, "实体唯一标识列，用于定位对象实例"),
    Dimension(2, "用于分组与筛选的分类型属性"),
    TimeDimension(3, "时间属性，支持按粒度切分聚合"),
    Measure(4, "可聚合的数值度量，如金额、数量");

    private final int value;
    private final String description;

    SemanticRole(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static SemanticRole parse(int value) {
        for (SemanticRole r : values()) {
            if (r.value == value) {
                return r;
            }
        }
        return Unknown;
    }
}