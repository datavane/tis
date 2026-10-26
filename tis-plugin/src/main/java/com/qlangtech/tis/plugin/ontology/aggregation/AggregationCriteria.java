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
 * <p>它继承 {@link ObjectTypeBinding.DataSourceQueryCriteria}，复用的是已经被验证过的两条
 * 安全边界：{@code targetCols} 白名单与过滤操作符白名单（{@code addFilter} 会把不在
 * targetCols 里的属性直接拒掉）。区别在于本类<b>不支持分页与排序</b>：
 * <ul>
 *   <li>分页对聚合结果没有意义（聚合已经把所有明细折成分组行），实现方不得调用
 *       {@code appendSqlLimit}；</li>
 *   <li>排序<b>不下推</b>：Top-N 依赖跨指标的汇总，留到调用方对少量分组行做。设置了
 *       {@code orderByCol} 的实现方应当直接拒绝，而不是静默忽略。</li>
 * </ul>
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
