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
package com.qlangtech.tis.sql.parser.er;

import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import java.util.Map;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-06-03 18:01
 */
public interface IPrimaryTabFinder {

    /**
     * 查找主表元信息
     *
     * @param entityName 可能是主表实体对象
     * @return
     */
    public Optional<TableMeta> getPrimaryTab(IDumpTable entityName);

    Map<EntityName, TabFieldProcessor> getTabFieldProcessorMap();
}
