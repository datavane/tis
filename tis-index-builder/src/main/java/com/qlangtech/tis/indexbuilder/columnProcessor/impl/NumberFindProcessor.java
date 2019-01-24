/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.indexbuilder.columnProcessor.impl;

import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.solr.common.SolrInputDocument;
import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;

/*
 * 字段中有多个数字，找到第一个数字
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class NumberFindProcessor extends AdapterExternalDataColumnProcessor {

    public static final String NAME = "numerFind";

    private static final Pattern PATTERN_NUMBER = Pattern.compile("(-?)\\d+");

    @Override
    public void process(SolrInputDocument doc, Entry<String, String> entry) {
        String val = entry.getValue();
        Matcher p = PATTERN_NUMBER.matcher(val);
        if (p.find()) {
            doc.setField(entry.getKey(), p.group());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    public static void main(String[] arg) {
        Matcher m = PATTERN_NUMBER.matcher("15868113480 15868113480");
        if (m.find()) {
            System.out.println(m.group());
        }
    }
}
