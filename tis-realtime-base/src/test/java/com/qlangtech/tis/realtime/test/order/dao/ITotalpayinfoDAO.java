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
import com.qlangtech.tis.realtime.test.order.pojo.Totalpayinfo;
import com.qlangtech.tis.realtime.test.order.pojo.TotalpayinfoCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ITotalpayinfoDAO {

    int countByExample(TotalpayinfoCriteria example);

    int countFromWriteDB(TotalpayinfoCriteria example);

    int deleteByExample(TotalpayinfoCriteria criteria);

    int deleteByPrimaryKey(String totalpayId);

    void insert(Totalpayinfo record);

    void insertSelective(Totalpayinfo record);

    List<Totalpayinfo> selectByExampleWithBLOBs(TotalpayinfoCriteria example);

    List<Totalpayinfo> selectByExampleWithoutBLOBs(TotalpayinfoCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(TotalpayinfoCriteria example, int page, int pageSize);

    List<Totalpayinfo> selectByExampleWithoutBLOBs(TotalpayinfoCriteria example, int page, int pageSize);

    Totalpayinfo selectByPrimaryKey(String totalpayId);

    int updateByExampleSelective(Totalpayinfo record, TotalpayinfoCriteria example);

    int updateByExampleWithBLOBs(Totalpayinfo record, TotalpayinfoCriteria example);

    int updateByExampleWithoutBLOBs(Totalpayinfo record, TotalpayinfoCriteria example);

    Totalpayinfo loadFromWriteDB(String totalpayId);
}
