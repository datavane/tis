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
package com.qlangtech.tis.manage.biz.dal.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class RdsTable implements Serializable {

    /**
     * prop:主键
     */
    private Long id;

    /**
     * prop:创建时间
     */
    private Date gmtCreate;

    /**
     * prop:修改时间
     */
    private Date gmtModified;

    /**
     * prop:应用实例id
     */
    private Long aId;

    /**
     * prop:rds实例id
     */
    private Long rId;

    /**
     * prop:表名
     */
    private String tableName;

    private static final long serialVersionUID = 1L;

    /**
     * get:主键
     */
    public Long getId() {
        return id;
    }

    /**
     * set:主键
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * get:创建时间
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * set:创建时间
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * get:修改时间
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * set:修改时间
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * get:应用实例id
     */
    public Long getaId() {
        return aId;
    }

    /**
     * set:应用实例id
     */
    public void setaId(Long aId) {
        this.aId = aId;
    }

    /**
     * get:rds实例id
     */
    public Long getrId() {
        return rId;
    }

    /**
     * set:rds实例id
     */
    public void setrId(Long rId) {
        this.rId = rId;
    }

    /**
     * get:表名
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * set:表名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName == null ? null : tableName.trim();
    }
}
