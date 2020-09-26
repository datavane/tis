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

/**
 * Schema编辑页面小白模式下页面校验
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年5月11日
 */
public class FieldErrorInfo {

    private final int id;

    public FieldErrorInfo(int id) {
        super();
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    private boolean fieldInputBlank;

    private boolean fieldTypeError;

    // 字段重复了
    private boolean duplicateError;

    private boolean namePatternError;

    public boolean isDuplicateError() {
        return duplicateError;
    }

    public boolean isNamePatternError() {
        return namePatternError;
    }

    public void setNamePatternError(boolean namePatternError) {
        this.namePatternError = namePatternError;
    }

    public void setDuplicateError(boolean duplicateError) {
        this.duplicateError = duplicateError;
    }

    public boolean isFieldTypeError() {
        return fieldTypeError;
    }

    public void setFieldTypeError(boolean fieldTypeError) {
        this.fieldTypeError = fieldTypeError;
    }

    public boolean isFieldInputBlank() {
        return fieldInputBlank;
    }

    public void setFieldInputBlank(boolean fieldInputBlank) {
        this.fieldInputBlank = fieldInputBlank;
    }
}
