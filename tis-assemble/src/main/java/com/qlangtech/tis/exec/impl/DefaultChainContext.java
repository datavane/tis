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
package com.qlangtech.tis.exec.impl;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.exec.lifecycle.hook.IIndexBuildLifeCycleHook;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.solr.common.cloud.ZkStateReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultChainContext implements IExecChainContext {

    public static final String KEY_PARTITION = "ps";

    // private final Date startTime;
    private final String ps;

    private TisZkClient zkClient;

    private ZkStateReader zkStateReader;

    private FileSystem fileSystem;

    // private String indexName;
    // private IIndexBuildLifeCycleHook indexBuildLifeCycleHook;
    // private ParseResult schemaParseResult;
    private IIndexMetaData indexMetaData;

    private final IParamContext httpExecContext;

    // public void setHttpExecContext(IParamContext httpExecContext) {
    // this.httpExecContext = httpExecContext;
    // }
    // public ParseResult getSchemaParseResult() {
    // if (schemaParseResult == null) {
    // throw new IllegalStateException("schemaParseResult can not be null");
    // }
    // return schemaParseResult;
    // }
    @Override
    public IIndexMetaData getIndexMetaData() {
        return this.indexMetaData;
    }

    // public void setSchemaParseResult(ParseResult schemaParseResult) {
    // this.schemaParseResult = schemaParseResult;
    // }
    // @Override
    // public IIndexBuildLifeCycleHook getIndexBuildLifeCycleHook() {
    // if (this.indexBuildLifeCycleHook == null) {
    // throw new IllegalStateException("indexBuildLifeCycleHook can not be null");
    // }
    // return this.indexBuildLifeCycleHook;
    // }
    public void setIndexMetaData(IIndexMetaData indexMetaData) {
        this.indexMetaData = indexMetaData;
    }

    // public void setIndexBuildLifeCycleHook(IIndexBuildLifeCycleHook indexBuildLifeCycleHook) {
    // this.indexBuildLifeCycleHook = indexBuildLifeCycleHook;
    // }
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public DefaultChainContext(IParamContext execContext) {
        super();
        // this.startTime = new Date();
        final SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        final Date startTime = new Date();
        ps = dataFormat.format(startTime);
        this.httpExecContext = execContext;
    }

    public ZkStateReader getZkStateReader() {
        return zkStateReader;
    }

    private final Map<String, Object> attribute = new HashMap<String, Object>();

    public void setAttribute(String key, Object v) {
        this.attribute.put(key, v);
    }

    @SuppressWarnings("all")
    public <T> T getAttribute(String key) {
        return (T) this.attribute.get(key);
    }

    public void setZkStateReader(ZkStateReader zkStateReader) {
        this.zkStateReader = zkStateReader;
    }

    @Override
    public TisZkClient getZkClient() {
        return this.zkClient;
    }

    public void setZkClient(TisZkClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public FileSystem getDistributeFileSystem() {
        return this.fileSystem;
    }

    @Override
    public String getIndexName() {
        String indexName = this.httpExecContext.getString("appname");
        if (StringUtils.isBlank(indexName)) {
            throw new IllegalArgumentException(indexName);
        }
        return indexName;
    }

    @Override
    public String getPartitionTimestamp() {
        String ps = StringUtils.defaultIfEmpty(getString(KEY_PARTITION), this.ps);
        if (!ps.startsWith("20")) {
            throw new IllegalArgumentException("ps:" + ps + " shall start with 20");
        }
        return ps;
    }

    @Override
    public final String getContextUserName() {
        // System.getProperty("user.name"));
        return "admin";
    }

    public String getString(String key) {
        return httpExecContext.getString(key);
    }

    public boolean getBoolean(String key) {
        return httpExecContext.getBoolean(key);
    }

    public int getInt(String key) {
        return httpExecContext.getInt(key);
    }

    public long getLong(String key) {
        return httpExecContext.getLong(key);
    }
    // @Override
    // public Date getStartTime() {
    // return this.startTime;
    // }
}
