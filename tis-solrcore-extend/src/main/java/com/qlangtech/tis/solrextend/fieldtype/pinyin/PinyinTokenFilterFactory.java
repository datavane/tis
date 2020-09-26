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

import java.util.Map;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

/**
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
