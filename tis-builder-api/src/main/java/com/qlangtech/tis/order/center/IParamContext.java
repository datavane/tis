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
package com.qlangtech.tis.order.center;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年12月11日 上午11:09:21
 */
public interface IParamContext {

    public String KEY_PARTITION = "ps";

    public String COMPONENT_START = "component.start";

    public String COMPONENT_END = "component.end";

    public String KEY_TASK_ID = "taskid";

    public String KEY_BUILD_TARGET_TABLE_NAME = "targetTableName";

    public String KEY_BUILD_INDEXING_ALL_ROWS_COUNT = "indexing.all.rows.count";

    public String getString(String key);

    public boolean getBoolean(String key);

    public int getInt(String key);

    public long getLong(String key);

    public String getPartitionTimestamp();
}
