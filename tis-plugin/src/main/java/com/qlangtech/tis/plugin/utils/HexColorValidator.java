package com.qlangtech.tis.plugin.utils;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;

import java.util.regex.Pattern;

/**
 * 十六进制色值校验。
 *
 * <p>支持 CSS 四种简写：{@code #RGB} / {@code #RGBA} / {@code #RRGGBB} / {@code #RRGGBBAA}，
 * 大小写均可。不 trim：校验的就是最终落盘的那个串，带空白的一律拒掉。
 *
 * <p>消费者可把本类当作「共享函数」而非「实例」—— 所有方法都是 static，构造器私有。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/10/22
 */
public final class HexColorValidator {

    /**
     * 十六进制色值格式：{@code #RGB} / {@code #RGBA} / {@code #RRGGBB} / {@code #RRGGBBAA}，大小写均可。
     * <p>
     * 覆盖 CSS 的四种简写 —— 三位与六位是日常写法，四位与八位带 alpha 通道（如 {@code #f5222dcc}）。
     */
    private static final Pattern HEX_COLOR_PATTERN =
            Pattern.compile("^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");

    private HexColorValidator() {
    }

    /**
     * 是否为合法的十六进制色值（不 trim，空白一律拒掉）。
     */
    public static boolean isHexColor(String value) {
        return value != null && HEX_COLOR_PATTERN.matcher(value).matches();
    }

    /**
     * 校验十六进制色值格式，不通过时写入 {@code msgHandler}。
     *
     * <p>框架按 {@code validate${FieldName}} 约定反射发现校验方法
     * （见 {@code Descriptor#createValidateMap}），空值不进来（由  负责）。
     *
     * @return 格式正确返回 {@code true}，否则通过 {@code msgHandler.addFieldError} 报错后返回 {@code false}
     */
    public static boolean validate(IFieldErrorHandler msgHandler, Context context,
                                   String fieldName, String value) {
        if (!isHexColor(value)) {
            msgHandler.addFieldError(context, fieldName,
                    "色值格式不正确，须为十六进制，如 #f5222d（支持 #RGB / #RGBA / #RRGGBB / #RRGGBBAA）");
            return false;
        }
        return true;
    }
}