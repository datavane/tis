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

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.schema.DatePointField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * 日期类型字段，原始内容中符合'yyyy-MM-dd'这样的格式
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-28 13:03
 */
public class TisDateField extends DatePointField {

    private static final String TIME_SUFFIX = "T00:00:00Z";

    private static final DateTimeFormatter format
            = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("GMT+8"));

    public static void main(String[] args) {
        System.out.println( format.format(Instant.ofEpochMilli(-225849600000l)) );
    }

    @Override
    public String toExternal(IndexableField f) {
        Number number = f.numericValue();
        return format.format(Instant.ofEpochMilli(number.longValue()));
    }


    @Override
    public Query getPointRangeQuery(QParser parser, SchemaField field, String min, String max, boolean minInclusive, boolean maxInclusive) {
        return super.getPointRangeQuery(parser, field
                , (min != null) ? (min + TIME_SUFFIX) : null, (max != null) ? (max + TIME_SUFFIX) : null, minInclusive, maxInclusive);
    }


    public Object toNativeType(Object val) {
        if (val instanceof CharSequence) {
            return super.toNativeType(val + TIME_SUFFIX);
        } else {
            return super.toNativeType(val);
        }
    }

    @Override
    public IndexableField createField(SchemaField field, Object value) {
//        Date date = (value instanceof Date)
//                ? ((Date)value)
//                : DateMathParser.parseMath(null, value.toString());
//        return new LongPoint(field.getName(), date.getTime());
        if (value instanceof Date) {
            return super.createField(field, value);
        } else {
            return super.createField(field, value + TIME_SUFFIX);
        }
    }

    @Override
    protected Query getExactQuery(SchemaField field, String externalVal) {
        return super.getExactQuery(field, externalVal + TIME_SUFFIX);
    }

    @Override
    public Query getSetQuery(QParser parser, SchemaField field, Collection<String> externalVals) {
        return super.getSetQuery(parser, field
                , externalVals.stream().map((r) -> r + TIME_SUFFIX).collect(Collectors.toList()));
    }

    @Override
    protected String indexedToReadable(BytesRef indexedForm) {
        return format.format(Instant.ofEpochMilli(LongPoint.decodeDimension(indexedForm.bytes, indexedForm.offset)));
    }
}
