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
package com.qlangtech.tis.openapi;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-12-20
 */
public class Column {

    private static final long serialVersionUID = 1L;

    private final String name;

    private Type fieldType;

    private boolean index = true;

    private boolean stored = true;

    public Column(String name) {
        super();
        this.name = name;
    }

    public boolean isIndex() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index = index;
    }

    public boolean isStored() {
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public void setFieldType(Type fieldType) {
        this.fieldType = fieldType;
    }

    public String getName() {
        return name;
    }

    private boolean unique;

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public enum Type {

        STRING("str"),
        INT("tint"),
        FLOAT("tfloat"),
        DOUBLE("tdouble"),
        SHORT("tshort"),
        LONG("tlong");

        private final String solrType;

        private Type(String solrType) {
            this.solrType = solrType;
        }

        public String getSolrType() {
            return solrType;
        }
    }
}
