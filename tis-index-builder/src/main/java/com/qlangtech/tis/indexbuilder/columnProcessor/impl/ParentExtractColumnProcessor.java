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
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map.Entry;

/**
 * 从父记录中解析出子记录
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ParentExtractColumnProcessor extends AdapterExternalDataColumnProcessor {

    public static final Logger logger = LoggerFactory.getLogger(ParentExtractColumnProcessor.class);

    public static final String NAME = "ParentExtractColumnProcessor";

    private final String extraColumn;

    private final SkuColumn[] childColumns;

    private final int childColumnsLength;

    private final String type;

    public ParentExtractColumnProcessor(ProcessorSchemaField processorMap) {
        extraColumn = processorMap.getParam("column");
        if (StringUtils.isBlank(extraColumn)) {
            throw new IllegalArgumentException("param column can not be null");
        }
        String[] childColumns = StringUtils.split(processorMap.getParam("childColmnNames"), ",");
        if (childColumns.length < 1) {
            throw new IllegalArgumentException("childColmnNames can not be null");
        }
        childColumnsLength = childColumns.length;
        this.childColumns = new SkuColumn[childColumnsLength];
        for (int i = 0; i < childColumnsLength; i++) {
            this.childColumns[i] = SkuColumn.create(childColumns[i]);
        }
        type = processorMap.getParam("type");
        if (StringUtils.isBlank("type")) {
            throw new IllegalArgumentException("type can not be null");
        }
    }

    @Override
    public void process(SolrInputDocument doc, Entry<String, String> entry) {
        String raw = entry.getValue();
        if (StringUtils.isBlank(raw)) {
            return;
        }
        String[] lines = raw.split(";");
        SolrInputDocument child = null;
        String[] values = null;
        for (String line : lines) {
            try {
                child = new SolrInputDocument();
                values = line.split("_", childColumnsLength);
                if (values.length < childColumnsLength) {
                    logger.info("line: " + line + " should have " + childColumnsLength + " child columns, " + doc.toString());
                    continue;
                }
                for (int i = 0; i < childColumnsLength; i++) {
                    SkuColumn key = childColumns[i];
                    if (key.skip) {
                        // 该列是否要跳过
                        continue;
                    }
                    String value = values[i];
                    if (StringUtils.isEmpty(value)) {
                        continue;
                    }
                    child.addField(key.name, value);
                }
                child.addField("type", type);
                doc.addChildDocument(child);
            } catch (Exception e) {
                throw new IllegalStateException("line:" + line + "; childColumnsLength:" + childColumnsLength + "; extraColumn:" + extraColumn, e);
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static class SkuColumn {

        private final String name;

        // 这一列是否要加入到sku列中去
        private boolean skip = false;

        static SkuColumn create(String col) {
            if (StringUtils.isBlank(col)) {
                throw new IllegalStateException("para col can not be empty");
            }
            SkuColumn c = null;
            int sepIndex = StringUtils.indexOf(col, ":");
            if (sepIndex > -1) {
                String[] temp = StringUtils.split(col, ":");
                if (temp.length != 2) {
                    throw new IllegalStateException("col:" + col + "just can contain one ':'");
                }
                c = new SkuColumn(temp[0]);
                c.skip = "skip".equalsIgnoreCase(temp[1]);
            } else {
                c = new SkuColumn(col);
            }
            return c;
        }

        public SkuColumn(String name) {
            super();
            this.name = StringUtils.trim(name);
        }
    }
}
