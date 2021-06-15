/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.solrdao.pojo;

import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.solrdao.ISchemaField;
import org.apache.commons.lang.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-11 16:30
 **/
public abstract class BasicSchemaField implements ISchemaField {

    // 分词类型
    private String tokenizerType;

    protected String name;

    private boolean indexed;

    private boolean stored;

    private boolean required;

    private boolean mltiValued;

    private boolean docValue;


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


    @Override
    public boolean equals(Object obj) {
        return StringUtils.equals(name, ((BasicSchemaField) obj).name);
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
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }


    /**
     * 分词类型
     *
     * @param tokenizerType
     */
    public void setTokenizerType(String tokenizerType) {
        this.tokenizerType = tokenizerType;
    }

    @Override
    public String getTokenizerType() {
        return this.tokenizerType;
    }


    protected static void setStringType(JSONObject f, String tokenizerType, String tokenizerTypeName) {
        f.put("split", true);
        f.put(ISchemaField.KEY_FIELD_TYPE, StringUtils.lowerCase(tokenizerTypeName));
        f.put("tokenizerType", tokenizerType);
    }

}
