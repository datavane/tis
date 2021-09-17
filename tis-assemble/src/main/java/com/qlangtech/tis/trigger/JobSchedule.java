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

package com.qlangtech.tis.trigger;

import java.io.Serializable;

/**
 * 任务执行计划
 * @date 2012-6-19
 */
public class JobSchedule implements Serializable {

	private static final long serialVersionUID = 1L;
	private final Long jobid;
	// 执行任务
	private final String crobexp;
	private final String indexName;

	/**
	 * 是否是暂停状态？
	 */
	private final boolean paused;

	public JobSchedule(String indexName, Long jobid, String crobexp// , boolean
                       // isStop
	) {
		super();
		this.jobid = jobid;
		this.indexName = indexName;
		this.crobexp = crobexp;
		this.paused = false;
	}

	public Long getJobid() {
		return jobid;
	}

	public boolean isPaused() {
		return paused;
	}

	public String getIndexName() {
		return indexName;
	}

	public String getCrobexp() {
		return crobexp;
	}

}
