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
package com.qlangtech.tis.indexbuilder.columnProcessor.impl;

import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import org.apache.solr.common.SolrInputDocument;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
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
