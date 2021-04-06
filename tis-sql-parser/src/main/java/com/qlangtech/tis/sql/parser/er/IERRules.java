/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.sql.parser.er;

import com.qlangtech.tis.fullbuild.indexbuild.IDumpTable;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.util.List;
import java.util.Optional;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-06 10:41
 */
public interface IERRules {
    List<PrimaryTableMeta> getPrimaryTabs();

    boolean isTriggerIgnore(EntityName entityName);

    List<TableRelation> getAllParent(EntityName entityName);

    List<TableRelation> getChildTabReference(EntityName entityName);

    Optional<TableMeta> getPrimaryTab(IDumpTable entityName);

    boolean hasSetTimestampVerColumn(EntityName entityName);

    TimeCharacteristic getTimeCharacteristic();

    boolean isTimestampVerColumn(EntityName entityName, String name);

    String getTimestampVerColumn(EntityName entityName);

    List<TabFieldProcessor> getTabFieldProcessors();

    Optional<TableRelation> getFirstParent(String tabName);

    Optional<PrimaryTableMeta> isPrimaryTable(String tabName);
}


