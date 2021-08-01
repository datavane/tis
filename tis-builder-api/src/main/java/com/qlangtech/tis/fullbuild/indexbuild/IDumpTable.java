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
package com.qlangtech.tis.fullbuild.indexbuild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDumpTable {
    static List<String> createPreservedPsCols() {
        List<String> pt = new ArrayList<>();
        pt.add(IDumpTable.PARTITION_PT);
        pt.add(IDumpTable.PARTITION_PMOD);
        return Collections.unmodifiableList(pt);
    }

    public List<String> preservedPsCols = createPreservedPsCols();


    String DEFAULT_DATABASE_NAME = "tis";

    String PARTITION_PT = "pt";

    String PARTITION_PMOD = "pmod";

    public String getDbName();

    public String getTableName();

    public String getFullName();
}
