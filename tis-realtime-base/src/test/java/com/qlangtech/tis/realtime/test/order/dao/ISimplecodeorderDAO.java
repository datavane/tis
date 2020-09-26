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
import com.qlangtech.tis.realtime.test.order.pojo.Simplecodeorder;
import com.qlangtech.tis.realtime.test.order.pojo.SimplecodeorderCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ISimplecodeorderDAO {

    int countByExample(SimplecodeorderCriteria example);

    int countFromWriteDB(SimplecodeorderCriteria example);

    int deleteByExample(SimplecodeorderCriteria criteria);

    int deleteByPrimaryKey(Long simpleCode);

    void insert(Simplecodeorder record);

    void insertSelective(Simplecodeorder record);

    List<Simplecodeorder> selectByExample(SimplecodeorderCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(SimplecodeorderCriteria example, int page, int pageSize);

    List<Simplecodeorder> selectByExample(SimplecodeorderCriteria example, int page, int pageSize);

    Simplecodeorder selectByPrimaryKey(Long simpleCode);

    int updateByExampleSelective(Simplecodeorder record, SimplecodeorderCriteria example);

    int updateByExample(Simplecodeorder record, SimplecodeorderCriteria example);

    Simplecodeorder loadFromWriteDB(Long simpleCode);
}
