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

import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApply;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationApplyCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IApplicationApplyDAO {

    int countByExample(ApplicationApplyCriteria example);

    int countFromWriteDB(ApplicationApplyCriteria example);

    int deleteByExample(ApplicationApplyCriteria criteria);

    int deleteByPrimaryKey(Integer appId);

    Integer insert(ApplicationApply record);

    Integer insertSelective(ApplicationApply record);

    List<ApplicationApply> selectByExample(ApplicationApplyCriteria criteria);

    List<ApplicationApply> selectByExample(ApplicationApplyCriteria example, int page, int pageSize);

    ApplicationApply selectByPrimaryKey(Integer appId);

    int updateByExampleSelective(ApplicationApply record, ApplicationApplyCriteria example);

    int updateByExample(ApplicationApply record, ApplicationApplyCriteria example);

    ApplicationApply loadFromWriteDB(Integer appId);
}
