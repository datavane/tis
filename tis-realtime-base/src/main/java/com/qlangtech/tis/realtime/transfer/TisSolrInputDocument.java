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
package com.qlangtech.tis.realtime.transfer;

import com.qlangtech.tis.cloud.ICloudInputDocument;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.solrdao.impl.ParseResult;
import com.qlangtech.tis.wangjubao.jingwei.AliasList;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年4月12日
 */
public class TisSolrInputDocument implements ICloudInputDocument {

    private final ParseResult parseResult;

    private final SolrInputDocument doc;

    // private final ITableProcessGetter tabProcessorGetter;
    // 是否被合并过
    private boolean merged = false;

    protected final Map<String, TisSolrInputDocument> children = new HashMap<>();

    public TisSolrInputDocument(ParseResult parseResult) {
        this(parseResult, new SolrInputDocument());
    }

    public TisSolrInputDocument(// ,
    ParseResult parseResult, // ITableProcessGetter tabProcessorGetter
    SolrInputDocument doc) {
        this.parseResult = parseResult;
        this.doc = doc;
        // this.tabProcessorGetter = tabProcessorGetter;
        final String uniqueKey = parseResult.getUniqueKey();
        Object cpk = null;
        if (this.doc.getChildDocumentCount() > 0) {
            for (SolrInputDocument d : this.doc.getChildDocuments()) {
                cpk = d.getFieldValue(uniqueKey);
                if (cpk == null) {
                    throw new IllegalStateException("doc can not find pk[" + uniqueKey + "]," + d.toString());
                }
                children.put(String.valueOf(cpk), new TisSolrInputDocument(parseResult, d));
            }
        }
    }

    @Override
    public SolrInputDocument unwrap() {
        return getInputDoc();
    }

    /**
     * 将文档类型转化为SolrDocument
     *
     * @return
     */
    public SolrDocument convertSolrDocument() {
        SolrDocument doc = new SolrDocument();
        for (String key : this.doc.keySet()) {
            doc.setField(key, this.doc.getFieldValue(key));
        }
        return doc;
    }

    // 是否有任何字段的值改變了
    private boolean hasAnyFieldChange = false;

    public final void setField(String colname, Object value) {
        this.setField(colname, value, false);
    }

    /**
     * @param colname
     * @param value
     * @param merge   在执行新老doc merge的时候可能会根据当前是否在做merge流程而做一流程操作
     */
    public void setField(String colname, Object value, boolean merge) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            this.clearField(colname);
            return;
        }
        if (!merge && isFieldNotAccept(colname)) {
            return;
        }
        if (!this.isHasAnyFieldChange()) {
            Object old = this.doc.getFieldValue(colname);
            if (old != null && !StringUtils.equals(String.valueOf(old), String.valueOf(value))) {
                hasAnyFieldChange = true;
            } else if (old == null) {
                hasAnyFieldChange = true;
            }
        }
        this.doc.setField(colname, value);
    }

    protected boolean isFieldNotAccept(String colname) {
        return !getSolrSchema().getFieldNameSet().contains(colname);
    }

    /**
     * 创建一个子document
     *
     * @return
     */
    protected TisSolrInputDocument createChild(Object pk, SolrDocument doc) {
        Assert.assertNotNull("pk can not be null", pk);
        String pkVal = String.valueOf(pk);
        if (this.children.containsKey(pkVal)) {
            throw new IllegalStateException(this.parseResult.getUniqueKey() + ":" + pkVal + " relevant is exist in 'children' map, can not be put agian," + this.children.get(pkVal));
        }
        TisSolrInputDocument child = new TisSolrInputDocument(this.getSolrSchema());
        child.merge2DocumentFields(doc);
        this.doc.addChildDocument(child.doc);
        this.children.put(pkVal, child);
        return child;
    }

    protected TisSolrInputDocument createChild(String pk) {
        return this.createChild(pk, new SolrDocument());
    }

    public void mergeOrCreateChild(IRowValueGetter valGetter, AliasList colsList) {
        Assert.assertNotNull(valGetter);
        String childPk = colsList.getPKVal(valGetter);
        TisSolrInputDocument child = this.children.get(childPk);
        if (child != null) {
            colsList.copy2TisDocument(valGetter, child, false);
        } else {
            colsList.copy2TisDocument(valGetter, this.createChild(childPk), false);
        }
    }

    /**
     * 將老的document的值合併到新document上
     *
     * @param oldDoc
     */
    public final TisSolrInputDocument merge2DocumentFields(SolrDocument oldDoc) {
        if (merged) {
            throw new IllegalStateException("this.doc:" + doc.toString() + " can not be merge twice");
        }
        for (String name : oldDoc.getFieldNames()) {
            this.setField(name, oldDoc.getFirstValue(name), shallForceMerge(oldDoc));
        }
        String pk = this.parseResult.getUniqueKey();
        if (oldDoc.hasChildDocuments()) {
            for (SolrDocument d : oldDoc.getChildDocuments()) {
                // .merge2DocumentFields(d);
                this.createChild(d.getFieldValue(pk), d);
            }
        }
        merged = true;
        return this;
    }

    /**
     * 是否要强力执行merge逻辑
     *
     * @param oldDoc
     * @return
     */
    protected boolean shallForceMerge(SolrDocument oldDoc) {
        return false;
    }

    /**
     * 取得字段值
     *
     * @param fieldName
     * @return
     */
    public Object getFieldValue(String fieldName) {
        return doc.getFieldValue(fieldName);
    }

    public int getInt(String fieldName, boolean careError) {
        Object val = getFieldValue(fieldName);
        if (val == null) {
            if (careError) {
                throw new NullPointerException("fieldname:" + fieldName + " is null");
            } else {
                return 0;
            }
        }
        if (val instanceof Integer) {
            return (Integer) val;
        } else if (val instanceof Long) {
            return ((Long) val).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (Throwable e) {
            if (careError) {
                throw new RuntimeException(e);
            } else {
                return 0;
            }
        }
    }

    public long getLong(String fieldName, boolean careError) {
        Object val = getFieldValue(fieldName);
        if (val == null) {
            if (careError) {
                throw new NullPointerException("fieldname:" + fieldName + " is null");
            } else {
                return 0;
            }
        // throw new NullPointerException("fieldname:" + fieldName
        // + " is null");
        }
        if (val instanceof Integer) {
            return ((Integer) val).longValue();
        } else if (val instanceof Long) {
            return ((Long) val);
        }
        try {
            return Long.parseLong(String.valueOf(val));
        } catch (Throwable e) {
            if (careError) {
                throw new RuntimeException(e);
            } else {
                return 0;
            }
        }
    }

    public void setField(String table, String colname, Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            this.clearField(colname);
            return;
        }
        // TabField field = null;
        // Table tab = tabProcessorGetter.getTableProcessor(table);
        // colname = addUnderline(colname).toString();
        // if (tab != null && (field = tab.findAliasColumn(colname)) != null) {
        // value = field.getAliasProcess().process(String.valueOf(value));
        // }
        setField(colname, value);
    }

    public void clearField(String colname) {
        Object old = this.doc.getFieldValue(colname);
        if (old != null && StringUtils.isNotBlank(String.valueOf(old))) {
            this.hasAnyFieldChange = true;
        }
        this.doc.removeField(colname);
    }

    public SolrInputDocument getInputDoc() {
        return this.doc;
    }

    protected ParseResult getSolrSchema() {
        return parseResult;
    }

    /**
     * 文档值是否发生变化
     *
     * @return
     */
    public boolean isHasAnyFieldChange() {
        if (hasAnyFieldChange) {
            return true;
        }
        for (TisSolrInputDocument d : this.children.values()) {
            if (d.isHasAnyFieldChange()) {
                return true;
            }
        }
        return false;
    }
    // private void setHasAnyFieldChange(boolean hasAnyFieldChange) {
    // this.hasAnyFieldChange = hasAnyFieldChange;
    // }
    // ===============================================================
}
