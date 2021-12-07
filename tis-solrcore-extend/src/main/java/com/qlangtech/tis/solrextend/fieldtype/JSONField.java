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
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.google.common.collect.Sets;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JSONField extends StrField {

    private String propPrefix;

    private Set<String> filterKeys;

    // private static final Logger logger =
    // LoggerFactory.getLogger(JSONField.class);
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        this.propPrefix = args.remove("prefix");
        // if (StringUtils.isEmpty(propPrefix)) {
        // throw new IllegalArgumentException("param prefix can not be null");
        // }
        final String fk = args.remove("filterKey");
        if (StringUtils.isNotBlank(fk)) {
            this.filterKeys = Sets.newHashSet(StringUtils.split(fk, ","));
        }
    }

    @Override
    protected void setArgs(IndexSchema schema, Map<String, String> args) {
        super.setArgs(schema, args);
    }

    @Override
    public List<IndexableField> createFields(SchemaField sf, Object value) {
        List<IndexableField> result = new ArrayList<>();
        String textValue = String.valueOf(value);
        if (value == null || !StringUtils.startsWith(textValue, "{")) {
            return Collections.emptyList();
        }
        JSONTokener tokener = new JSONTokener(textValue);
        JSONObject json = null;
        if (filterKeys != null && !this.filterKeys.isEmpty()) {
            json = new JSONObject(tokener, this.filterKeys.toArray(new String[] {}));
        } else {
            json = new JSONObject(tokener);
        }
        if ((sf.getProperties() & STORED) > 0) {
            result.add(this.createField(new SchemaField(sf.getName(), sf.getType(), OMIT_NORMS | OMIT_TF_POSITIONS | STORED, ""), json.toString()));
        }
        if (StringUtils.isNotEmpty(this.propPrefix)) {
            SchemaField field = null;
            String fieldValue = null;
            for (String key : json.keySet()) {
                field = new SchemaField(propPrefix + key, sf.getType(), OMIT_NORMS | OMIT_TF_POSITIONS | STORED | INDEXED, "");
                fieldValue = json.getString(key);
                if ("null".equalsIgnoreCase(fieldValue) || (filterKeys != null && !this.filterKeys.contains(key))) {
                    continue;
                }
                result.add(this.createField(field, fieldValue));
            }
        }
        return result;
    }

    @Override
    public boolean isPolyField() {
        return true;
    }
}
