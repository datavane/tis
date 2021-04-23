/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.common.apps;

import com.qlangtech.tis.manage.biz.dal.dao.IUsrDptRelationDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.common.TriggerCrontab;
import com.qlangtech.tis.manage.common.apps.AppsFetcher.CriteriaSetter;

import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 *  @date 2014年7月26日下午7:15:16
 */
public interface IAppsFetcher extends IDepartmentGetter {

  boolean hasGrantAuthority(String permissionCode);

  /**
   * 取得当前用户所在部门的应用
   *
   * @param setter
   * @return
   */
  List<Application> getApps(CriteriaSetter setter);

  /**
   * 统计符合条件的应用数目
   *
   * @param setter
   * @return
   */
  int count(CriteriaSetter setter);

  /**
   * 更新应用
   *
   * @param app
   * @param setter
   * @return
   */
  int update(Application app, CriteriaSetter setter);


  /**
   * 显示所有的定时任务
   *
   * @param usrDptRelationDAO
   * @return
   */
  List<TriggerCrontab> getTriggerTabs(IUsrDptRelationDAO usrDptRelationDAO);

}
