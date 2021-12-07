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
