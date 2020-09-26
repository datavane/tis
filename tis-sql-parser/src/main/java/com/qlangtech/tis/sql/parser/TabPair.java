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
package com.qlangtech.tis.sql.parser;

import com.qlangtech.tis.sql.parser.meta.DependencyNode;

/**
 * 描述dataflow中的表之间有两两依赖关系表关系，两个表之间 完全为对等关系
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TabPair {

    private final DependencyNode one;

    private final DependencyNode another;

    public TabPair(DependencyNode one, DependencyNode another) {
        this.one = one;
        this.another = another;
    }

    public DependencyNode getOne() {
        return this.one;
    }

    public DependencyNode getAnother() {
        return this.another;
    }
}
