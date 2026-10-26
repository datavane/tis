package com.qlangtech.tis.plugin.ontology.aggregation;

import com.qlangtech.tis.extension.DescriptorUseableShortComment;

/**
 * 时间分桶粒度。
 *
 * <p>原先是 {@code TemporalBucketing} 的嵌套枚举。提升为独立类型是为了让核心层的
 * {@link GroupKey} 能引用它——分组键需要表达「这一列按哪一级时间粒度切桶」，
 * 而 {@code TemporalBucketing} 属于插件工程的表单模型，核心层不该依赖它。
 *
 * <h3>为什么常量名不能改</h3>
 * XStream 按<b>常量名</b>持久化枚举，表单选项也按常量名下发；改名会让存量落盘配置读不出来。
 *
 * <h3>时 / 分 / 秒只适用于时间戳属性</h3>
 * 纯日期（{@code DATE}）属性没有时分秒，切到 HOUR 以下会得到「整个表只有一个桶」的结果。
 * 这个约束由使用侧（{@code AggregationPushdownPlanner}）按属性类型校验，枚举本身编码不了。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/24
 * @see GroupKey
 */
public enum TimeGranularity implements DescriptorUseableShortComment {
    YEAR("年"),
    QUARTER("季度"),
    MONTH("月"),
    WEEK("周"),
    DAY("天"),
    HOUR("小时（仅时间戳属性）"),
    MINUTE("分钟（仅时间戳属性）"),
    SECOND("秒（仅时间戳属性）");

    private final String comment;

    TimeGranularity(String comment) {
        this.comment = comment;
    }

    @Override
    public String shortComment() {
        return comment;
    }
}
