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
package com.qlangtech.tis.manage.biz.dal.dao.impl;

import java.util.Date;
import java.util.List;
import com.qlangtech.tis.dataplatform.dao.IClusterSnapshotDAO;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshot;
import com.qlangtech.tis.dataplatform.pojo.ClusterSnapshotCriteria;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MockClusterSnapshotDAO implements IClusterSnapshotDAO {

    @Override
    public void insertList(List<ClusterSnapshot> records) {
    }

    @Override
    public void createTodaySummary(Date today) {
    }

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return 0;
    }

    @Override
    public Integer insert(ClusterSnapshot record) {
        return null;
    }

    @Override
    public Integer insertSelective(ClusterSnapshot record) {
        return null;
    }

    @Override
    public ClusterSnapshot selectByPrimaryKey(Integer id) {
        return null;
    }

    @Override
    public ClusterSnapshot loadFromWriteDB(Integer id) {
        return null;
    }

    @Override
    public int countByExample(ClusterSnapshotCriteria example) {
        return 0;
    }

    @Override
    public int countFromWriteDB(ClusterSnapshotCriteria example) {
        return 0;
    }

    @Override
    public int deleteByExample(ClusterSnapshotCriteria criteria) {
        return 0;
    }

    @Override
    public List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria criteria) {
        return null;
    }

    @Override
    public List<ClusterSnapshot> selectByExample(ClusterSnapshotCriteria example, int page, int pageSize) {
        return null;
    }

    @Override
    public int updateByExample(ClusterSnapshot record, ClusterSnapshotCriteria example) {
        return 0;
    }

    @Override
    public int updateByExampleSelective(ClusterSnapshot record, ClusterSnapshotCriteria example) {
        return 0;
    }
}
