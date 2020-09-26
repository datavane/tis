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
import com.qlangtech.tis.realtime.test.order.pojo.Waitingorderdetail;
import com.qlangtech.tis.realtime.test.order.pojo.WaitingorderdetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IWaitingorderdetailDAO {

    int countByExample(WaitingorderdetailCriteria example);

    int countFromWriteDB(WaitingorderdetailCriteria example);

    int deleteByExample(WaitingorderdetailCriteria criteria);

    int deleteByPrimaryKey(String waitingorderId);

    void insert(Waitingorderdetail record);

    void insertSelective(Waitingorderdetail record);

    List<Waitingorderdetail> selectByExampleWithBLOBs(WaitingorderdetailCriteria example);

    List<Waitingorderdetail> selectByExampleWithoutBLOBs(WaitingorderdetailCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(WaitingorderdetailCriteria example, int page, int pageSize);

    List<Waitingorderdetail> selectByExampleWithoutBLOBs(WaitingorderdetailCriteria example, int page, int pageSize);

    Waitingorderdetail selectByPrimaryKey(String waitingorderId);

    int updateByExampleSelective(Waitingorderdetail record, WaitingorderdetailCriteria example);

    int updateByExampleWithBLOBs(Waitingorderdetail record, WaitingorderdetailCriteria example);

    int updateByExampleWithoutBLOBs(Waitingorderdetail record, WaitingorderdetailCriteria example);

    Waitingorderdetail loadFromWriteDB(String waitingorderId);
}
