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
import com.qlangtech.tis.realtime.test.order.pojo.Queuestatus;
import com.qlangtech.tis.realtime.test.order.pojo.QueuestatusCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IQueuestatusDAO {

    int countByExample(QueuestatusCriteria example);

    int countFromWriteDB(QueuestatusCriteria example);

    int deleteByExample(QueuestatusCriteria criteria);

    int deleteByPrimaryKey(String entityId);

    void insert(Queuestatus record);

    void insertSelective(Queuestatus record);

    List<Queuestatus> selectByExample(QueuestatusCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(QueuestatusCriteria example, int page, int pageSize);

    List<Queuestatus> selectByExample(QueuestatusCriteria example, int page, int pageSize);

    Queuestatus selectByPrimaryKey(String entityId);

    int updateByExampleSelective(Queuestatus record, QueuestatusCriteria example);

    int updateByExample(Queuestatus record, QueuestatusCriteria example);

    Queuestatus loadFromWriteDB(String entityId);
}
