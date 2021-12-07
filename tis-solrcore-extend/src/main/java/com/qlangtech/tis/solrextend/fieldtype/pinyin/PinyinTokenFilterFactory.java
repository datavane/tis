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
