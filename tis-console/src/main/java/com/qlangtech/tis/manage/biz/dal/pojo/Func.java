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
public class Func implements Serializable {

    /**
     * prop:涓婚敭
     */
    private Integer funId;

    /**
     * prop:fun_key
     */
    private String funKey;

    /**
     * prop:func_name
     */
    private String funcName;

    /**
     * prop:gmt_create
     */
    private Date gmtCreate;

    /**
     * prop:gmt_modified
     */
    private Date gmtModified;

    /**
     * prop:鍔熻兘缁刱ey
     */
    private Integer funcGroupKey;

    /**
     * prop:鍔熻兘缁勫悕绉�
     */
    private String funcGroupName;

    private static final long serialVersionUID = 1L;

    /**
     * get:涓婚敭
     */
    public Integer getFunId() {
        return funId;
    }

    /**
     * set:涓婚敭
     */
    public void setFunId(Integer funId) {
        this.funId = funId;
    }

    /**
     * get:fun_key
     */
    public String getFunKey() {
        return funKey;
    }

    /**
     * set:fun_key
     */
    public void setFunKey(String funKey) {
        this.funKey = funKey == null ? null : funKey.trim();
    }

    /**
     * get:func_name
     */
    public String getFuncName() {
        return funcName;
    }

    /**
     * set:func_name
     */
    public void setFuncName(String funcName) {
        this.funcName = funcName == null ? null : funcName.trim();
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

    /**
     * get:鍔熻兘缁刱ey
     */
    public Integer getFuncGroupKey() {
        return funcGroupKey;
    }

    /**
     * set:鍔熻兘缁刱ey
     */
    public void setFuncGroupKey(Integer funcGroupKey) {
        this.funcGroupKey = funcGroupKey;
    }

    /**
     * get:鍔熻兘缁勫悕绉�
     */
    public String getFuncGroupName() {
        return funcGroupName;
    }

    /**
     * set:鍔熻兘缁勫悕绉�
     */
    public void setFuncGroupName(String funcGroupName) {
        this.funcGroupName = funcGroupName == null ? null : funcGroupName.trim();
    }
}
