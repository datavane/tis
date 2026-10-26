package com.qlangtech.tis.plugin.ontology.aggregation;

import com.qlangtech.tis.ibatis.RowMap;

import java.util.List;

/**
 * 数据库端聚合的结果。
 *
 * <p>行里的键是稳定的别名（不是物理列名）：
 * {@link AggregationCriteria#groupAlias(int)} 与
 * {@link AggregationCriteria#metricAlias(int)}。调用方按自己的分组键 / 指标顺序取值，
 * 不必关心方言或 SQL 别名。
 *
 * @param rows      聚合后的分组行，最多 {@link AggregationCriteria#getMaxGroups()} 行
 * @param truncated 分组数超过上限时为 {@code true}，此时只返回前 maxGroups 组
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/24
 */
public record AggregationResult(List<RowMap> rows, boolean truncated) {
}
