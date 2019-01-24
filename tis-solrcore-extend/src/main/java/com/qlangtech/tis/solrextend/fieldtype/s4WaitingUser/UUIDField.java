/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.solrextend.fieldtype.s4WaitingUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/*
 * 现在系统中大量使用了uuid作為pk或者fk的情況，這樣的話不利於字段排序，因為string類型的doc val會有问题<br>
 * http://mozhenghua.iteye.com/blog/2380836
 * 为了避免使用string类型的docval生成docvalue 转成数字类型的
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class UUIDField extends StrField {

    private final HashFunction hashFunction;

    public UUIDField() {
        super();
        this.hashFunction = Hashing.murmur3_128();
    }

    @Override
    public List<IndexableField> createFields(SchemaField field, Object value) {
        IndexableField fval = createField(field, value);
        if (field.hasDocValues()) {
            IndexableField docval = new NumericDocValuesField(field.getName(), hashFunction.hashString(value.toString()).asLong());
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
