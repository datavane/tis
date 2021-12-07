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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;

/**
 * 针对店铺的多分枝机构，需要将各个分枝机构的entitiyid分词可以让查询搜索到<br>
 * 另外要对倒数第二个分枝结构的名称保存其的docvalue值，这样可以排序
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TuplesField extends org.apache.solr.schema.TextField {

    private static final Pattern PATTERN_TUPLES = Pattern.compile("([^\004]+)\005([^\004]+)");

    private static final String NULL = "-1";

    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        List<IndexableField> fields = new ArrayList<>();
        String v = String.valueOf(value);
        int size = 0;
        if (NULL.equals(v)) {
            v = "-1\0050";
            fields.add(new SortedSetDocValuesField(field.getName(), new BytesRef(v)));
        } else {
            Matcher matcher = PATTERN_TUPLES.matcher(v);
            // final List<String> names = new ArrayList<String>();
            while (matcher.find()) {
                size++;
            }
            fields.add(new SortedSetDocValuesField(field.getName(), new BytesRef(v)));
        }
        fields.add(createField(field, v));
        this.createTermsCountField(fields, size);
        return fields;
    }

    protected void createTermsCountField(List<IndexableField> fields, int size) {
    }

    @Override
    public void checkSchemaField(SchemaField field) {
    // super.checkSchemaField(field);
    }

    @Override
    protected IndexableField createField(String name, String val, IndexableFieldType type) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        Field f = new Field(name, val, type);
        Matcher matcher = PATTERN_TUPLES.matcher(val);
        final List<String> keys = new ArrayList<String>(4);
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        int keysSize = keys.size();
        if (keysSize > 0) {
            f.setTokenStream(new TokenStream() {

                private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

                private final PositionIncrementAttribute postIncr = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);

                int index = 0;

                @Override
                public boolean incrementToken() throws IOException {
                    clearAttributes();
                    if (index >= keys.size()) {
                        return false;
                    } else {
                        postIncr.setPositionIncrement(1);
                        termAtt.setEmpty().append(keys.get(index++));
                        return true;
                    }
                }
            });
        // terms越多，排序越在下面
        // f.setBoost((1 / keysSize) * 100);
        }
        return f;
    }

    @Override
    public boolean isPolyField() {
        return true;
    }
}
