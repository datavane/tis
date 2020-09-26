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
import com.qlangtech.tis.realtime.test.order.pojo.InstanceAsset;
import com.qlangtech.tis.realtime.test.order.pojo.InstanceAssetCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IInstanceAssetDAO {

    int countByExample(InstanceAssetCriteria example);

    int countFromWriteDB(InstanceAssetCriteria example);

    int deleteByExample(InstanceAssetCriteria criteria);

    int deleteByPrimaryKey(String id);

    void insert(InstanceAsset record);

    void insertSelective(InstanceAsset record);

    List<InstanceAsset> selectByExample(InstanceAssetCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(InstanceAssetCriteria example, int page, int pageSize);

    List<InstanceAsset> selectByExample(InstanceAssetCriteria example, int page, int pageSize);

    InstanceAsset selectByPrimaryKey(String id);

    int updateByExampleSelective(InstanceAsset record, InstanceAssetCriteria example);

    int updateByExample(InstanceAsset record, InstanceAssetCriteria example);

    InstanceAsset loadFromWriteDB(String id);
}
