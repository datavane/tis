package com.qlangtech.tis.plugin.ontology.impl.objtype;

import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.manage.common.IEnumTypeSer;

/**
 * 对象属性过滤的谓词算子。
 *
 * <p>它同时是<b>三件事的唯一来源</b>，替换了此前并存的四份定义：
 * <ul>
 *   <li>{@link ObjectTypeBinding.DataSourceQueryCriteria} 上九个 {@code OP_*} 字符串常量与
 *       {@code SUPPORTED_FILTER_OPERATORS} 白名单 —— 现在合法性由<b>类型</b>保证，
 *       {@code addFilter} 不再是白名单闸门；</li>
 *   <li>插件侧两个同名嵌套枚举（{@code ObjectSetFilterConfig.FilterOperator} /
 *       {@code FilterItemConfig.FilterOperator}）—— 它们的中文标签与多值/无值判定原样搬到这里；</li>
 *   <li>表单选项列表：{@code FormFieldType.ENUM} 反射本枚举生成下拉，
 *       值取 {@link #valueToken()}，中文取 {@link #shortComment()}。</li>
 * </ul>
 *
 * <h3>为什么实现 {@link IEnumTypeSer}</h3>
 * {@code in} 是 Java 关键字，不能作常量名，故常量叫 {@link #in_}。表单值 / 落盘 token 必须
 * 是线格式的 {@code in}（前端 {@code OntologyFilterOperator} 只认它），因此由
 * {@link #valueToken()} 给出 —— 否则前端拿到的会是常量名 {@code in_}，一路传到
 * {@code addFilter} 才炸。其余常量名与 token 逐字相同。
 *
 * <p>XStream 落盘仍按<b>常量名</b>（{@code <operator>in_</operator>}），与实现本接口无关，
 * 存量配置无需迁移。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/7
 */
public enum FilterOperator implements IEnumTypeSer, DescriptorUseableShortComment {
    equals("equals", "等于"),
    contains("contains", "包含，等同数据库LIKE操作"),
    greaterThan("greaterThan", "大于"),
    lessThan("lessThan", "小于"),
    greaterThanOrEqual("greaterThanOrEqual", "大于等于"),
    lessThanOrEqual("lessThanOrEqual", "小于等于"),
    in_("in", "属于集合"),
    isNull("isNull", "为空"),
    /**
     * 集合成员或空值：{@code (col IN (...) OR col IS NULL)}。
     *
     * <p>为什么需要它而不是让前端拼两条谓词：本引擎的谓词一律以「与」相连
     * （{@code addFilter} 只往 {@code filters} 里追加），要表达「属于这些值之一<b>或者</b>为空」
     * 就只能是一个算子。取值<b>允许为空集合</b>，此时退化成一个 {@code IS NULL}。
     */
    inOrNull("inOrNull", "属于集合或为空");

    private final String token;
    private final String comment;

    FilterOperator(String token, String comment) {
        this.token = token;
        this.comment = comment;
    }

    /**
     * 线格式 token（落盘值 / 前端选项 value）。
     */
    @Override
    public String valueToken() {
        return this.token;
    }

    /**
     * 中文标签，作为表单选项的 help 下发（{@code createEnumOptions}）。
     */
    @Override
    public String shortComment() {
        return this.comment;
    }

    /**
     * 该算子的取值是否必须是多个值（{@code IN (...)}）。
     */
    public boolean isMultiValued() {
        return this == in_ || this == inOrNull;
    }

    /**
     * 该算子<b>不需要</b>比较值 —— 只有 {@link #isNull}。
     *
     * <p>{@code ObjectSetDefinitionConfig#resolveFilterCriteria()} 的「比较值为空即跳过该行」
     * 必须为它让路，否则 {@code isNull} 会因为「用户没填值」被静默丢弃，
     * 表现是「配了空值过滤但结果没变」。
     */
    public boolean isValueFree() {
        return this == isNull;
    }

    /**
     * 把<b>外部字符串</b>（HTTP JSON 里的 operator、旧配置里的常量名）翻译成枚举。
     *
     * <p>这是全工程唯一一处「字符串 → 算子」的转换点：之后所有调用方拿到的都是枚举，
     * 非法值在这里就被拒掉，不会像以前那样一路透传到 SQL 拼装期才报
     * {@code illegal filter operator}。
     *
     * <p>先按 {@link #valueToken()} 匹配、再按 {@link #name()} 匹配，因此 {@code in} 与
     * {@code in_} 都接受 —— 前端旧产物、手工拼的 JSON、既有单测用的常量名都能进得来。
     *
     * @param raw 线格式 token 或常量名
     * @throws IllegalArgumentException 空值或无法识别的算子
     */
    public static FilterOperator parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("filter operator can not be empty");
        }
        final String token = raw.trim();
        for (FilterOperator operator : values()) {
            if (token.equals(operator.token) || token.equals(operator.name())) {
                return operator;
            }
        }
        throw new IllegalArgumentException("illegal filter operator:'" + token + "', support:"
                + java.util.Arrays.toString(values()));
    }
}
