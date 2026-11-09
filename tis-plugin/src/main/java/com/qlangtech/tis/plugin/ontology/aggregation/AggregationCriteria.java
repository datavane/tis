package com.qlangtech.tis.plugin.ontology.aggregation;

import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.plugin.ontology.AggregationFunc;
import com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 数据库端聚合查询条件：分组键 + 聚合指标 + 过滤条件。
 *
 * <p>它继承 {@link ObjectTypeBinding.DataSourceQueryCriteria}，复用的是已经被验证过的安全边界：
 * 属性白名单（{@code addFilter} 只接受已经由 {@code addTargetCol} 或 {@code addFilterCol}
 * 声明过的属性）与 {@link com.qlangtech.tis.plugin.ontology.impl.objtype.FilterOperator} 枚举
 * （非法算子根本构造不出来）。区别在于本类<b>不支持分页与排序</b>：
 * <ul>
 *   <li>分页对聚合结果没有意义（聚合已经把所有明细折成分组行），实现方不得调用
 *       {@code appendSqlLimit}；</li>
 *   <li>排序<b>不下推</b>：Top-N 依赖跨指标的汇总，留到调用方对少量分组行做。设置了
 *       {@code orderByCol} 的实现方应当直接拒绝，而不是静默忽略。</li>
 * </ul>
 *
 * <h3>分组键可以为空</h3>
 * 空的 {@code groupKeys} + 至少一个指标 = 「单行标量聚合」（{@code select MIN(x), MAX(x) from t}），
 * 结果恒一行、只有 {@code m0…} 指标键。直方图先用它取一次 MIN/MAX 定桶宽，再用
 * {@link GroupKey#numeric(String, double, int)} 做第二趟分组 —— 没有这个形态就只剩
 * 「先拉全部明细再内存分桶」，那在大表上会被 {@link #getMaxGroups()} 截断成一份残缺分布。
 *
 * <h3>指标也可以为空</h3>
 * 有分组键、没有指标 = 「按分组键去重」（{@code select a, b from t group by 1, 2}），
 * 结果行里只有 {@code c0…} 分组键。只要候选值、不要计数时用这种形态
 * （直方图的桶查询即是）。
 *
 * <p><b>只有两者都为空才非法</b>：那会渲染成 {@code select from t}。实现方不要按
 * {@code metrics.isEmpty()} 一刀切，否则「按分组键去重」被静默拦掉，表现是候选恒为空。
 *
 * <h3>空值分组</h3>
 * 默认把 X 分组键为空的那一组挡在 {@code (c0) IS NOT NULL} 之后；要拿到它请调
 * {@link #setIncludeNullGroups(boolean)} —— 见该方法的说明。
 *
 * <h3>别名契约</h3>
 * SQL 的 SELECT 顺序固定为「分组键在前、指标在后」，结果行里的键由
 * {@link #groupAlias(int)} / {@link #metricAlias(int)} 给出。调用方按自己的分组键与指标
 * 顺序取值即可，不依赖具体方言的别名大小写规则。
 *
 * <h3>指标去重</h3>
 * 相同 {@code (func, property)} 只占一个槽位：多个图表系列用同一种聚合时读同一个别名，
 * 既少一个聚合表达式，也让「同一 shape 内的指标顺序」保持确定。去重后仍可通过
 * {@link #metricIndex(AggregationFunc, String)} 找回槽位。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/24
 * @see GroupKey
 * @see AggregationFunc
 */
public class AggregationCriteria extends ObjectTypeBinding.DataSourceQueryCriteria {

    /**
     * 默认分组数上限。
     *
     * <p>它同时是内存 / 传输边界：最多把这么多聚合行读回 JVM。50000 行 × 少量列
     * （{@code RowMap} 是 HashMap）约数 MB，相比原来拉百万级明细行是两个量级。
     */
    public static final int DEFAULT_MAX_GROUPS = 50_000;

    public static final int MIN_MAX_GROUPS = 1;
    public static final int MAX_MAX_GROUPS = 1_000_000;

    /**
     * 最多两个分组键：X 轴 + segment。再多就需要第三种别名与图表语义，当前不需要。
     */
    public static final int MAX_GROUP_KEYS = 2;

    /** X 分组键的别名 */
    public static final String GROUP_ALIAS = "c0";
    /** segment 分组键的别名（可选） */
    public static final String SEGMENT_ALIAS = "c1";

    private final List<GroupKey> groupKeys = new ArrayList<>(MAX_GROUP_KEYS);
    private final List<Metric> metrics = new ArrayList<>();
    private int maxGroups = DEFAULT_MAX_GROUPS;
    private boolean includeNullGroups;

    /**
     * 追加一个分组键（按追加顺序映射到 {@code c0}、{@code c1}）。
     *
     * <p>列必须已经通过 {@link #addTargetCol(String)} 进入白名单 —— 与 {@code addFilter}
     * 同一条边界：能进 SQL 的标识符只可能是调用方自己声明过的列。
     */
    public AggregationCriteria addGroupKey(GroupKey key) {
        Objects.requireNonNull(key, "group key can not be null");
        if (this.groupKeys.size() >= MAX_GROUP_KEYS) {
            throw new IllegalStateException("at most " + MAX_GROUP_KEYS
                    + " group keys are supported (x + segment), but already:" + this.groupKeys);
        }
        requireTargetCol(key.column(), "group key");
        this.groupKeys.add(key);
        return this;
    }

    /**
     * 追加一个聚合指标（相同 {@code (func, property)} 自动去重）。
     *
     * @param func     聚合函数；{@link AggregationFunc#COUNT} 不接受 {@code property}
     * @param property 参与聚合的属性名；COUNT 传 {@code null}，其余必须非空且已在 targetCols 中
     */
    public AggregationCriteria addMetric(AggregationFunc func, String property) {
        Metric metric = new Metric(func, property);
        if (func != AggregationFunc.COUNT) {
            requireTargetCol(property, "metric");
        }
        if (!this.metrics.contains(metric)) {
            this.metrics.add(metric);
        }
        return this;
    }

    /**
     * 指标在 SELECT 列表中的槽位（用于 {@link #metricAlias(int)}）。
     *
     * @throws IllegalStateException 指标未被添加过
     */
    public int metricIndex(AggregationFunc func, String property) {
        int index = this.metrics.indexOf(new Metric(func, property));
        if (index < 0) {
            throw new IllegalStateException("metric " + func + "(" + property + ") has not been added, existing:" + metrics);
        }
        return index;
    }

    /**
     * @param maxGroups 分组数上限，1 ~ {@link #MAX_MAX_GROUPS}
     */
    public AggregationCriteria setMaxGroups(int maxGroups) {
        if (maxGroups < MIN_MAX_GROUPS || maxGroups > MAX_MAX_GROUPS) {
            throw new IllegalArgumentException("maxGroups must be in [" + MIN_MAX_GROUPS + "," + MAX_MAX_GROUPS
                    + "], but was:" + maxGroups);
        }
        this.maxGroups = maxGroups;
        return this;
    }

    public List<GroupKey> getGroupKeys() {
        return Collections.unmodifiableList(groupKeys);
    }

    /**
     * 是否把 X 分组键为空的那一组也查出来。
     *
     * <p>默认 {@code false}：实现方会追加 {@code (c0) IS NOT NULL}，把空值分组挡在外头
     * —— 图表轴上的空类别既画不出来也点不中。置 {@code true} 时该谓词被去掉，空值单独成组
     * 并以 {@code null} 值出现在结果行里。
     *
     * <p>目前的<b>唯一</b>消费者是 Filter List 的候选项查询
     * （{@code ObjectSetOpsSource}）：Palantir 的空值是一个可勾选的独立候选行。
     */
    public AggregationCriteria setIncludeNullGroups(boolean includeNullGroups) {
        this.includeNullGroups = includeNullGroups;
        return this;
    }

    public boolean isIncludeNullGroups() {
        return includeNullGroups;
    }

    public List<Metric> getMetrics() {
        return Collections.unmodifiableList(metrics);
    }

    public int getMaxGroups() {
        return maxGroups;
    }

    public static String groupAlias(int index) {
        return switch (index) {
            case 0 -> GROUP_ALIAS;
            case 1 -> SEGMENT_ALIAS;
            default -> throw new IllegalArgumentException("illegal group index:" + index
                    + ", only 0 (" + GROUP_ALIAS + ") and 1 (" + SEGMENT_ALIAS + ") are supported");
        };
    }

    public static String metricAlias(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("illegal metric index:" + index);
        }
        return "m" + index;
    }

    private void requireTargetCol(String col, String what) {
        if (!getTargetCols().contains(col)) {
            throw new IllegalArgumentException(what + " column:" + col
                    + " must be one of targetCols:" + getTargetCols());
        }
    }

    /**
     * 一次聚合调用里的一个指标。
     *
     * <p>构造期即校验列名：非 COUNT 的 {@code property} 必须匹配
     * {@link ValidatorCommons#PATTERN_DB_COL_NAME}。合法的列名也仍然要过
     * {@link #addMetric(AggregationFunc, String)} 的 targetCols 白名单。
     */
    public record Metric(AggregationFunc func, String property) {

        public Metric {
            Objects.requireNonNull(func, "aggregation func can not be null");
            if (func == AggregationFunc.COUNT) {
                if (property != null) {
                    throw new IllegalArgumentException("COUNT does not take a metric property, but was:" + property);
                }
            } else if (property == null
                    || !ValidatorCommons.PATTERN_DB_COL_NAME.matcher(property).matches()) {
                throw new IllegalArgumentException("illegal metric property:" + property
                        + " for aggregation:" + func
                        + ", must match " + ValidatorCommons.PATTERN_DB_COL_NAME.pattern());
            }
        }
    }
}
