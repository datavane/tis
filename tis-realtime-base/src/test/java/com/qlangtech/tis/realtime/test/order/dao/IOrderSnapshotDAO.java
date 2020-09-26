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
package com.qlangtech.tis.realtime.test.order.dao;

import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.order.pojo.OrderSnapshot;
import com.qlangtech.tis.realtime.test.order.pojo.OrderSnapshotCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IOrderSnapshotDAO {

    int countByExample(OrderSnapshotCriteria example);

    int countFromWriteDB(OrderSnapshotCriteria example);

    int deleteByExample(OrderSnapshotCriteria criteria);

    int deleteByPrimaryKey(String snapshotId);

    void insert(OrderSnapshot record);

    void insertSelective(OrderSnapshot record);

    List<OrderSnapshot> selectByExampleWithBLOBs(OrderSnapshotCriteria example);

    List<OrderSnapshot> selectByExampleWithoutBLOBs(OrderSnapshotCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(OrderSnapshotCriteria example, int page, int pageSize);

    List<OrderSnapshot> selectByExampleWithoutBLOBs(OrderSnapshotCriteria example, int page, int pageSize);

    OrderSnapshot selectByPrimaryKey(String snapshotId);

    int updateByExampleSelective(OrderSnapshot record, OrderSnapshotCriteria example);

    int updateByExampleWithBLOBs(OrderSnapshot record, OrderSnapshotCriteria example);

    int updateByExampleWithoutBLOBs(OrderSnapshot record, OrderSnapshotCriteria example);

    OrderSnapshot loadFromWriteDB(String snapshotId);
}
