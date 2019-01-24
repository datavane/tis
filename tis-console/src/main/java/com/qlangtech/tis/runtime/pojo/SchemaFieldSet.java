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
package com.qlangtech.tis.runtime.pojo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SchemaFieldSet extends HashSet<SchemaField> {

    private static final long serialVersionUID = 6434092100460517602L;

    private final Addable addable;

    private final HashSet<SchemaField> allfields = new HashSet<SchemaField>();

    public abstract static class Addable {

        public abstract boolean can(SchemaField e);
    }

    public SchemaFieldSet(Addable addable) {
        super();
        this.addable = addable;
    }

    @Override
    public boolean addAll(Collection<? extends SchemaField> c) {
        for (SchemaField f : c) {
            this.add(f);
        }
        allfields.addAll(c);
        return true;
    }

    public final Set<SchemaField> getSolrSchema() {
        return this.allfields;
    }

    @Override
    public boolean add(SchemaField e) {
        if (addable.can(e)) {
            return super.add(e);
        }
        return true;
    }
}
