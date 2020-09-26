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
import com.qlangtech.tis.realtime.test.order.pojo.Specialfee;
import com.qlangtech.tis.realtime.test.order.pojo.SpecialfeeCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ISpecialfeeDAO {

    int countByExample(SpecialfeeCriteria example);

    int countFromWriteDB(SpecialfeeCriteria example);

    int deleteByExample(SpecialfeeCriteria criteria);

    int deleteByPrimaryKey(String specialfeeId);

    void insert(Specialfee record);

    void insertSelective(Specialfee record);

    List<Specialfee> selectByExample(SpecialfeeCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(SpecialfeeCriteria example, int page, int pageSize);

    List<Specialfee> selectByExample(SpecialfeeCriteria example, int page, int pageSize);

    Specialfee selectByPrimaryKey(String specialfeeId);

    int updateByExampleSelective(Specialfee record, SpecialfeeCriteria example);

    int updateByExample(Specialfee record, SpecialfeeCriteria example);

    Specialfee loadFromWriteDB(String specialfeeId);
}
