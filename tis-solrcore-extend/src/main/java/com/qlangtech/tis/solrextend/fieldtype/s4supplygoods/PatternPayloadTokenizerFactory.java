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
package com.qlangtech.tis.solrextend.fieldtype.s4supplygoods;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/*
 * ddddd_dddd[,ddddd_dddd]<br>
 * 字段分前后两部分，'_'前的部分作为term , '_'后的部分作为payload
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PatternPayloadTokenizerFactory extends TokenizerFactory {

    // 标示是否要截取 term 的前幾位作為一個客查詢的
    // minGramSize;
    private int prefixTermLength;

    public PatternPayloadTokenizerFactory(Map<String, String> args) {
        super(args);
        this.prefixTermLength = Integer.parseInt(StringUtils.defaultIfEmpty(args.get("minGramSize"), "-1"));
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        PatternPayloadTokenizer tokenizer = new PatternPayloadTokenizer(factory);
        tokenizer.setPrefixTermLength(prefixTermLength);
        return tokenizer;
    }
}
