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
package com.qlangtech.tis.manage.biz.dal.pojo;

import java.io.Serializable;
import java.util.Date;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class FuncRoleRelation implements Serializable {

    /**
     * prop:id
     */
    private Integer id;

    /**
     * prop:r_id
     */
    private Integer rId;

    /**
     * prop:role_name
     */
    private String roleName;

    /**
     * prop:func_id
     */
    private Integer funcId;

    /**
     * prop:func_key
     */
    private String funcKey;

    /**
     * prop:gmt_create
     */
    private Date gmtCreate;

    /**
     * prop:gmt_modified
     */
    private Date gmtModified;

    /**
     * prop:func_name
     */
    private String funcName;

    private static final long serialVersionUID = 1L;

    /**
     * get:id
     */
    public Integer getId() {
        return id;
    }

    /**
     * set:id
     */
    public void setId(Integer id) {
        this.id = id;
    }

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
     * get:func_id
     */
    public Integer getFuncId() {
        return funcId;
    }

    /**
     * set:func_id
     */
    public void setFuncId(Integer funcId) {
        this.funcId = funcId;
    }

    /**
     * get:func_key
     */
    public String getFuncKey() {
        return funcKey;
    }

    /**
     * set:func_key
     */
    public void setFuncKey(String funcKey) {
        this.funcKey = funcKey == null ? null : funcKey.trim();
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
}
