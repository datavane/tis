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
import com.qlangtech.tis.realtime.test.order.pojo.OrderTag;
import com.qlangtech.tis.realtime.test.order.pojo.OrderTagCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IOrderTagDAO {

    int countByExample(OrderTagCriteria example);

    int countFromWriteDB(OrderTagCriteria example);

    int deleteByExample(OrderTagCriteria criteria);

    int deleteByPrimaryKey(Long id);

    void insert(OrderTag record);

    void insertSelective(OrderTag record);

    List<OrderTag> selectByExample(OrderTagCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(OrderTagCriteria example, int page, int pageSize);

    List<OrderTag> selectByExample(OrderTagCriteria example, int page, int pageSize);

    OrderTag selectByPrimaryKey(Long id);

    int updateByExampleSelective(OrderTag record, OrderTagCriteria example);

    int updateByExample(OrderTag record, OrderTagCriteria example);

    OrderTag loadFromWriteDB(Long id);
}
