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
package com.qlangtech.tis.solrextend.fieldtype.st;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;

import java.util.Map;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
