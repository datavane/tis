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
package com.qlangtech.tis.solrextend.fieldtype.s4supplySupplier;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import java.util.Map;

/**
 * 对把字符串里的字符变成小写，其他符号保持不变
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CharLowerCaseTokenizerFactory extends TokenizerFactory {

    public CharLowerCaseTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        return new CharLowerCaseTokenizer(factory);
    }

    private static class CharLowerCaseTokenizer extends CharTokenizer {

        CharLowerCaseTokenizer(AttributeFactory factory) {
            super(factory);
        }

        @Override
        protected boolean isTokenChar(int c) {
            return true;
        }
        // @Override
        // protected int normalize(int c) {
        // return Character.toLowerCase(c);
        // }
    }
}
