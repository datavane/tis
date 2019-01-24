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
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;

/* *
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
