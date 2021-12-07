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
package com.qlangtech.tis.indexbuilder.doc;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
        super.setField(name, value);
    }
}
