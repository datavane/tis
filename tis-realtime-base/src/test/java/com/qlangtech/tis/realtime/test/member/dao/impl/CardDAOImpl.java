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
package com.qlangtech.tis.realtime.test.member.dao.impl;

import com.qlangtech.tis.ibatis.BasicDAO;
import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.member.dao.ICardDAO;
import com.qlangtech.tis.realtime.test.member.pojo.Card;
import com.qlangtech.tis.realtime.test.member.pojo.CardCriteria;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class CardDAOImpl extends BasicDAO<Card, CardCriteria> implements ICardDAO {

    public CardDAOImpl() {
        super();
    }

    public final String getEntityName() {
        return "card";
    }

    public int countByExample(CardCriteria example) {
        Integer count = (Integer) this.count("card.ibatorgenerated_countByExample", example);
        return count;
    }

    public int countFromWriteDB(CardCriteria example) {
        Integer count = (Integer) this.countFromWriterDB("card.ibatorgenerated_countByExample", example);
        return count;
    }

    public int deleteByExample(CardCriteria criteria) {
        return this.deleteRecords("card.ibatorgenerated_deleteByExample", criteria);
    }

    public int deleteByPrimaryKey(String id) {
        Card key = new Card();
        key.setId(id);
        return this.deleteRecords("card.ibatorgenerated_deleteByPrimaryKey", key);
    }

    public void insert(Card record) {
        this.insert("card.ibatorgenerated_insert", record);
    }

    public void insertSelective(Card record) {
        this.insert("card.ibatorgenerated_insertSelective", record);
    }

    public List<Card> selectByExample(CardCriteria criteria) {
        return this.selectByExample(criteria, 1, 100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(CardCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if (example.isTargetColsEmpty()) {
            throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.member.pojo.CardCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("card.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<Card> selectByExample(CardCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<Card> list = this.list("card.ibatorgenerated_selectByExample", example);
        return list;
    }

    public Card selectByPrimaryKey(String id) {
        Card key = new Card();
        key.setId(id);
        Card record = (Card) this.load("card.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(Card record, CardCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("card.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(Card record, CardCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("card.ibatorgenerated_updateByExample", parms);
    }

    public Card loadFromWriteDB(String id) {
        Card key = new Card();
        key.setId(id);
        Card record = (Card) this.loadFromWriterDB("card.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    private static class UpdateByExampleParms extends CardCriteria {

        private Object record;

        public UpdateByExampleParms(Object record, CardCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}
