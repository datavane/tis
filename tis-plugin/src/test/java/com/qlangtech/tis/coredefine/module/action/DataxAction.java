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
package com.qlangtech.tis.coredefine.module.action;

import com.google.common.collect.Lists;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.util.TestHeteroList;
import edu.emory.mathcs.backport.java.util.Collections;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-13 11:00
 */
public class DataxAction {

    public static List<Option> getDepartments() {
        return Collections.emptyList();
    }

    public static List<String> getTablesInDB(String dataxName) {
        Assert.assertEquals("dataxName must equal", TestHeteroList.DATAX_INSTANCE_NAME, dataxName);
        return Lists.newArrayList("table1", "table2", "table3");
    }
}
