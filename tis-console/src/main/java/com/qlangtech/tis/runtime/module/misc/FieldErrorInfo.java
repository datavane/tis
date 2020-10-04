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
package com.qlangtech.tis.runtime.module.misc;

/**
 * Schema编辑页面小白模式下页面校验
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年5月11日
 */
public class FieldErrorInfo {

  private final int id;
  // 包括： 字段为空，字段重复，字段名称正则式
  private boolean fieldNameError;
  // 字段重复了
//  private boolean duplicateError;
//  private boolean namePatternError;

  private boolean fieldTypeError;

  // 至少设置一个，可查询，存储，可排序的属性
  private boolean fieldPropRequiredError;

  public FieldErrorInfo(int id) {
    this.id = id;
  }

  public boolean isFieldPropRequiredError() {
    return fieldPropRequiredError;
  }

  public void setFieldPropRequiredError(boolean fieldPropRequiredError) {
    this.fieldPropRequiredError = fieldPropRequiredError;
  }

  public boolean isFieldNameError() {
    return fieldNameError;
  }

  public void setFieldNameError(boolean fieldNameError) {
    this.fieldNameError = fieldNameError;
  }


  public int getId() {
    return this.id;
  }


//  public boolean isDuplicateError() {
//    return duplicateError;
//  }
//
//  public boolean isNamePatternError() {
//    return namePatternError;
//  }

//  public void setNamePatternError(boolean namePatternError) {
//    this.namePatternError = namePatternError;
//  }
//
//  public void setDuplicateError(boolean duplicateError) {
//    this.duplicateError = duplicateError;
//  }

  public boolean isFieldTypeError() {
    return fieldTypeError;
  }

  public void setFieldTypeError(boolean fieldTypeError) {
    this.fieldTypeError = fieldTypeError;
  }

  // public boolean isFieldInputBlank() {
//    return fieldInputBlank;
//  }

//  public void setFieldInputBlank(boolean fieldInputBlank) {
//    this.fieldInputBlank = fieldInputBlank;
//  }
}
