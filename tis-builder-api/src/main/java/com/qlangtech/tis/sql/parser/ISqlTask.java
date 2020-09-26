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

import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.fullbuild.indexbuild.ITabPartition;
import com.qlangtech.tis.fullbuild.taskflow.ITemplateContext;
import com.qlangtech.tis.sql.parser.er.IPrimaryTabFinder;
import com.qlangtech.tis.sql.parser.meta.DependencyNode;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ISqlTask {

    public String getId();

    String getExportName();

    public List<DependencyNode> getDependencies();

    String getSql();

    RewriteSql getRewriteSql(String taskName, Map<IDumpTable, ITabPartition> dumpPartition, IPrimaryTabFinder erRules, ITemplateContext templateContext, boolean isFinalNode);

    class RewriteSql {

        public final String sqlContent;

        public final IAliasTable primaryTable;

        public RewriteSql(String sqlContent, IAliasTable primaryTable) {
            this.sqlContent = sqlContent;
            this.primaryTable = primaryTable;
        }
    }
}
