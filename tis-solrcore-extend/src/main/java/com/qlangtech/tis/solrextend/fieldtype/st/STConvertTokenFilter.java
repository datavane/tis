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

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.io.IOException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class STConvertTokenFilter extends TokenFilter {

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private String delimiter = ",";

    private STConvertType convertType = STConvertType.SIMPLE_2_TRADITIONAL;

    private Boolean keepBoth = false;

    public STConvertTokenFilter(TokenStream in, STConvertType convertType, String delimiter, Boolean keepBoth) {
        super(in);
        this.delimiter = delimiter;
        this.convertType = convertType;
        this.keepBoth = keepBoth;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();
        String str = termAtt.toString();
        termAtt.setEmpty();
        String converted = STConverter.getInstance().convert(str, convertType);
        if (!converted.isEmpty()) {
            stringBuilder.append(converted);
            if (keepBoth) {
                stringBuilder.append(delimiter);
                stringBuilder.append(str);
            }
        } else {
            stringBuilder.append(str);
        }
        // termAtt.setEmpty();
        termAtt.resizeBuffer(stringBuilder.length());
        termAtt.append(stringBuilder.toString());
        termAtt.setLength(stringBuilder.length());
        return true;
    }

    @Override
    public final void end() throws IOException {
        super.end();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
    }
}
