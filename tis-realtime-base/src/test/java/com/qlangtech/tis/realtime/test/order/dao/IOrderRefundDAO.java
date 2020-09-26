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
import com.qlangtech.tis.realtime.test.order.pojo.OrderRefund;
import com.qlangtech.tis.realtime.test.order.pojo.OrderRefundCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IOrderRefundDAO {

    int countByExample(OrderRefundCriteria example);

    int countFromWriteDB(OrderRefundCriteria example);

    int deleteByExample(OrderRefundCriteria criteria);

    int deleteByPrimaryKey(String id);

    void insert(OrderRefund record);

    void insertSelective(OrderRefund record);

    List<OrderRefund> selectByExample(OrderRefundCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(OrderRefundCriteria example, int page, int pageSize);

    List<OrderRefund> selectByExample(OrderRefundCriteria example, int page, int pageSize);

    OrderRefund selectByPrimaryKey(String id);

    int updateByExampleSelective(OrderRefund record, OrderRefundCriteria example);

    int updateByExample(OrderRefund record, OrderRefundCriteria example);

    OrderRefund loadFromWriteDB(String id);
}
