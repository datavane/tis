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
