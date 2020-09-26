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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 当索引中有给定的值，则采用为主键，否则自增
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class GivenOrAutoIncrColumnProcessor extends AdapterExternalDataColumnProcessor {

    public static final String NAME = "given_or_auto_incr";

    private static final String DEST = "dest";

    private static final String GIVEN = "given";

    private String dest;

    private String given;

    private final AtomicInteger increase;

    public GivenOrAutoIncrColumnProcessor(ProcessorSchemaField processorMap) {
        dest = processorMap.getParam(DEST);
        given = processorMap.getParam(GIVEN);
        if (dest == null || given == null) {
            throw new IllegalStateException("dest or given cannot be null in GivenOrAutoIncrColumnProcessor in schema");
        }
        this.increase = new AtomicInteger();
    }

    @Override
    public void process(SolrInputDocument doc, Map<String, String> entry) {
        // Object field = doc.getFieldValue(given);
        String givenFieldValue = entry.get(given);
        String dataType = entry.get("data_type");
        // if(field != null ){
        // givenFieldValue = ((IndexableField)field).stringValue();
        // }
        doc.addField(dest, (StringUtils.equals(dataType, "0") || givenFieldValue == null) ? this.increase.getAndIncrement() : givenFieldValue);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
