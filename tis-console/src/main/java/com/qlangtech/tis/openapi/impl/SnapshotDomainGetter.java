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
package com.qlangtech.tis.openapi.impl;

import com.qlangtech.tis.manage.biz.dal.dao.ISnapshotViewDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.common.SnapshotDomain;
import com.qlangtech.tis.openapi.SnapshotNotFindException;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2013-12-20
 */
public class SnapshotDomainGetter {

  // private static final long serialVersionUID = 1L;
  private final RunContext runContext;

  public SnapshotDomainGetter(RunContext runContext) {
    super();
    this.runContext = runContext;
  }

  // @Override
  public // HttpServletRequest
  SnapshotDomain getSnapshot(// request)
                             AppKey appKey) throws SnapshotNotFindException {
    SnapshotInfoFromRequest result = new SnapshotInfoFromRequest();
    // final String resources = getResources(request);
    if (appKey.getTargetSnapshotId() != null && appKey.getTargetSnapshotId() > 0) {
      result.snapshotId = appKey.getTargetSnapshotId().intValue();
    } else {
      final ServerGroup group = runContext.getServerGroupDAO().load(appKey.appName, appKey.groupIndex, appKey.runtime.getId());
      if (group == null) {
        throw new SnapshotNotFindException("appName:" + appKey.appName + " groupIndex:" + appKey.groupIndex + " runtime:"
          + appKey.runtime + " has not a corresponding server group in db");
      }
      if (group.getPublishSnapshotId() == null) {
        throw new SnapshotNotFindException("groupid:" + group.getGid() + " has not set publish snapshot id");
      }
      result.snapshotId = group.getPublishSnapshotId();
    }
    // 如果在request中设置了unmergeglobalparams 这个参数
    if (!appKey.unmergeglobalparams) {
      result.runtime = appKey.runtime;
    }
    if (result.snapshotId == null) {
      throw new IllegalStateException("result.snapshotId can not be null");
    }
    ISnapshotViewDAO snapshotViewDAO = runContext.getSnapshotViewDAO();
    if (snapshotViewDAO == null) {
      throw new IllegalStateException("snapshotViewDAO can not be null");
    }
    return snapshotViewDAO.getView(result.snapshotId);
  }
}
