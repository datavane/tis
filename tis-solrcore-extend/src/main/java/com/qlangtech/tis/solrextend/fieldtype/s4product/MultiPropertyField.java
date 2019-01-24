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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;

/*
 * size:23,color:蓝， 精确匹配
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MultiPropertyField extends org.apache.solr.schema.TextField {

    private static final String PROP_FIELD_PREFIX = "pp_";

    private IndexSchema schema;

    // private FieldType strType;
    // 匹配key:val的值对模式
    private static final Pattern PATTERN_TUPLE = Pattern.compile("([^,]+):([^,]+)");

    private static final Pattern PATTERN_TERM = Pattern.compile("[^\\u0060]+");

    public static void main(String[] args) {
        Matcher matcher = PATTERN_TERM.matcher("aaa`bbbb");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        this.schema = schema;
        this.setQueryAnalyzer(new WhitespaceAnalyzer());
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        List<IndexableField> fields = new ArrayList<>();
        final String val = String.valueOf(value);
        List<String> pps = new ArrayList<>();
        final Matcher matcher = PATTERN_TUPLE.matcher(val);
        while (matcher.find()) {
            fields.add(createPropField(matcher.group(1), matcher.group(2)));
            pps.add(matcher.group(2));
        }
        IndexableField origin = createPPField(field, val, pps);
        if (origin != null) {
            fields.add(origin);
        }
        return fields;
    }

    @Override
    public Query getFieldQuery(QParser parser, SchemaField field, String externalVal) {
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        Matcher matcher = PATTERN_TERM.matcher(externalVal);
        while (matcher.find()) {
            queryBuilder.add(new TermQuery(new Term(field.getName(), matcher.group())), Occur.MUST);
        }
        return queryBuilder.build();
    }

    private IndexableField createPPField(SchemaField field, String val, List<String> pps) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        final int length = pps.size();
        org.apache.lucene.document.FieldType newType = new org.apache.lucene.document.FieldType();
        newType.setTokenized(true);
        newType.setStored(field.stored());
        newType.setOmitNorms(true);
        newType.setIndexOptions(IndexOptions.DOCS);
        org.apache.lucene.document.Field f = new Field(field.getName(), val, newType);
//        f.setBoost(1f);
        f.setTokenStream(new TokenStream() {

            private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

            int index = 0;

            @Override
            public boolean incrementToken() throws IOException {
                clearAttributes();
                if (index >= length) {
                    return false;
                } else {
                    termAtt.setEmpty().append(pps.get(index++));
                    return true;
                }
            }
        });
        return f;
    }

    private IndexableField createPropField(String field, String value) {
        SchemaField f = schema.getField(PROP_FIELD_PREFIX + StringUtils.lowerCase(field));
        return f.createField(StringUtils.lowerCase(value));
    }

    @Override
    public boolean isPolyField() {
        return true;
    }
}
