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
package com.qlangtech.tis.runtime.pojo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-12-19
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
