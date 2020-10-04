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
package com.qlangtech.tis.runtime.module.misc;

import com.qlangtech.tis.solrdao.ISchemaField;
import org.apache.commons.lang.StringUtils;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-12-14
 */
public class SchemaField implements ISchemaField {

    // 字段编号（頁面操作會話過程中不可變）
    private int id;

    private String name;

    private String fieldtype;

    private boolean indexed = false;

    private boolean stored = true;

    private boolean required;

    private boolean multiValue = false;

    private boolean sortable;

    // 如果选择了string分词
    private String textAnalysis;

    // 当选择了String类型之后，可以选择String类型的分词类型
    private boolean split;

    // 是否开通docvalue
    private boolean docval;

//    // 如果选择数字类型的话 ，是否支持区间查询
//    private boolean range;

//    @Override
//    public boolean isRange() {
//        return range;
//    }
//
//    public void setRange(boolean range) {
//        this.range = range;
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean isMultiValue() {
        return this.multiValue;
    }

    // 是否是主键
    private boolean uniqueKey;

    // 是否是分组键
    private boolean sharedKey;

    @Override
    public boolean isDynamic() {
        return StringUtils.indexOf(this.name, "*") > -1;
    }

    // 输入控件是否可编辑，在新建索引流程，希望用户不需要更改name属性，这样可以保持和数据源中的字段一致
    // private boolean inputDisabled;
    //
    // public boolean isInputDisabled() {
    // return inputDisabled;
    // }
    //
    // public void setInputDisabled(boolean inputDisabled) {
    // this.inputDisabled = inputDisabled;
    // }
    // 选择正则分词，的正则规则
    // private String regularSymbol;
    // public String getRegularSymbol() {
    // return regularSymbol;
    // }
    //
    // public void setRegularSymbol(String regularSymbol) {
    // this.regularSymbol = regularSymbol;
    // }
    @Override
    public String getTisFieldType() {
        return this.fieldtype;
    }

    public boolean isSharedKey() {
        return sharedKey;
    }

    public void setSharedKey(boolean sharedKey) {
        this.sharedKey = sharedKey;
    }

    public boolean isUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(boolean uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    @Override
    public boolean isDocValue() {
        return this.getSortable();
    }

//    public void setDocval(boolean docval) {
//        this.docval = docval;
//    }

    private String defaultValue;

    public boolean isSplit() {
        return split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    // public String getTextAnalysis() {
    // return textAnalysis;
    // }
    //
    // public void setTextAnalysis(String textAnalysis) {
    // this.textAnalysis = textAnalysis;
    // }
    public void setTokenizerType(String value) {
        this.textAnalysis = value;
    }

    public String getTokenizerType() {
        return this.textAnalysis;
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

    public String getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(String fieldtype) {
        this.fieldtype = fieldtype;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean getMultiValue() {
        return this.multiValue;
    }

    public void setMultiValue(boolean multiValue) {
        this.multiValue = multiValue;
    }

    public boolean isStored() {
        return stored;
    }

    public void setStored(Boolean stored) {
        this.stored = stored;
    }

//    public void setRangequery(boolean value) {
//        this.sortable = value;
//    }
//
//    public Boolean getRangequery() {
//        return this.sortable;
//    }
}
