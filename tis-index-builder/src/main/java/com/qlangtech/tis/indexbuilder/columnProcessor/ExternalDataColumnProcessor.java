/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.indexbuilder.columnProcessor;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.indexbuilder.columnProcessor.impl.AutoIncrColumnProcessor;
import com.qlangtech.tis.indexbuilder.columnProcessor.impl.BitwiseBuildProcessor;
import com.qlangtech.tis.indexbuilder.columnProcessor.impl.CopyFieldColumnProcessor;
import com.qlangtech.tis.indexbuilder.columnProcessor.impl.GivenOrAutoIncrColumnProcessor;
import com.qlangtech.tis.indexbuilder.columnProcessor.impl.MD5DataColumnProcessor;
import com.qlangtech.tis.indexbuilder.columnProcessor.impl.NumberFindProcessor;
import com.qlangtech.tis.indexbuilder.columnProcessor.impl.ParentExtractColumnProcessor;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;

/* *
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
