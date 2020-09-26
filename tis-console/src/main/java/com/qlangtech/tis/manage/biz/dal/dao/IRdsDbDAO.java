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

import com.qlangtech.tis.manage.biz.dal.pojo.RdsDb;
import com.qlangtech.tis.manage.biz.dal.pojo.RdsDbCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IRdsDbDAO {

    int countByExample(RdsDbCriteria example);

    int countFromWriteDB(RdsDbCriteria example);

    int deleteByExample(RdsDbCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Integer insert(RdsDb record);

    Integer insertSelective(RdsDb record);

    List<RdsDb> selectByExample(RdsDbCriteria criteria);

    List<RdsDb> selectByExample(RdsDbCriteria example, int page, int pageSize);

    RdsDb selectByPrimaryKey(Long id);

    int updateByExampleSelective(RdsDb record, RdsDbCriteria example);

    int updateByExample(RdsDb record, RdsDbCriteria example);

    RdsDb loadFromWriteDB(Long id);
}
