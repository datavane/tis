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
