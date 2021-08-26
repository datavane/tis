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
package com.qlangtech.tis.trigger.biz.dal.dao;

/**
 * @date 2012-7-2
 */
public interface IJobMetaDataDAO {

  public TriggerJob queryJob(String appName, Integer jobtype);

  public void setStop(String appName, boolean stop);

  public AppTrigger queryJob(String appName);

}
