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
package com.qlangtech.tis.indexbuilder.doc.impl;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.update.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qlangtech.tis.indexbuilder.columnProcessor.ExternalDataColumnProcessor;
import com.qlangtech.tis.indexbuilder.doc.IInputDocCreator;
import com.qlangtech.tis.indexbuilder.doc.ReusableSolrInputDocument;
import com.qlangtech.tis.indexbuilder.map.RawDataProcessor;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AbstractInputDocCreator implements IInputDocCreator {

    // public static String DOC_CREATOR_TYPE_SEQUENCE_NEST_DOC =
    // "sequence-nest-doc-creator";
    public static String DOC_CREATOR_DEFAULT = "default";

    private final RawDataProcessor rawDataProcessor;

    protected final IndexSchema indexSchema;

    private final boolean hasRowProcessor;

    protected final String uniqueKeyFieldName;

    private final String newVersion;

    private static final Logger logger = LoggerFactory.getLogger(AbstractInputDocCreator.class);

    /**
     * @param typeName
     * @param rawDataProcessor
     * @param indexSchema
     * @param newVersion
     * @return
     */
    @SuppressWarnings("all")
    public static IInputDocCreator createDocumentCreator(String typeName, RawDataProcessor rawDataProcessor, IndexSchema indexSchema, String newVersion) {
        if (StringUtils.isBlank(typeName)) {
            throw new IllegalStateException("param typeName can not be null");
        }
        if (DOC_CREATOR_DEFAULT.equals(typeName)) {
            logger.info("inputdoc type:default");
            return new DefaultInputDocCreator(rawDataProcessor, indexSchema, newVersion);
        }
        try {
            Class<IInputDocCreator> clazz = (Class<IInputDocCreator>) Class.forName(typeName);
            Constructor<IInputDocCreator> constructor = clazz.getConstructor(RawDataProcessor.class, IndexSchema.class, String.class);
            return constructor.newInstance(rawDataProcessor, indexSchema, newVersion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    // if (DOC_CREATOR_TYPE_SEQUENCE_NEST_DOC.equals(typeName)) {
    // logger.info("inputdoc type:" + DOC_CREATOR_TYPE_SEQUENCE_NEST_DOC);
    // return new SequenceFileNestInputDocCreator(rawDataProcessor, indexSchema,
    // newVersion);
    // } else {
    // 
    // }
    }

    AbstractInputDocCreator(RawDataProcessor rawDataProcessor, IndexSchema indexSchema, String newVersion) {
        super();
        this.rawDataProcessor = rawDataProcessor;
        this.indexSchema = indexSchema;
        this.hasRowProcessor = (rawDataProcessor.getRowProcess().size() > 0);
        this.uniqueKeyFieldName = indexSchema.getUniqueKeyField().getName();
        this.newVersion = newVersion;
    }

    // public SolrInputDocument createSolrInputDocument(SourceReader recordReader)
    // throws Exception;
    protected final SolrInputDocument getLuceneDocument(Map<String, String> fieldValues) {
        ReusableSolrInputDocument solrDoc = createDocument();
        if (this.hasRowProcessor) {
            for (ExternalDataColumnProcessor rowProcessor : rawDataProcessor.getRowProcess()) {
                rowProcessor.process(solrDoc, fieldValues);
            }
        }
        for (Entry<String, String> entry : fieldValues.entrySet()) {
            // String value = fieldValues.get(name);
            // 只重置值不创建field，减少gc
            // fieldKey = entry.getKey();
            ExternalDataColumnProcessor pas = rawDataProcessor.getExternalColumnProcessors(entry.getKey());
            // if (pas != null) {
            pas.process(solrDoc, entry);
        // } else {
        // if (entry.getValue() != null) {
        // solrDoc.reSetField(entry.getKey(), entry.getValue());
        // } else {
        // solrDoc.removeField(entry.getKey());
        // }
        // }
        }
        if (solrDoc.getField(uniqueKeyFieldName) == null) {
            throw new IllegalStateException("lack of pk field:" + uniqueKeyFieldName + ",doc:" + solrDoc.toString());
        }
        if (!solrDoc.containsKey(CommonParams.VERSION_FIELD)) {
            solrDoc.setField(CommonParams.VERSION_FIELD, newVersion);
        }
        return solrDoc;
    }

    protected ReusableSolrInputDocument createDocument() {
        return new ReusableSolrInputDocument(indexSchema);
    }
}
