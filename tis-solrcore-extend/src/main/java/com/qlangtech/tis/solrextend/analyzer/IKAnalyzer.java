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
package com.qlangtech.tis.solrextend.analyzer;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.wltea.analyzer.lucene.IKTokenizer;

/**
 * IK 分词
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IKAnalyzer extends Analyzer {

    public boolean useSmart() {
        // 多生成一些term
        return false;
    }

    public IKAnalyzer() {
    }

    @Override
    protected Analyzer.TokenStreamComponents createComponents(String text) {
        Reader reader = new BufferedReader(new StringReader(text));
        Tokenizer _IKTokenizer = new IKTokenizer(reader, useSmart());
        return new Analyzer.TokenStreamComponents(_IKTokenizer);
    }
}
