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

/*
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
//        type.setTokenized(true);
        Field f = new Field(name, val, type);
        Tokenizer tokenizer = tokenizerFactory.create();
        tokenizer.setReader(new StringReader(val));
        f.setTokenStream(tokenizer);
        return f;
    }
}
