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
package com.qlangtech.tis.solrextend.fieldtype.s4personas;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.StringUtils;
import org.apache.solr.schema.TextField;

/**
 * store的值不变，被索引的值根据field字段空或者不空来设置f或者t
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TextHasValIndexField extends TextField {

    @Override
    protected IndexableField createField(String name, String val, org.apache.lucene.index.IndexableFieldType type) {
        // @Override
        // protected IndexableField createField(String name, final String val,
        // org.apache.lucene.document.FieldType type) {
        Field f = new Field(name, val, type);
        // f.setBoost(boost);
        AtomicBoolean incrable = new AtomicBoolean(true);
        f.setTokenStream(new TokenStream() {

            private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

            @Override
            public boolean incrementToken() throws IOException {
                this.clearAttributes();
                termAtt.setEmpty().append(StringUtils.isEmpty(val) ? 'f' : 't');
                return incrable.getAndSet(false);
            }
        });
        return f;
    }
}
