/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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

/* *
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
