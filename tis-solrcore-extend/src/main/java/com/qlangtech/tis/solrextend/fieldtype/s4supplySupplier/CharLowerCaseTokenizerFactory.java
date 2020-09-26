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
package com.qlangtech.tis.solrextend.fieldtype.s4supplySupplier;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import java.util.Map;

/**
 * 对把字符串里的字符变成小写，其他符号保持不变
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CharLowerCaseTokenizerFactory extends TokenizerFactory {

    public CharLowerCaseTokenizerFactory(Map<String, String> args) {
        super(args);
    }

    @Override
    public Tokenizer create(AttributeFactory factory) {
        return new CharLowerCaseTokenizer(factory);
    }

    private static class CharLowerCaseTokenizer extends CharTokenizer {

        CharLowerCaseTokenizer(AttributeFactory factory) {
            super(factory);
        }

        @Override
        protected boolean isTokenChar(int c) {
            return true;
        }
        // @Override
        // protected int normalize(int c) {
        // return Character.toLowerCase(c);
        // }
    }
}
