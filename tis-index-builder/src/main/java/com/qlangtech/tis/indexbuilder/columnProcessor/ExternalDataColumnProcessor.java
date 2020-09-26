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
package com.qlangtech.tis.indexbuilder.columnProcessor;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.indexbuilder.columnProcessor.impl.*;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class ExternalDataColumnProcessor {

    /**
     * 创建列处理器
     *
     * @param processorSchema
     * @return
     */
    public static ExternalDataColumnProcessor create(ProcessorSchemaField processorSchema, ParseResult schemaParseResult) {
        Assert.assertNotNull("param schemaParseResult can not be null", schemaParseResult);
        if (StringUtils.equals(ParentExtractColumnProcessor.NAME, processorSchema.getProcessorName())) {
            return new ParentExtractColumnProcessor(processorSchema);
        } else if (StringUtils.equals(CopyFieldColumnProcessor.NAME, processorSchema.getProcessorName())) {
            return new CopyFieldColumnProcessor(processorSchema);
        } else if (StringUtils.equals(MD5DataColumnProcessor.NAME, processorSchema.getProcessorName())) {
            return new MD5DataColumnProcessor(processorSchema, schemaParseResult);
        } else if (StringUtils.equals(AutoIncrColumnProcessor.NAME, processorSchema.getProcessorName())) {
            return new AutoIncrColumnProcessor(processorSchema);
        } else if (StringUtils.equals(GivenOrAutoIncrColumnProcessor.NAME, processorSchema.getProcessorName())) {
            return new GivenOrAutoIncrColumnProcessor(processorSchema);
        } else if (NumberFindProcessor.NAME.equals(processorSchema.getProcessorName())) {
            return new NumberFindProcessor();
        } else if (BitwiseBuildProcessor.NAME.equals(processorSchema.getProcessorName())) {
            return new BitwiseBuildProcessor(processorSchema);
        }
        throw new IllegalStateException("processName:" + processorSchema.getProcessorName() + " is illegal");
    }

    public abstract void process(SolrInputDocument doc, Map<String, String> entry);

    /**
     * @param doc
     *            新增solr文档
     * @param entry
     *            从hive中读取的一条记录
     */
    public abstract void process(SolrInputDocument doc, Map.Entry<String, String> entry);

    public abstract String getName();

    public String toString() {
        return StringUtils.EMPTY;
    }
}
