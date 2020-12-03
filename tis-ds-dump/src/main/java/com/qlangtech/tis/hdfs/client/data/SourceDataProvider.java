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
package com.qlangtech.tis.hdfs.client.data;

import java.util.Map;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface SourceDataProvider<K, V> {

//    public String getDbHost();
//
//    public String getDsName();
//
//    // baisui add for init the obj
//    public void setDumpContext(TSearcherDumpContext context);
//
//    /**
//     * 初始化操作
//     */
//    public void init() throws SourceDataReadException;
//
//    /**
//     * 打开资源连接如数据库,文件句柄
//     *
//     * @throws Exception
//     */
//    public void openResource() throws SourceDataReadException;
//
//    /**
//     * 释放数据库或者文件资源
//     *
//     * @throws SourceDataReadException
//     */
//    public void closeResource() throws SourceDataReadException;
//
//    /**
//     * 取得行记录条数
//     * @return
//     */
//    public int getRowSize();
//
//    /**
//     * 判断是否还有数据
//     *
//     * @return
//     * @throws Exception
//     */
//    public boolean hasNext() throws SourceDataReadException;
//
//    /**
//     * 获取一条记录
//     *
//     * @return
//     * @throws Exception
//     */
//    public Map<K, V> next() throws SourceDataReadException;
}
