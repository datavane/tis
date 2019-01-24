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
package com.qlangtech.tis.solrextend.fieldtype.s4card;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import com.qlangtech.tis.solrextend.fieldtype.pinyin.AllWithNGramTokenFilter;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AllWithPositionNGramTokenFilter extends AllWithNGramTokenFilter {

    private final OffsetAttribute offsetAttribute;

    public AllWithPositionNGramTokenFilter(TokenStream input, int minGram, int maxGram) {
        super(input, minGram, maxGram);
        this.offsetAttribute = this.addAttribute(OffsetAttribute.class);
    }

    @Override
    protected void appendAttribute(char[] curTermBuffer, int curTermLength, int start, int end) {
        this.offsetAttribute.setOffset(start, end);
    }
    // @Override
    // protected void appendAttribute(char[] curTermBuffer, int
    // curTermBufferLength, int start,
    // int end) {
    // 
    // // --113480,5,10
    // // --134,6,8
    // // --1348,6,9
    // // --13480,6,10
    // // --13480
    // 
    // // if (end + 1 > curTermBufferLength) {
    // // System.out.println("xx" + new String(curTermBuffer, start, end -
    // // start + 1) + ","
    // // + start + "," + end);
    // // return;
    // // }
    // //
    // this.offsetAtt.setOffset(start, end);
    // //
    // System.out.println(
    // "--" + new String(curTermBuffer, start, end - start + 1) + "," + start +
    // "," + end);
    // 
    // }
}
