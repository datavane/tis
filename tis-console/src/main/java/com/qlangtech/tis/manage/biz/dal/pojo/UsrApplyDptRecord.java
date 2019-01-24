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
public class UsrApplyDptRecord implements Serializable {

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
    private Date agreedTime;

    /**
     * prop:鎵�睘閮ㄩ棬id
     */
    private Integer dptId;

    /**
     * prop:鎵�睘閮ㄩ棬鍚嶇О
     */
    private String dptName;

    /**
     * prop:鐢ㄦ埛id
     */
    private String usrId;

    /**
     * prop:鐢ㄦ埛鍚�
     */
    private String usrName;

    /**
     * prop:鍚屾剰鐢宠鐨勭敤鎴穒d
     */
    private String agreedUsrId;

    /**
     * prop:鍚屾剰鐢宠鐨勭敤鎴峰悕
     */
    private String agreedUsrName;

    /**
     * prop:鐢宠鏄惁鍚屾剰锛岄粯璁
     */
    private String granted;

    /**
     * prop:澶栭敭锛屽搴攗sr_dpt_extra_relation涓潯鐩�
     */
    private Long udrId;

    /**
     * prop:鐢宠閮ㄩ棬浜哄憳鍒楄〃
     */
    private String dptUsrList;

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
    public Date getAgreedTime() {
        return agreedTime;
    }

    /**
     * set:淇敼鏃堕棿
     */
    public void setAgreedTime(Date agreedTime) {
        this.agreedTime = agreedTime;
    }

    /**
     * get:鎵�睘閮ㄩ棬id
     */
    public Integer getDptId() {
        return dptId;
    }

    /**
     * set:鎵�睘閮ㄩ棬id
     */
    public void setDptId(Integer dptId) {
        this.dptId = dptId;
    }

    /**
     * get:鎵�睘閮ㄩ棬鍚嶇О
     */
    public String getDptName() {
        return dptName;
    }

    /**
     * set:鎵�睘閮ㄩ棬鍚嶇О
     */
    public void setDptName(String dptName) {
        this.dptName = dptName == null ? null : dptName.trim();
    }

    /**
     * get:鐢ㄦ埛id
     */
    public String getUsrId() {
        return usrId;
    }

    /**
     * set:鐢ㄦ埛id
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
     * get:鍚屾剰鐢宠鐨勭敤鎴穒d
     */
    public String getAgreedUsrId() {
        return agreedUsrId;
    }

    /**
     * set:鍚屾剰鐢宠鐨勭敤鎴穒d
     */
    public void setAgreedUsrId(String agreedUsrId) {
        this.agreedUsrId = agreedUsrId == null ? null : agreedUsrId.trim();
    }

    /**
     * get:鍚屾剰鐢宠鐨勭敤鎴峰悕
     */
    public String getAgreedUsrName() {
        return agreedUsrName;
    }

    /**
     * set:鍚屾剰鐢宠鐨勭敤鎴峰悕
     */
    public void setAgreedUsrName(String agreedUsrName) {
        this.agreedUsrName = agreedUsrName == null ? null : agreedUsrName.trim();
    }

    /**
     * get:鐢宠鏄惁鍚屾剰锛岄粯璁
     */
    public String getGranted() {
        return granted;
    }

    /**
     * set:鐢宠鏄惁鍚屾剰锛岄粯璁
     */
    public void setGranted(String granted) {
        this.granted = granted == null ? null : granted.trim();
    }

    /**
     * get:澶栭敭锛屽搴攗sr_dpt_extra_relation涓潯鐩�
     */
    public Long getUdrId() {
        return udrId;
    }

    /**
     * set:澶栭敭锛屽搴攗sr_dpt_extra_relation涓潯鐩�
     */
    public void setUdrId(Long udrId) {
        this.udrId = udrId;
    }

    /**
     * get:鐢宠閮ㄩ棬浜哄憳鍒楄〃
     */
    public String getDptUsrList() {
        return dptUsrList;
    }

    /**
     * set:鐢宠閮ㄩ棬浜哄憳鍒楄〃
     */
    public void setDptUsrList(String dptUsrList) {
        this.dptUsrList = dptUsrList == null ? null : dptUsrList.trim();
    }
}
