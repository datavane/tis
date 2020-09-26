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

    public static void main(String[] args) {
    // System.out.println(WhitespaceTokenizer.replaceWhiteSpace("二维火 花卷"));
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
