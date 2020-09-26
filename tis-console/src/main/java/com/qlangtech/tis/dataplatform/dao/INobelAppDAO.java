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

import com.qlangtech.tis.dataplatform.pojo.NobelApp;
import com.qlangtech.tis.dataplatform.pojo.NobelAppCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface INobelAppDAO {

    int countByExample(NobelAppCriteria example);

    int countFromWriteDB(NobelAppCriteria example);

    int deleteByExample(NobelAppCriteria criteria);

    int deleteByPrimaryKey(Long id);

    Long insert(NobelApp record);

    Long insertSelective(NobelApp record);

    List<NobelApp> selectByExample(NobelAppCriteria criteria);

    List<NobelApp> selectByExample(NobelAppCriteria example, int page, int pageSize);

    NobelApp selectByPrimaryKey(Long id);

    int updateByExampleSelective(NobelApp record, NobelAppCriteria example);

    int updateByExample(NobelApp record, NobelAppCriteria example);

    NobelApp loadFromWriteDB(Long id);
}
