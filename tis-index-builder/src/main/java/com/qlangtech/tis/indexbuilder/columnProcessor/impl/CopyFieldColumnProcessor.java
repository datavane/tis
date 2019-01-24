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
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;

/*
 * Created by Qinjiu(Qinjiu@2dfire.com) on 2/22/2017.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class CopyFieldColumnProcessor extends AdapterExternalDataColumnProcessor {

    private final String dest;

    private final String origin;

    public static final Logger logger = LoggerFactory.getLogger(CopyFieldColumnProcessor.class);

    public static final String NAME = "CopyFieldColumnProcessor";

    private static final String DEST = "dest";

    private static final String ORIGIN = "column";

    public CopyFieldColumnProcessor(ProcessorSchemaField processorMap) {
        origin = processorMap.getParam(ORIGIN);
        if (origin == null) {
            throw new IllegalStateException("origin cannot be null in CopyFieldColumnProcessor in schema");
        }
        dest = processorMap.getParam(DEST);
        if (dest == null) {
            throw new IllegalStateException("dest cannot be null in CopyFieldColumnProcessor in schema");
        }
    }

    @Override
    public void process(SolrInputDocument doc, Map.Entry<String, String> entry) {
        String raw = entry.getValue();
        if (StringUtils.isBlank(raw)) {
            return;
        }
        doc.addField(origin, entry.getValue());
        doc.addField(dest, entry.getValue());
    }

    @Override
    public String getName() {
        return NAME;
    }
}
