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

/* *
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
