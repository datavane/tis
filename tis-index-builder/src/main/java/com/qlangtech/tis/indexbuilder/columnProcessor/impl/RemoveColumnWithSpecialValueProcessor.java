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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;

/*
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
