/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
