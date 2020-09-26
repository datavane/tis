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
package com.facebook.presto.sql;

import java.util.List;
import java.util.Optional;
import com.facebook.presto.sql.tree.Expression;
import com.facebook.presto.sql.tree.GroupingElement;
import com.facebook.presto.sql.tree.OrderBy;
import com.facebook.presto.sql.tree.SortItem;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TisExpressionFormatter {

    public static String formatGroupBy(List<GroupingElement> groupingElements) {
        return ExpressionFormatter.formatGroupBy(groupingElements);
    }

    public static String formatGroupBy(List<GroupingElement> groupingElements, Optional<List<Expression>> parameters) {
        return ExpressionFormatter.formatGroupBy(groupingElements, parameters);
    }

    public static String formatOrderBy(OrderBy orderBy, Optional<List<Expression>> parameters) {
        return ExpressionFormatter.formatOrderBy(orderBy, parameters);
    }

    public static String formatSortItems(List<SortItem> sortItems, Optional<List<Expression>> parameters) {
        return ExpressionFormatter.formatSortItems(sortItems, parameters);
    }

    public static String formatStringLiteral(String s) {
        return ExpressionFormatter.formatStringLiteral(s);
    }
}
