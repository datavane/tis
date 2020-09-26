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
package com.qlangtech.tis.realtime.test.member.dao;

import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.member.pojo.Card;
import com.qlangtech.tis.realtime.test.member.pojo.CardCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface ICardDAO {

    int countByExample(CardCriteria example);

    int countFromWriteDB(CardCriteria example);

    int deleteByExample(CardCriteria criteria);

    int deleteByPrimaryKey(String id);

    void insert(Card record);

    void insertSelective(Card record);

    List<Card> selectByExample(CardCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(CardCriteria example, int page, int pageSize);

    List<Card> selectByExample(CardCriteria example, int page, int pageSize);

    Card selectByPrimaryKey(String id);

    int updateByExampleSelective(Card record, CardCriteria example);

    int updateByExample(Card record, CardCriteria example);

    Card loadFromWriteDB(String id);
}
