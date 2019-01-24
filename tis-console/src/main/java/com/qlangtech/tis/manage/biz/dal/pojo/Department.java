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
public class Department implements Serializable {

    private Integer dptId;

    private Integer parentId;

    private String name;

    private Date gmtCreate;

    private Date gmtModified;

    private Integer indexsetSnapshot;

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

    private Integer alibabaDptId;

    private static final long serialVersionUID = 1L;

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

    public Integer getIndexsetSnapshot() {
        return indexsetSnapshot;
    }

    public void setIndexsetSnapshot(Integer indexsetSnapshot) {
        this.indexsetSnapshot = indexsetSnapshot;
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

    public Integer getAlibabaDptId() {
        return alibabaDptId;
    }

    public void setAlibabaDptId(Integer alibabaDptId) {
        this.alibabaDptId = alibabaDptId;
    }
}
