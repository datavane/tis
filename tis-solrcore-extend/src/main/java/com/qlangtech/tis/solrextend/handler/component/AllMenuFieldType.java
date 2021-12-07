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

/**
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
        // type.setTokenized(true);
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
