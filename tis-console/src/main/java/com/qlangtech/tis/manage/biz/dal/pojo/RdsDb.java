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
public class RdsDb implements Serializable {

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
     * prop:host地址
     */
    private String host;

    /**
     * prop:数据库所属rds实例名
     */
    private String rdsName;

    /**
     * prop:用户名
     */
    private String userName;

    /**
     * prop:密码
     */
    private String password;

    /**
     * prop:isv id
     */
    private Long iId;

    /**
     * prop:数据库名
     */
    private String dbName;

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
     * get:host地址
     */
    public String getHost() {
        return host;
    }

    /**
     * set:host地址
     */
    public void setHost(String host) {
        this.host = host == null ? null : host.trim();
    }

    /**
     * get:数据库所属rds实例名
     */
    public String getRdsName() {
        return rdsName;
    }

    /**
     * set:数据库所属rds实例名
     */
    public void setRdsName(String rdsName) {
        this.rdsName = rdsName == null ? null : rdsName.trim();
    }

    /**
     * get:用户名
     */
    public String getUserName() {
        return userName;
    }

    /**
     * set:用户名
     */
    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    /**
     * get:密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * set:密码
     */
    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    /**
     * get:isv id
     */
    public Long getiId() {
        return iId;
    }

    /**
     * set:isv id
     */
    public void setiId(Long iId) {
        this.iId = iId;
    }

    /**
     * get:数据库名
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * set:数据库名
     */
    public void setDbName(String dbName) {
        this.dbName = dbName == null ? null : dbName.trim();
    }
}
