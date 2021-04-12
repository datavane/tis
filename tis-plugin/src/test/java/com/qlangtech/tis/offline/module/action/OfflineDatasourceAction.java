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
package com.qlangtech.tis.offline.module.action;

import com.qlangtech.tis.manage.common.Option;

import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-12 09:33
 */
public class OfflineDatasourceAction {

    public static List<Option> getExistDbs(String describleName) {
        String name = "com.qlangtech.tis.plugin.ds.mysql.MySQLDataSourceFactory";
        return Collections.singletonList(new Option("testDbName", "testDbName"));
    }
}
