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
package com.qlangtech.tis.dataplatform.dao;

import com.qlangtech.tis.dataplatform.pojo.DsDatasource;
import com.qlangtech.tis.dataplatform.pojo.DsDatasourceCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDsDatasourceDAO {

    int countByExample(DsDatasourceCriteria example);

    int countFromWriteDB(DsDatasourceCriteria example);

    int deleteByExample(DsDatasourceCriteria criteria);

    int deleteByPrimaryKey(Integer dsId);

    Integer insert(DsDatasource record);

    Integer insertSelective(DsDatasource record);

    List<DsDatasource> selectByExample(DsDatasourceCriteria criteria);

    List<DsDatasource> selectByExample(DsDatasourceCriteria example, int page, int pageSize);

    DsDatasource selectByPrimaryKey(Integer dsId);

    int updateByExampleSelective(DsDatasource record, DsDatasourceCriteria example);

    int updateByExample(DsDatasource record, DsDatasourceCriteria example);

    DsDatasource loadFromWriteDB(Integer dsId);
}
