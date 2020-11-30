/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.solrextend.fieldtype;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认字段格式为 “tag1_priority2,tag2_priority2[,tag_priority]" 这里的权重系数 priority取值需要在[0-1]之间
 * 实现：http://tis.pub/blog/tag-match-sort/
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TagPayloadTokenizer extends Tokenizer {

    public TagPayloadTokenizer(AttributeFactory factory) {
        super(factory);
    }

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private final PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);

    public static final Pattern KV_PAIR_LIST_PATTERN = Pattern.compile("([^,]+?)_(.+?)");


    private List<PayLoadTerm> payloadTerms;

    private int termsIndex;

    private int termsLength;

    @Override
    public final boolean incrementToken() throws IOException {
        this.clearAttributes();
        PayLoadTerm t = null;

        if (payloadTerms == null) {
            payloadTerms = new ArrayList<>();
            Matcher kvPairMatcher = KV_PAIR_LIST_PATTERN.matcher(IOUtils.toString(this.input));
            int start = 0;
            float payload;
            while (kvPairMatcher.find()) {
                payload = getPayload(kvPairMatcher);
                byte[] bytes = new byte[4];
                // 将float序列化成bytes

                NumericUtils.intToSortableBytes(NumericUtils.floatToSortableInt(payload), bytes, 0);
                // builder.copyBytes(,0,4);   //   NumericUtils.floatToSortableInt(payload > 0 ? payload : 0f, 0, builder);
                t = new PayLoadTerm(kvPairMatcher.group(1), new BytesRef(bytes));
                payloadTerms.add(t);

                start = start + kvPairMatcher.group(0).length() + 1;
            }
            termsIndex = 0;
            termsLength = payloadTerms.size();
        }

        if (termsIndex >= termsLength) {
            termsIndex = -1;
            payloadTerms = null;
            return false;
        }
        t = payloadTerms.get(termsIndex++);
        termAtt.setEmpty().append(t.key);

        payloadAtt.setPayload(t.payload);
        return true;
    }

    public static float decode(BytesRef bytes) {
        return NumericUtils.sortableIntToFloat( //
                NumericUtils.sortableBytesToInt(bytes.bytes, bytes.offset));
    }


    private static float getPayload(Matcher kvPairMatcher) {
        try {
            return Float.parseFloat(kvPairMatcher.group(2));
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class PayLoadTerm {

        private final String key;

        private final BytesRef payload;

        public PayLoadTerm(String key, BytesRef payload) {
            super();
            this.key = key;
            this.payload = payload;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println((float) Math.log(8423085591l));
        System.out.println((float) Math.log(8423085592l));
    }
}
