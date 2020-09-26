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
package com.qlangtech.tis.realtime.test.order.dao;

import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.order.pojo.GridField;
import com.qlangtech.tis.realtime.test.order.pojo.GridFieldCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IGridFieldDAO {

    int countByExample(GridFieldCriteria example);

    int countFromWriteDB(GridFieldCriteria example);

    int deleteByExample(GridFieldCriteria criteria);

    int deleteByPrimaryKey(Long id);

    void insert(GridField record);

    void insertSelective(GridField record);

    List<GridField> selectByExample(GridFieldCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(GridFieldCriteria example, int page, int pageSize);

    List<GridField> selectByExample(GridFieldCriteria example, int page, int pageSize);

    GridField selectByPrimaryKey(Long id);

    int updateByExampleSelective(GridField record, GridFieldCriteria example);

    int updateByExample(GridField record, GridFieldCriteria example);

    GridField loadFromWriteDB(Long id);
}
