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
package com.qlangtech.tis.solrextend.fieldtype.s4card;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import com.qlangtech.tis.solrextend.fieldtype.pinyin.AllWithNGramTokenFilter;

/**
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
