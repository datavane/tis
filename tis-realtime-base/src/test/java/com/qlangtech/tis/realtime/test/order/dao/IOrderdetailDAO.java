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
import com.qlangtech.tis.realtime.test.order.pojo.Orderdetail;
import com.qlangtech.tis.realtime.test.order.pojo.OrderdetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IOrderdetailDAO {

    int countByExample(OrderdetailCriteria example);

    int countFromWriteDB(OrderdetailCriteria example);

    int deleteByExample(OrderdetailCriteria criteria);

    int deleteByPrimaryKey(String orderId);

    void insert(Orderdetail record);

    void insertSelective(Orderdetail record);

    List<Orderdetail> selectByExampleWithBLOBs(OrderdetailCriteria example);

    List<Orderdetail> selectByExampleWithoutBLOBs(OrderdetailCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(OrderdetailCriteria example, int page, int pageSize);

    List<Orderdetail> selectByExampleWithoutBLOBs(OrderdetailCriteria example, int page, int pageSize);

    Orderdetail selectByPrimaryKey(String orderId);

    int updateByExampleSelective(Orderdetail record, OrderdetailCriteria example);

    int updateByExampleWithBLOBs(Orderdetail record, OrderdetailCriteria example);

    int updateByExampleWithoutBLOBs(Orderdetail record, OrderdetailCriteria example);

    Orderdetail loadFromWriteDB(String orderId);
}
