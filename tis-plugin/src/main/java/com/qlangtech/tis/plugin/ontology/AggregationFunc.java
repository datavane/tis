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
 * 聚合方式的 kind 标签。
 *
 * <p>两处消费方：
 * <ul>
 *   <li>本体 Derived property 的聚合子类层次（{@code impl.aggregation.AggregationKind}，
 *       在插件工程中）—— 描述「属性怎么算出来」；</li>
 *   <li>数据库端聚合下推（{@link com.qlangtech.tis.plugin.ontology.aggregation.AggregationCriteria}）
 *       —— 描述「一条 GROUP BY 里要哪些聚合函数」。</li>
 * </ul>
 *
 * <p><b>不是所有值都能下推</b>：{@code COLLECT_LIST / COLLECT_SET} 返回数组，而通用聚合
 * 结果行按标量建模，方言层会明确拒绝。查询是否支持以
 * {@link com.qlangtech.tis.plugin.ontology.aggregation.AggregationNotSupportedException} 为准。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/9
 */
public enum AggregationFunc {
    COUNT,
    SUM,
    AVG,
    MIN,
    MAX,
    APPROX_CARDINALITY,
    COUNT_DISTINCT,
    COLLECT_LIST,
    COLLECT_SET
}