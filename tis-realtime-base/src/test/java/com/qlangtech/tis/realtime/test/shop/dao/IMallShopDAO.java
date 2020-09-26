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
package com.qlangtech.tis.realtime.test.shop.dao;

import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.shop.pojo.MallShop;
import com.qlangtech.tis.realtime.test.shop.pojo.MallShopCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IMallShopDAO {

    int countByExample(MallShopCriteria example);

    int countFromWriteDB(MallShopCriteria example);

    int deleteByExample(MallShopCriteria criteria);

    int deleteByPrimaryKey(String id);

    void insert(MallShop record);

    void insertSelective(MallShop record);

    List<MallShop> selectByExample(MallShopCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(MallShopCriteria example, int page, int pageSize);

    List<MallShop> selectByExample(MallShopCriteria example, int page, int pageSize);

    MallShop selectByPrimaryKey(String id);

    int updateByExampleSelective(MallShop record, MallShopCriteria example);

    int updateByExample(MallShop record, MallShopCriteria example);

    MallShop loadFromWriteDB(String id);
}
