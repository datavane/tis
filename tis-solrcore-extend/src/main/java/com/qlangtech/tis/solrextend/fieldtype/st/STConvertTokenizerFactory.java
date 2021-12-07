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
