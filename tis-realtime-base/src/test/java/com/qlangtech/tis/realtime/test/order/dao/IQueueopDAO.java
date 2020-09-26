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
import com.qlangtech.tis.realtime.test.order.pojo.Queueop;
import com.qlangtech.tis.realtime.test.order.pojo.QueueopCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IQueueopDAO {

    int countByExample(QueueopCriteria example);

    int countFromWriteDB(QueueopCriteria example);

    int deleteByExample(QueueopCriteria criteria);

    int deleteByPrimaryKey(String queueopId);

    void insert(Queueop record);

    void insertSelective(Queueop record);

    List<Queueop> selectByExample(QueueopCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(QueueopCriteria example, int page, int pageSize);

    List<Queueop> selectByExample(QueueopCriteria example, int page, int pageSize);

    Queueop selectByPrimaryKey(String queueopId);

    int updateByExampleSelective(Queueop record, QueueopCriteria example);

    int updateByExample(Queueop record, QueueopCriteria example);

    Queueop loadFromWriteDB(String queueopId);
}
