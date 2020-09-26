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

import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshot;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotCriteria;
import java.util.Date;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IClusterSnapshotDAO {

    /**
     * 创建当天的历史统计
     * @param today
     */
    public void createTodaySummary(Date today);

    @SuppressWarnings("all")
    public void insertList(List<ClusterSnapshot> records);

    int countByExample(ClusterSnapshotCriteria example);

    int countFromWriteDB(ClusterSnapshotCriteria example);

    int deleteByExample(ClusterSnapshotCriteria criteria);

    int deleteByPrimaryKey(Integer id);

    Integer insert(ClusterSnapshot record);

    Integer insertSelective(ClusterSnapshot record);

    List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria criteria);

    List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria example, int page, int pageSize);

    ClusterSnapshot selectByPrimaryKey(Integer id);

    int updateByExampleSelective(ClusterSnapshot record, ClusterSnapshotCriteria example);

    int updateByExample(ClusterSnapshot record, ClusterSnapshotCriteria example);

    ClusterSnapshot loadFromWriteDB(Integer id);
}
