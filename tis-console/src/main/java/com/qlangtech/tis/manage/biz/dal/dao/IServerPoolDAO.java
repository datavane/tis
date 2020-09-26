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
package com.qlangtech.tis.manage.biz.dal.dao;

import com.qlangtech.tis.manage.biz.dal.pojo.ServerPool;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerPoolCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IServerPoolDAO {

    int countByExample(ServerPoolCriteria example);

    int countFromWriteDB(ServerPoolCriteria example);

    int deleteByExample(ServerPoolCriteria criteria);

    int deleteByPrimaryKey(Integer spId);

    void insert(ServerPool record);

    Integer insertSelective(ServerPool record);

    List<ServerPool> selectByExample(ServerPoolCriteria criteria);

    List<ServerPool> selectByExample(ServerPoolCriteria example, int page, int pageSize);

    ServerPool selectByPrimaryKey(Integer spId);

    int updateByExampleSelective(ServerPool record, ServerPoolCriteria example);

    int updateByExample(ServerPool record, ServerPoolCriteria example);

    ServerPool loadFromWriteDB(Integer spId);
}
