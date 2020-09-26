/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.assemble;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月17日
 */
public enum ExecResult {

    SUCCESS(1, "成功"), FAILD(-1, "失败"), DOING(2, "执行中"), CANCEL(3, "终止");

    private final int value;

    private final String literal;

    public static ExecResult parse(int value) {
        if (SUCCESS.value == value) {
            return ExecResult.SUCCESS;
        } else if (FAILD.value == value) {
            return ExecResult.FAILD;
        } else if (DOING.value == value) {
            return DOING;
        } else if (CANCEL.value == value) {
            return CANCEL;
        } else {
            throw new IllegalStateException("vale:" + value + " is illegal");
        }
    }

    private ExecResult(int value, String literal) {
        this.value = value;
        this.literal = literal;
    }

    public String getLiteral() {
        return this.literal;
    }

    public int getValue() {
        return this.value;
    }
}
