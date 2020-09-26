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
package com.qlangtech.tis.solrextend.fieldtype.s4supplygoods;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/**
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
