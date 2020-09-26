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
package com.qlangtech.tis.manage.biz.dal.dao;

import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.ClusterSnapshotQuery;
import java.util.List;

/**
 * 监控报表接口，提供5小时，当天，15天，一个月的查询视图
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2018年5月31日
 */
public interface IClusterSnapshotDAO {

    // int countByExample(ClusterSnapshotCriteria example);
    // 
    // int countFromWriteDB(ClusterSnapshotCriteria example);
    // List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria criteria);
    // 
    // List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria example, int page, int pageSize);
    // ClusterSnapshot loadFromWriteDB(Long id);
    List<ClusterSnapshot> reportClusterStatus(ClusterSnapshotQuery query);
}
