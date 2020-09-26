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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.exception.SourceDataReadException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;

/**
 * @description
 * @since 2011-9-24 涓嬪崍02:21:30
 * @version 1.0
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
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
