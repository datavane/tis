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

import com.qlangtech.tis.realtime.transfer.DTO.EventType;
import java.util.Map;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月5日 下午5:24:40
 */
public interface ITable extends IRowValueGetter {

    public String getTableName();

    /**
     * 数据更新版本，一般以时间戳标记，防止在row更新過程中被脏数据覆盖
     *
     * @return
     */
    // public long getVersion();
    public Map<String, String> getColumns();

    public EventType getEventType();

    // /**
    // * 取得更新之后最新值
    // *
    // * @param key
    // * @return
    // */
    // public String getColumn(String key);
    /**
     * 取得更新后的 int值
     *
     * @param key
     * @param errorCare 当转型错误是否要抛出异常
     * @return
     */
    public int getInt(String key, boolean errorCare);

    public long getLong(String key, boolean errorCare);

    public double getDouble(String key, boolean errCare);

    public float getFloat(String key, boolean errCare);

    /**
     * 取得老字段值
     *
     * @param key
     * @return
     */
    public String getOldColumn(String key);

    /**
     * 对于参数中提示的几个列是否有更新
     *
     * @param keys
     * @return
     */
    public boolean columnChange(Set<String> keys);

    public void desc(StringBuffer buffer);
}
