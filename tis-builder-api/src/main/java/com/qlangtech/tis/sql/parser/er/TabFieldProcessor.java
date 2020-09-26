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

import com.qlangtech.tis.sql.parser.meta.ColumnTransfer;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 表字段内存处理，例如时间格式化等，将来要做到可以自己扩充
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class TabFieldProcessor {

    public final EntityName tabName;

    private final List<ColumnTransfer> colTransfers;

    private Map<String, ColumnTransfer> colTransfersMap;

    public TabFieldProcessor(EntityName tabName, List<ColumnTransfer> colTransfers) {
        this.tabName = tabName;
        this.colTransfers = colTransfers;
    }

    public String getName() {
        return this.tabName.getTabName();
    }

    // @JSONField(serialize = false)
    public Map<String, /**
     * col key
     */
    ColumnTransfer> colTransfersMap() {
        if (colTransfersMap == null) {
            colTransfersMap = this.colTransfers.stream().collect(Collectors.toMap((r) -> r.getColKey(), (r) -> r));
        }
        return colTransfersMap;
    }

    public List<ColumnTransfer> getColTransfers() {
        return this.colTransfers;
    }
}
