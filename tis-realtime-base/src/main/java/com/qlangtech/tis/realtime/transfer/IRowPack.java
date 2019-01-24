/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.realtime.transfer;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
     * @param tableName
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
