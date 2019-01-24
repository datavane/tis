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
package com.qlangtech.tis.hdfs.client.data;

import java.util.Map;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;

/*
 * @description
 * @since 2011-8-3 涓嬪崍04:40:51
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface SourceDataProvider<K, V> {

    public String getDbHost();

    public String getDsName();

    // baisui add for init the obj
    public void setDumpContext(TSearcherDumpContext context);

    /**
     * 初始化操作
     */
    public void init() throws SourceDataReadException;

    /**
     * 打开资源连接如数据库,文件句柄
     *
     * @throws Exception
     */
    public void openResource() throws SourceDataReadException;

    /**
     * 释放数据库或者文件资源
     *
     * @throws SourceDataReadException
     */
    public void closeResource() throws SourceDataReadException;

    /**
     * 判断是否还有数据
     *
     * @return
     * @throws Exception
     */
    public boolean hasNext() throws SourceDataReadException;

    /**
     * 获取一条记录
     *
     * @return
     * @throws Exception
     */
    public Map<K, V> next() throws SourceDataReadException;
    /**
     * 获取最终并行执行单位 如果对应数据库为数据源 则以库或者以表为单位的个数，对应启动多少个线程进行数据并发处理 如果是对应文本数据则是切分文件的个数
     *
     * @return
     */
    // public List<SourceDataProvider> getSplit();
}
