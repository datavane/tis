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
package com.qlangtech.tis.solrextend.fieldtype.s4WaitingUser;

import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 现在系统中大量使用了uuid作為pk或者fk的情況，這樣的話不利於字段排序，因為string類型的doc val會有问题<br>
 * http://mozhenghua.iteye.com/blog/2380836
 * 为了避免使用string类型的docval生成docvalue 转成数字类型的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UUIDField extends StrField {

    public UUIDField() {
        super();
    // this.hashFunction = Hashing.murmur3_128();
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        IndexableField fval = createField(field, value);
        if (field.hasDocValues()) {
            IndexableField docval = new NumericDocValuesField(field.getName(), value.hashCode());
            // Only create a list of we have 2 values...
            if (fval != null) {
                List<IndexableField> fields = new ArrayList<>(2);
                fields.add(fval);
                fields.add(docval);
                return fields;
            }
            fval = docval;
        }
        return Collections.singletonList(fval);
    }
}
