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
package com.qlangtech.tis.indexbuilder.map;

import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import com.qlangtech.tis.indexbuilder.columnProcessor.ExternalDataColumnProcessor;
import org.apache.solr.common.SolrInputDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
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
        for (Entry<String, ExternalDataColumnProcessor> /* columnName */
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
