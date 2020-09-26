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

import com.qlangtech.tis.manage.biz.dal.pojo.Role;
import com.qlangtech.tis.manage.biz.dal.pojo.RoleCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IRoleDAO {

    int countByExample(RoleCriteria example);

    int countFromWriteDB(RoleCriteria example);

    int deleteByExample(RoleCriteria criteria);

    int deleteByPrimaryKey(Integer rId);

    Integer insert(Role record);

    Integer insertSelective(Role record);

    List<Role> selectByExample(RoleCriteria criteria);

    List<Role> selectByExample(RoleCriteria example, int page, int pageSize);

    Role selectByPrimaryKey(Integer rId);

    int updateByExampleSelective(Role record, RoleCriteria example);

    int updateByExample(Role record, RoleCriteria example);

    Role loadFromWriteDB(Integer rId);
}
