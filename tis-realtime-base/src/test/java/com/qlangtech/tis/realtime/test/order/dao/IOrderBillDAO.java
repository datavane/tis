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
import com.qlangtech.tis.realtime.test.order.pojo.OrderBill;
import com.qlangtech.tis.realtime.test.order.pojo.OrderBillCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IOrderBillDAO {

    int countByExample(OrderBillCriteria example);

    int countFromWriteDB(OrderBillCriteria example);

    int deleteByExample(OrderBillCriteria criteria);

    int deleteByPrimaryKey(String id);

    void insert(OrderBill record);

    void insertSelective(OrderBill record);

    List<OrderBill> selectByExampleWithBLOBs(OrderBillCriteria example);

    List<OrderBill> selectByExampleWithoutBLOBs(OrderBillCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(OrderBillCriteria example, int page, int pageSize);

    List<OrderBill> selectByExampleWithoutBLOBs(OrderBillCriteria example, int page, int pageSize);

    OrderBill selectByPrimaryKey(String id);

    int updateByExampleSelective(OrderBill record, OrderBillCriteria example);

    int updateByExampleWithBLOBs(OrderBill record, OrderBillCriteria example);

    int updateByExampleWithoutBLOBs(OrderBill record, OrderBillCriteria example);

    OrderBill loadFromWriteDB(String id);
}
