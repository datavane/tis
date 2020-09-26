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

import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IApplicationDAO {

    int countByExample(ApplicationCriteria example);

    int countFromWriteDB(ApplicationCriteria example);

    int deleteByExample(ApplicationCriteria criteria);

    int deleteByPrimaryKey(Integer appId);

    Integer insert(Application record);

    Integer insertSelective(Application record);

    List<Application> selectByExample(ApplicationCriteria criteria);

    List<Application> selectByExample(ApplicationCriteria example, int page, int pageSize);

    Application selectByPrimaryKey(Integer appId);

    Application selectByName(String name);

    int updateByExampleSelective(Application record, ApplicationCriteria example);

    int updateLastProcessTime(String appname);

    int updateByExample(Application record, ApplicationCriteria example);

    Application loadFromWriteDB(Integer appId);
}
