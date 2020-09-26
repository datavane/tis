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
package com.qlangtech.tis.health.check;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public interface IStatusChecker {

    /**
     * 初始化
     */
    void init();

    /**
     * 自己在StatusChecker 列表的排序位置，越靠前越小<br>
     * 从1开始
     *
     * @return
     */
    int order();

    /**
     * StatusChecker支持的模型
     *
     * @return
     * @see
     */
    Mode mode();

    /**
     * 具体业务实现类，实现监控检查
     *
     * @return
     */
    StatusModel check();
}
