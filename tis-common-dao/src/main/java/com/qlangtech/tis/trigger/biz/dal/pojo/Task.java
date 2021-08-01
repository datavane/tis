/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 *   This program is free software: you can use, redistribute, and/or modify
 *   it under the terms of the GNU Affero General Public License, version 3
 *   or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.qlangtech.tis.trigger.biz.dal.pojo;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
	private Long taskId;

	private Long jobId;

	private String triggerFrom;

	private String execState;

	private String domain;

	private Date gmtCreate;

	private Date gmtModified;

	private String runtime;
	private String fromIp;
	
	private Long phrase;
	
	private String errLogId;
	
	private String errMsg;
	
	

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	public Long getPhrase() {
		return phrase;
	}

	public void setPhrase(Long phrase) {
		this.phrase = phrase;
	}


	public String getErrLogId() {
		return errLogId;
	}

	public void setErrLogId(String errLogId) {
		this.errLogId = errLogId == null ? null : errLogId.trim();
	}

	public String getFromIp() {
		return fromIp;
	}

	public void setFromIp(String fromIp) {
		this.fromIp = fromIp;
	}

	public String getRuntime() {
		return runtime;
	}

	public void setRuntime(String runtime) {
		this.runtime = runtime == null ? null : runtime.trim();
	}

	private static final long serialVersionUID = 1L;

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}

	public String getTriggerFrom() {
		return triggerFrom;
	}

	public void setTriggerFrom(String triggerFrom) {
		this.triggerFrom = triggerFrom == null ? null : triggerFrom.trim();
	}

	public String getExecState() {
		return execState;
	}

	public void setExecState(String execState) {
		this.execState = execState == null ? null : execState.trim();
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain == null ? null : domain.trim();
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
}