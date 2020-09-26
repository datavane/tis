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
import com.qlangtech.tis.realtime.test.order.pojo.TakeoutOrderExtra;
import com.qlangtech.tis.realtime.test.order.pojo.TakeoutOrderExtraCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ITakeoutOrderExtraDAO {

    int countByExample(TakeoutOrderExtraCriteria example);

    int countFromWriteDB(TakeoutOrderExtraCriteria example);

    int deleteByExample(TakeoutOrderExtraCriteria criteria);

    int deleteByPrimaryKey(String orderId);

    void insert(TakeoutOrderExtra record);

    void insertSelective(TakeoutOrderExtra record);

    List<TakeoutOrderExtra> selectByExampleWithBLOBs(TakeoutOrderExtraCriteria example);

    List<TakeoutOrderExtra> selectByExampleWithoutBLOBs(TakeoutOrderExtraCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(TakeoutOrderExtraCriteria example, int page, int pageSize);

    List<TakeoutOrderExtra> selectByExampleWithoutBLOBs(TakeoutOrderExtraCriteria example, int page, int pageSize);

    TakeoutOrderExtra selectByPrimaryKey(String orderId);

    int updateByExampleSelective(TakeoutOrderExtra record, TakeoutOrderExtraCriteria example);

    int updateByExampleWithBLOBs(TakeoutOrderExtra record, TakeoutOrderExtraCriteria example);

    int updateByExampleWithoutBLOBs(TakeoutOrderExtra record, TakeoutOrderExtraCriteria example);

    TakeoutOrderExtra loadFromWriteDB(String orderId);
}
