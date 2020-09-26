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
