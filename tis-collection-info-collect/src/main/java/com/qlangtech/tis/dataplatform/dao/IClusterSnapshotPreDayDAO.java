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
package com.qlangtech.tis.dataplatform.dao;

import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotPreDay;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotPreDayCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IClusterSnapshotPreDayDAO {

    int countByExample(ClusterSnapshotPreDayCriteria example);

    int countFromWriteDB(ClusterSnapshotPreDayCriteria example);

    int deleteByExample(ClusterSnapshotPreDayCriteria criteria);

    int deleteByPrimaryKey(Integer id);

    Integer insert(ClusterSnapshotPreDay record);

    Integer insertSelective(ClusterSnapshotPreDay record);

    List<ClusterSnapshotPreDay> selectByExample(ClusterSnapshotPreDayCriteria criteria);

    List<ClusterSnapshotPreDay> selectByExample(ClusterSnapshotPreDayCriteria example, int page, int pageSize);

    ClusterSnapshotPreDay selectByPrimaryKey(Integer id);

    int updateByExampleSelective(ClusterSnapshotPreDay record, ClusterSnapshotPreDayCriteria example);

    int updateByExample(ClusterSnapshotPreDay record, ClusterSnapshotPreDayCriteria example);

    ClusterSnapshotPreDay loadFromWriteDB(Integer id);
}
