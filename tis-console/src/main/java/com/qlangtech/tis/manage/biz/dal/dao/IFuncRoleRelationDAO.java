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

import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelation;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncRoleRelationCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IFuncRoleRelationDAO {

    int countByExample(FuncRoleRelationCriteria example);

    int countFromWriteDB(FuncRoleRelationCriteria example);

    int deleteByExample(FuncRoleRelationCriteria criteria);

    int deleteByPrimaryKey(Integer id);

    Integer insert(FuncRoleRelation record);

    Integer insertSelective(FuncRoleRelation record);

    List<FuncRoleRelation> selectByExample(FuncRoleRelationCriteria criteria);

    List<FuncRoleRelation> selectByExample(FuncRoleRelationCriteria example, int page, int pageSize);

    FuncRoleRelation selectByPrimaryKey(Integer id);

    int updateByExampleSelective(FuncRoleRelation record, FuncRoleRelationCriteria example);

    int updateByExample(FuncRoleRelation record, FuncRoleRelationCriteria example);

    FuncRoleRelation loadFromWriteDB(Integer id);

    List<String> selectFuncListByUsrid(String usrid);
}
