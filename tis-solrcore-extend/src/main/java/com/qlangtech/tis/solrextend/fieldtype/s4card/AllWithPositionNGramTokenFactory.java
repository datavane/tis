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
