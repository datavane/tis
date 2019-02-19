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
public class Application implements Serializable {

	private Integer appId;

	private String projectName;

	private String recept;

	private String manager;

	private Date createTime;

	private Date updateTime;

	private String isDeleted;

	/**
	 * prop:dpt_id
	 */
	private Integer dptId;

	// private String indexsetName;
	/**
	 * prop:dpt_name
	 */
	private String dptName;

	// private String yuntiPath;
	// <result column="nobel_app_id" property="nobleAppId" jdbcType="INTEGER" />
	// <result column="nobel_app_name" property="nobleAppName"
	// jdbcType="VARCHAR" />
	//private Integer nobleAppId;

	//private String nobleAppName;

//	public Integer getNobleAppId() {
//		return nobleAppId;
//	}
//
//	public void setNobleAppId(Integer nobleAppId) {
//		this.nobleAppId = nobleAppId;
//	}
//
//	public String getNobleAppName() {
//		return nobleAppName;
//	}
//
//	public void setNobleAppName(String nobleAppName) {
//		this.nobleAppName = nobleAppName;
//	}

	private static final long serialVersionUID = 1L;

	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName == null ? null : projectName.trim();
	}

	public String getRecept() {
		return recept;
	}

	public void setRecept(String recept) {
		this.recept = recept == null ? null : recept.trim();
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager == null ? null : manager.trim();
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted == null ? null : isDeleted.trim();
	}

	/**
	 * get:dpt_id
	 */
	public Integer getDptId() {
		return dptId;
	}

	/**
	 * set:dpt_id
	 */
	public void setDptId(Integer dptId) {
		this.dptId = dptId;
	}

	/**
	 * get:dpt_name
	 */
	public String getDptName() {
		return dptName;
	}

	/**
	 * set:dpt_name
	 */
	public void setDptName(String dptName) {
		this.dptName = dptName == null ? null : dptName.trim();
	}
}
