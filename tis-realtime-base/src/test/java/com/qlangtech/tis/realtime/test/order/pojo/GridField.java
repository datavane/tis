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
package com.qlangtech.tis.realtime.test.order.pojo;

import com.qlangtech.tis.realtime.transfer.AbstractRowValueGetter;
import java.io.Serializable;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GridField extends AbstractRowValueGetter implements Serializable {

    /**
     * prop:主键ID
     */
    private Long id;

    /**
     * prop:表格名称
     */
    private String gridName;

    /**
     * prop:字段英文名称
     */
    private String fieldName;

    /**
     * prop:字段标题名
     */
    private String fieldCaption;

    /**
     * prop:显示顺序
     */
    private Integer displayOrder;

    /**
     * prop:字段默认值(多选逗号分隔)
     */
    private String fieldDefaultValue;

    /**
     * prop:字段可选值(多选逗号分隔)
     */
    private String fieldCanUsed;

    /**
     * prop:数据字典编号
     */
    private String dicNo;

    /**
     * prop:字段类型
     */
    private Byte fieldType;

    /**
     * prop:是否有效,0:无效,1:有效
     */
    private Boolean isValid;

    /**
     * prop:创建时间
     */
    private Long createTime;

    /**
     * prop:修改时间
     */
    private Long opTime;

    /**
     * prop:版本号
     */
    private Integer lastVer;

    private static final long serialVersionUID = 1L;

    /**
     * get:主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * set:主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * get:表格名称
     */
    public String getGridName() {
        return gridName;
    }

    /**
     * set:表格名称
     */
    public void setGridName(String gridName) {
        this.gridName = gridName == null ? null : gridName.trim();
    }

    /**
     * get:字段英文名称
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * set:字段英文名称
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName == null ? null : fieldName.trim();
    }

    /**
     * get:字段标题名
     */
    public String getFieldCaption() {
        return fieldCaption;
    }

    /**
     * set:字段标题名
     */
    public void setFieldCaption(String fieldCaption) {
        this.fieldCaption = fieldCaption == null ? null : fieldCaption.trim();
    }

    /**
     * get:显示顺序
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * set:显示顺序
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * get:字段默认值(多选逗号分隔)
     */
    public String getFieldDefaultValue() {
        return fieldDefaultValue;
    }

    /**
     * set:字段默认值(多选逗号分隔)
     */
    public void setFieldDefaultValue(String fieldDefaultValue) {
        this.fieldDefaultValue = fieldDefaultValue == null ? null : fieldDefaultValue.trim();
    }

    /**
     * get:字段可选值(多选逗号分隔)
     */
    public String getFieldCanUsed() {
        return fieldCanUsed;
    }

    /**
     * set:字段可选值(多选逗号分隔)
     */
    public void setFieldCanUsed(String fieldCanUsed) {
        this.fieldCanUsed = fieldCanUsed == null ? null : fieldCanUsed.trim();
    }

    /**
     * get:数据字典编号
     */
    public String getDicNo() {
        return dicNo;
    }

    /**
     * set:数据字典编号
     */
    public void setDicNo(String dicNo) {
        this.dicNo = dicNo == null ? null : dicNo.trim();
    }

    /**
     * get:字段类型
     */
    public Byte getFieldType() {
        return fieldType;
    }

    /**
     * set:字段类型
     */
    public void setFieldType(Byte fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * get:是否有效,0:无效,1:有效
     */
    public Boolean getIsValid() {
        return isValid;
    }

    /**
     * set:是否有效,0:无效,1:有效
     */
    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * get:创建时间
     */
    public Long getCreateTime() {
        return createTime;
    }

    /**
     * set:创建时间
     */
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    /**
     * get:修改时间
     */
    public Long getOpTime() {
        return opTime;
    }

    /**
     * set:修改时间
     */
    public void setOpTime(Long opTime) {
        this.opTime = opTime;
    }

    /**
     * get:版本号
     */
    public Integer getLastVer() {
        return lastVer;
    }

    /**
     * set:版本号
     */
    public void setLastVer(Integer lastVer) {
        this.lastVer = lastVer;
    }
}
