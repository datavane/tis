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
import com.qlangtech.tis.realtime.test.order.pojo.PresellOrderExtra;
import com.qlangtech.tis.realtime.test.order.pojo.PresellOrderExtraCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IPresellOrderExtraDAO {

    int countByExample(PresellOrderExtraCriteria example);

    int countFromWriteDB(PresellOrderExtraCriteria example);

    int deleteByExample(PresellOrderExtraCriteria criteria);

    int deleteByPrimaryKey(String orderId);

    void insert(PresellOrderExtra record);

    void insertSelective(PresellOrderExtra record);

    List<PresellOrderExtra> selectByExampleWithBLOBs(PresellOrderExtraCriteria example);

    List<PresellOrderExtra> selectByExampleWithoutBLOBs(PresellOrderExtraCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(PresellOrderExtraCriteria example, int page, int pageSize);

    List<PresellOrderExtra> selectByExampleWithoutBLOBs(PresellOrderExtraCriteria example, int page, int pageSize);

    PresellOrderExtra selectByPrimaryKey(String orderId);

    int updateByExampleSelective(PresellOrderExtra record, PresellOrderExtraCriteria example);

    int updateByExampleWithBLOBs(PresellOrderExtra record, PresellOrderExtraCriteria example);

    int updateByExampleWithoutBLOBs(PresellOrderExtra record, PresellOrderExtraCriteria example);

    PresellOrderExtra loadFromWriteDB(String orderId);
}
