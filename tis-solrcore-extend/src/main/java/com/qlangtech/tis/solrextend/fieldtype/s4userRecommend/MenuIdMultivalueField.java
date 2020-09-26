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
package com.qlangtech.tis.solrextend.fieldtype.s4userRecommend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MenuIdMultivalueField extends org.apache.solr.schema.TextField {

    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        List<IndexableField> fields = new ArrayList<>();
        String v = String.valueOf(value);
        for (String menuId : StringUtils.split(v, ",")) {
            fields.add(createField(field, menuId));
        }
        return fields;
    }

    @Override
    public boolean isPolyField() {
        return true;
    }
}
