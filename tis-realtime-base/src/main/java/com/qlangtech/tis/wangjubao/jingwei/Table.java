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
package com.qlangtech.tis.wangjubao.jingwei;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Table {

    // 所有的master节点
    // private static final String[] ips = { "10.96.42.116", "10.96.45.117",
    // "10.96.50.149", "10.96.72.123", "10.96.75.165", "10.96.83.187",
    // "10.236.18.82", "10.236.24.32" };
    private final long groupSize;

    private final String name;

    private String primaryKey;

    private final String indexName;

    private final Set<String> ignorFields = new HashSet<String>();

    private final ParseResult schemaParseResult;

    private static final Log log = LogFactory.getLog(Table.class);

    private final List<ITableIgnorRule> recordIgnorRules = new ArrayList<ITableIgnorRule>();

    public void addRecordIgnorRule(ITableIgnorRule rule) {
        this.recordIgnorRules.add(rule);
    }

    public boolean shallIgnorRecord(Map<String, String> record) {
        for (ITableIgnorRule rule : recordIgnorRules) {
            if (rule.ignor(record)) {
                return true;
            }
        }
        return false;
    }

    private final List<String> logkeys = new ArrayList<String>();

    /**
     * 是否需要将这条记录记录到log4j日志中
     *
     * @param key
     */
    public boolean shallRecord2IncrLog(String key) {
        return logkeys.contains(key);
    }

    public boolean hasCols2IncrLog() {
        return logkeys.size() > 0;
    }

    public void setLogKeys(String[] keys) {
        logkeys.addAll(Arrays.asList(keys));
    }

    public Table(String name, String indexName, long grouSize) {
        super();
        this.name = name;
        this.indexName = indexName;
        this.groupSize = grouSize;
        SolrFieldsParser parser = new SolrFieldsParser();
        // aa: for (String ip : ips) {
        // for (int i = 0; i < RdsTablesListener.GROUP_SIZE; i++) {
        InputStream inputStream = null;
        String classpath = "/schema/" + indexName + ".txt";
        try {
            inputStream = this.getClass().getResourceAsStream(classpath);
            if (inputStream == null) {
                this.schemaParseResult = new ParseResult(false);
            } else {
                this.schemaParseResult = parser.parseSchema(inputStream);
            }
        } catch (Throwable e) {
            throw new IllegalStateException("parseResult can not be null, classpath:" + classpath, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        if (!this.schemaParseResult.isValid()) {
            throw new IllegalStateException("schema:" + indexName + " parse faild ,summary:" + this.schemaParseResult.getErrorSummary());
        }
        if (log.isInfoEnabled()) {
            StringBuffer schemaFields = new StringBuffer("schema:" + indexName + ",fields:");
            for (PSchemaField field : this.schemaParseResult.dFields) {
                schemaFields.append(field.getName()).append(",[").append(this.schemaParseResult.dFields.getField(field.getName()) == null).append("]");
            }
            log.info(schemaFields.toString());
        }
    }

    public long getGroupSize() {
        return groupSize;
    }

    public void setIgnorFiles(String ignorField) {
        ignorFields.addAll(Arrays.asList(StringUtils.split(ignorField, ",")));
    }

    public boolean shallIgnor(String col) {
        return ignorFields.contains(col) || this.schemaParseResult.dFields.getField(col) == null;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public TabField findAliasColumn(String column) {
        TabField field = alias.get(column);
        return (field == null) ? null : field;
    }

    public boolean hasAliasColumnConfig() {
        return alias.size() > 0;
    }

    public String getName() {
        return name;
    }

    private final Map<String, TabField> alias = new HashMap<String, TabField>();

    // 在表中添加
    // <deletecriteria>record['id']+'_'+record['sellerid']+'_'+record['buyer_id']+'_'+record['fieldId']</deletecriteria>
    private TabField deleteCriteria;

    public TabField getDeleteCriteria() {
        return deleteCriteria;
    }

    public void setDeleteCriteria(TabField deleteCriteria) {
        this.deleteCriteria = deleteCriteria;
    }

    public void addAliasField(TabField field) {
        alias.put(field.getColumn(), field);
    }

    public Collection<TabField> getAliasColumn() {
        return alias.values();
    }

    public static interface FieldProcess {

        public void process(TabField field);
    }

    // public TabField getColumn(String colName) {
    // return fields.get(colName);
    // }
    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    public static void main(String[] arg) throws Exception {
        System.out.println(IOUtils.toString("".getClass().getResourceAsStream("/log4j.xml")));
    }
}
