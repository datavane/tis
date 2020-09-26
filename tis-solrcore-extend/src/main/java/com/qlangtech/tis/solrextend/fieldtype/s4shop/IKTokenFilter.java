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
package com.qlangtech.tis.solrextend.fieldtype.s4shop;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.wltea.analyzer.lucene.IKTokenizer;

/**
 * 使用IK分词生成
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class IKTokenFilter extends TokenFilter {

    private IKTokenizer ikTokenizer;

    protected IKTokenFilter(TokenStream input) {
        super(input);
        this.ikTokenizer = new IKTokenizer(this.getAttributeFactory(), true);
    }

    @Override
    public boolean incrementToken() throws IOException {
        return ikTokenizer.incrementToken();
    }
}
