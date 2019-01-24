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
package com.qlangtech.tis.solrdao.pojo;

import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.solrdao.SolrFieldsParser.SolrType;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class PSchemaField {

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

    public void setMltiValued(boolean mltiValued) {
        this.mltiValued = mltiValued;
    }

    public boolean isStored() {
        return useDocValuesAsStored || stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }
}
