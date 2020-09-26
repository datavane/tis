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
import com.qlangtech.tis.realtime.test.order.pojo.Globalcodeorder;
import com.qlangtech.tis.realtime.test.order.pojo.GlobalcodeorderCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IGlobalcodeorderDAO {

    int countByExample(GlobalcodeorderCriteria example);

    int countFromWriteDB(GlobalcodeorderCriteria example);

    int deleteByExample(GlobalcodeorderCriteria criteria);

    int deleteByPrimaryKey(String globalCode);

    void insert(Globalcodeorder record);

    void insertSelective(Globalcodeorder record);

    List<Globalcodeorder> selectByExample(GlobalcodeorderCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(GlobalcodeorderCriteria example, int page, int pageSize);

    List<Globalcodeorder> selectByExample(GlobalcodeorderCriteria example, int page, int pageSize);

    Globalcodeorder selectByPrimaryKey(String globalCode);

    int updateByExampleSelective(Globalcodeorder record, GlobalcodeorderCriteria example);

    int updateByExample(Globalcodeorder record, GlobalcodeorderCriteria example);

    Globalcodeorder loadFromWriteDB(String globalCode);
}
