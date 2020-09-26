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
import com.qlangtech.tis.realtime.test.order.pojo.DiscountDetail;
import com.qlangtech.tis.realtime.test.order.pojo.DiscountDetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDiscountDetailDAO {

    int countByExample(DiscountDetailCriteria example);

    int countFromWriteDB(DiscountDetailCriteria example);

    int deleteByExample(DiscountDetailCriteria criteria);

    int deleteByPrimaryKey(String id);

    void insert(DiscountDetail record);

    void insertSelective(DiscountDetail record);

    List<DiscountDetail> selectByExampleWithBLOBs(DiscountDetailCriteria example);

    List<DiscountDetail> selectByExampleWithoutBLOBs(DiscountDetailCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(DiscountDetailCriteria example, int page, int pageSize);

    List<DiscountDetail> selectByExampleWithoutBLOBs(DiscountDetailCriteria example, int page, int pageSize);

    DiscountDetail selectByPrimaryKey(String id);

    int updateByExampleSelective(DiscountDetail record, DiscountDetailCriteria example);

    int updateByExampleWithBLOBs(DiscountDetail record, DiscountDetailCriteria example);

    int updateByExampleWithoutBLOBs(DiscountDetail record, DiscountDetailCriteria example);

    DiscountDetail loadFromWriteDB(String id);
}
