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

/**
 * 
 */
package com.qlangtech.tis.trigger;

import java.io.Serializable;
import java.util.Date;

/**
 * @date 2012-7-30
 */
public class JobDesc implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Long jobid;

	private RTriggerKey triggerKey;

	private Date previousFireTime;

	private String crontabExpression;

	private String serverIp;

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public String getCrontabExpression() {
		return crontabExpression;
	}

	public void setCrontabExpression(String crontabExpression) {
		this.crontabExpression = crontabExpression;
	}

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	public JobDesc(Long jobid) {
		super();
		this.jobid = jobid;
	}

	public Long getJobid() {
		return jobid;
	}

	public RTriggerKey getTriggerKey() {
		return triggerKey;
	}

	public void setTriggerKey(RTriggerKey triggerKey) {
		this.triggerKey = triggerKey;
	}

}
