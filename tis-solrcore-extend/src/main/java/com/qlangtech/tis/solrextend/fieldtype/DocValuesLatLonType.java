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
