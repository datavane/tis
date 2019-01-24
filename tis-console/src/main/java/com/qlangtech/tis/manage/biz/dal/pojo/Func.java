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
