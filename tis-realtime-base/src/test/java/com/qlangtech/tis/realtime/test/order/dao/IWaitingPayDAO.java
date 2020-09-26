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
import com.qlangtech.tis.realtime.test.order.pojo.WaitingPay;
import com.qlangtech.tis.realtime.test.order.pojo.WaitingPayCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IWaitingPayDAO {

    int countByExample(WaitingPayCriteria example);

    int countFromWriteDB(WaitingPayCriteria example);

    int deleteByExample(WaitingPayCriteria criteria);

    int deleteByPrimaryKey(String id);

    void insert(WaitingPay record);

    void insertSelective(WaitingPay record);

    List<WaitingPay> selectByExample(WaitingPayCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(WaitingPayCriteria example, int page, int pageSize);

    List<WaitingPay> selectByExample(WaitingPayCriteria example, int page, int pageSize);

    WaitingPay selectByPrimaryKey(String id);

    int updateByExampleSelective(WaitingPay record, WaitingPayCriteria example);

    int updateByExample(WaitingPay record, WaitingPayCriteria example);

    WaitingPay loadFromWriteDB(String id);
}
