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
package com.qlangtech.tis.solrextend.fieldtype.s4product;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private static final Log logger = LogFactory.getLog(JsonParserField.class);

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
