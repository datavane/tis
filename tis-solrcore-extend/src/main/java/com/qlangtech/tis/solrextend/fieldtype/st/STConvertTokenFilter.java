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
