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
