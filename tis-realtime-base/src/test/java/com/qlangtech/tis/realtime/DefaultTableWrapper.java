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
package com.qlangtech.tis.realtime;

import com.qlangtech.tis.realtime.test.util.DefaultRowValueGetter;
import com.qlangtech.tis.realtime.transfer.BasicRMListener;
import com.qlangtech.tis.realtime.transfer.impl.DefaultTable;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class DefaultTableWrapper extends DefaultTable {

    public DefaultTableWrapper(String tableName, BasicRMListener listenerBean, DefaultRowValueGetter rowVals) {
        super(tableName, listenerBean.getTableProcessor(tableName));
        for (Map.Entry<String, String> entry : rowVals.vals.entrySet()) {
            this.addColumn(entry.getKey(), entry.getValue());
        }
    }
}
