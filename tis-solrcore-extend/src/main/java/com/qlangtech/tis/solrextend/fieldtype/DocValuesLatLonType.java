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
package com.qlangtech.tis.solrextend.fieldtype;

import java.util.List;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.LatLonType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.util.SpatialUtils;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Point;

/**
 * 地理位置查询启用docvalue
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DocValuesLatLonType extends LatLonType {

    @Override
    public void checkSchemaField(SchemaField field) {
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        List<IndexableField> fields = super.createFields(field, value);
        String externalVal = value.toString();
        if (field.hasDocValues()) {
            Point point = SpatialUtils.parsePointSolrException(externalVal, SpatialContext.GEO);
            SchemaField subLatSF = subField(field, LAT, schema);
            fields.add(new DoubleDocValuesField(subLatSF.getName(), point.getY()));
            SchemaField subLonSF = subField(field, LON, schema);
            fields.add(new DoubleDocValuesField(subLonSF.getName(), point.getX()));
        }
        return fields;
    }
}
