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
package com.qlangtech.tis.plugin.annotation;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月11日
 */
public enum FormFieldType {

    INPUTTEXT(1),
    /**
     * 有多个选项可以选择
     */
    SELECTABLE(6),
    /**
     * 密码
     */
    PASSWORD(7),
    TEXTAREA(2),
    DATE(3),
    /**
     * 输入一个数字
     */
    INT_NUMBER(4),
    ENUM(5);

    private final int identity;

    FormFieldType(int val) {
        this.identity = val;
    }

    public int getIdentity() {
        return this.identity;
    }
}
