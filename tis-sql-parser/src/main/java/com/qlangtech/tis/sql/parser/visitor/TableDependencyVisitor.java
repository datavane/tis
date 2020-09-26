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
package com.qlangtech.tis.sql.parser.visitor;

import java.util.Set;
import com.facebook.presto.sql.tree.DefaultTraversalVisitor;
import com.facebook.presto.sql.tree.Table;
import com.google.common.collect.Sets;

/**
 * 统计一个Sql的依赖表
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年5月13日
 */
public class TableDependencyVisitor extends DefaultTraversalVisitor<Void, Void> {

    private Set<String> tabDependencies = Sets.newHashSet();

    public Set<String> getTabDependencies() {
        return tabDependencies;
    }

    public static TableDependencyVisitor create() {
        return new TableDependencyVisitor();
    }

    @Override
    protected Void visitTable(Table node, Void context) {
        tabDependencies.add(String.valueOf(node.getName()));
        return null;
    }
}
