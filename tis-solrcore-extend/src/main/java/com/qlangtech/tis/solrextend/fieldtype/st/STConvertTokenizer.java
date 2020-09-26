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

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;
import java.io.IOException;
import java.io.Reader;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class STConvertTokenizer extends Tokenizer {

    private static final int DEFAULT_BUFFER_SIZE = 256;

    private boolean done = false;

    private int finalOffset;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private String delimiter;

    private STConvertType convertType;

    private Boolean keepBoth;

    public STConvertTokenizer(AttributeFactory factory, STConvertType convertType, String delimiter, Boolean keepBoth) {
        this(DEFAULT_BUFFER_SIZE, factory);
        this.delimiter = delimiter;
        this.convertType = convertType;
        this.keepBoth = keepBoth;
    }

    public STConvertTokenizer(int bufferSize, AttributeFactory factory) {
        super(factory);
        termAtt.resizeBuffer(bufferSize);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!done) {
            clearAttributes();
            done = true;
            int upto = 0;
            char[] buffer = termAtt.buffer();
            while (true) {
                final int length = input.read(buffer, upto, buffer.length - upto);
                if (length == -1)
                    break;
                upto += length;
                if (upto == buffer.length)
                    buffer = termAtt.resizeBuffer(1 + buffer.length);
            }
            termAtt.setLength(upto);
            String str = termAtt.toString();
            termAtt.setEmpty();
            String converted = STConverter.getInstance().convert(str, convertType);
            termAtt.append(converted);
            if (keepBoth) {
                termAtt.append(delimiter);
                termAtt.append(str);
            }
            finalOffset = correctOffset(upto);
            offsetAtt.setOffset(correctOffset(0), finalOffset);
            return true;
        }
        return false;
    }

    @Override
    public final void end() {
        // set final offset
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    public void reset(Reader input) throws IOException {
        super.reset();
        this.done = false;
    }

    public void reset() throws IOException {
        super.reset();
        this.done = false;
    }
}
