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
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * warehouseId_days 在parser阶段需要将warhouseid 切分成两个terms<br>
 * warehouseId 从第9位开始到最后一位
 * warehouseId 前8位也就是selfentityid
 * 另外每个term都要带上days作为payload参数
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class Prefix8SplitWithPayloadTokenizerFactory extends TokenizerFactory {

    public Prefix8SplitWithPayloadTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public Tokenizer create(AttributeFactory attributeFactory) {
        return new Prefix8SplitWithPayloadTokenizer(attributeFactory);
    }
}

class Prefix8SplitWithPayloadTokenizer extends Tokenizer {

    private final CharTermAttribute termAttr = (CharTermAttribute) addAttribute(CharTermAttribute.class);

    private final PayloadAttribute payloadAttr = (PayloadAttribute) addAttribute(PayloadAttribute.class);

    private static final Pattern KV_PAIR_LIST_PATTERN = Pattern.compile("(\\d{8})(\\w+)_(-?\\d+)");

    private PayloadTerm[] payloadTermArray;

    private int termsIndex = 0;

    private int termsNum = 0;

    Prefix8SplitWithPayloadTokenizer(AttributeFactory factory) {
        super(factory);
    }

    @Override
    public boolean incrementToken() throws IOException {
        this.clearAttributes();
        if (payloadTermArray == null) {
            Map<String, PayloadTerm> payloadTermMap = new HashMap<>();
            Matcher kvPairMatcher = KV_PAIR_LIST_PATTERN.matcher(IOUtils.toString(this.input));
            while (kvPairMatcher.find()) {
                assert (kvPairMatcher.groupCount() == 3);
                long payload = Long.parseLong(kvPairMatcher.group(3));
                addToMap(kvPairMatcher.group(1), payload, payloadTermMap);
                addToMap(kvPairMatcher.group(2), payload, payloadTermMap);
            }
            termsIndex = 0;
            termsNum = payloadTermMap.size();
            payloadTermArray = payloadTermMap.values().toArray(new PayloadTerm[termsNum]);
        }
        if (termsIndex >= termsNum) {
            termsIndex = -1;
            payloadTermArray = null;
            return false;
        }
        PayloadTerm term = payloadTermArray[termsIndex++];
        termAttr.setEmpty().append(term.getKey());
        payloadAttr.setPayload(longToBytesRef(term.getPayload()));
        return true;
    }

    // key有重复的，选择payload较大的那个
    private void addToMap(String key, long payload, Map<String, PayloadTerm> map) {
        if (!map.containsKey(key) || map.get(key).getPayload() < payload) {
            map.put(key, new PayloadTerm(key, payload));
        }
    }

    private static BytesRef longToBytesRef(long payload) {
        BytesRefBuilder builder = new BytesRefBuilder();
        // NumericUtils.longToPrefixCoded(payload, 0, builder);
        return builder.get();
    }

    private static final class PayloadTerm {

        private final String key;

        private final long payload;

        PayloadTerm(String key, long payload) {
            this.key = key;
            this.payload = payload;
        }

        String getKey() {
            return key;
        }

        long getPayload() {
            return payload;
        }
    }
}
