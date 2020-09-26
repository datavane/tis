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

import com.qlangtech.tis.dataplatform.pojo.DsTable;
import com.qlangtech.tis.dataplatform.pojo.DsTableCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDsTableDAO {

    int countByExample(DsTableCriteria example);

    int countFromWriteDB(DsTableCriteria example);

    int deleteByExample(DsTableCriteria criteria);

    int deleteByPrimaryKey(Long tabId);

    Long insert(DsTable record);

    Long insertSelective(DsTable record);

    List<DsTable> selectByExampleWithBLOBs(DsTableCriteria example);

    List<DsTable> selectByExampleWithoutBLOBs(DsTableCriteria criteria);

    List<DsTable> selectByExampleWithoutBLOBs(DsTableCriteria example, int page, int pageSize);

    DsTable selectByPrimaryKey(Long tabId);

    int updateByExampleSelective(DsTable record, DsTableCriteria example);

    int updateByExampleWithBLOBs(DsTable record, DsTableCriteria example);

    int updateByExampleWithoutBLOBs(DsTable record, DsTableCriteria example);

    DsTable loadFromWriteDB(Long tabId);
}
