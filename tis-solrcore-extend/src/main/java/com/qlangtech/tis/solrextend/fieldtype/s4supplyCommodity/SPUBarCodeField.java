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
package com.qlangtech.tis.solrextend.fieldtype.s4supplyCommodity;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TextField;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SPUBarCodeField extends TextField {

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
    }

    @Override
    public void checkSchemaField(SchemaField field) {
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return Collections.<IndexableField>emptyList();
        }
        List<IndexableField> fields = new LinkedList<>();
        fields.add(createField(field, value));
        if (field.hasDocValues()) {
            BytesRef bytes = new BytesRef(value.toString());
            fields.add(new SortedDocValuesField(field.getName(), bytes));
        }
        return fields;
    }

    @Override
    protected IndexableField createField(String name, String val, IndexableFieldType type) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        org.apache.lucene.document.Field f = new Field(name, val, type);
        List<String> categories = getCategories(val);
        int length = categories.size();
        f.setTokenStream(new TokenStream() {

            private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

            int index = 0;

            @Override
            public boolean incrementToken() throws IOException {
                clearAttributes();
                if (index >= length) {
                    return false;
                } else {
                    termAtt.setEmpty().append(categories.get(index++));
                    return true;
                }
            }
        });
        return f;
    }

    @Override
    public boolean isPolyField() {
        return true;
    }

    private List<String> getCategories(String raw) {
        List<String> categories = new LinkedList<>();
        int pos = 2;
        while (raw.length() >= pos) {
            categories.add(raw.substring(0, pos));
            pos += 2;
        }
        return categories;
    }
}
