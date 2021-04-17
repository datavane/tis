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
package com.qlangtech.tis.wangjubao.jingwei;

import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Table {

    private static final Logger log = LoggerFactory.getLogger(Table.class);

    private final String name;

    // private String primaryKey;
    // private final String indexName;
    private final Set<String> ignorFields = new HashSet<String>();

    private final ParseResult schemaParseResult;

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

    public Table(String name, ParseResult schemaParseResult) {
        super();
        this.name = name;
        // this.indexName = indexName;
        // this.groupSize = grouSize;
        this.schemaParseResult = schemaParseResult;
        if (schemaParseResult == null) {
            throw new IllegalArgumentException("param 'schemaParseResult' can not be null");
        }
        if (!this.schemaParseResult.isValid()) {
            throw new IllegalStateException("schema parse faild ,summary:" + this.schemaParseResult.getErrorSummary());
        }
    }

    public void setIgnorFiles(String ignorField) {
        ignorFields.addAll(Arrays.asList(StringUtils.split(ignorField, ",")));
    }

    public boolean shallIgnor(String col) {
        return ignorFields.contains(col) || this.schemaParseResult.dFields.getField(col) == null;
    }

    // public String getIndexName() {
    // return this.indexName;
    // }
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
}
