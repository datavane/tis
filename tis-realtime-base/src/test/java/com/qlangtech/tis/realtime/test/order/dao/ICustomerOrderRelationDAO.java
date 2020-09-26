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
import com.qlangtech.tis.realtime.test.order.pojo.CustomerOrderRelation;
import com.qlangtech.tis.realtime.test.order.pojo.CustomerOrderRelationCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ICustomerOrderRelationDAO {

    int countByExample(CustomerOrderRelationCriteria example);

    int countFromWriteDB(CustomerOrderRelationCriteria example);

    int deleteByExample(CustomerOrderRelationCriteria criteria);

    int deleteByPrimaryKey(String customerregisterId, String waitingorderId);

    void insert(CustomerOrderRelation record);

    void insertSelective(CustomerOrderRelation record);

    List<CustomerOrderRelation> selectByExample(CustomerOrderRelationCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(CustomerOrderRelationCriteria example, int page, int pageSize);

    List<CustomerOrderRelation> selectByExample(CustomerOrderRelationCriteria example, int page, int pageSize);

    CustomerOrderRelation selectByPrimaryKey(String customerregisterId, String waitingorderId);

    int updateByExampleSelective(CustomerOrderRelation record, CustomerOrderRelationCriteria example);

    int updateByExample(CustomerOrderRelation record, CustomerOrderRelationCriteria example);

    CustomerOrderRelation loadFromWriteDB(String customerregisterId, String waitingorderId);
}
