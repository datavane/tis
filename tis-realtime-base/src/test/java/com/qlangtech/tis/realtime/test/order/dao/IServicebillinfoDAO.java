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
import com.qlangtech.tis.realtime.test.order.pojo.Servicebillinfo;
import com.qlangtech.tis.realtime.test.order.pojo.ServicebillinfoCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IServicebillinfoDAO {

    int countByExample(ServicebillinfoCriteria example);

    int countFromWriteDB(ServicebillinfoCriteria example);

    int deleteByExample(ServicebillinfoCriteria criteria);

    int deleteByPrimaryKey(String servicebillId);

    void insert(Servicebillinfo record);

    void insertSelective(Servicebillinfo record);

    List<Servicebillinfo> selectByExampleWithBLOBs(ServicebillinfoCriteria example);

    List<Servicebillinfo> selectByExampleWithoutBLOBs(ServicebillinfoCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(ServicebillinfoCriteria example, int page, int pageSize);

    List<Servicebillinfo> selectByExampleWithoutBLOBs(ServicebillinfoCriteria example, int page, int pageSize);

    Servicebillinfo selectByPrimaryKey(String servicebillId);

    int updateByExampleSelective(Servicebillinfo record, ServicebillinfoCriteria example);

    int updateByExampleWithBLOBs(Servicebillinfo record, ServicebillinfoCriteria example);

    int updateByExampleWithoutBLOBs(Servicebillinfo record, ServicebillinfoCriteria example);

    Servicebillinfo loadFromWriteDB(String servicebillId);
}
