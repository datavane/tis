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
public class UsrDptExtraRelation implements Serializable {

    /**
     * prop:涓婚敭
     */
    private Long id;

    /**
     * prop:鍒涘缓鏃堕棿
     */
    private Date gmtCreate;

    /**
     * prop:淇敼鏃堕棿
     */
    private Date gmtModified;

    /**
     * prop:鐢ㄦ埛涓婚敭
     */
    private String usrId;

    /**
     * prop:鐢ㄦ埛鍚�
     */
    private String usrName;

    /**
     * prop:閮ㄩ棬鍚嶇О
     */
    private String dptName;

    /**
     * prop:閮ㄩ棬id
     */
    private Integer dptId;

    private static final long serialVersionUID = 1L;

    /**
     * get:涓婚敭
     */
    public Long getId() {
        return id;
    }

    /**
     * set:涓婚敭
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * get:鍒涘缓鏃堕棿
     */
    public Date getGmtCreate() {
        return gmtCreate;
    }

    /**
     * set:鍒涘缓鏃堕棿
     */
    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    /**
     * get:淇敼鏃堕棿
     */
    public Date getGmtModified() {
        return gmtModified;
    }

    /**
     * set:淇敼鏃堕棿
     */
    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    /**
     * get:鐢ㄦ埛涓婚敭
     */
    public String getUsrId() {
        return usrId;
    }

    /**
     * set:鐢ㄦ埛涓婚敭
     */
    public void setUsrId(String usrId) {
        this.usrId = usrId == null ? null : usrId.trim();
    }

    /**
     * get:鐢ㄦ埛鍚�
     */
    public String getUsrName() {
        return usrName;
    }

    /**
     * set:鐢ㄦ埛鍚�
     */
    public void setUsrName(String usrName) {
        this.usrName = usrName == null ? null : usrName.trim();
    }

    /**
     * get:閮ㄩ棬鍚嶇О
     */
    public String getDptName() {
        return dptName;
    }

    /**
     * set:閮ㄩ棬鍚嶇О
     */
    public void setDptName(String dptName) {
        this.dptName = dptName == null ? null : dptName.trim();
    }

    /**
     * get:閮ㄩ棬id
     */
    public Integer getDptId() {
        return dptId;
    }

    /**
     * set:閮ㄩ棬id
     */
    public void setDptId(Integer dptId) {
        this.dptId = dptId;
    }
}
