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

import java.util.List;
import com.qlangtech.tis.manage.biz.dal.pojo.Server;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerCriteria;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IServerDAO {

    int countByExample(ServerCriteria example);

    int countFromWriteDB(ServerCriteria example);

    int deleteByExample(ServerCriteria criteria);

    int deleteByPrimaryKey(Integer sid);

    Integer insert(Server record);

    Integer insertSelective(Server record);

    List<Server> selectByExample(ServerCriteria criteria);

    List<Server> selectByExample(ServerCriteria example, int page, int pageSize);

    Server selectByPrimaryKey(Integer sid);

    int updateByExampleSelective(Server record, ServerCriteria example);

    int updateByExample(Server record, ServerCriteria example);
}
