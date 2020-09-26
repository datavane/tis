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
package com.qlangtech.tis.solrextend.fieldtype.pinyin.test;

import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import com.qlangtech.tis.solrextend.utils.Assert;

/**
 * 用于分词器测试的一个简单工具类(用于打印分词情况，包括Term的起始位置和结束位置(即所谓的偏移量)，
 * 位置增量，Term字符串，Term字符串类型(字符串/阿拉伯数字之类的))
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AnalyzerUtils {

    public static void main(String[] args) throws IOException {
    // String text = "java <B>Hello</B> world";
    // Analyzer analyzer = new BoldAnalyzer();
    // displayTokens(analyzer, text);
    }

    public static void displayTokens(Analyzer analyzer, String text) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream("text", text);
        displayTokens(tokenStream);
    }

    public static void displayTokens(TokenStream tokenStream) throws IOException {
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);
        tokenStream.reset();
        int position = 0;
        while (tokenStream.incrementToken()) {
            int increment = positionIncrementAttribute.getPositionIncrement();
            if (increment > 0) {
                position = position + increment;
                System.out.print(position + ":");
            }
            int startOffset = offsetAttribute.startOffset();
            int endOffset = offsetAttribute.endOffset();
            String term = charTermAttribute.toString();
            System.out.println("[" + term + "]" + ":(" + startOffset + "-->" + endOffset + "):" + typeAttribute.type());
        }
    }

    /**
     * 断言分词结果
     *
     * @param analyzer
     * @param text
     *            源字符串
     * @param expecteds
     *            期望分词后结果
     * @throws IOException
     */
    public static void assertAnalyzerTo(Analyzer analyzer, String text, String[] expecteds) throws IOException {
        TokenStream tokenStream = analyzer.tokenStream("text", text);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        for (String expected : expecteds) {
            Assert.assertTrue(tokenStream.incrementToken());
            Assert.assertEquals(expected, charTermAttribute.toString());
        }
        Assert.assertFalse(tokenStream.incrementToken());
        tokenStream.close();
    }
}
