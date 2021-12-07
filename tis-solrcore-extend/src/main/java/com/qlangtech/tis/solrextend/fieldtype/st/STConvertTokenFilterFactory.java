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

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * 繁体转简体
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年10月26日
 */
public class STConvertTokenFilterFactory extends TokenFilterFactory {

    private  String delimiter = ",";

    private String type = "t2s";

    private final Boolean keepBoth = false;

    private final STConvertType convertType = STConvertType.TRADITIONAL_2_SIMPLE;

    public STConvertTokenFilterFactory(Map<String, String> args) {
        super(args);
        delimiter = StringUtils.defaultIfBlank(args.remove("delimiter"), delimiter);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new STConvertTokenFilter(tokenStream, convertType, delimiter, keepBoth);
    }
}
