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
package com.qlangtech.tis.manage.biz.dal.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Department implements Serializable {
  private static final long serialVersionUID = 1L;
  private Integer dptId;

  private Integer parentId;

  private String name;

  private Date gmtCreate;

  private Date gmtModified;


  private Integer templateFlag;

  // Indexset 应用模板特性，是否有实时特性，是否有普通模式特性
  public Integer getTemplateFlag() {
    return templateFlag;
  }

  public void setTemplateFlag(Integer templateFlag) {
    this.templateFlag = templateFlag;
  }

  /**
   * prop:full_name
   */
  private String fullName;

  /**
   * prop:leaf
   */
  private Boolean leaf;


  public Integer getDptId() {
    return dptId;
  }

  public void setDptId(Integer dptId) {
    this.dptId = dptId;
  }

  public Integer getParentId() {
    return parentId;
  }

  public void setParentId(Integer parentId) {
    this.parentId = parentId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name == null ? null : name.trim();
  }

  public Date getGmtCreate() {
    return gmtCreate;
  }

  public void setGmtCreate(Date gmtCreate) {
    this.gmtCreate = gmtCreate;
  }

  public Date getGmtModified() {
    return gmtModified;
  }

  public void setGmtModified(Date gmtModified) {
    this.gmtModified = gmtModified;
  }

  /**
   * get:full_name
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * set:full_name
   */
  public void setFullName(String fullName) {
    this.fullName = fullName == null ? null : fullName.trim();
  }

  /**
   * get:leaf
   */
  public Boolean getLeaf() {
    return leaf;
  }

  /**
   * set:leaf
   */
  public void setLeaf(Boolean leaf) {
    this.leaf = leaf;
  }

}
