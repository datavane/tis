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
package com.qlangtech.tis.solrextend.fieldtype.st;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class STConvertTokenizerFactory extends TokenizerFactory {

    private final STConvertType convertType = STConvertType.TRADITIONAL_2_SIMPLE;

    private String delimiter = ",";

    private final Boolean keepBoth = false;

    public STConvertTokenizerFactory(Map<String, String> args) {
        super(args);
        delimiter = StringUtils.defaultIfBlank(args.remove("delimiter"), ",");
    // String keepBothStr = settings.get("keep_both", "false");
    // if(keepBothStr.equals("true")) {
    // 
    // }
    // keepBoth = true;
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        // 
        STConvertTokenizer tokenizer = new STConvertTokenizer(factory, convertType, delimiter, keepBoth);
        return tokenizer;
    }
    // @Override
    // public TokenStream create(TokenStream input) {
    // 
    // STConvertTokenizer tokenizer = new STConvertTokenizer(convertType,
    // delimiter,keepBoth);
    // 
    // return tokenizer;
    // }
    // @Override
    // public Tokenizer create() {
    // STConvertType convertType= STConvertType.SIMPLE_2_TRADITIONAL;
    // if(type.equals("t2s")){
    // convertType = STConvertType.TRADITIONAL_2_SIMPLE;
    // }
    // 
    // return new STConvertTokenizer(convertType, delimiter,keepBoth);
    // }
}
