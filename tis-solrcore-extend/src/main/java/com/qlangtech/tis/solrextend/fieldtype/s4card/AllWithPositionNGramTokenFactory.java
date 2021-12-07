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

import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import com.qlangtech.tis.solrextend.fieldtype.pinyin.AllWithNGramTokenFactory;

/**
 * 帶有位置信息的term，目前在s4card中的手机号码查询中使用
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AllWithPositionNGramTokenFactory extends AllWithNGramTokenFactory {

    public AllWithPositionNGramTokenFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public TokenStream create(TokenStream input) {
        return new AllWithPositionNGramTokenFilter(input, this.minGramSize, this.maxGramSize);
    }
}
