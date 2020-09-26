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
import com.qlangtech.tis.realtime.test.order.pojo.Waitinginstanceinfo;
import com.qlangtech.tis.realtime.test.order.pojo.WaitinginstanceinfoCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IWaitinginstanceinfoDAO {

    int countByExample(WaitinginstanceinfoCriteria example);

    int countFromWriteDB(WaitinginstanceinfoCriteria example);

    int deleteByExample(WaitinginstanceinfoCriteria criteria);

    int deleteByPrimaryKey(String waitinginstanceId);

    void insert(Waitinginstanceinfo record);

    void insertSelective(Waitinginstanceinfo record);

    List<Waitinginstanceinfo> selectByExampleWithBLOBs(WaitinginstanceinfoCriteria example);

    List<Waitinginstanceinfo> selectByExampleWithoutBLOBs(WaitinginstanceinfoCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(WaitinginstanceinfoCriteria example, int page, int pageSize);

    List<Waitinginstanceinfo> selectByExampleWithoutBLOBs(WaitinginstanceinfoCriteria example, int page, int pageSize);

    Waitinginstanceinfo selectByPrimaryKey(String waitinginstanceId);

    int updateByExampleSelective(Waitinginstanceinfo record, WaitinginstanceinfoCriteria example);

    int updateByExampleWithBLOBs(Waitinginstanceinfo record, WaitinginstanceinfoCriteria example);

    int updateByExampleWithoutBLOBs(Waitinginstanceinfo record, WaitinginstanceinfoCriteria example);

    Waitinginstanceinfo loadFromWriteDB(String waitinginstanceId);
}
