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
