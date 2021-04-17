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
package com.qlangtech.tis.datax;

import com.qlangtech.tis.plugin.ds.DataSourceMeta;

import java.util.Iterator;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:36
 */
public interface IDataxReader extends DataSourceMeta {

    /**
     * 是否支持导入多个子表，当reader如果只支持单个表，那writer如果是MysqlWriter就可以指定表名称和列名
     *
     * @return
     */
    public boolean hasMulitTable();

    /**
     * 像Mysql会有明确的表名，而OSS没有明确的表名
     *
     * @return
     */
    public boolean hasExplicitTable();

    /**
     * 取得子任务
     *
     * @return
     */
    public Iterator<IDataxContext> getSubTasks();

    /**
     * 取得配置模版
     *
     * @return
     */
    public String getTemplate();
}
