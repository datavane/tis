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
