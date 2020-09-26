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
public class Role implements Serializable {

    /**
     * prop:r_id
     */
    private Integer rId;

    /**
     * prop:role_name
     */
    private String roleName;

    /**
     * prop:gmt_create
     */
    private Date gmtCreate;

    /**
     * prop:gmt_modified
     */
    private Date gmtModified;

    private static final long serialVersionUID = 1L;

    /**
     * get:r_id
     */
    public Integer getrId() {
        return rId;
    }

    /**
     * set:r_id
     */
    public void setrId(Integer rId) {
        this.rId = rId;
    }

    /**
     * get:role_name
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * set:role_name
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName == null ? null : roleName.trim();
    }

    /**
     * get:gmt_create
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * set:gmt_create
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * get:gmt_modified
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * set:gmt_modified
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }
}
