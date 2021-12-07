/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
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
