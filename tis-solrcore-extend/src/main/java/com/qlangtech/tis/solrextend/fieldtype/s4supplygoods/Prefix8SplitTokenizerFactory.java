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

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Created by Qinjiu(Qinjiu@2dfire.com) on 2017/1/17.
 * <p>
 * <p>
 * <p>
 * <p>
 * 原始值为warehouseid_lastVer;[warehouseid_lastVer]
 * paser取内容中的warehouseid作为term
 * warehouseId 从第9位开始到最后一位
 * warehouseId 前8位也就是selfentityid
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Prefix8SplitTokenizerFactory extends TokenizerFactory {

    public Prefix8SplitTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public Tokenizer create(AttributeFactory attributeFactory) {
        return new Prefix8SplitTokenizer(attributeFactory);
    }
}

class Prefix8SplitTokenizer extends Tokenizer {

    private final CharTermAttribute termAttr = (CharTermAttribute) addAttribute(CharTermAttribute.class);

    String[] stockInfoArray;

    private int termsIndex = 0;

    private int termsNum = 0;

    static final Pattern KV_PAIR_LIST_PATTERN = Pattern.compile("(\\d{8})(\\w+)_(\\d+)");

    Prefix8SplitTokenizer(AttributeFactory factory) {
        super(factory);
    }

    @Override
    public boolean incrementToken() throws IOException {
        this.clearAttributes();
        if (stockInfoArray == null) {
            Set<String> stockInfoTermSet = new HashSet<>();
            Matcher kvPairMatcher = KV_PAIR_LIST_PATTERN.matcher(IOUtils.toString(this.input));
            while (kvPairMatcher.find()) {
                assert (kvPairMatcher.groupCount() == 3);
                stockInfoTermSet.add(kvPairMatcher.group(1));
                stockInfoTermSet.add(kvPairMatcher.group(2));
            }
            termsIndex = 0;
            termsNum = stockInfoTermSet.size();
            stockInfoArray = stockInfoTermSet.toArray(new String[termsNum]);
        }
        if (termsIndex >= termsNum) {
            termsIndex = -1;
            termsNum = 0;
            stockInfoArray = null;
            return false;
        }
        termAttr.setEmpty().append(stockInfoArray[termsIndex++]);
        return true;
    }
}
