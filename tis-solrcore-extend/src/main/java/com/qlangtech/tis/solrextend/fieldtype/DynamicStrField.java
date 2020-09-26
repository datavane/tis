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
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DynamicStrField extends StrField {

    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
    }

    @Override
    public List<IndexableField> createFields(SchemaField sf, Object value) {
        List<IndexableField> result = new ArrayList<>();
        String textValue = String.valueOf(value);
        if (value == null || StringUtils.isBlank(textValue)) {
            return Collections.emptyList();
        }
        String[] fields = StringUtils.split(textValue, ";");
        String[] pair = null;
        SchemaField field = null;
        for (String f : fields) {
            pair = StringUtils.split(f, "_");
            if (pair.length < 3) {
                continue;
            }
            field = new SchemaField("m_" + pair[0], sf.getType(), OMIT_NORMS | OMIT_TF_POSITIONS | STORED, "");
            result.add(this.createField(field, pair[1] + "_" + pair[2]));
        }
        if ((sf.getProperties() & STORED) > 0) {
            result.add(this.createField(new SchemaField(sf.getName(), sf.getType(), INDEXED | OMIT_NORMS | OMIT_TF_POSITIONS | STORED, ""), value));
        }
        return result;
    }

    @Override
    public boolean isPolyField() {
        return true;
    }
}
