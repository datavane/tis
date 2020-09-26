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
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 数据中心中有额外大量的空值使用了-1这种值，在构建全量的时候需要将这种值的Key去掉
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RemoveColumnWithSpecialValueProcessor extends AdapterExternalDataColumnProcessor {

    public static final String NAME = "remove_column_with_special_value";

    private String testValue;

    public RemoveColumnWithSpecialValueProcessor(ProcessorSchemaField processorMap) {
        super();
        this.testValue = processorMap.getParam("testValue");
        if (StringUtils.isBlank(this.testValue)) {
            throw new IllegalArgumentException("param testValue can not be null");
        }
    }

    public void process(SolrInputDocument doc, Map<String, String> val) {
        Iterator<Map.Entry<String, String>> it = val.entrySet().iterator();
        Map.Entry<String, String> next = null;
        while (it.hasNext()) {
            next = it.next();
            if (testValue.equals(next.getValue())) {
                it.remove();
            }
        }
    }

    public static void main(String[] args) {
        Map<String, String> test = new HashMap<String, String>();
        test.put("userName", "baisui");
        test.put("age", "-1");
        RemoveColumnWithSpecialValueProcessor p = new RemoveColumnWithSpecialValueProcessor(ProcessorSchemaField.create(NAME, "testValue=-2"));
        p.process(null, test);
        for (Map.Entry<String, String> e : test.entrySet()) {
            System.out.println(e.getKey() + ":" + e.getValue());
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
