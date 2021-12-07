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
import java.util.Map;

/**
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
