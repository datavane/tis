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

import java.io.Closeable;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年10月5日 下午5:02:14
 */
public interface IPojo extends Closeable {

    public IPk getPK();

    public boolean isAdd();

    // for create document _version_
    public long getVersion();

    /**
     * 取得solr集群名称
     *
     * @return
     */
    public String getCollection();

    public IRowPack getRowPack(String tableName);

    /**
     * 是否存在該表的記錄,只要有一个不为空就返回true
     *
     * @param tableName
     * @return
     */
    public boolean isTabRowExist(String... tableName);

    public Set<Entry<String, IRowPack>> getRowsPack();

    /**
     * 发生时间
     *
     * @return
     */
    public long occurTime();

    public boolean setTable(String tableName, ITable table);

    // 需要关注rowpack中的覆写等待时间窗口吗？
    public boolean careRowpackTimeWindow();
    // public String getPrimaryTableName();
}
