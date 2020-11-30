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
package com.qlangtech.tis.util;

import com.alibaba.citrus.turbine.Context;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IPluginContext {

    /**
     * 是否在索引
     *
     * @return
     */
    boolean isCollectionAware();

    /**
     * 是否和数据源相关
     *
     * @return
     */
    boolean isDataSourceAware();

    /**
     * 向数据库中新添加一条db的记录
     *
     * @param dbName
     * @param context
     */
    public void addDb(String dbName, Context context);

    public String getCollectionName();
}
