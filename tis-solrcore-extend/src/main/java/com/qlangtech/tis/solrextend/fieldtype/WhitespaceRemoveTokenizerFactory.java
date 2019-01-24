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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import java.io.IOException;
import java.util.Map;

/*
 * Created by Qinjiu(Qinjiu@2dfire.com) on 2/17/2017. 去掉字符中间的空格
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
