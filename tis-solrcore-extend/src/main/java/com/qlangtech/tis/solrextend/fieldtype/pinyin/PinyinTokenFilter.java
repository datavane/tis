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

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音过滤器[负责将汉字转换为拼音]
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PinyinTokenFilter extends TokenFilter {

    private final CharTermAttribute termAtt;

    /**
     * 汉语拼音输出转换器[基于Pinyin4j]
     */
    private static final HanyuPinyinOutputFormat outputFormat;

    private static final Logger logger = LoggerFactory.getLogger(PinyinTokenFilter.class);

    static {
        outputFormat = new HanyuPinyinOutputFormat();
        outputFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        outputFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    // logger.info("successful load pinyin4j");
    }

    /**
     * 对于多音字会有多个拼音,firstChar即表示只取第一个,否则会取多个拼音
     */
    // private boolean firstChar;
    /**
     * Term最小长度[小于这个最小长度的不进行拼音转换]
     */
    private int minTermLength;

    // private char[] curTermBuffer;
    // private int curTermLength;
    private String[] currTermsArg;

    public PinyinTokenFilter(TokenStream input) {
        this(input, Constant.DEFAULT_FIRST_CHAR, Constant.DEFAULT_MIN_TERM_LRNGTH);
    }

    public PinyinTokenFilter(TokenStream input, boolean firstChar) {
        this(input, firstChar, Constant.DEFAULT_MIN_TERM_LRNGTH);
    }

    public PinyinTokenFilter(TokenStream input, boolean firstChar, int minTermLenght) {
        this(input, firstChar, minTermLenght, Constant.DEFAULT_NGRAM_CHINESE);
    }

    public PinyinTokenFilter(TokenStream input, boolean firstChar, int minTermLenght, boolean outChinese) {
        super(input);
        this.termAtt = ((CharTermAttribute) addAttribute(CharTermAttribute.class));
        this.minTermLength = Constant.DEFAULT_MIN_TERM_LRNGTH;
        this.minTermLength = minTermLenght;
        if (this.minTermLength < 1) {
            this.minTermLength = 1;
        }
    }

    public static boolean containsChinese(String s) {
        if ((s == null) || ("".equals(s.trim())))
            return false;
        for (int i = 0; i < s.length(); i++) {
            if (isChinese(s.charAt(i)))
                return true;
        }
        return false;
    }

    public static boolean isChinese(char a) {
        int v = a;
        return (v >= 19968) && (v <= 171941);
    }

    public final boolean incrementToken() throws IOException {
        try {
            while (true) {
                if (this.currTermsArg == null) {
                    if (!this.input.incrementToken()) {
                        return false;
                    }
                    this.currTermsArg = getPinyinString(this.termAtt.toString());
                }
                String term = null;
                for (int i = 0; i < this.currTermsArg.length; i++) {
                    term = this.currTermsArg[i];
                    if (term != null && term.length() >= this.minTermLength) {
                        this.clearAttributes();
                        this.termAtt.copyBuffer(term.toCharArray(), 0, term.length());
                        this.currTermsArg[i] = null;
                        return true;
                    }
                }
                this.currTermsArg = null;
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            throw new IOException(e);
        }
    }

    public void reset() throws IOException {
        super.reset();
    }

    @SuppressWarnings("all")
    public static String[] getPinyinString(String chinese) throws BadHanyuPinyinOutputFormatCombination {
        String[] outTermAry = new String[3];
        if (containsChinese(chinese)) {
            StringBuilder sb = new StringBuilder();
            StringBuilder full = new StringBuilder();
            for (int i = 0; i < chinese.length(); i++) {
                String[] array = PinyinHelper.toHanyuPinyinStringArray(chinese.charAt(i), outputFormat);
                if ((array != null) && (array.length != 0)) {
                    String s = array[0];
                    char c = s.charAt(0);
                    sb.append(c);
                    full.append(s);
                }
            }
            // single pinyin
            outTermAry[0] = sb.toString();
            // full pinyin
            outTermAry[1] = full.toString();
        }
        // origin word
        outTermAry[2] = chinese;
        // return chineseTerm;
        return outTermAry;
    }
}
