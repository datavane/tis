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
package com.qlangtech.tis.solrextend.fieldtype.s4product;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.IndexableField;
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
public class JsonParserField extends StrField {

    private static final Logger logger = LoggerFactory.getLogger(JsonParserField.class);

    private IndexSchema schema;

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        this.schema = schema;
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        List<IndexableField> fields = new ArrayList<IndexableField>();
        String jsonStr = String.valueOf(value);
        // logger.info("the jsonStr is " + jsonStr);
        if (value == null || !StringUtils.startsWith(jsonStr, "{")) {
            return Collections.emptyList();
        }
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        for (String key : jsonObject.keySet()) {
            JSONArray jsonArray = jsonObject.getJSONArray(key);
            if (jsonArray.size() == 0)
                continue;
            JSONObject lang_value = jsonArray.getJSONObject(0);
            String field_name = key + "_" + lang_value.get("lang");
            String field_value = lang_value.getString("value");
            if ("null".equalsIgnoreCase(field_value))
                continue;
            fields.add(schema.getFieldOrNull(field_name).createField(field_value));
        }
        if (field.stored()) {
            fields.add(this.createField(field, jsonStr));
        }
        return fields;
    }
}
