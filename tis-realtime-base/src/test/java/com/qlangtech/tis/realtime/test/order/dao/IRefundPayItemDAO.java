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
import com.qlangtech.tis.realtime.test.order.pojo.RefundPayItem;
import com.qlangtech.tis.realtime.test.order.pojo.RefundPayItemCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IRefundPayItemDAO {

    int countByExample(RefundPayItemCriteria example);

    int countFromWriteDB(RefundPayItemCriteria example);

    int deleteByExample(RefundPayItemCriteria criteria);

    int deleteByPrimaryKey(String id);

    void insert(RefundPayItem record);

    void insertSelective(RefundPayItem record);

    List<RefundPayItem> selectByExample(RefundPayItemCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(RefundPayItemCriteria example, int page, int pageSize);

    List<RefundPayItem> selectByExample(RefundPayItemCriteria example, int page, int pageSize);

    RefundPayItem selectByPrimaryKey(String id);

    int updateByExampleSelective(RefundPayItem record, RefundPayItemCriteria example);

    int updateByExample(RefundPayItem record, RefundPayItemCriteria example);

    RefundPayItem loadFromWriteDB(String id);
}
