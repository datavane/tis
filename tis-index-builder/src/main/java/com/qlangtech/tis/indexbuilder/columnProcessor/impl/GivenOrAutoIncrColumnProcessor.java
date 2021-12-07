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
