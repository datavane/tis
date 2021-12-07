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
package com.qlangtech.tis.solrextend.fieldtype.s4menu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.StrField;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MenuSpecParserField extends StrField {

    public MenuSpecParserField() {
        super();
    }

    private static final Logger log = LoggerFactory.getLogger(MenuSpecParserField.class);

    public static final String KEY_LABEL_STRING = "label_info";

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
    }

    public static String getLables(String val) {
        JSONTokener tokener = new JSONTokener(val);
        JSONObject json = new JSONObject(tokener);
        if (json.isNull(KEY_LABEL_STRING)) {
            return null;
        }
        final String labelArrays = json.getString(KEY_LABEL_STRING);
        return labelArrays;
    }

    @Override
    protected IndexableField createField(String name, String val, org.apache.lucene.index.IndexableFieldType type) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        Field f = null;
        try {
            final String labelArrays = getLables(val);
            if (labelArrays == null) {
                f = new Field(name, StringUtils.EMPTY, type);
                return f;
            }
            final List<Integer> labsid = splitLabels(labelArrays);
            final int labelsCount = labsid.size();
            if (labelArrays == null || labelsCount < 1) {
                f = new Field(name, StringUtils.EMPTY, type);
                return f;
            }
            if (!type.tokenized()) {
                throw new IllegalStateException("type.tokenized shall be true");
            }
            JSONObject j = new JSONObject();
            j.put(KEY_LABEL_STRING, labelArrays);
            f = new Field(name, j.toString(), type);
            f.setTokenStream(new TokenStream() {

                private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

                int index = 0;

                @Override
                public boolean incrementToken() throws IOException {
                    clearAttributes();
                    if (index >= labelsCount) {
                        return false;
                    }
                    try {
                        termAtt.setEmpty().append(String.valueOf((labsid.get(index++))));
                    } catch (Throwable e) {
                    }
                    return true;
                }
            });
        } catch (Throwable e) {
            log.warn(val, e);
        }
        return f;
    }

    private static final Pattern LABEL_PATTERN = Pattern.compile("\\d+");

    public static List<Integer> splitLabels(final String labelArrays) /* "123;232,444" */
    {
        List<Integer> result = new ArrayList<>();
        Matcher matcher = LABEL_PATTERN.matcher(labelArrays);
        while (matcher.find()) {
            try {
                result.add(Integer.parseInt(matcher.group(0)));
            } catch (Throwable e) {
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<Integer> result = splitLabels("123;232,444");
        for (Integer i : result) {
            System.out.println(i);
        }
        System.out.println("================");
        result = splitLabels("123");
        for (Integer i : result) {
            System.out.println(i);
        }
    }

    @Override
    public boolean isPolyField() {
        return false;
    }
}
