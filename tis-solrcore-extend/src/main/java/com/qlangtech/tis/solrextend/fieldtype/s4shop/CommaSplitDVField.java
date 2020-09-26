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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CommaSplitDVField extends StrField {

    private static final Log logger = LogFactory.getLog(CommaSplitDVField.class);

    private IndexSchema schema;

    private String SEPARATOR = ",";

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        this.schema = schema;
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        List<IndexableField> fields = new ArrayList<IndexableField>();
        String val = String.valueOf(value);
        String[] valArr = StringUtils.split(val, SEPARATOR);
        if (valArr == null || valArr.length == 0) {
            return Collections.emptyList();
        }
        for (String v : valArr) {
            fields.add(this.createField(field, v));
            fields.add(new SortedSetDocValuesField(field.getName(), new BytesRef(v)));
        }
        return fields;
    }

    @Override
    public boolean isPolyField() {
        return true;
    }
}
