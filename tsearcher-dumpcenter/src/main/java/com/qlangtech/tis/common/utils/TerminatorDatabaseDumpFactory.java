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
package com.qlangtech.tis.common.utils;

import java.util.List;
import org.springframework.beans.factory.FactoryBean;
import com.qlangtech.tis.hdfs.client.process.DataProcessor;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorDatabaseDumpFactory implements FactoryBean {

    /**
     * 在全量和增量执行过程中都会执行sql
     */
    private String sql;

    private List<String> datasourceList;

    private String incrWhereAppend;

    private String fullWhereAppend;

    @SuppressWarnings("all")
    private DataProcessor dataProcessor;

    @Override
    public Object getObject() throws Exception {
        return null;
    }

    @Override
    public Class<TerminatorDataProvider> getObjectType() {
        return TerminatorDataProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<String> getDatasourceList() {
        return datasourceList;
    }

    public void setDatasourceList(List<String> datasourceList) {
        this.datasourceList = datasourceList;
    }

    public String getIncrWhereAppend() {
        return incrWhereAppend;
    }

    public void setIncrWhereAppend(String incrWhereAppend) {
        this.incrWhereAppend = incrWhereAppend;
    }

    public String getFullWhereAppend() {
        return fullWhereAppend;
    }

    public void setFullWhereAppend(String fullWhereAppend) {
        this.fullWhereAppend = fullWhereAppend;
    }

    @SuppressWarnings("all")
    public DataProcessor getDataProcessor() {
        return dataProcessor;
    }

    @SuppressWarnings("all")
    public void setDataProcessor(DataProcessor dataProcessor) {
        this.dataProcessor = dataProcessor;
    }
}
