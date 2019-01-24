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
package com.qlangtech.tis.solrextend.fieldtype.pinyin;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import java.util.Map;

/*
 * 为了能够在N元分词时候，保留一个源词的最长分词作为查询条件
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AllWithNGramTokenFactory extends TokenFilterFactory {

    private static final int DEFAULT_MIN_NGRAM_SIZE = 1;

    private static final int DEFAULT_MAX_NGRAM_SIZE = 7;

    protected final int minGramSize;

    protected final int maxGramSize;

    public AllWithNGramTokenFactory(Map<String, String> args) {
        super(args);
        minGramSize = getInt(args, "minGramSize", DEFAULT_MIN_NGRAM_SIZE);
        maxGramSize = getInt(args, "maxGramSize", DEFAULT_MAX_NGRAM_SIZE);
    }

    @Override
    public TokenStream create(TokenStream input) {
        AllWithNGramTokenFilter tokenFilter = new AllWithNGramTokenFilter(input, minGramSize, maxGramSize);
        return tokenFilter;
    }
}
