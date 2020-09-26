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
