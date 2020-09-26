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
