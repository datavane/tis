package com.qlangtech.tis.plugin.ontology.aggregation;

import com.qlangtech.tis.plugin.ValidatorCommons;

import java.util.Objects;

/**
 * 聚合查询的一个分组键。
 *
 * <p>只有两种形态：
 * <ul>
 *   <li>{@link #rawColumn(String)} —— 按列值直接分组（分类轴、segment 轴）</li>
 *   <li>{@link #temporal(String, TimeGranularity, int)} —— 按时间粒度切桶后分组（时间轴）</li>
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
 * @param granularity 时间粒度；{@code null} 表示按原始列值分组
 * @param unitValue   一个刻度包含几个粒度单位，至少为 1
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/24
 */
public record GroupKey(String column, TimeGranularity granularity, int unitValue) {

    public GroupKey {
        if (column == null || !ValidatorCommons.PATTERN_DB_COL_NAME.matcher(column).matches()) {
            throw new IllegalArgumentException("illegal group column name:" + column
                    + ", must match " + ValidatorCommons.PATTERN_DB_COL_NAME.pattern());
        }
        if (unitValue < 1) {
            throw new IllegalArgumentException("group key unitValue must be >= 1, but was:" + unitValue
                    + ", column:" + column);
        }
    }

    /**
     * 按列值直接分组。
     */
    public static GroupKey rawColumn(String column) {
        return new GroupKey(column, null, 1);
    }

    /**
     * 按时间粒度切桶后分组。
     *
     * @param unitValue {@code null} 或小于 1 时归一为 1（与表单默认值一致）
     */
    public static GroupKey temporal(String column, TimeGranularity granularity, Integer unitValue) {
        Objects.requireNonNull(granularity, "time granularity can not be null");
        return new GroupKey(column, granularity, unitValue == null || unitValue < 1 ? 1 : unitValue);
    }

    public boolean isTemporal() {
        return this.granularity != null;
    }
}
