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
package com.qlangtech.tis.solrextend.fieldtype;

import junit.framework.TestCase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.StringReader;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestBitwiseField extends TestCase {

    public void test() throws Exception {
        final WhitespaceAnalyzer spaceAnalyzer = new WhitespaceAnalyzer();
        int i = 0;
        while (true) {
            TokenStream ts = spaceAnalyzer.tokenStream("testFiled", new StringReader("hello" + (i++) + " body" + (i++)));
            ts.reset();
            CharTermAttribute attr = null;
            while (ts.incrementToken()) {
                attr = ts.getAttribute(CharTermAttribute.class);
                System.out.println(attr);
            }
            ts.reset();
        }
    }
}
