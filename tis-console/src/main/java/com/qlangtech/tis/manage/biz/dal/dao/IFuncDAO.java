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

import com.qlangtech.tis.manage.biz.dal.pojo.Func;
import com.qlangtech.tis.manage.biz.dal.pojo.FuncCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IFuncDAO {

    int countByExample(FuncCriteria example);

    int countFromWriteDB(FuncCriteria example);

    int deleteByExample(FuncCriteria criteria);

    int deleteByPrimaryKey(Integer funId);

    Integer insert(Func record);

    Integer insertSelective(Func record);

    List<Func> selectByExample(FuncCriteria criteria);

    List<Func> selectByExample(FuncCriteria example, int page, int pageSize);

    Func selectByPrimaryKey(Integer funId);

    int updateByExampleSelective(Func record, FuncCriteria example);

    int updateByExample(Func record, FuncCriteria example);

    Func loadFromWriteDB(Integer funId);
}
