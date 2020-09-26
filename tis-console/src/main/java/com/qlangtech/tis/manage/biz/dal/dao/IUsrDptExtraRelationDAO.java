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

import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.UsrDptExtraRelationCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IUsrDptExtraRelationDAO {

    int countByExample(UsrDptExtraRelationCriteria example);

    int countFromWriteDB(UsrDptExtraRelationCriteria example);

    int deleteByExample(UsrDptExtraRelationCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Long insert(UsrDptExtraRelation record);

    Long insertSelective(UsrDptExtraRelation record);

    List<UsrDptExtraRelation> selectByExample(UsrDptExtraRelationCriteria criteria);

    List<UsrDptExtraRelation> selectByExample(UsrDptExtraRelationCriteria example, int page, int pageSize);

    UsrDptExtraRelation selectByPrimaryKey(Long id);

    int updateByExampleSelective(UsrDptExtraRelation record, UsrDptExtraRelationCriteria example);

    int updateByExample(UsrDptExtraRelation record, UsrDptExtraRelationCriteria example);

    UsrDptExtraRelation loadFromWriteDB(Long id);
}
