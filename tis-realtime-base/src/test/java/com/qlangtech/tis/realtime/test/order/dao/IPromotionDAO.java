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
import com.qlangtech.tis.realtime.test.order.pojo.Promotion;
import com.qlangtech.tis.realtime.test.order.pojo.PromotionCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IPromotionDAO {

    int countByExample(PromotionCriteria example);

    int countFromWriteDB(PromotionCriteria example);

    int deleteByExample(PromotionCriteria criteria);

    int deleteByPrimaryKey(String promotionId);

    void insert(Promotion record);

    void insertSelective(Promotion record);

    List<Promotion> selectByExampleWithBLOBs(PromotionCriteria example);

    List<Promotion> selectByExampleWithoutBLOBs(PromotionCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(PromotionCriteria example, int page, int pageSize);

    List<Promotion> selectByExampleWithoutBLOBs(PromotionCriteria example, int page, int pageSize);

    Promotion selectByPrimaryKey(String promotionId);

    int updateByExampleSelective(Promotion record, PromotionCriteria example);

    int updateByExampleWithBLOBs(Promotion record, PromotionCriteria example);

    int updateByExampleWithoutBLOBs(Promotion record, PromotionCriteria example);

    Promotion loadFromWriteDB(String promotionId);
}
