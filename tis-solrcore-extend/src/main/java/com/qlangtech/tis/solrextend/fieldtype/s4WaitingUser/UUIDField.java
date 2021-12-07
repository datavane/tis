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
