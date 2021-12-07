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
