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
package com.qlangtech.tis.solrextend.fieldtype.pinyin;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;
import java.util.Map;

/**
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
