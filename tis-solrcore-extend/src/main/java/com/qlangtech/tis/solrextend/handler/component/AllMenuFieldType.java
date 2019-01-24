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
package com.qlangtech.tis.solrextend.handler.component;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.StrField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * menu_id-name-kindmenu_id-num-fee-ratio_fee[;menu_id-name-kindmenu_id-num-fee-
 * ratio_fee] <br>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AllMenuFieldType extends StrField {

    public AllMenuFieldType() {
        super();
    }

    private static final Logger log = LoggerFactory.getLogger(AllMenuFieldType.class);

    // 取值的第几位 start with 0
    private int valIndex = -1;

    private static final String KEY_VAL_INDEX = "valIndex";

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        valIndex = Integer.parseInt(args.get(KEY_VAL_INDEX));
        log.info("typename:" + this.getTypeName() + ",valIndex:" + valIndex);
        args.remove(KEY_VAL_INDEX);
        super.init(schema, args);
    }

    @Override
    protected IndexableField createField(String name, String val, IndexableFieldType type) {
        if (valIndex < 0) {
            throw new IllegalStateException("valIndex can not be small than 0");
        }
        // 将menuid作为token存储
        final String[] instanceAry = StringUtils.split(val, ";");
//        type.setTokenized(true);
        Field f = new Field(name, val, type);
        f.setTokenStream(new TokenStream() {

            private final CharTermAttribute termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

            int index = 0;

            String[] tuple = null;

            @Override
            public boolean incrementToken() throws IOException {
                clearAttributes();
                if (index >= instanceAry.length) {
                    return false;
                }
                tuple = TripleValueMapReduceComponent.getAllMenuTuple(instanceAry[index++]);
                if (tuple != null && StringUtils.isNotBlank(tuple[valIndex])) {
                    termAtt.setEmpty().append(tuple[valIndex]);
                }
                return true;
            }
        });
        return f;
    }

    // public IndexableField createField(SchemaField field, Object value,
    // float boost) {
    // 
    // 
    // 
    // return super.createField(field, value, boost);
    // }
    @Override
    public boolean isPolyField() {
        return false;
    }
}
