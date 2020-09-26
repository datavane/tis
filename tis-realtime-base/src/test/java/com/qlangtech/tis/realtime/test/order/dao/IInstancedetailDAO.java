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
import com.qlangtech.tis.realtime.test.order.pojo.Instancedetail;
import com.qlangtech.tis.realtime.test.order.pojo.InstancedetailCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IInstancedetailDAO {

    int countByExample(InstancedetailCriteria example);

    int countFromWriteDB(InstancedetailCriteria example);

    int deleteByExample(InstancedetailCriteria criteria);

    int deleteByPrimaryKey(String instanceId);

    void insert(Instancedetail record);

    void insertSelective(Instancedetail record);

    List<Instancedetail> selectByExampleWithBLOBs(InstancedetailCriteria example);

    List<Instancedetail> selectByExampleWithoutBLOBs(InstancedetailCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(InstancedetailCriteria example, int page, int pageSize);

    List<Instancedetail> selectByExampleWithoutBLOBs(InstancedetailCriteria example, int page, int pageSize);

    Instancedetail selectByPrimaryKey(String instanceId);

    int updateByExampleSelective(Instancedetail record, InstancedetailCriteria example);

    int updateByExampleWithBLOBs(Instancedetail record, InstancedetailCriteria example);

    int updateByExampleWithoutBLOBs(Instancedetail record, InstancedetailCriteria example);

    Instancedetail loadFromWriteDB(String instanceId);
}
