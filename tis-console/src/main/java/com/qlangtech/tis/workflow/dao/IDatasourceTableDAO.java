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

import com.qlangtech.tis.workflow.pojo.DatasourceTable;
import com.qlangtech.tis.workflow.pojo.DatasourceTableCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDatasourceTableDAO {

    int countByExample(DatasourceTableCriteria example);

    int countFromWriteDB(DatasourceTableCriteria example);

    int deleteByExample(DatasourceTableCriteria criteria);

    int deleteByPrimaryKey(Integer id);

    Integer insert(DatasourceTable record);

    Integer insertSelective(DatasourceTable record);

    List<DatasourceTable> selectByExample(DatasourceTableCriteria criteria);

    List<DatasourceTable> selectByExample(DatasourceTableCriteria example, int page, int pageSize);

    List<DatasourceTable> minSelectByExample(DatasourceTableCriteria criteria);

    List<DatasourceTable> minSelectByExample(DatasourceTableCriteria example, int page, int pageSize);

    DatasourceTable selectByPrimaryKey(Integer id);

    int updateByExampleSelective(DatasourceTable record, DatasourceTableCriteria example);

    int updateByExample(DatasourceTable record, DatasourceTableCriteria example);

    DatasourceTable loadFromWriteDB(Integer id);
}
