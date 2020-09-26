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
package com.qlangtech.tis.openapi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-12-20
 */
public class ModifySchemaParam {

    // private static final long serialVersionUID = 1L;
    private final List<Column> add = new ArrayList<Column>();

    private final List<Column> delete = new ArrayList<Column>();

    public void add(Column column) {
        this.add.add(column);
    }

    public void delete(Column column) {
        this.delete.add(column);
    }

    public List<Column> getAdd() {
        return add;
    }

    public List<Column> getDelete() {
        return delete;
    }
}
