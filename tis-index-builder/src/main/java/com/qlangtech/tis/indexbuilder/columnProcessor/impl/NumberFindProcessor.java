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
