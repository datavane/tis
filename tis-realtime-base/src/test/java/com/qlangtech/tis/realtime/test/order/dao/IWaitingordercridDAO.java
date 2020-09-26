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
import com.qlangtech.tis.realtime.test.order.pojo.Waitingordercrid;
import com.qlangtech.tis.realtime.test.order.pojo.WaitingordercridCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IWaitingordercridDAO {

    int countByExample(WaitingordercridCriteria example);

    int countFromWriteDB(WaitingordercridCriteria example);

    int deleteByExample(WaitingordercridCriteria criteria);

    int deleteByPrimaryKey(String waitingorderId);

    void insert(Waitingordercrid record);

    void insertSelective(Waitingordercrid record);

    List<Waitingordercrid> selectByExample(WaitingordercridCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(WaitingordercridCriteria example, int page, int pageSize);

    List<Waitingordercrid> selectByExample(WaitingordercridCriteria example, int page, int pageSize);

    Waitingordercrid selectByPrimaryKey(String waitingorderId);

    int updateByExampleSelective(Waitingordercrid record, WaitingordercridCriteria example);

    int updateByExample(Waitingordercrid record, WaitingordercridCriteria example);

    Waitingordercrid loadFromWriteDB(String waitingorderId);
}
