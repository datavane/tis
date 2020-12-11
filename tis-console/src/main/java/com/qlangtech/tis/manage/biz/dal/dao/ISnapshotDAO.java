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

import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ISnapshotDAO {

    int countByExample(SnapshotCriteria example);

    int countFromWriteDB(SnapshotCriteria example);

    int deleteByExample(SnapshotCriteria criteria);

    List<Snapshot> findPassTestSnapshot(SnapshotCriteria example);

    int deleteByPrimaryKey(Integer snId);

    Integer insert(Snapshot record);

    Integer insertSelective(Snapshot record);

    List<Snapshot> selectByExample(SnapshotCriteria criteria);

    List<Snapshot> selectByExample(SnapshotCriteria example, int page, int pageSize);

    Integer getMaxSnapshotId(SnapshotCriteria criteria);

    Snapshot selectByPrimaryKey(Integer snId);

    int updateByExampleSelective(Snapshot record, SnapshotCriteria example);

    int updateByExample(Snapshot record, SnapshotCriteria example);

    Snapshot loadFromWriteDB(Integer snId);
}
