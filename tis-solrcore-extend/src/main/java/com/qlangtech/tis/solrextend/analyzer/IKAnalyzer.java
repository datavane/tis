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
