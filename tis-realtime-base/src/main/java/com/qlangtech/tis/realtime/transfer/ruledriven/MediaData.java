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
package com.qlangtech.tis.realtime.transfer.ruledriven;

import com.qlangtech.tis.realtime.transfer.IRowValueGetter;
import java.util.HashMap;

/**
 * 中间数据集
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年5月29日
 */
public class MediaData extends HashMap<String, Object> implements IRowValueGetter {

    private static final long serialVersionUID = 1L;

    @Override
    public String getColumn(String key) {
        return String.valueOf(this.get(key));
    }

    public Integer getInt(String key) {
        return (Integer) this.get(key);
    }

    public Double getDouble(String key) {
        return (Double) this.get(key);
    }
}
