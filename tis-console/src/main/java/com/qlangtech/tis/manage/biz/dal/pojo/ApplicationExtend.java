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
public class ApplicationExtend implements Serializable {

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
     * prop:application_id
     */
    private Long aId;

    /**
     * prop:是否上锁
     */
    private String isLock;

    /**
     * prop:是否放弃
     */
    private String isRelease;

    /**
     * prop:odps表名
     */
    private Long odpsTable;

    /**
     * prop:rds表和odps表中列的md5值，用作校验rds列是否改变
     */
    private String columnsMd5;

    /**
     * prop:注释，最长200个字符
     */
    private String annotation;

    /**
     * prop:淘宝用户id
     */
    private Long userId;

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
     * get:application_id
     */
    public Long getaId() {
        return aId;
    }

    /**
     * set:application_id
     */
    public void setaId(Long aId) {
        this.aId = aId;
    }

    /**
     * get:是否上锁
     */
    public String getIsLock() {
        return isLock;
    }

    /**
     * set:是否上锁
     */
    public void setIsLock(String isLock) {
        this.isLock = isLock == null ? null : isLock.trim();
    }

    /**
     * get:是否放弃
     */
    public String getIsRelease() {
        return isRelease;
    }

    /**
     * set:是否放弃
     */
    public void setIsRelease(String isRelease) {
        this.isRelease = isRelease == null ? null : isRelease.trim();
    }

    /**
     * get:odps表名
     */
    public Long getOdpsTable() {
        return odpsTable;
    }

    /**
     * set:odps表名
     */
    public void setOdpsTable(Long odpsTable) {
        this.odpsTable = odpsTable;
    }

    /**
     * get:rds表和odps表中列的md5值，用作校验rds列是否改变
     */
    public String getColumnsMd5() {
        return columnsMd5;
    }

    /**
     * set:rds表和odps表中列的md5值，用作校验rds列是否改变
     */
    public void setColumnsMd5(String columnsMd5) {
        this.columnsMd5 = columnsMd5 == null ? null : columnsMd5.trim();
    }

    /**
     * get:注释，最长200个字符
     */
    public String getAnnotation() {
        return annotation;
    }

    /**
     * set:注释，最长200个字符
     */
    public void setAnnotation(String annotation) {
        this.annotation = annotation == null ? null : annotation.trim();
    }

    /**
     * get:淘宝用户id
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * set:淘宝用户id
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
