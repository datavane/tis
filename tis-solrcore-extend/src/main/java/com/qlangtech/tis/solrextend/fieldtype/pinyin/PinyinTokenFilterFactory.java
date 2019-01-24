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

import java.util.Map;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/*
 * PinyinTokenFilter工厂类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PinyinTokenFilterFactory extends TokenFilterFactory {

    private boolean firstChar;

    private boolean outChinese;

    private int minTermLenght;

    public PinyinTokenFilterFactory(Map<String, String> args) {
        super(args);
        this.firstChar = getBoolean(args, "firstChar", Constant.DEFAULT_FIRST_CHAR);
        this.outChinese = getBoolean(args, "outChinese", Constant.DEFAULT_OUT_CHINESE);
        this.minTermLenght = getInt(args, "minTermLength", Constant.DEFAULT_MIN_TERM_LRNGTH);
    }

    public TokenFilter create(TokenStream input) {
        // throw new IllegalStateException("create");
        return new PinyinTokenFilter(input, this.firstChar, this.minTermLenght, this.outChinese);
    }

    public boolean isFirstChar() {
        return firstChar;
    }

    public void setFirstChar(boolean firstChar) {
        this.firstChar = firstChar;
    }

    public boolean isOutChinese() {
        return outChinese;
    }

    public void setOutChinese(boolean outChinese) {
        this.outChinese = outChinese;
    }

    public int getMinTermLenght() {
        return minTermLenght;
    }

    public void setMinTermLenght(int minTermLenght) {
        this.minTermLenght = minTermLenght;
    }
}
