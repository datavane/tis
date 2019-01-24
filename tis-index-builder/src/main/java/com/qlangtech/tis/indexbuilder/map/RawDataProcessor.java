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
package com.qlangtech.tis.indexbuilder.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.solr.common.SolrInputDocument;
import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import com.qlangtech.tis.indexbuilder.columnProcessor.ExternalDataColumnProcessor;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class RawDataProcessor {

    private final Map<String, ExternalDataColumnProcessor> /* columnName */
    externalColumnProcessors = new HashMap<>();

    private final List<ExternalDataColumnProcessor> rowProcess = new ArrayList<>();

    private static final ExternalDataColumnProcessor DEFAULT_COLUMN_PROCESSOR = new AdapterExternalDataColumnProcessor() {

        @Override
        public void process(SolrInputDocument doc, Entry<String, String> entry) {
            if (entry.getValue() != null) {
                doc.setField(entry.getKey(), entry.getValue());
            } else {
                doc.removeField(entry.getKey());
            }
        }

        @Override
        public String getName() {
            return "defaultColumnProcessor";
        }
    };

    public void addRowProcessor(ExternalDataColumnProcessor p) {
        this.rowProcess.add(p);
    }

    public void addColumnProcessor(String columnName, ExternalDataColumnProcessor p) {
        this.externalColumnProcessors.put(columnName, p);
    }

    public ExternalDataColumnProcessor getExternalColumnProcessors(String columnName) {
        ExternalDataColumnProcessor processor = externalColumnProcessors.get(columnName);
        if (processor != null) {
            return processor;
        } else {
            return DEFAULT_COLUMN_PROCESSOR;
        }
    }

    public List<ExternalDataColumnProcessor> getRowProcess() {
        return rowProcess;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("RawDataProcessor,col process:(");
        for (Map.Entry<String, ExternalDataColumnProcessor> /* columnName */
        e : externalColumnProcessors.entrySet()) {
            buffer.append(e.getValue().getName()).append("for col[").append(e.getKey()).append("]");
        }
        buffer.append("),row process:(");
        for (ExternalDataColumnProcessor p : rowProcess) {
            buffer.append(p.getName()).append(",");
        }
        buffer.append(")");
        return buffer.toString();
    }
}
