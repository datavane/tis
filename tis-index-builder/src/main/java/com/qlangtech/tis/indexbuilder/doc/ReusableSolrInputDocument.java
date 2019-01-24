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
package com.qlangtech.tis.indexbuilder.doc;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ReusableSolrInputDocument extends SolrInputDocument {

    private static final long serialVersionUID = 1L;

    public static final Logger logger = LoggerFactory.getLogger(ReusableSolrInputDocument.class);

    private final IndexSchema indexSchema;

    public ReusableSolrInputDocument(IndexSchema indexSchema) {
        super();
        this.indexSchema = indexSchema;
    }

    public void reSetField(String name, Object value) {
        reSetField(name, value, 1.0f);
    }

    public void reSetField(String name, Object value, float boost) {
        SolrInputField field = getField(name);
        if (field == null) {
            setField(name, value);
        } else {
            field.setValue(value);
        }
    }

    @Override
    public void setField(String name, Object value) {
        if (!indexSchema.getFields().keySet().contains(name) && !indexSchema.isDynamicField(name)) {
            return;
        }
        // if (!this.schemaFields.contains(name)) {
        // return;
        // }
        super.setField(name, value);
    }
}
