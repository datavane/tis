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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import java.io.IOException;
import java.util.Map;

/**
 *去掉字符中间的空格
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class WhitespaceRemoveTokenizerFactory extends TokenizerFactory {

    public WhitespaceRemoveTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        return new WhitespaceTokenizer(factory);
    }



    private static class WhitespaceTokenizer extends Tokenizer {

        private static final String WHITESPACE = " ";

        private boolean done = false;

        private final CharTermAttribute termAttr = (CharTermAttribute) addAttribute(CharTermAttribute.class);

        WhitespaceTokenizer(AttributeFactory factory) {
            super(factory);
        }

        @Override
        public boolean incrementToken() throws IOException {
            if (done) {
                return false;
            }
            this.clearAttributes();
            termAttr.setEmpty().append(replaceWhiteSpace(IOUtils.toString(this.input)));
            return done = true;
        }

        private String replaceWhiteSpace(String input) {
            if (StringUtils.indexOf(input, WHITESPACE) <= -1) {
                return input;
            } else {
                return StringUtils.replaceChars(input, WHITESPACE, StringUtils.EMPTY);
            }
        }

        @Override
        public void reset() throws IOException {
            super.reset();
            done = false;
        }
    }
}
