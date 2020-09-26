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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年12月23日
 */
public interface IRowPack {

    /**
     * 是否是新增操作
     *
     * @return
     */
    public boolean isNew();

    public long getVersion();

    public String getShareId(String shareIdName);

    public String getTableName();

    public int getRowSize();

    /**
     * 处理的binlog的表的timewindow不相同的，<br>
     * 当有单独一个binlog接收到之后需要快速响应的则需要覆写，这个方法默认实现的是返回null
     *
     * @return
     */
    public Long getTimeWindow();

    /**
     * 是否是脏数据
     *
     * @param
     * @param table
     * @return true: 脏数据 ，false:是新版本数据
     */
    public boolean isNotDirtyAndPut(ITable table);

    public boolean isDirty(ITable old, ITable newt);

    /**
     * 可选择性访问
     *
     * @param visitor
     * @throws Exception
     */
    public void vistRow(IRowVisitor visitor) throws Exception;

    /**
     * 访问全部记录
     *
     * @param visitor
     * @throws Exception
     */
    public void vistAllRow(IAllRowsVisitor visitor) throws Exception;
}
