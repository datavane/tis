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

import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.AppTriggerJobRelationCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IAppTriggerJobRelationDAO {

    int countByExample(AppTriggerJobRelationCriteria example);

    int countFromWriteDB(AppTriggerJobRelationCriteria example);

    int deleteByExample(AppTriggerJobRelationCriteria criteria);

    int deleteByPrimaryKey(Long atId);

    Long insert(AppTriggerJobRelation record);

    Long insertSelective(AppTriggerJobRelation record);

    List<AppTriggerJobRelation> selectByExample(AppTriggerJobRelationCriteria criteria);

    List<AppTriggerJobRelation> selectByExample(AppTriggerJobRelationCriteria example, int page, int pageSize);

    AppTriggerJobRelation selectByPrimaryKey(Long atId);

    int updateByExampleSelective(AppTriggerJobRelation record, AppTriggerJobRelationCriteria example);

    int updateByExample(AppTriggerJobRelation record, AppTriggerJobRelationCriteria example);

    AppTriggerJobRelation loadFromWriteDB(Long atId);
}
