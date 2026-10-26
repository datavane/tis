/**
 * 对象集的数据库端聚合（GROUP BY 下推）API。
 *
 * <h3>为什么需要这一层</h3>
 * {@link com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeBinding} 原本只有
 * {@code select / where / order / limit}。图表这类天然要聚合的消费方如果拿不到 GROUP BY，
 * 就只能把明细行拉回 JVM 自己算 —— 在 Doris / StarRocks 这类 MPP 数据库上，明细行动辄
 * 上亿，网络传输与单机计算都会先于数据库成为瓶颈，也完全用不上数据库的并行聚合能力。
 *
 * <p>本包把「分组键 + 聚合函数」抽象成结构化的查询条件，由各数据源绑定翻译成
 * 数据库方言的 {@code SELECT … GROUP BY …}，JVM 只接收聚合后的少量分组行。
 *
 * <h3>API 契约</h3>
 * <ol>
 *   <li>构造 {@link AggregationCriteria}：先 {@code addTargetCol}，再
 *       {@link AggregationCriteria#addGroupKey(GroupKey)} /
 *       {@link AggregationCriteria#addMetric(com.qlangtech.tis.plugin.ontology.AggregationFunc, String)}；
 *       过滤条件沿用 {@code addFilter}。</li>
 *   <li>调用 {@code ObjectTypeBinding#executeAggregation(AggregationCriteria)}。
 *       默认实现抛 {@link AggregationNotSupportedException}。</li>
 *   <li>读结果：分组键在 {@link AggregationCriteria#groupAlias(int)}，
 *       指标在 {@link AggregationCriteria#metricAlias(int)}；{@link AggregationResult#truncated()}
 *       表示分组数超过上限。</li>
 * </ol>
 *
 * <h3>支持的方言</h3>
 * <table>
 *   <tr><th>EndType</th><th>COUNT / SUM / AVG / MIN / MAX</th><th>COUNT_DISTINCT</th><th>APPROX_CARDINALITY</th></tr>
 *   <tr><td>MySQL / MariaDB / TiDB / OceanBase</td><td>原生函数</td><td>COUNT(DISTINCT)</td><td>COUNT(DISTINCT)（精确回退）</td></tr>
 *   <tr><td>Doris / StarRocks</td><td>原生函数</td><td>COUNT(DISTINCT)</td><td>APPROX_COUNT_DISTINCT</td></tr>
 * </table>
 * 其它方言直接抛 {@link AggregationNotSupportedException}，<b>不回落内存聚合</b>：
 * 静默回落会让「看起来更慢」变成「数据量一大就 OOM / 超时」。
 *
 * <h3>安全边界</h3>
 * 分组列与指标列都先过 {@code ValidatorCommons.PATTERN_DB_COL_NAME}，再要求已在
 * {@code targetCols} 中；表达式只能由 {@link GroupKey} 与
 * {@link com.qlangtech.tis.plugin.ontology.AggregationFunc} 组合，<b>不接受裸 SQL</b>。
 *
 * <h3>已知限制</h3>
 * <ul>
 *   <li>不做 ORDER BY 下推：Top-N 由调用方对聚合行排序完成。</li>
 *   <li>时间分桶以 naive DATE / DATETIME 计算，要求数据库会话时区与 JVM 默认时区一致。</li>
 *   <li>分类分组的相等性由数据库 collation 决定（如 MySQL 默认大小写不敏感）。</li>
 *   <li>不支持分页；同一查询里的多个分组形状需要多次调用，且不在同一快照上。</li>
 * </ul>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/24
 */
package com.qlangtech.tis.plugin.ontology.aggregation;
