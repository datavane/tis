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
public enum TriggerType {

    CRONTAB(1, "定时"),
    /* 定时任务触发 */
    MANUAL(2, "手动");

    /* 手动任务触发 */
    private final int value;

    private final String literal;

    private TriggerType(int value, String literal) {
        this.value = value;
        this.literal = literal;
    }

    public int getValue() {
        return value;
    }

    /**
     * 字面量
     * @return
     */
    public String getLiteral() {
        return this.literal;
    }

    public static TriggerType parse(int val) {
        if (CRONTAB.value == val) {
            return CRONTAB;
        } else if (MANUAL.value == val) {
            return MANUAL;
        }
        throw new IllegalStateException("val:" + val + " is illegal");
    }
}
