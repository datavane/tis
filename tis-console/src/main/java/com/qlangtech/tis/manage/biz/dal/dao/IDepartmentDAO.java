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

import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDepartmentDAO {

    int countByExample(DepartmentCriteria example);

    int countFromWriteDB(DepartmentCriteria example);

    int deleteByExample(DepartmentCriteria criteria);

    int deleteByPrimaryKey(Integer dptId);

    Integer insert(Department record);

    Integer insertSelective(Department record);

    List<Department> selectByExample(DepartmentCriteria criteria);

    // baisui add 20130520
    List<Department> selectByInnerJoinWithExtraDptUsrRelation(String userid);

    List<Department> selectByExample(DepartmentCriteria example, int page, int pageSize);

    Department selectByPrimaryKey(Integer dptId);

    int updateByExampleSelective(Department record, DepartmentCriteria example);

    int updateByExample(Department record, DepartmentCriteria example);

    Department loadFromWriteDB(Integer dptId);
}
