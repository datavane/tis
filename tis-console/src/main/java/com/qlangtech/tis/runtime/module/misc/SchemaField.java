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
package com.qlangtech.tis.runtime.module.misc;

import org.apache.commons.lang.StringUtils;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SchemaField {

    private String name;

    private String type;

    private Boolean indexed;

    private Boolean stored;

    private Boolean required;

    private Boolean mltiValued;

    private Boolean sortable;

    // 字段分词器 庖丁，like，text_ws,正则
    private String textAnalysis;

    // 选择正则分词，的正则规则
    private String regularSymbol;

    public String getRegularSymbol() {
        return regularSymbol;
    }

    public void setRegularSymbol(String regularSymbol) {
        this.regularSymbol = regularSymbol;
    }

    private String defaultValue;

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTextAnalysis() {
        return textAnalysis;
    }

    public void setTextAnalysis(String textAnalysis) {
        this.textAnalysis = textAnalysis;
    }

    public Boolean getSortable() {
        return sortable;
    }

    public void setSortable(Boolean sortable) {
        this.sortable = sortable;
    }

    @Override
    public boolean equals(Object obj) {
        return StringUtils.equals(name, ((SchemaField) obj).name);
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

    public Boolean isRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(Boolean indexed) {
        this.indexed = indexed;
    }

    public Boolean isMltiValued() {
        return mltiValued;
    }

    public void setMltiValued(Boolean mltiValued) {
        this.mltiValued = mltiValued;
    }

    public Boolean isStored() {
        return stored;
    }

    public void setStored(Boolean stored) {
        this.stored = stored;
    }
}
