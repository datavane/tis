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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * @author 百岁（baisui@taobao.com）
 * @date 2012-7-30
 */
public interface TriggerJobConsole extends Remote {

	public List<JobDesc> getAllJobsInServer() throws RemoteException;

	public List<JobDesc> getJob(String indexName, Long jobid)
			throws RemoteException;

	public boolean isServing(String coreName) throws RemoteException;

	/**
	 * 停止执行
	 * 
	 * @param jobids
	 * @throws RemoteException
	 */
	public void pause(String coreName) throws RemoteException;

	/**
	 * core是否是任务终止状态
	 * 
	 * @param coreName
	 * @return
	 * @throws RemoteException
	 */
	public boolean isPause(String coreName) throws RemoteException;

	/**
	 * 重新启动
	 * 
	 * @param coreName
	 * @throws RemoteException
	 */
	public void resume(String coreName) throws RemoteException;
}
