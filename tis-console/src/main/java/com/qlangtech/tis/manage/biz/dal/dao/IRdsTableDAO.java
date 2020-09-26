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

import com.qlangtech.tis.manage.biz.dal.pojo.RdsTable;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsTableCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IRdsTableDAO {

    int countByExample(RdsTableCriteria example);

    int countFromWriteDB(RdsTableCriteria example);

    int deleteByExample(RdsTableCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Integer insert(RdsTable record);

    Integer insertSelective(RdsTable record);

    List<RdsTable> selectByExample(RdsTableCriteria criteria);

    List<RdsTable> selectByExample(RdsTableCriteria example, int page, int pageSize);

    RdsTable selectByPrimaryKey(Long id);

    int updateByExampleSelective(RdsTable record, RdsTableCriteria example);

    int updateByExample(RdsTable record, RdsTableCriteria example);

    RdsTable loadFromWriteDB(Long id);
}
