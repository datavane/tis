package com.qlangtech.tis.plugin.ontology.aggregation;

/**
 * 当前绑定 / 方言不支持数据库端聚合。
 *
 * <p>刻意从 {@link UnsupportedOperationException} 派生但<b>只用于这一种语义</b>：
 * 调用方应当只捕获本异常并把「不支持」翻译成可读错误。若直接捕获宽泛的
 * {@code UnsupportedOperationException}，实现里的集合 / 反射 bug 也会被误报成
 * 「该数据库不支持聚合」，掩盖真正的故障。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/24
 */
public class AggregationNotSupportedException extends UnsupportedOperationException {

    private static final long serialVersionUID = 1L;

    public AggregationNotSupportedException(String message) {
        super(message);
    }

    public AggregationNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
