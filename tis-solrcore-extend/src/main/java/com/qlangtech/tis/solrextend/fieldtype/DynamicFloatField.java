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
package com.qlangtech.tis.solrextend.fieldtype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import org.apache.solr.schema.TrieFloatField;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DynamicFloatField extends StrField {

    private static final Log log = LogFactory.getLog(DynamicFloatField.class);

    private static final List<IndexableField> NULL = Collections.emptyList();

    private TrieFloatField indexedField;

    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        indexedField = (TrieFloatField) schema.getFieldTypes().get("tfloat");
        if (indexedField == null) {
            StringBuffer typesDesc = new StringBuffer();
            for (Map.Entry<String, FieldType> entry : schema.getFieldTypes().entrySet()) {
                typesDesc.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
            }
            throw new IllegalStateException("can not get file type:tfloat in schema:" + typesDesc.toString());
        }
    }

    @Override
    public List<IndexableField> createFields(SchemaField sf, Object value) {
        String externalVal = null;
        if (value == null || StringUtils.isBlank(externalVal = String.valueOf(value))) {
            return NULL;
        }
        List<IndexableField> fields = new ArrayList<IndexableField>();
        SchemaField tfield = null;
        try {
            String[] multifloat = StringUtils.split(externalVal, ";");
            String[] pair = null;
            for (String p : multifloat) {
                pair = StringUtils.split(p, "_");
                if (pair.length < 2) {
                    continue;
                }
                tfield = new SchemaField("pt_" + pair[0], indexedField, INDEXED | OMIT_NORMS | OMIT_TF_POSITIONS | STORED, "");
                fields.add(indexedField.createField(tfield, getFloatValue(pair[1])));
            }
            if ((sf.getProperties() & STORED) > 0) {
                fields.add(super.createField(new SchemaField(sf.getName(), sf.getType(), INDEXED | OMIT_NORMS | OMIT_TF_POSITIONS | STORED, ""), value));
            }
            return fields;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private String getFloatValue(String value) {
        try {
            return String.format("%.2f", new Object[] { Double.valueOf(Float.parseFloat(value)) });
        } catch (NumberFormatException e) {
        }
        return "0";
    }

    public boolean isPolyField() {
        return true;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(String.format("%.2f", new Object[] { Double.valueOf(0.9999999999999999D) }));
    }
}
