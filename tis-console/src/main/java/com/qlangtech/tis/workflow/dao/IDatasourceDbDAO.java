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
package com.qlangtech.tis.workflow.dao;

import com.qlangtech.tis.workflow.pojo.DatasourceDb;
import com.qlangtech.tis.workflow.pojo.DatasourceDbCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDatasourceDbDAO {

    int countByExample(DatasourceDbCriteria example);

    int countFromWriteDB(DatasourceDbCriteria example);

    int deleteByExample(DatasourceDbCriteria criteria);

    int deleteByPrimaryKey(Integer id);

    Integer insert(DatasourceDb record);

    Integer insertSelective(DatasourceDb record);

    List<DatasourceDb> selectByExample(DatasourceDbCriteria criteria);

    List<DatasourceDb> selectByExample(DatasourceDbCriteria example, int page, int pageSize);

    List<DatasourceDb> minSelectByExample(DatasourceDbCriteria criteria);

    List<DatasourceDb> minSelectByExample(DatasourceDbCriteria example, int page, int pageSize);

    DatasourceDb selectByPrimaryKey(Integer id);

    int updateByExampleSelective(DatasourceDb record, DatasourceDbCriteria example);

    int updateByExample(DatasourceDb record, DatasourceDbCriteria example);

    DatasourceDb loadFromWriteDB(Integer id);
}
