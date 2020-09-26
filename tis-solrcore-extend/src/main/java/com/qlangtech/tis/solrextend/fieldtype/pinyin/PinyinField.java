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
package com.qlangtech.tis.solrextend.fieldtype.pinyin;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TextField;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PinyinField extends TextField {

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        List<IndexableField> fields = new ArrayList<>();
        IndexableField f = createField(field, value);
        fields.add(f);
        if (field.hasDocValues()) {
            BytesRef bytes = new BytesRef(value.toString());
            fields.add(new SortedDocValuesField(field.getName(), bytes));
        }
        return fields;
    }

    @Override
    public boolean isPolyField() {
        return true;
    }

    @Override
    public void checkSchemaField(SchemaField field) {
    // do nothing
    // support docValues
    }
}
