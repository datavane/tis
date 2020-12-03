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
package com.qlangtech.tis.hdfs.client.data;

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
    //public abstract void closeResource(boolean isIterator) throws SourceDataReadException;
}
