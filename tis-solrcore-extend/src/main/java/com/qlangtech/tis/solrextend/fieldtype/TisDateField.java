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

import org.apache.lucene.search.Query;
import org.apache.solr.schema.DatePointField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-28 13:03
 */
public class TisDateField extends DatePointField {

    private static final String TIME_SUFFIX = "T00:00:00Z";

    public Query getPointRangeQuery(QParser parser, SchemaField field, String min, String max, boolean minInclusive, boolean maxInclusive) {
        return super.getPointRangeQuery(parser, field, min + TIME_SUFFIX, max + TIME_SUFFIX, minInclusive, maxInclusive);
    }

    public Object toNativeType(Object val) {
        if (val instanceof CharSequence) {
            return super.toNativeType(val + TIME_SUFFIX);
        } else {
            return super.toNativeType(val);
        }
    }

    @Override
    protected Query getExactQuery(SchemaField field, String externalVal) {
        return getExactQuery(field, externalVal + TIME_SUFFIX);
    }

    @Override
    public Query getSetQuery(QParser parser, SchemaField field, Collection<String> externalVals) {
        return super.getSetQuery(parser, field
                , externalVals.stream().map((r) -> r + TIME_SUFFIX).collect(Collectors.toList()));
    }
}
