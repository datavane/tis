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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.solr.common.SolrInputDocument;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import java.util.Map;

/*
 * ParentExtractColumnProcessor Tester.
 * @version 1.0
 * @since <pre>03/10/2017</pre>
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ParentExtractColumnProcessorTest extends TestCase {

    public ParentExtractColumnProcessorTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ParentExtractColumnProcessorTest.class);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Method: main(String[] args)
     */
    public void testMain() throws Exception {
        ProcessorSchemaField processorSchema = ProcessorSchemaField.create("ParentExtractColumnProcessor", "childColmnNames=id,sku_goods_id,sku_standard_goods_id,sku_standard_category_id,sku_standard_inner_code,sku_sort_code,sku_spec_name,sku_package_unit,sku_package_num,sku_order_min_num,sku_order_max_num,sku_status,sku_create_time,sku_bar_code,sku_is_valid,sku_last_ver column=sku_string type=c");
        ParentExtractColumnProcessor processor = new ParentExtractColumnProcessor(processorSchema);
        SolrInputDocument doc = new SolrInputDocument();
        Map.Entry<String, String> entry = new Map.Entry<String, String>() {

            @Override
            public String getKey() {
                return null;
            }

            @Override
            public String getValue() {
                return "000873615ab1b5d4015ab1db282a0005_000873615a894c9c015a8ce7646f0013____0___1_2_-1_1_20170309145654314_2000000000299_1_0";
            }

            @Override
            public String setValue(String value) {
                return null;
            }
        };
        processor.process(doc, entry);
        int i = 1;
    }
}
