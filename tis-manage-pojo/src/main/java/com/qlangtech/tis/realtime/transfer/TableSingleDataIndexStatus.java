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
package com.qlangtech.tis.realtime.transfer;

import java.util.HashMap;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TableSingleDataIndexStatus extends ListenerStatusKeeper {

    // map用来存储一个索引在一个分片中所有表的消费信息
    private HashMap<String, Long> /*tableName*/
    tableConsumeData;

    public TableSingleDataIndexStatus() {
        tableConsumeData = new HashMap<>();
    }

    public void put(String tableName, Long accumulation) {
        tableConsumeData.put(tableName, accumulation);
    }

    public int tableSize() {
        return tableConsumeData.size();
    }

    public HashMap<String, Long> getTableConsumeData() {
        return tableConsumeData;
    }
}
