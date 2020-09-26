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
package com.qlangtech.tis.dump;

/**
 * 获取和路径相关的值，HiveRemoveHistoryDataTask的刪除和有table相關的路徑，還有index相關的build出來的倒排索引內容，所以兩種路徑的獲取方式是不相同的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年6月26日
 */
public interface INameWithPathGetter {

    public String getNameWithPath();
}
