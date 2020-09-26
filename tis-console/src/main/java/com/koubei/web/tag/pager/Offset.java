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
package com.koubei.web.tag.pager;

/**
 * 分页控件偏移
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Offset {

    // public static final String COMMON_IGNORE = "<li class=\"ellipses\">...</li>";
    /**
     * 步长
     */
    private final int setp;

    /**
     * 省略符号
     */
    private final String ignor;

    public Offset(int setp, String ignor) {
        super();
        this.setp = setp;
        this.ignor = ignor;
    }

    // public Offset(int setp) {
    // this(setp, COMMON_IGNORE);
    // }
    public int getSetp() {
        return setp;
    }

    public String getIgnor() {
        return ignor;
    }
}
