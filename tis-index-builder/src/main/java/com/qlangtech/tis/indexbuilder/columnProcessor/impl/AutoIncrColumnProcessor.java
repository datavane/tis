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
import org.apache.solr.common.SolrInputDocument;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
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
