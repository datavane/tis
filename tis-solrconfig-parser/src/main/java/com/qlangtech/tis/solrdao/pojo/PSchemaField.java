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
package com.qlangtech.tis.solrdao.pojo;

import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.solrdao.ISchemaField;
import com.qlangtech.tis.solrdao.SolrFieldsParser.SolrType;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class PSchemaField implements ISchemaField {

    private String name;

    private SolrType type;

    private boolean indexed;

    private boolean stored;

    private boolean required;

    private boolean mltiValued;

    // 是否为动态字段
    private boolean dynamic;

    private boolean docValue;

    private boolean useDocValuesAsStored;

    public boolean isUseDocValuesAsStored() {
        return useDocValuesAsStored;
    }

    public void setUseDocValuesAsStored(boolean useDocValuesAsStored) {
        this.useDocValuesAsStored = useDocValuesAsStored;
    }

    public boolean isDocValue() {
        return docValue;
    }

    public void setDocValue(boolean docValue) {
        this.docValue = docValue;
    }

    private String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    @Override
    public boolean equals(Object obj) {
        return StringUtils.equals(name, ((PSchemaField) obj).name);
    }

    public String getSetMethodName() {
        return "set" + StringUtils.capitalize(this.getPropertyName());
    }

    public String getGetterMethodName() {
        return "get" + StringUtils.capitalize(this.getPropertyName());
    }

    public String getFileTypeLiteria() {
        if (this.isMltiValued()) {
            return "List<" + this.getSimpleType() + ">";
        } else {
            return this.getSimpleType();
        }
    }

    public String getPropertyName() {
        StringBuffer result = new StringBuffer();
        boolean isLetterGap = false;
        char[] nameChar = this.name.toCharArray();
        for (int i = 0; i < nameChar.length; i++) {
            if (isLetterGap) {
                result.append(Character.toUpperCase(nameChar[i]));
                isLetterGap = ('_' == nameChar[i]);
                continue;
            }
            if (isLetterGap = ('_' == nameChar[i])) {
                continue;
            }
            // if (isLetterGap) {
            // result.append(Character.toUpperCase(this.name.charAt(i)));
            // } else {
            result.append(nameChar[i]);
        // }
        }
        return result.toString();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SolrType getType() {
        return type;
    }

    public String getSimpleType() {
        return this.getType().getJavaType().getSimpleName();
    }

    public void setType(SolrType type) {
        this.type = type;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isMltiValued() {
        return mltiValued;
    }

    @Override
    public boolean isMultiValue() {
        return isMltiValued();
    }

    public void setMltiValued(boolean mltiValued) {
        this.mltiValued = mltiValued;
    }

    @Override
    public boolean isStored() {
        return useDocValuesAsStored || stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }

    @Override
    public String getTisFieldTypeName() {
       // return this.type.getSolrType();
        return this.type.getSType().getName();
    }

    @Override
    public String getTokenizerType() {
        return null;
    }
}
