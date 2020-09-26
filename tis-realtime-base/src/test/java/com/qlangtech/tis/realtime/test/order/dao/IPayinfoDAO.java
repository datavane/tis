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
import com.qlangtech.tis.realtime.test.order.pojo.Payinfo;
import com.qlangtech.tis.realtime.test.order.pojo.PayinfoCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IPayinfoDAO {

    int countByExample(PayinfoCriteria example);

    int countFromWriteDB(PayinfoCriteria example);

    int deleteByExample(PayinfoCriteria criteria);

    int deleteByPrimaryKey(String payId);

    void insert(Payinfo record);

    void insertSelective(Payinfo record);

    List<Payinfo> selectByExampleWithBLOBs(PayinfoCriteria example);

    List<Payinfo> selectByExampleWithoutBLOBs(PayinfoCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(PayinfoCriteria example, int page, int pageSize);

    List<Payinfo> selectByExampleWithoutBLOBs(PayinfoCriteria example, int page, int pageSize);

    Payinfo selectByPrimaryKey(String payId);

    int updateByExampleSelective(Payinfo record, PayinfoCriteria example);

    int updateByExampleWithBLOBs(Payinfo record, PayinfoCriteria example);

    int updateByExampleWithoutBLOBs(Payinfo record, PayinfoCriteria example);

    Payinfo loadFromWriteDB(String payId);
}
