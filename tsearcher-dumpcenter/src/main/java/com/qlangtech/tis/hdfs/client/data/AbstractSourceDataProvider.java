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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;

/*
 * @description
 * @since 2011-9-24 涓嬪崍02:21:30
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractSourceDataProvider implements SourceDataProvider<String, String> {

    protected TSearcherDumpContext dumpContext;

    public TSearcherDumpContext getDumpContext() {
        return dumpContext;
    }

    public void setDumpContext(TSearcherDumpContext dumpContext) {
        this.dumpContext = dumpContext;
    }

    /**
     * @param isIterator
     *            是否嵌套关闭资源
     * @throws Exception
     */
    public abstract void closeResource(boolean isIterator) throws SourceDataReadException;

    /**
     * @uml.property name="nextDataProvider"
     * @uml.associationEnd
     */
    // public abstract SourceDataProvider getNextDataProvider();
    // public abstract boolean hasNextDatProvider();
    /**
     * @param nextProvider
     * @uml.property name="nextDataProvider"
     */
    // public abstract void setNextDataProvider(SourceDataProvider nextProvider);
    Log logger = LogFactory.getLog(SourceDataProvider.class);

    /**
     * 获取最终执行单位 如果对应数据库为数据源 则以库或者以表为单位的个数，对应启动多少个线程进行数据并发处理 如果是对应文本数据则是切分文件的个数
     *
     * @return
     */
    public abstract String getShardKey();
    // public abstract void setTimeManager(FileTimeProvider timeManager);
}
