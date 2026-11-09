package com.qlangtech.tis.plugin.ontology.aggregation;

import com.qlangtech.tis.plugin.ValidatorCommons;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 聚合查询的一个分组键。
 *
 * <p>三种形态：
 * <ul>
 *   <li>{@link #rawColumn(String)} —— 按列值直接分组（分类轴、segment 轴）</li>
 *   <li>{@link #temporal(String, TimeGranularity, int)} —— 按时间粒度切桶后分组（时间轴）</li>
 *   <li>{@link #numeric(String, double, int)} —— 按固定宽度的数值区间切桶后分组（直方图）</li>
 * </ul>
 *
 * <h3>为什么是结构化对象而不是 SQL 片段</h3>
 * 核心 API <b>不接受任何裸 SQL 字符串</b>：表达式只能由 {@code GroupKey} 与
 * {@link com.qlangtech.tis.plugin.ontology.AggregationFunc} 组合，由方言 renderer 负责
 * 拼装。这样「哪些列能进 SQL」的校验点只有一个，新增方言时也不会各写一套转义。
 *
 * <h3>列名为什么在这里就做正则校验</h3>
 * {@code IDBReservedKeys.getEscapedEntity} 只是把列名包进转义符里，<b>列名自身带转义符
 * 就能穿出去</b>，不构成注入防护。{@link ValidatorCommons#PATTERN_DB_COL_NAME} 限定了
 * 字母/数字/下划线，才是真正有效的边界。调用方还必须先 {@code addTargetCol} 过
 * {@link AggregationCriteria#addGroupKey(GroupKey)} 的白名单。
 *
 * @param column      物理列名（= 对象属性名）
 * @param granularity 时间粒度；{@code null} 表示不按时间切桶
 * @param unitValue   一个刻度包含几个粒度单位（时间）/ 桶宽度（数值），至少为 1
 * @param bucketWidth 数值分桶的桶宽；{@code null} 表示不是数值分桶
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/24
 */
public record GroupKey(String column, TimeGranularity granularity, int unitValue, Double bucketWidth) {

    public GroupKey {
        if (column == null || !ValidatorCommons.PATTERN_DB_COL_NAME.matcher(column).matches()) {
            throw new IllegalArgumentException("illegal group column name:" + column
                    + ", must match " + ValidatorCommons.PATTERN_DB_COL_NAME.pattern());
        }
        if (unitValue < 1) {
            throw new IllegalArgumentException("group key unitValue must be >= 1, but was:" + unitValue
                    + ", column:" + column);
        }
        if (granularity != null && bucketWidth != null) {
            throw new IllegalArgumentException("group key can not be temporal and numeric at the same time, column:"
                    + column);
        }
        // NaN / Infinity 在这里一并被拒：与 0 比较恒为 false
        if (bucketWidth != null && !(bucketWidth > 0)) {
            throw new IllegalArgumentException("numeric group key bucketWidth must be a finite value > 0, but was:"
                    + bucketWidth + ", column:" + column);
        }
    }

    /**
     * 兼容三参写法（历史调用点与单测）。
     *
     * <p>record 的规范构造器加了数值分桶分量之后，原来的 {@code new GroupKey(col, granularity, unit)}
     * 会失去匹配目标 —— 它不是公开 API 的扩展点，但存量调用方逐个改没有收益。
     */
    public GroupKey(String column, TimeGranularity granularity, int unitValue) {
        this(column, granularity, unitValue, null);
    }

    /**
     * 按列值直接分组。
     */
    public static GroupKey rawColumn(String column) {
        return new GroupKey(column, null, 1, null);
    }

    /**
     * 按时间粒度切桶后分组。
     *
     * @param unitValue {@code null} 或小于 1 时归一为 1（与表单默认值一致）
     */
    public static GroupKey temporal(String column, TimeGranularity granularity, Integer unitValue) {
        Objects.requireNonNull(granularity, "time granularity can not be null");
        return new GroupKey(column, granularity, unitValue == null || unitValue < 1 ? 1 : unitValue, null);
    }

    /**
     * 按固定宽度的数值区间切桶后分组。
     *
     * <p>SQL 侧只返回<b>桶序号</b>（{@code FLOOR(col / width)}，可为负），归还桶区间起点
     * 由调用方用 {@link #bucketLowerBound(Number)} 计算 —— 与时间分桶「SQL 返序号、
     * Java 还原」是同一套接缝，理由也相同：数值除法的格式 / 精度问题只留在 SQL 一处。
     *
     * @param width 桶宽，必须为正的有限值
     */
    public static GroupKey numeric(String column, double width, int unitValue) {
        if (!(width > 0) ) {
            throw new IllegalArgumentException("numeric group key bucketWidth must be > 0, but was:" + width);
        }
        return new GroupKey(column, null, unitValue < 1 ? 1 : unitValue, width);
    }

    public boolean isTemporal() {
        return this.granularity != null;
    }

    public boolean isNumeric() {
        return this.bucketWidth != null;
    }

    /**
     * 该桶的下界（含）。
     *
     * <p>桶定义为 {@code [lower, lower + width)}，与前端直方图的柱区间一致：
     * 点柱子选中的就是这个半开区间。
     *
     * @param bucketIndex SQL 返回的桶序号
     */
    public BigDecimal bucketLowerBound(Number bucketIndex) {
        if (!isNumeric()) {
            throw new IllegalStateException("bucketLowerBound is only meaningful for numeric group key, column:"
                    + column);
        }
        return BigDecimal.valueOf(bucketIndex.doubleValue())
                .multiply(BigDecimal.valueOf(bucketWidth));
    }
}
