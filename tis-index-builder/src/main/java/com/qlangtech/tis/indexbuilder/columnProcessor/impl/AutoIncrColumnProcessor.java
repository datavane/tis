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

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.solr.common.SolrInputDocument;
import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;

/*
 * 索引中无法提供唯一主键，在构建索引的时候自动创建一个自增主键
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class AutoIncrColumnProcessor extends AdapterExternalDataColumnProcessor {

    public static final String NAME = "auto_incr";

    private static final String DEST = "dest";

    private final String dest;

    private final AtomicInteger increase;

    public AutoIncrColumnProcessor(ProcessorSchemaField processorMap) {
        dest = processorMap.getParam(DEST);
        if (dest == null) {
            throw new IllegalStateException("dest cannot be null in AutoIncrColumnProcessor in schema");
        }
        this.increase = new AtomicInteger();
    }

    @Override
    public void process(SolrInputDocument doc, Map<String, String> entry) {
        doc.addField(dest, this.increase.getAndIncrement());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
