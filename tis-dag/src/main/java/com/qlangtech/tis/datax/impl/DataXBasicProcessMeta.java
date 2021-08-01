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

package com.qlangtech.tis.datax.impl;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-05-18 09:50
 **/
public class DataXBasicProcessMeta {
    private boolean readerRDBMS;
    private boolean explicitTable;
    private boolean writerRDBMS;
    private boolean isWriterSupportMultiTableInReader;

    public boolean isWriterSupportMultiTableInReader() {
        return isWriterSupportMultiTableInReader;
    }

    public void setWriterSupportMultiTableInReader(boolean writerSupportMultiTableInReader) {
        isWriterSupportMultiTableInReader = writerSupportMultiTableInReader;
    }

    /**
     * 从非结构化的数据源导入到结构化的数据源，例如从OSS导入到MySQL
     *
     * @return
     */
    public boolean isReaderUnStructed() {
        return !readerRDBMS;
    }

    public boolean isWriterRDBMS() {
        return this.writerRDBMS;
    }

    public boolean isReaderRDBMS() {
        return readerRDBMS;
    }

    public boolean isExplicitTable() {
        return explicitTable;
    }

    public void setReaderRDBMS(boolean readerRDBMS) {
        this.readerRDBMS = readerRDBMS;
    }

    public void setReaderHasExplicitTable(boolean explicitTable) {
        this.explicitTable = explicitTable;
    }

    public void setWriterRDBMS(boolean writerRDBMS) {
        this.writerRDBMS = writerRDBMS;
    }

    @Override
    public String toString() {
        return "ProcessMeta{" +
                "readerRDBMS=" + readerRDBMS +
                ", writerRDBMS=" + writerRDBMS +
                '}';
    }
}
