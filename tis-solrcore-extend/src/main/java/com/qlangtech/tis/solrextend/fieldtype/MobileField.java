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
package com.qlangtech.tis.solrextend.fieldtype;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizerFactory;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.IndexableFieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.StrField;

/**
 * 电话号码字段EdgeNGram分詞
 * @time 2017年8月14日上午10:56:14
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MobileField extends StrField {

    private EdgeNGramTokenizerFactory tokenizerFactory;

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        Map<String, String> params = new HashMap<String, String>();
        params.put("minGramSize", "1");
        params.put("maxGramSize", "20");
        this.tokenizerFactory = new EdgeNGramTokenizerFactory(params);
    }

    @Override
    protected IndexableField createField(String name, String val, IndexableFieldType type) {
        // type.setTokenized(true);
        Field f = new Field(name, val, type);
        Tokenizer tokenizer = tokenizerFactory.create();
        tokenizer.setReader(new StringReader(val));
        f.setTokenStream(tokenizer);
        return f;
    }
}
