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
package com.qlangtech.tis.realtime.test.order.pojo;

import com.qlangtech.tis.ibatis.BasicCriteria;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WaitingorderdetailCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<WaitingorderdetailColEnum> cols = Sets.newHashSet();

    public WaitingorderdetailCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected WaitingorderdetailCriteria(WaitingorderdetailCriteria example) {
        this.orderByClause = example.orderByClause;
        this.oredCriteria = example.oredCriteria;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
    }

    public final boolean isTargetColsEmpty() {
        return this.cols.size() < 1;
    }

    public final List<WaitingorderdetailColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(WaitingorderdetailColEnum... colName) {
        for (WaitingorderdetailColEnum c : colName) {
            if (!c.isPK()) {
                this.cols.add(c);
            }
        }
    }

    public static class Criteria {

        protected List<String> criteriaWithoutValue;

        protected List<Map<String, Object>> criteriaWithSingleValue;

        protected List<Map<String, Object>> criteriaWithListValue;

        protected List<Map<String, Object>> criteriaWithBetweenValue;

        protected Criteria() {
            super();
            criteriaWithoutValue = new ArrayList<String>();
            criteriaWithSingleValue = new ArrayList<Map<String, Object>>();
            criteriaWithListValue = new ArrayList<Map<String, Object>>();
            criteriaWithBetweenValue = new ArrayList<Map<String, Object>>();
        }

        public boolean isValid() {
            return criteriaWithoutValue.size() > 0 || criteriaWithSingleValue.size() > 0 || criteriaWithListValue.size() > 0 || criteriaWithBetweenValue.size() > 0;
        }

        public List<String> getCriteriaWithoutValue() {
            return criteriaWithoutValue;
        }

        public List<Map<String, Object>> getCriteriaWithSingleValue() {
            return criteriaWithSingleValue;
        }

        public List<Map<String, Object>> getCriteriaWithListValue() {
            return criteriaWithListValue;
        }

        public List<Map<String, Object>> getCriteriaWithBetweenValue() {
            return criteriaWithBetweenValue;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteriaWithoutValue.add(condition);
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("condition", condition);
            map.put("value", value);
            criteriaWithSingleValue.add(map);
        }

        protected void addCriterion(String condition, List<? extends Object> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("condition", condition);
            map.put("values", values);
            criteriaWithListValue.add(map);
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            List<Object> list = new ArrayList<Object>();
            list.add(value1);
            list.add(value2);
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("condition", condition);
            map.put("values", list);
            criteriaWithBetweenValue.add(map);
        }

        public Criteria andWaitingorderIdIsNull() {
            addCriterion("waitingorder_id is null");
            return this;
        }

        public Criteria andWaitingorderIdIsNotNull() {
            addCriterion("waitingorder_id is not null");
            return this;
        }

        public Criteria andWaitingorderIdEqualTo(String value) {
            addCriterion("waitingorder_id =", value, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdNotEqualTo(String value) {
            addCriterion("waitingorder_id <>", value, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdGreaterThan(String value) {
            addCriterion("waitingorder_id >", value, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdGreaterThanOrEqualTo(String value) {
            addCriterion("waitingorder_id >=", value, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdLessThan(String value) {
            addCriterion("waitingorder_id <", value, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdLessThanOrEqualTo(String value) {
            addCriterion("waitingorder_id <=", value, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdLike(String value) {
            addCriterion("waitingorder_id like", value, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdNotLike(String value) {
            addCriterion("waitingorder_id not like", value, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdIn(List<String> values) {
            addCriterion("waitingorder_id in", values, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdNotIn(List<String> values) {
            addCriterion("waitingorder_id not in", values, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdBetween(String value1, String value2) {
            addCriterion("waitingorder_id between", value1, value2, "waitingorderId");
            return this;
        }

        public Criteria andWaitingorderIdNotBetween(String value1, String value2) {
            addCriterion("waitingorder_id not between", value1, value2, "waitingorderId");
            return this;
        }

        public Criteria andOrderFromIsNull() {
            addCriterion("order_from is null");
            return this;
        }

        public Criteria andOrderFromIsNotNull() {
            addCriterion("order_from is not null");
            return this;
        }

        public Criteria andOrderFromEqualTo(Short value) {
            addCriterion("order_from =", value, "orderFrom");
            return this;
        }

        public Criteria andOrderFromNotEqualTo(Short value) {
            addCriterion("order_from <>", value, "orderFrom");
            return this;
        }

        public Criteria andOrderFromGreaterThan(Short value) {
            addCriterion("order_from >", value, "orderFrom");
            return this;
        }

        public Criteria andOrderFromGreaterThanOrEqualTo(Short value) {
            addCriterion("order_from >=", value, "orderFrom");
            return this;
        }

        public Criteria andOrderFromLessThan(Short value) {
            addCriterion("order_from <", value, "orderFrom");
            return this;
        }

        public Criteria andOrderFromLessThanOrEqualTo(Short value) {
            addCriterion("order_from <=", value, "orderFrom");
            return this;
        }

        public Criteria andOrderFromIn(List<Short> values) {
            addCriterion("order_from in", values, "orderFrom");
            return this;
        }

        public Criteria andOrderFromNotIn(List<Short> values) {
            addCriterion("order_from not in", values, "orderFrom");
            return this;
        }

        public Criteria andOrderFromBetween(Short value1, Short value2) {
            addCriterion("order_from between", value1, value2, "orderFrom");
            return this;
        }

        public Criteria andOrderFromNotBetween(Short value1, Short value2) {
            addCriterion("order_from not between", value1, value2, "orderFrom");
            return this;
        }

        public Criteria andBatchMsgIsNull() {
            addCriterion("batch_msg is null");
            return this;
        }

        public Criteria andBatchMsgIsNotNull() {
            addCriterion("batch_msg is not null");
            return this;
        }

        public Criteria andBatchMsgEqualTo(String value) {
            addCriterion("batch_msg =", value, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgNotEqualTo(String value) {
            addCriterion("batch_msg <>", value, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgGreaterThan(String value) {
            addCriterion("batch_msg >", value, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgGreaterThanOrEqualTo(String value) {
            addCriterion("batch_msg >=", value, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgLessThan(String value) {
            addCriterion("batch_msg <", value, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgLessThanOrEqualTo(String value) {
            addCriterion("batch_msg <=", value, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgLike(String value) {
            addCriterion("batch_msg like", value, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgNotLike(String value) {
            addCriterion("batch_msg not like", value, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgIn(List<String> values) {
            addCriterion("batch_msg in", values, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgNotIn(List<String> values) {
            addCriterion("batch_msg not in", values, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgBetween(String value1, String value2) {
            addCriterion("batch_msg between", value1, value2, "batchMsg");
            return this;
        }

        public Criteria andBatchMsgNotBetween(String value1, String value2) {
            addCriterion("batch_msg not between", value1, value2, "batchMsg");
            return this;
        }

        public Criteria andKindIsNull() {
            addCriterion("kind is null");
            return this;
        }

        public Criteria andKindIsNotNull() {
            addCriterion("kind is not null");
            return this;
        }

        public Criteria andKindEqualTo(Short value) {
            addCriterion("kind =", value, "kind");
            return this;
        }

        public Criteria andKindNotEqualTo(Short value) {
            addCriterion("kind <>", value, "kind");
            return this;
        }

        public Criteria andKindGreaterThan(Short value) {
            addCriterion("kind >", value, "kind");
            return this;
        }

        public Criteria andKindGreaterThanOrEqualTo(Short value) {
            addCriterion("kind >=", value, "kind");
            return this;
        }

        public Criteria andKindLessThan(Short value) {
            addCriterion("kind <", value, "kind");
            return this;
        }

        public Criteria andKindLessThanOrEqualTo(Short value) {
            addCriterion("kind <=", value, "kind");
            return this;
        }

        public Criteria andKindIn(List<Short> values) {
            addCriterion("kind in", values, "kind");
            return this;
        }

        public Criteria andKindNotIn(List<Short> values) {
            addCriterion("kind not in", values, "kind");
            return this;
        }

        public Criteria andKindBetween(Short value1, Short value2) {
            addCriterion("kind between", value1, value2, "kind");
            return this;
        }

        public Criteria andKindNotBetween(Short value1, Short value2) {
            addCriterion("kind not between", value1, value2, "kind");
            return this;
        }

        public Criteria andCodeIsNull() {
            addCriterion("code is null");
            return this;
        }

        public Criteria andCodeIsNotNull() {
            addCriterion("code is not null");
            return this;
        }

        public Criteria andCodeEqualTo(String value) {
            addCriterion("code =", value, "code");
            return this;
        }

        public Criteria andCodeNotEqualTo(String value) {
            addCriterion("code <>", value, "code");
            return this;
        }

        public Criteria andCodeGreaterThan(String value) {
            addCriterion("code >", value, "code");
            return this;
        }

        public Criteria andCodeGreaterThanOrEqualTo(String value) {
            addCriterion("code >=", value, "code");
            return this;
        }

        public Criteria andCodeLessThan(String value) {
            addCriterion("code <", value, "code");
            return this;
        }

        public Criteria andCodeLessThanOrEqualTo(String value) {
            addCriterion("code <=", value, "code");
            return this;
        }

        public Criteria andCodeLike(String value) {
            addCriterion("code like", value, "code");
            return this;
        }

        public Criteria andCodeNotLike(String value) {
            addCriterion("code not like", value, "code");
            return this;
        }

        public Criteria andCodeIn(List<String> values) {
            addCriterion("code in", values, "code");
            return this;
        }

        public Criteria andCodeNotIn(List<String> values) {
            addCriterion("code not in", values, "code");
            return this;
        }

        public Criteria andCodeBetween(String value1, String value2) {
            addCriterion("code between", value1, value2, "code");
            return this;
        }

        public Criteria andCodeNotBetween(String value1, String value2) {
            addCriterion("code not between", value1, value2, "code");
            return this;
        }

        public Criteria andSeatCodeIsNull() {
            addCriterion("seat_code is null");
            return this;
        }

        public Criteria andSeatCodeIsNotNull() {
            addCriterion("seat_code is not null");
            return this;
        }

        public Criteria andSeatCodeEqualTo(String value) {
            addCriterion("seat_code =", value, "seatCode");
            return this;
        }

        public Criteria andSeatCodeNotEqualTo(String value) {
            addCriterion("seat_code <>", value, "seatCode");
            return this;
        }

        public Criteria andSeatCodeGreaterThan(String value) {
            addCriterion("seat_code >", value, "seatCode");
            return this;
        }

        public Criteria andSeatCodeGreaterThanOrEqualTo(String value) {
            addCriterion("seat_code >=", value, "seatCode");
            return this;
        }

        public Criteria andSeatCodeLessThan(String value) {
            addCriterion("seat_code <", value, "seatCode");
            return this;
        }

        public Criteria andSeatCodeLessThanOrEqualTo(String value) {
            addCriterion("seat_code <=", value, "seatCode");
            return this;
        }

        public Criteria andSeatCodeLike(String value) {
            addCriterion("seat_code like", value, "seatCode");
            return this;
        }

        public Criteria andSeatCodeNotLike(String value) {
            addCriterion("seat_code not like", value, "seatCode");
            return this;
        }

        public Criteria andSeatCodeIn(List<String> values) {
            addCriterion("seat_code in", values, "seatCode");
            return this;
        }

        public Criteria andSeatCodeNotIn(List<String> values) {
            addCriterion("seat_code not in", values, "seatCode");
            return this;
        }

        public Criteria andSeatCodeBetween(String value1, String value2) {
            addCriterion("seat_code between", value1, value2, "seatCode");
            return this;
        }

        public Criteria andSeatCodeNotBetween(String value1, String value2) {
            addCriterion("seat_code not between", value1, value2, "seatCode");
            return this;
        }

        public Criteria andNameIsNull() {
            addCriterion("name is null");
            return this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("name is not null");
            return this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("name =", value, "name");
            return this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("name <>", value, "name");
            return this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("name >", value, "name");
            return this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("name >=", value, "name");
            return this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("name <", value, "name");
            return this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("name <=", value, "name");
            return this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("name like", value, "name");
            return this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("name not like", value, "name");
            return this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("name in", values, "name");
            return this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("name not in", values, "name");
            return this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("name between", value1, value2, "name");
            return this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("name not between", value1, value2, "name");
            return this;
        }

        public Criteria andPeopleCountIsNull() {
            addCriterion("people_count is null");
            return this;
        }

        public Criteria andPeopleCountIsNotNull() {
            addCriterion("people_count is not null");
            return this;
        }

        public Criteria andPeopleCountEqualTo(Integer value) {
            addCriterion("people_count =", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountNotEqualTo(Integer value) {
            addCriterion("people_count <>", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountGreaterThan(Integer value) {
            addCriterion("people_count >", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("people_count >=", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountLessThan(Integer value) {
            addCriterion("people_count <", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountLessThanOrEqualTo(Integer value) {
            addCriterion("people_count <=", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountIn(List<Integer> values) {
            addCriterion("people_count in", values, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountNotIn(List<Integer> values) {
            addCriterion("people_count not in", values, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountBetween(Integer value1, Integer value2) {
            addCriterion("people_count between", value1, value2, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountNotBetween(Integer value1, Integer value2) {
            addCriterion("people_count not between", value1, value2, "peopleCount");
            return this;
        }

        public Criteria andMobileIsNull() {
            addCriterion("mobile is null");
            return this;
        }

        public Criteria andMobileIsNotNull() {
            addCriterion("mobile is not null");
            return this;
        }

        public Criteria andMobileEqualTo(String value) {
            addCriterion("mobile =", value, "mobile");
            return this;
        }

        public Criteria andMobileNotEqualTo(String value) {
            addCriterion("mobile <>", value, "mobile");
            return this;
        }

        public Criteria andMobileGreaterThan(String value) {
            addCriterion("mobile >", value, "mobile");
            return this;
        }

        public Criteria andMobileGreaterThanOrEqualTo(String value) {
            addCriterion("mobile >=", value, "mobile");
            return this;
        }

        public Criteria andMobileLessThan(String value) {
            addCriterion("mobile <", value, "mobile");
            return this;
        }

        public Criteria andMobileLessThanOrEqualTo(String value) {
            addCriterion("mobile <=", value, "mobile");
            return this;
        }

        public Criteria andMobileLike(String value) {
            addCriterion("mobile like", value, "mobile");
            return this;
        }

        public Criteria andMobileNotLike(String value) {
            addCriterion("mobile not like", value, "mobile");
            return this;
        }

        public Criteria andMobileIn(List<String> values) {
            addCriterion("mobile in", values, "mobile");
            return this;
        }

        public Criteria andMobileNotIn(List<String> values) {
            addCriterion("mobile not in", values, "mobile");
            return this;
        }

        public Criteria andMobileBetween(String value1, String value2) {
            addCriterion("mobile between", value1, value2, "mobile");
            return this;
        }

        public Criteria andMobileNotBetween(String value1, String value2) {
            addCriterion("mobile not between", value1, value2, "mobile");
            return this;
        }

        public Criteria andTelIsNull() {
            addCriterion("tel is null");
            return this;
        }

        public Criteria andTelIsNotNull() {
            addCriterion("tel is not null");
            return this;
        }

        public Criteria andTelEqualTo(String value) {
            addCriterion("tel =", value, "tel");
            return this;
        }

        public Criteria andTelNotEqualTo(String value) {
            addCriterion("tel <>", value, "tel");
            return this;
        }

        public Criteria andTelGreaterThan(String value) {
            addCriterion("tel >", value, "tel");
            return this;
        }

        public Criteria andTelGreaterThanOrEqualTo(String value) {
            addCriterion("tel >=", value, "tel");
            return this;
        }

        public Criteria andTelLessThan(String value) {
            addCriterion("tel <", value, "tel");
            return this;
        }

        public Criteria andTelLessThanOrEqualTo(String value) {
            addCriterion("tel <=", value, "tel");
            return this;
        }

        public Criteria andTelLike(String value) {
            addCriterion("tel like", value, "tel");
            return this;
        }

        public Criteria andTelNotLike(String value) {
            addCriterion("tel not like", value, "tel");
            return this;
        }

        public Criteria andTelIn(List<String> values) {
            addCriterion("tel in", values, "tel");
            return this;
        }

        public Criteria andTelNotIn(List<String> values) {
            addCriterion("tel not in", values, "tel");
            return this;
        }

        public Criteria andTelBetween(String value1, String value2) {
            addCriterion("tel between", value1, value2, "tel");
            return this;
        }

        public Criteria andTelNotBetween(String value1, String value2) {
            addCriterion("tel not between", value1, value2, "tel");
            return this;
        }

        public Criteria andReserveDateIsNull() {
            addCriterion("reserve_date is null");
            return this;
        }

        public Criteria andReserveDateIsNotNull() {
            addCriterion("reserve_date is not null");
            return this;
        }

        public Criteria andReserveDateEqualTo(Long value) {
            addCriterion("reserve_date =", value, "reserveDate");
            return this;
        }

        public Criteria andReserveDateNotEqualTo(Long value) {
            addCriterion("reserve_date <>", value, "reserveDate");
            return this;
        }

        public Criteria andReserveDateGreaterThan(Long value) {
            addCriterion("reserve_date >", value, "reserveDate");
            return this;
        }

        public Criteria andReserveDateGreaterThanOrEqualTo(Long value) {
            addCriterion("reserve_date >=", value, "reserveDate");
            return this;
        }

        public Criteria andReserveDateLessThan(Long value) {
            addCriterion("reserve_date <", value, "reserveDate");
            return this;
        }

        public Criteria andReserveDateLessThanOrEqualTo(Long value) {
            addCriterion("reserve_date <=", value, "reserveDate");
            return this;
        }

        public Criteria andReserveDateIn(List<Long> values) {
            addCriterion("reserve_date in", values, "reserveDate");
            return this;
        }

        public Criteria andReserveDateNotIn(List<Long> values) {
            addCriterion("reserve_date not in", values, "reserveDate");
            return this;
        }

        public Criteria andReserveDateBetween(Long value1, Long value2) {
            addCriterion("reserve_date between", value1, value2, "reserveDate");
            return this;
        }

        public Criteria andReserveDateNotBetween(Long value1, Long value2) {
            addCriterion("reserve_date not between", value1, value2, "reserveDate");
            return this;
        }

        public Criteria andMemoIsNull() {
            addCriterion("memo is null");
            return this;
        }

        public Criteria andMemoIsNotNull() {
            addCriterion("memo is not null");
            return this;
        }

        public Criteria andMemoEqualTo(String value) {
            addCriterion("memo =", value, "memo");
            return this;
        }

        public Criteria andMemoNotEqualTo(String value) {
            addCriterion("memo <>", value, "memo");
            return this;
        }

        public Criteria andMemoGreaterThan(String value) {
            addCriterion("memo >", value, "memo");
            return this;
        }

        public Criteria andMemoGreaterThanOrEqualTo(String value) {
            addCriterion("memo >=", value, "memo");
            return this;
        }

        public Criteria andMemoLessThan(String value) {
            addCriterion("memo <", value, "memo");
            return this;
        }

        public Criteria andMemoLessThanOrEqualTo(String value) {
            addCriterion("memo <=", value, "memo");
            return this;
        }

        public Criteria andMemoLike(String value) {
            addCriterion("memo like", value, "memo");
            return this;
        }

        public Criteria andMemoNotLike(String value) {
            addCriterion("memo not like", value, "memo");
            return this;
        }

        public Criteria andMemoIn(List<String> values) {
            addCriterion("memo in", values, "memo");
            return this;
        }

        public Criteria andMemoNotIn(List<String> values) {
            addCriterion("memo not in", values, "memo");
            return this;
        }

        public Criteria andMemoBetween(String value1, String value2) {
            addCriterion("memo between", value1, value2, "memo");
            return this;
        }

        public Criteria andMemoNotBetween(String value1, String value2) {
            addCriterion("memo not between", value1, value2, "memo");
            return this;
        }

        public Criteria andTotalPriceIsNull() {
            addCriterion("total_price is null");
            return this;
        }

        public Criteria andTotalPriceIsNotNull() {
            addCriterion("total_price is not null");
            return this;
        }

        public Criteria andTotalPriceEqualTo(BigDecimal value) {
            addCriterion("total_price =", value, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceNotEqualTo(BigDecimal value) {
            addCriterion("total_price <>", value, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceGreaterThan(BigDecimal value) {
            addCriterion("total_price >", value, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("total_price >=", value, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceLessThan(BigDecimal value) {
            addCriterion("total_price <", value, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("total_price <=", value, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceIn(List<BigDecimal> values) {
            addCriterion("total_price in", values, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceNotIn(List<BigDecimal> values) {
            addCriterion("total_price not in", values, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_price between", value1, value2, "totalPrice");
            return this;
        }

        public Criteria andTotalPriceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("total_price not between", value1, value2, "totalPrice");
            return this;
        }

        public Criteria andRealPriceIsNull() {
            addCriterion("real_price is null");
            return this;
        }

        public Criteria andRealPriceIsNotNull() {
            addCriterion("real_price is not null");
            return this;
        }

        public Criteria andRealPriceEqualTo(BigDecimal value) {
            addCriterion("real_price =", value, "realPrice");
            return this;
        }

        public Criteria andRealPriceNotEqualTo(BigDecimal value) {
            addCriterion("real_price <>", value, "realPrice");
            return this;
        }

        public Criteria andRealPriceGreaterThan(BigDecimal value) {
            addCriterion("real_price >", value, "realPrice");
            return this;
        }

        public Criteria andRealPriceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("real_price >=", value, "realPrice");
            return this;
        }

        public Criteria andRealPriceLessThan(BigDecimal value) {
            addCriterion("real_price <", value, "realPrice");
            return this;
        }

        public Criteria andRealPriceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("real_price <=", value, "realPrice");
            return this;
        }

        public Criteria andRealPriceIn(List<BigDecimal> values) {
            addCriterion("real_price in", values, "realPrice");
            return this;
        }

        public Criteria andRealPriceNotIn(List<BigDecimal> values) {
            addCriterion("real_price not in", values, "realPrice");
            return this;
        }

        public Criteria andRealPriceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("real_price between", value1, value2, "realPrice");
            return this;
        }

        public Criteria andRealPriceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("real_price not between", value1, value2, "realPrice");
            return this;
        }

        public Criteria andShopnameIsNull() {
            addCriterion("shopname is null");
            return this;
        }

        public Criteria andShopnameIsNotNull() {
            addCriterion("shopname is not null");
            return this;
        }

        public Criteria andShopnameEqualTo(String value) {
            addCriterion("shopname =", value, "shopname");
            return this;
        }

        public Criteria andShopnameNotEqualTo(String value) {
            addCriterion("shopname <>", value, "shopname");
            return this;
        }

        public Criteria andShopnameGreaterThan(String value) {
            addCriterion("shopname >", value, "shopname");
            return this;
        }

        public Criteria andShopnameGreaterThanOrEqualTo(String value) {
            addCriterion("shopname >=", value, "shopname");
            return this;
        }

        public Criteria andShopnameLessThan(String value) {
            addCriterion("shopname <", value, "shopname");
            return this;
        }

        public Criteria andShopnameLessThanOrEqualTo(String value) {
            addCriterion("shopname <=", value, "shopname");
            return this;
        }

        public Criteria andShopnameLike(String value) {
            addCriterion("shopname like", value, "shopname");
            return this;
        }

        public Criteria andShopnameNotLike(String value) {
            addCriterion("shopname not like", value, "shopname");
            return this;
        }

        public Criteria andShopnameIn(List<String> values) {
            addCriterion("shopname in", values, "shopname");
            return this;
        }

        public Criteria andShopnameNotIn(List<String> values) {
            addCriterion("shopname not in", values, "shopname");
            return this;
        }

        public Criteria andShopnameBetween(String value1, String value2) {
            addCriterion("shopname between", value1, value2, "shopname");
            return this;
        }

        public Criteria andShopnameNotBetween(String value1, String value2) {
            addCriterion("shopname not between", value1, value2, "shopname");
            return this;
        }

        public Criteria andAddressIsNull() {
            addCriterion("address is null");
            return this;
        }

        public Criteria andAddressIsNotNull() {
            addCriterion("address is not null");
            return this;
        }

        public Criteria andAddressEqualTo(String value) {
            addCriterion("address =", value, "address");
            return this;
        }

        public Criteria andAddressNotEqualTo(String value) {
            addCriterion("address <>", value, "address");
            return this;
        }

        public Criteria andAddressGreaterThan(String value) {
            addCriterion("address >", value, "address");
            return this;
        }

        public Criteria andAddressGreaterThanOrEqualTo(String value) {
            addCriterion("address >=", value, "address");
            return this;
        }

        public Criteria andAddressLessThan(String value) {
            addCriterion("address <", value, "address");
            return this;
        }

        public Criteria andAddressLessThanOrEqualTo(String value) {
            addCriterion("address <=", value, "address");
            return this;
        }

        public Criteria andAddressLike(String value) {
            addCriterion("address like", value, "address");
            return this;
        }

        public Criteria andAddressNotLike(String value) {
            addCriterion("address not like", value, "address");
            return this;
        }

        public Criteria andAddressIn(List<String> values) {
            addCriterion("address in", values, "address");
            return this;
        }

        public Criteria andAddressNotIn(List<String> values) {
            addCriterion("address not in", values, "address");
            return this;
        }

        public Criteria andAddressBetween(String value1, String value2) {
            addCriterion("address between", value1, value2, "address");
            return this;
        }

        public Criteria andAddressNotBetween(String value1, String value2) {
            addCriterion("address not between", value1, value2, "address");
            return this;
        }

        public Criteria andPayModeIsNull() {
            addCriterion("pay_mode is null");
            return this;
        }

        public Criteria andPayModeIsNotNull() {
            addCriterion("pay_mode is not null");
            return this;
        }

        public Criteria andPayModeEqualTo(Short value) {
            addCriterion("pay_mode =", value, "payMode");
            return this;
        }

        public Criteria andPayModeNotEqualTo(Short value) {
            addCriterion("pay_mode <>", value, "payMode");
            return this;
        }

        public Criteria andPayModeGreaterThan(Short value) {
            addCriterion("pay_mode >", value, "payMode");
            return this;
        }

        public Criteria andPayModeGreaterThanOrEqualTo(Short value) {
            addCriterion("pay_mode >=", value, "payMode");
            return this;
        }

        public Criteria andPayModeLessThan(Short value) {
            addCriterion("pay_mode <", value, "payMode");
            return this;
        }

        public Criteria andPayModeLessThanOrEqualTo(Short value) {
            addCriterion("pay_mode <=", value, "payMode");
            return this;
        }

        public Criteria andPayModeIn(List<Short> values) {
            addCriterion("pay_mode in", values, "payMode");
            return this;
        }

        public Criteria andPayModeNotIn(List<Short> values) {
            addCriterion("pay_mode not in", values, "payMode");
            return this;
        }

        public Criteria andPayModeBetween(Short value1, Short value2) {
            addCriterion("pay_mode between", value1, value2, "payMode");
            return this;
        }

        public Criteria andPayModeNotBetween(Short value1, Short value2) {
            addCriterion("pay_mode not between", value1, value2, "payMode");
            return this;
        }

        public Criteria andPayTypeIsNull() {
            addCriterion("pay_type is null");
            return this;
        }

        public Criteria andPayTypeIsNotNull() {
            addCriterion("pay_type is not null");
            return this;
        }

        public Criteria andPayTypeEqualTo(Short value) {
            addCriterion("pay_type =", value, "payType");
            return this;
        }

        public Criteria andPayTypeNotEqualTo(Short value) {
            addCriterion("pay_type <>", value, "payType");
            return this;
        }

        public Criteria andPayTypeGreaterThan(Short value) {
            addCriterion("pay_type >", value, "payType");
            return this;
        }

        public Criteria andPayTypeGreaterThanOrEqualTo(Short value) {
            addCriterion("pay_type >=", value, "payType");
            return this;
        }

        public Criteria andPayTypeLessThan(Short value) {
            addCriterion("pay_type <", value, "payType");
            return this;
        }

        public Criteria andPayTypeLessThanOrEqualTo(Short value) {
            addCriterion("pay_type <=", value, "payType");
            return this;
        }

        public Criteria andPayTypeIn(List<Short> values) {
            addCriterion("pay_type in", values, "payType");
            return this;
        }

        public Criteria andPayTypeNotIn(List<Short> values) {
            addCriterion("pay_type not in", values, "payType");
            return this;
        }

        public Criteria andPayTypeBetween(Short value1, Short value2) {
            addCriterion("pay_type between", value1, value2, "payType");
            return this;
        }

        public Criteria andPayTypeNotBetween(Short value1, Short value2) {
            addCriterion("pay_type not between", value1, value2, "payType");
            return this;
        }

        public Criteria andPayMemoIsNull() {
            addCriterion("pay_memo is null");
            return this;
        }

        public Criteria andPayMemoIsNotNull() {
            addCriterion("pay_memo is not null");
            return this;
        }

        public Criteria andPayMemoEqualTo(String value) {
            addCriterion("pay_memo =", value, "payMemo");
            return this;
        }

        public Criteria andPayMemoNotEqualTo(String value) {
            addCriterion("pay_memo <>", value, "payMemo");
            return this;
        }

        public Criteria andPayMemoGreaterThan(String value) {
            addCriterion("pay_memo >", value, "payMemo");
            return this;
        }

        public Criteria andPayMemoGreaterThanOrEqualTo(String value) {
            addCriterion("pay_memo >=", value, "payMemo");
            return this;
        }

        public Criteria andPayMemoLessThan(String value) {
            addCriterion("pay_memo <", value, "payMemo");
            return this;
        }

        public Criteria andPayMemoLessThanOrEqualTo(String value) {
            addCriterion("pay_memo <=", value, "payMemo");
            return this;
        }

        public Criteria andPayMemoLike(String value) {
            addCriterion("pay_memo like", value, "payMemo");
            return this;
        }

        public Criteria andPayMemoNotLike(String value) {
            addCriterion("pay_memo not like", value, "payMemo");
            return this;
        }

        public Criteria andPayMemoIn(List<String> values) {
            addCriterion("pay_memo in", values, "payMemo");
            return this;
        }

        public Criteria andPayMemoNotIn(List<String> values) {
            addCriterion("pay_memo not in", values, "payMemo");
            return this;
        }

        public Criteria andPayMemoBetween(String value1, String value2) {
            addCriterion("pay_memo between", value1, value2, "payMemo");
            return this;
        }

        public Criteria andPayMemoNotBetween(String value1, String value2) {
            addCriterion("pay_memo not between", value1, value2, "payMemo");
            return this;
        }

        public Criteria andOutfeeIsNull() {
            addCriterion("outfee is null");
            return this;
        }

        public Criteria andOutfeeIsNotNull() {
            addCriterion("outfee is not null");
            return this;
        }

        public Criteria andOutfeeEqualTo(BigDecimal value) {
            addCriterion("outfee =", value, "outfee");
            return this;
        }

        public Criteria andOutfeeNotEqualTo(BigDecimal value) {
            addCriterion("outfee <>", value, "outfee");
            return this;
        }

        public Criteria andOutfeeGreaterThan(BigDecimal value) {
            addCriterion("outfee >", value, "outfee");
            return this;
        }

        public Criteria andOutfeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("outfee >=", value, "outfee");
            return this;
        }

        public Criteria andOutfeeLessThan(BigDecimal value) {
            addCriterion("outfee <", value, "outfee");
            return this;
        }

        public Criteria andOutfeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("outfee <=", value, "outfee");
            return this;
        }

        public Criteria andOutfeeIn(List<BigDecimal> values) {
            addCriterion("outfee in", values, "outfee");
            return this;
        }

        public Criteria andOutfeeNotIn(List<BigDecimal> values) {
            addCriterion("outfee not in", values, "outfee");
            return this;
        }

        public Criteria andOutfeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("outfee between", value1, value2, "outfee");
            return this;
        }

        public Criteria andOutfeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("outfee not between", value1, value2, "outfee");
            return this;
        }

        public Criteria andCardEntityIdIsNull() {
            addCriterion("card_entity_id is null");
            return this;
        }

        public Criteria andCardEntityIdIsNotNull() {
            addCriterion("card_entity_id is not null");
            return this;
        }

        public Criteria andCardEntityIdEqualTo(String value) {
            addCriterion("card_entity_id =", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdNotEqualTo(String value) {
            addCriterion("card_entity_id <>", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdGreaterThan(String value) {
            addCriterion("card_entity_id >", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdGreaterThanOrEqualTo(String value) {
            addCriterion("card_entity_id >=", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdLessThan(String value) {
            addCriterion("card_entity_id <", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdLessThanOrEqualTo(String value) {
            addCriterion("card_entity_id <=", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdLike(String value) {
            addCriterion("card_entity_id like", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdNotLike(String value) {
            addCriterion("card_entity_id not like", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdIn(List<String> values) {
            addCriterion("card_entity_id in", values, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdNotIn(List<String> values) {
            addCriterion("card_entity_id not in", values, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdBetween(String value1, String value2) {
            addCriterion("card_entity_id between", value1, value2, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdNotBetween(String value1, String value2) {
            addCriterion("card_entity_id not between", value1, value2, "cardEntityId");
            return this;
        }

        public Criteria andCardIdIsNull() {
            addCriterion("card_id is null");
            return this;
        }

        public Criteria andCardIdIsNotNull() {
            addCriterion("card_id is not null");
            return this;
        }

        public Criteria andCardIdEqualTo(String value) {
            addCriterion("card_id =", value, "cardId");
            return this;
        }

        public Criteria andCardIdNotEqualTo(String value) {
            addCriterion("card_id <>", value, "cardId");
            return this;
        }

        public Criteria andCardIdGreaterThan(String value) {
            addCriterion("card_id >", value, "cardId");
            return this;
        }

        public Criteria andCardIdGreaterThanOrEqualTo(String value) {
            addCriterion("card_id >=", value, "cardId");
            return this;
        }

        public Criteria andCardIdLessThan(String value) {
            addCriterion("card_id <", value, "cardId");
            return this;
        }

        public Criteria andCardIdLessThanOrEqualTo(String value) {
            addCriterion("card_id <=", value, "cardId");
            return this;
        }

        public Criteria andCardIdLike(String value) {
            addCriterion("card_id like", value, "cardId");
            return this;
        }

        public Criteria andCardIdNotLike(String value) {
            addCriterion("card_id not like", value, "cardId");
            return this;
        }

        public Criteria andCardIdIn(List<String> values) {
            addCriterion("card_id in", values, "cardId");
            return this;
        }

        public Criteria andCardIdNotIn(List<String> values) {
            addCriterion("card_id not in", values, "cardId");
            return this;
        }

        public Criteria andCardIdBetween(String value1, String value2) {
            addCriterion("card_id between", value1, value2, "cardId");
            return this;
        }

        public Criteria andCardIdNotBetween(String value1, String value2) {
            addCriterion("card_id not between", value1, value2, "cardId");
            return this;
        }

        public Criteria andPayIdIsNull() {
            addCriterion("pay_id is null");
            return this;
        }

        public Criteria andPayIdIsNotNull() {
            addCriterion("pay_id is not null");
            return this;
        }

        public Criteria andPayIdEqualTo(String value) {
            addCriterion("pay_id =", value, "payId");
            return this;
        }

        public Criteria andPayIdNotEqualTo(String value) {
            addCriterion("pay_id <>", value, "payId");
            return this;
        }

        public Criteria andPayIdGreaterThan(String value) {
            addCriterion("pay_id >", value, "payId");
            return this;
        }

        public Criteria andPayIdGreaterThanOrEqualTo(String value) {
            addCriterion("pay_id >=", value, "payId");
            return this;
        }

        public Criteria andPayIdLessThan(String value) {
            addCriterion("pay_id <", value, "payId");
            return this;
        }

        public Criteria andPayIdLessThanOrEqualTo(String value) {
            addCriterion("pay_id <=", value, "payId");
            return this;
        }

        public Criteria andPayIdLike(String value) {
            addCriterion("pay_id like", value, "payId");
            return this;
        }

        public Criteria andPayIdNotLike(String value) {
            addCriterion("pay_id not like", value, "payId");
            return this;
        }

        public Criteria andPayIdIn(List<String> values) {
            addCriterion("pay_id in", values, "payId");
            return this;
        }

        public Criteria andPayIdNotIn(List<String> values) {
            addCriterion("pay_id not in", values, "payId");
            return this;
        }

        public Criteria andPayIdBetween(String value1, String value2) {
            addCriterion("pay_id between", value1, value2, "payId");
            return this;
        }

        public Criteria andPayIdNotBetween(String value1, String value2) {
            addCriterion("pay_id not between", value1, value2, "payId");
            return this;
        }

        public Criteria andAdvancePayIsNull() {
            addCriterion("advance_pay is null");
            return this;
        }

        public Criteria andAdvancePayIsNotNull() {
            addCriterion("advance_pay is not null");
            return this;
        }

        public Criteria andAdvancePayEqualTo(BigDecimal value) {
            addCriterion("advance_pay =", value, "advancePay");
            return this;
        }

        public Criteria andAdvancePayNotEqualTo(BigDecimal value) {
            addCriterion("advance_pay <>", value, "advancePay");
            return this;
        }

        public Criteria andAdvancePayGreaterThan(BigDecimal value) {
            addCriterion("advance_pay >", value, "advancePay");
            return this;
        }

        public Criteria andAdvancePayGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("advance_pay >=", value, "advancePay");
            return this;
        }

        public Criteria andAdvancePayLessThan(BigDecimal value) {
            addCriterion("advance_pay <", value, "advancePay");
            return this;
        }

        public Criteria andAdvancePayLessThanOrEqualTo(BigDecimal value) {
            addCriterion("advance_pay <=", value, "advancePay");
            return this;
        }

        public Criteria andAdvancePayIn(List<BigDecimal> values) {
            addCriterion("advance_pay in", values, "advancePay");
            return this;
        }

        public Criteria andAdvancePayNotIn(List<BigDecimal> values) {
            addCriterion("advance_pay not in", values, "advancePay");
            return this;
        }

        public Criteria andAdvancePayBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("advance_pay between", value1, value2, "advancePay");
            return this;
        }

        public Criteria andAdvancePayNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("advance_pay not between", value1, value2, "advancePay");
            return this;
        }

        public Criteria andAdvanceSeatPayIsNull() {
            addCriterion("advance_seat_pay is null");
            return this;
        }

        public Criteria andAdvanceSeatPayIsNotNull() {
            addCriterion("advance_seat_pay is not null");
            return this;
        }

        public Criteria andAdvanceSeatPayEqualTo(BigDecimal value) {
            addCriterion("advance_seat_pay =", value, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayNotEqualTo(BigDecimal value) {
            addCriterion("advance_seat_pay <>", value, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayGreaterThan(BigDecimal value) {
            addCriterion("advance_seat_pay >", value, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("advance_seat_pay >=", value, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayLessThan(BigDecimal value) {
            addCriterion("advance_seat_pay <", value, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayLessThanOrEqualTo(BigDecimal value) {
            addCriterion("advance_seat_pay <=", value, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayIn(List<BigDecimal> values) {
            addCriterion("advance_seat_pay in", values, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayNotIn(List<BigDecimal> values) {
            addCriterion("advance_seat_pay not in", values, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("advance_seat_pay between", value1, value2, "advanceSeatPay");
            return this;
        }

        public Criteria andAdvanceSeatPayNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("advance_seat_pay not between", value1, value2, "advanceSeatPay");
            return this;
        }

        public Criteria andPayStatusIsNull() {
            addCriterion("pay_status is null");
            return this;
        }

        public Criteria andPayStatusIsNotNull() {
            addCriterion("pay_status is not null");
            return this;
        }

        public Criteria andPayStatusEqualTo(Short value) {
            addCriterion("pay_status =", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusNotEqualTo(Short value) {
            addCriterion("pay_status <>", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusGreaterThan(Short value) {
            addCriterion("pay_status >", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("pay_status >=", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusLessThan(Short value) {
            addCriterion("pay_status <", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusLessThanOrEqualTo(Short value) {
            addCriterion("pay_status <=", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusIn(List<Short> values) {
            addCriterion("pay_status in", values, "payStatus");
            return this;
        }

        public Criteria andPayStatusNotIn(List<Short> values) {
            addCriterion("pay_status not in", values, "payStatus");
            return this;
        }

        public Criteria andPayStatusBetween(Short value1, Short value2) {
            addCriterion("pay_status between", value1, value2, "payStatus");
            return this;
        }

        public Criteria andPayStatusNotBetween(Short value1, Short value2) {
            addCriterion("pay_status not between", value1, value2, "payStatus");
            return this;
        }

        public Criteria andReserveSeatIdIsNull() {
            addCriterion("reserve_seat_id is null");
            return this;
        }

        public Criteria andReserveSeatIdIsNotNull() {
            addCriterion("reserve_seat_id is not null");
            return this;
        }

        public Criteria andReserveSeatIdEqualTo(String value) {
            addCriterion("reserve_seat_id =", value, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdNotEqualTo(String value) {
            addCriterion("reserve_seat_id <>", value, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdGreaterThan(String value) {
            addCriterion("reserve_seat_id >", value, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdGreaterThanOrEqualTo(String value) {
            addCriterion("reserve_seat_id >=", value, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdLessThan(String value) {
            addCriterion("reserve_seat_id <", value, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdLessThanOrEqualTo(String value) {
            addCriterion("reserve_seat_id <=", value, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdLike(String value) {
            addCriterion("reserve_seat_id like", value, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdNotLike(String value) {
            addCriterion("reserve_seat_id not like", value, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdIn(List<String> values) {
            addCriterion("reserve_seat_id in", values, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdNotIn(List<String> values) {
            addCriterion("reserve_seat_id not in", values, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdBetween(String value1, String value2) {
            addCriterion("reserve_seat_id between", value1, value2, "reserveSeatId");
            return this;
        }

        public Criteria andReserveSeatIdNotBetween(String value1, String value2) {
            addCriterion("reserve_seat_id not between", value1, value2, "reserveSeatId");
            return this;
        }

        public Criteria andReserveTimeIdIsNull() {
            addCriterion("reserve_time_id is null");
            return this;
        }

        public Criteria andReserveTimeIdIsNotNull() {
            addCriterion("reserve_time_id is not null");
            return this;
        }

        public Criteria andReserveTimeIdEqualTo(String value) {
            addCriterion("reserve_time_id =", value, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdNotEqualTo(String value) {
            addCriterion("reserve_time_id <>", value, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdGreaterThan(String value) {
            addCriterion("reserve_time_id >", value, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdGreaterThanOrEqualTo(String value) {
            addCriterion("reserve_time_id >=", value, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdLessThan(String value) {
            addCriterion("reserve_time_id <", value, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdLessThanOrEqualTo(String value) {
            addCriterion("reserve_time_id <=", value, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdLike(String value) {
            addCriterion("reserve_time_id like", value, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdNotLike(String value) {
            addCriterion("reserve_time_id not like", value, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdIn(List<String> values) {
            addCriterion("reserve_time_id in", values, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdNotIn(List<String> values) {
            addCriterion("reserve_time_id not in", values, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdBetween(String value1, String value2) {
            addCriterion("reserve_time_id between", value1, value2, "reserveTimeId");
            return this;
        }

        public Criteria andReserveTimeIdNotBetween(String value1, String value2) {
            addCriterion("reserve_time_id not between", value1, value2, "reserveTimeId");
            return this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return this;
        }

        public Criteria andStatusEqualTo(Short value) {
            addCriterion("status =", value, "status");
            return this;
        }

        public Criteria andStatusNotEqualTo(Short value) {
            addCriterion("status <>", value, "status");
            return this;
        }

        public Criteria andStatusGreaterThan(Short value) {
            addCriterion("status >", value, "status");
            return this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("status >=", value, "status");
            return this;
        }

        public Criteria andStatusLessThan(Short value) {
            addCriterion("status <", value, "status");
            return this;
        }

        public Criteria andStatusLessThanOrEqualTo(Short value) {
            addCriterion("status <=", value, "status");
            return this;
        }

        public Criteria andStatusIn(List<Short> values) {
            addCriterion("status in", values, "status");
            return this;
        }

        public Criteria andStatusNotIn(List<Short> values) {
            addCriterion("status not in", values, "status");
            return this;
        }

        public Criteria andStatusBetween(Short value1, Short value2) {
            addCriterion("status between", value1, value2, "status");
            return this;
        }

        public Criteria andStatusNotBetween(Short value1, Short value2) {
            addCriterion("status not between", value1, value2, "status");
            return this;
        }

        public Criteria andHideStatusIsNull() {
            addCriterion("hide_status is null");
            return this;
        }

        public Criteria andHideStatusIsNotNull() {
            addCriterion("hide_status is not null");
            return this;
        }

        public Criteria andHideStatusEqualTo(Short value) {
            addCriterion("hide_status =", value, "hideStatus");
            return this;
        }

        public Criteria andHideStatusNotEqualTo(Short value) {
            addCriterion("hide_status <>", value, "hideStatus");
            return this;
        }

        public Criteria andHideStatusGreaterThan(Short value) {
            addCriterion("hide_status >", value, "hideStatus");
            return this;
        }

        public Criteria andHideStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("hide_status >=", value, "hideStatus");
            return this;
        }

        public Criteria andHideStatusLessThan(Short value) {
            addCriterion("hide_status <", value, "hideStatus");
            return this;
        }

        public Criteria andHideStatusLessThanOrEqualTo(Short value) {
            addCriterion("hide_status <=", value, "hideStatus");
            return this;
        }

        public Criteria andHideStatusIn(List<Short> values) {
            addCriterion("hide_status in", values, "hideStatus");
            return this;
        }

        public Criteria andHideStatusNotIn(List<Short> values) {
            addCriterion("hide_status not in", values, "hideStatus");
            return this;
        }

        public Criteria andHideStatusBetween(Short value1, Short value2) {
            addCriterion("hide_status between", value1, value2, "hideStatus");
            return this;
        }

        public Criteria andHideStatusNotBetween(Short value1, Short value2) {
            addCriterion("hide_status not between", value1, value2, "hideStatus");
            return this;
        }

        public Criteria andReserveStatusIsNull() {
            addCriterion("reserve_status is null");
            return this;
        }

        public Criteria andReserveStatusIsNotNull() {
            addCriterion("reserve_status is not null");
            return this;
        }

        public Criteria andReserveStatusEqualTo(Short value) {
            addCriterion("reserve_status =", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusNotEqualTo(Short value) {
            addCriterion("reserve_status <>", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusGreaterThan(Short value) {
            addCriterion("reserve_status >", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("reserve_status >=", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusLessThan(Short value) {
            addCriterion("reserve_status <", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusLessThanOrEqualTo(Short value) {
            addCriterion("reserve_status <=", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusIn(List<Short> values) {
            addCriterion("reserve_status in", values, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusNotIn(List<Short> values) {
            addCriterion("reserve_status not in", values, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusBetween(Short value1, Short value2) {
            addCriterion("reserve_status between", value1, value2, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusNotBetween(Short value1, Short value2) {
            addCriterion("reserve_status not between", value1, value2, "reserveStatus");
            return this;
        }

        public Criteria andAuditStatusIsNull() {
            addCriterion("audit_status is null");
            return this;
        }

        public Criteria andAuditStatusIsNotNull() {
            addCriterion("audit_status is not null");
            return this;
        }

        public Criteria andAuditStatusEqualTo(Short value) {
            addCriterion("audit_status =", value, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusNotEqualTo(Short value) {
            addCriterion("audit_status <>", value, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusGreaterThan(Short value) {
            addCriterion("audit_status >", value, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("audit_status >=", value, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusLessThan(Short value) {
            addCriterion("audit_status <", value, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusLessThanOrEqualTo(Short value) {
            addCriterion("audit_status <=", value, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusIn(List<Short> values) {
            addCriterion("audit_status in", values, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusNotIn(List<Short> values) {
            addCriterion("audit_status not in", values, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusBetween(Short value1, Short value2) {
            addCriterion("audit_status between", value1, value2, "auditStatus");
            return this;
        }

        public Criteria andAuditStatusNotBetween(Short value1, Short value2) {
            addCriterion("audit_status not between", value1, value2, "auditStatus");
            return this;
        }

        public Criteria andOrderIdIsNull() {
            addCriterion("order_id is null");
            return this;
        }

        public Criteria andOrderIdIsNotNull() {
            addCriterion("order_id is not null");
            return this;
        }

        public Criteria andOrderIdEqualTo(String value) {
            addCriterion("order_id =", value, "orderId");
            return this;
        }

        public Criteria andOrderIdNotEqualTo(String value) {
            addCriterion("order_id <>", value, "orderId");
            return this;
        }

        public Criteria andOrderIdGreaterThan(String value) {
            addCriterion("order_id >", value, "orderId");
            return this;
        }

        public Criteria andOrderIdGreaterThanOrEqualTo(String value) {
            addCriterion("order_id >=", value, "orderId");
            return this;
        }

        public Criteria andOrderIdLessThan(String value) {
            addCriterion("order_id <", value, "orderId");
            return this;
        }

        public Criteria andOrderIdLessThanOrEqualTo(String value) {
            addCriterion("order_id <=", value, "orderId");
            return this;
        }

        public Criteria andOrderIdLike(String value) {
            addCriterion("order_id like", value, "orderId");
            return this;
        }

        public Criteria andOrderIdNotLike(String value) {
            addCriterion("order_id not like", value, "orderId");
            return this;
        }

        public Criteria andOrderIdIn(List<String> values) {
            addCriterion("order_id in", values, "orderId");
            return this;
        }

        public Criteria andOrderIdNotIn(List<String> values) {
            addCriterion("order_id not in", values, "orderId");
            return this;
        }

        public Criteria andOrderIdBetween(String value1, String value2) {
            addCriterion("order_id between", value1, value2, "orderId");
            return this;
        }

        public Criteria andOrderIdNotBetween(String value1, String value2) {
            addCriterion("order_id not between", value1, value2, "orderId");
            return this;
        }

        public Criteria andDealMessageIsNull() {
            addCriterion("deal_message is null");
            return this;
        }

        public Criteria andDealMessageIsNotNull() {
            addCriterion("deal_message is not null");
            return this;
        }

        public Criteria andDealMessageEqualTo(String value) {
            addCriterion("deal_message =", value, "dealMessage");
            return this;
        }

        public Criteria andDealMessageNotEqualTo(String value) {
            addCriterion("deal_message <>", value, "dealMessage");
            return this;
        }

        public Criteria andDealMessageGreaterThan(String value) {
            addCriterion("deal_message >", value, "dealMessage");
            return this;
        }

        public Criteria andDealMessageGreaterThanOrEqualTo(String value) {
            addCriterion("deal_message >=", value, "dealMessage");
            return this;
        }

        public Criteria andDealMessageLessThan(String value) {
            addCriterion("deal_message <", value, "dealMessage");
            return this;
        }

        public Criteria andDealMessageLessThanOrEqualTo(String value) {
            addCriterion("deal_message <=", value, "dealMessage");
            return this;
        }

        public Criteria andDealMessageLike(String value) {
            addCriterion("deal_message like", value, "dealMessage");
            return this;
        }

        public Criteria andDealMessageNotLike(String value) {
            addCriterion("deal_message not like", value, "dealMessage");
            return this;
        }

        public Criteria andDealMessageIn(List<String> values) {
            addCriterion("deal_message in", values, "dealMessage");
            return this;
        }

        public Criteria andDealMessageNotIn(List<String> values) {
            addCriterion("deal_message not in", values, "dealMessage");
            return this;
        }

        public Criteria andDealMessageBetween(String value1, String value2) {
            addCriterion("deal_message between", value1, value2, "dealMessage");
            return this;
        }

        public Criteria andDealMessageNotBetween(String value1, String value2) {
            addCriterion("deal_message not between", value1, value2, "dealMessage");
            return this;
        }

        public Criteria andErrormessageIsNull() {
            addCriterion("errormessage is null");
            return this;
        }

        public Criteria andErrormessageIsNotNull() {
            addCriterion("errormessage is not null");
            return this;
        }

        public Criteria andErrormessageEqualTo(String value) {
            addCriterion("errormessage =", value, "errormessage");
            return this;
        }

        public Criteria andErrormessageNotEqualTo(String value) {
            addCriterion("errormessage <>", value, "errormessage");
            return this;
        }

        public Criteria andErrormessageGreaterThan(String value) {
            addCriterion("errormessage >", value, "errormessage");
            return this;
        }

        public Criteria andErrormessageGreaterThanOrEqualTo(String value) {
            addCriterion("errormessage >=", value, "errormessage");
            return this;
        }

        public Criteria andErrormessageLessThan(String value) {
            addCriterion("errormessage <", value, "errormessage");
            return this;
        }

        public Criteria andErrormessageLessThanOrEqualTo(String value) {
            addCriterion("errormessage <=", value, "errormessage");
            return this;
        }

        public Criteria andErrormessageLike(String value) {
            addCriterion("errormessage like", value, "errormessage");
            return this;
        }

        public Criteria andErrormessageNotLike(String value) {
            addCriterion("errormessage not like", value, "errormessage");
            return this;
        }

        public Criteria andErrormessageIn(List<String> values) {
            addCriterion("errormessage in", values, "errormessage");
            return this;
        }

        public Criteria andErrormessageNotIn(List<String> values) {
            addCriterion("errormessage not in", values, "errormessage");
            return this;
        }

        public Criteria andErrormessageBetween(String value1, String value2) {
            addCriterion("errormessage between", value1, value2, "errormessage");
            return this;
        }

        public Criteria andErrormessageNotBetween(String value1, String value2) {
            addCriterion("errormessage not between", value1, value2, "errormessage");
            return this;
        }

        public Criteria andSenderIsNull() {
            addCriterion("sender is null");
            return this;
        }

        public Criteria andSenderIsNotNull() {
            addCriterion("sender is not null");
            return this;
        }

        public Criteria andSenderEqualTo(String value) {
            addCriterion("sender =", value, "sender");
            return this;
        }

        public Criteria andSenderNotEqualTo(String value) {
            addCriterion("sender <>", value, "sender");
            return this;
        }

        public Criteria andSenderGreaterThan(String value) {
            addCriterion("sender >", value, "sender");
            return this;
        }

        public Criteria andSenderGreaterThanOrEqualTo(String value) {
            addCriterion("sender >=", value, "sender");
            return this;
        }

        public Criteria andSenderLessThan(String value) {
            addCriterion("sender <", value, "sender");
            return this;
        }

        public Criteria andSenderLessThanOrEqualTo(String value) {
            addCriterion("sender <=", value, "sender");
            return this;
        }

        public Criteria andSenderLike(String value) {
            addCriterion("sender like", value, "sender");
            return this;
        }

        public Criteria andSenderNotLike(String value) {
            addCriterion("sender not like", value, "sender");
            return this;
        }

        public Criteria andSenderIn(List<String> values) {
            addCriterion("sender in", values, "sender");
            return this;
        }

        public Criteria andSenderNotIn(List<String> values) {
            addCriterion("sender not in", values, "sender");
            return this;
        }

        public Criteria andSenderBetween(String value1, String value2) {
            addCriterion("sender between", value1, value2, "sender");
            return this;
        }

        public Criteria andSenderNotBetween(String value1, String value2) {
            addCriterion("sender not between", value1, value2, "sender");
            return this;
        }

        public Criteria andSenderIdIsNull() {
            addCriterion("sender_id is null");
            return this;
        }

        public Criteria andSenderIdIsNotNull() {
            addCriterion("sender_id is not null");
            return this;
        }

        public Criteria andSenderIdEqualTo(String value) {
            addCriterion("sender_id =", value, "senderId");
            return this;
        }

        public Criteria andSenderIdNotEqualTo(String value) {
            addCriterion("sender_id <>", value, "senderId");
            return this;
        }

        public Criteria andSenderIdGreaterThan(String value) {
            addCriterion("sender_id >", value, "senderId");
            return this;
        }

        public Criteria andSenderIdGreaterThanOrEqualTo(String value) {
            addCriterion("sender_id >=", value, "senderId");
            return this;
        }

        public Criteria andSenderIdLessThan(String value) {
            addCriterion("sender_id <", value, "senderId");
            return this;
        }

        public Criteria andSenderIdLessThanOrEqualTo(String value) {
            addCriterion("sender_id <=", value, "senderId");
            return this;
        }

        public Criteria andSenderIdLike(String value) {
            addCriterion("sender_id like", value, "senderId");
            return this;
        }

        public Criteria andSenderIdNotLike(String value) {
            addCriterion("sender_id not like", value, "senderId");
            return this;
        }

        public Criteria andSenderIdIn(List<String> values) {
            addCriterion("sender_id in", values, "senderId");
            return this;
        }

        public Criteria andSenderIdNotIn(List<String> values) {
            addCriterion("sender_id not in", values, "senderId");
            return this;
        }

        public Criteria andSenderIdBetween(String value1, String value2) {
            addCriterion("sender_id between", value1, value2, "senderId");
            return this;
        }

        public Criteria andSenderIdNotBetween(String value1, String value2) {
            addCriterion("sender_id not between", value1, value2, "senderId");
            return this;
        }

        public Criteria andCustomerregisterIdIsNull() {
            addCriterion("customerregister_id is null");
            return this;
        }

        public Criteria andCustomerregisterIdIsNotNull() {
            addCriterion("customerregister_id is not null");
            return this;
        }

        public Criteria andCustomerregisterIdEqualTo(String value) {
            addCriterion("customerregister_id =", value, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdNotEqualTo(String value) {
            addCriterion("customerregister_id <>", value, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdGreaterThan(String value) {
            addCriterion("customerregister_id >", value, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdGreaterThanOrEqualTo(String value) {
            addCriterion("customerregister_id >=", value, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdLessThan(String value) {
            addCriterion("customerregister_id <", value, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdLessThanOrEqualTo(String value) {
            addCriterion("customerregister_id <=", value, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdLike(String value) {
            addCriterion("customerregister_id like", value, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdNotLike(String value) {
            addCriterion("customerregister_id not like", value, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdIn(List<String> values) {
            addCriterion("customerregister_id in", values, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdNotIn(List<String> values) {
            addCriterion("customerregister_id not in", values, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdBetween(String value1, String value2) {
            addCriterion("customerregister_id between", value1, value2, "customerregisterId");
            return this;
        }

        public Criteria andCustomerregisterIdNotBetween(String value1, String value2) {
            addCriterion("customerregister_id not between", value1, value2, "customerregisterId");
            return this;
        }

        public Criteria andEntityIdIsNull() {
            addCriterion("entity_id is null");
            return this;
        }

        public Criteria andEntityIdIsNotNull() {
            addCriterion("entity_id is not null");
            return this;
        }

        public Criteria andEntityIdEqualTo(String value) {
            addCriterion("entity_id =", value, "entityId");
            return this;
        }

        public Criteria andEntityIdNotEqualTo(String value) {
            addCriterion("entity_id <>", value, "entityId");
            return this;
        }

        public Criteria andEntityIdGreaterThan(String value) {
            addCriterion("entity_id >", value, "entityId");
            return this;
        }

        public Criteria andEntityIdGreaterThanOrEqualTo(String value) {
            addCriterion("entity_id >=", value, "entityId");
            return this;
        }

        public Criteria andEntityIdLessThan(String value) {
            addCriterion("entity_id <", value, "entityId");
            return this;
        }

        public Criteria andEntityIdLessThanOrEqualTo(String value) {
            addCriterion("entity_id <=", value, "entityId");
            return this;
        }

        public Criteria andEntityIdLike(String value) {
            addCriterion("entity_id like", value, "entityId");
            return this;
        }

        public Criteria andEntityIdNotLike(String value) {
            addCriterion("entity_id not like", value, "entityId");
            return this;
        }

        public Criteria andEntityIdIn(List<String> values) {
            addCriterion("entity_id in", values, "entityId");
            return this;
        }

        public Criteria andEntityIdNotIn(List<String> values) {
            addCriterion("entity_id not in", values, "entityId");
            return this;
        }

        public Criteria andEntityIdBetween(String value1, String value2) {
            addCriterion("entity_id between", value1, value2, "entityId");
            return this;
        }

        public Criteria andEntityIdNotBetween(String value1, String value2) {
            addCriterion("entity_id not between", value1, value2, "entityId");
            return this;
        }

        public Criteria andIsValidIsNull() {
            addCriterion("is_valid is null");
            return this;
        }

        public Criteria andIsValidIsNotNull() {
            addCriterion("is_valid is not null");
            return this;
        }

        public Criteria andIsValidEqualTo(Short value) {
            addCriterion("is_valid =", value, "isValid");
            return this;
        }

        public Criteria andIsValidNotEqualTo(Short value) {
            addCriterion("is_valid <>", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThan(Short value) {
            addCriterion("is_valid >", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(Short value) {
            addCriterion("is_valid >=", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThan(Short value) {
            addCriterion("is_valid <", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThanOrEqualTo(Short value) {
            addCriterion("is_valid <=", value, "isValid");
            return this;
        }

        public Criteria andIsValidIn(List<Short> values) {
            addCriterion("is_valid in", values, "isValid");
            return this;
        }

        public Criteria andIsValidNotIn(List<Short> values) {
            addCriterion("is_valid not in", values, "isValid");
            return this;
        }

        public Criteria andIsValidBetween(Short value1, Short value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return this;
        }

        public Criteria andIsValidNotBetween(Short value1, Short value2) {
            addCriterion("is_valid not between", value1, value2, "isValid");
            return this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return this;
        }

        public Criteria andCreateTimeEqualTo(Long value) {
            addCriterion("create_time =", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotEqualTo(Long value) {
            addCriterion("create_time <>", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeGreaterThan(Long value) {
            addCriterion("create_time >", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("create_time >=", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeLessThan(Long value) {
            addCriterion("create_time <", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Long value) {
            addCriterion("create_time <=", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeIn(List<Long> values) {
            addCriterion("create_time in", values, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotIn(List<Long> values) {
            addCriterion("create_time not in", values, "createTime");
            return this;
        }

        public Criteria andCreateTimeBetween(Long value1, Long value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotBetween(Long value1, Long value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return this;
        }

        public Criteria andOpTimeIsNull() {
            addCriterion("op_time is null");
            return this;
        }

        public Criteria andOpTimeIsNotNull() {
            addCriterion("op_time is not null");
            return this;
        }

        public Criteria andOpTimeEqualTo(Long value) {
            addCriterion("op_time =", value, "opTime");
            return this;
        }

        public Criteria andOpTimeNotEqualTo(Long value) {
            addCriterion("op_time <>", value, "opTime");
            return this;
        }

        public Criteria andOpTimeGreaterThan(Long value) {
            addCriterion("op_time >", value, "opTime");
            return this;
        }

        public Criteria andOpTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("op_time >=", value, "opTime");
            return this;
        }

        public Criteria andOpTimeLessThan(Long value) {
            addCriterion("op_time <", value, "opTime");
            return this;
        }

        public Criteria andOpTimeLessThanOrEqualTo(Long value) {
            addCriterion("op_time <=", value, "opTime");
            return this;
        }

        public Criteria andOpTimeIn(List<Long> values) {
            addCriterion("op_time in", values, "opTime");
            return this;
        }

        public Criteria andOpTimeNotIn(List<Long> values) {
            addCriterion("op_time not in", values, "opTime");
            return this;
        }

        public Criteria andOpTimeBetween(Long value1, Long value2) {
            addCriterion("op_time between", value1, value2, "opTime");
            return this;
        }

        public Criteria andOpTimeNotBetween(Long value1, Long value2) {
            addCriterion("op_time not between", value1, value2, "opTime");
            return this;
        }

        public Criteria andLastVerIsNull() {
            addCriterion("last_ver is null");
            return this;
        }

        public Criteria andLastVerIsNotNull() {
            addCriterion("last_ver is not null");
            return this;
        }

        public Criteria andLastVerEqualTo(Long value) {
            addCriterion("last_ver =", value, "lastVer");
            return this;
        }

        public Criteria andLastVerNotEqualTo(Long value) {
            addCriterion("last_ver <>", value, "lastVer");
            return this;
        }

        public Criteria andLastVerGreaterThan(Long value) {
            addCriterion("last_ver >", value, "lastVer");
            return this;
        }

        public Criteria andLastVerGreaterThanOrEqualTo(Long value) {
            addCriterion("last_ver >=", value, "lastVer");
            return this;
        }

        public Criteria andLastVerLessThan(Long value) {
            addCriterion("last_ver <", value, "lastVer");
            return this;
        }

        public Criteria andLastVerLessThanOrEqualTo(Long value) {
            addCriterion("last_ver <=", value, "lastVer");
            return this;
        }

        public Criteria andLastVerIn(List<Long> values) {
            addCriterion("last_ver in", values, "lastVer");
            return this;
        }

        public Criteria andLastVerNotIn(List<Long> values) {
            addCriterion("last_ver not in", values, "lastVer");
            return this;
        }

        public Criteria andLastVerBetween(Long value1, Long value2) {
            addCriterion("last_ver between", value1, value2, "lastVer");
            return this;
        }

        public Criteria andLastVerNotBetween(Long value1, Long value2) {
            addCriterion("last_ver not between", value1, value2, "lastVer");
            return this;
        }

        public Criteria andOutIdIsNull() {
            addCriterion("out_id is null");
            return this;
        }

        public Criteria andOutIdIsNotNull() {
            addCriterion("out_id is not null");
            return this;
        }

        public Criteria andOutIdEqualTo(String value) {
            addCriterion("out_id =", value, "outId");
            return this;
        }

        public Criteria andOutIdNotEqualTo(String value) {
            addCriterion("out_id <>", value, "outId");
            return this;
        }

        public Criteria andOutIdGreaterThan(String value) {
            addCriterion("out_id >", value, "outId");
            return this;
        }

        public Criteria andOutIdGreaterThanOrEqualTo(String value) {
            addCriterion("out_id >=", value, "outId");
            return this;
        }

        public Criteria andOutIdLessThan(String value) {
            addCriterion("out_id <", value, "outId");
            return this;
        }

        public Criteria andOutIdLessThanOrEqualTo(String value) {
            addCriterion("out_id <=", value, "outId");
            return this;
        }

        public Criteria andOutIdLike(String value) {
            addCriterion("out_id like", value, "outId");
            return this;
        }

        public Criteria andOutIdNotLike(String value) {
            addCriterion("out_id not like", value, "outId");
            return this;
        }

        public Criteria andOutIdIn(List<String> values) {
            addCriterion("out_id in", values, "outId");
            return this;
        }

        public Criteria andOutIdNotIn(List<String> values) {
            addCriterion("out_id not in", values, "outId");
            return this;
        }

        public Criteria andOutIdBetween(String value1, String value2) {
            addCriterion("out_id between", value1, value2, "outId");
            return this;
        }

        public Criteria andOutIdNotBetween(String value1, String value2) {
            addCriterion("out_id not between", value1, value2, "outId");
            return this;
        }

        public Criteria andOutTypeIsNull() {
            addCriterion("out_type is null");
            return this;
        }

        public Criteria andOutTypeIsNotNull() {
            addCriterion("out_type is not null");
            return this;
        }

        public Criteria andOutTypeEqualTo(Short value) {
            addCriterion("out_type =", value, "outType");
            return this;
        }

        public Criteria andOutTypeNotEqualTo(Short value) {
            addCriterion("out_type <>", value, "outType");
            return this;
        }

        public Criteria andOutTypeGreaterThan(Short value) {
            addCriterion("out_type >", value, "outType");
            return this;
        }

        public Criteria andOutTypeGreaterThanOrEqualTo(Short value) {
            addCriterion("out_type >=", value, "outType");
            return this;
        }

        public Criteria andOutTypeLessThan(Short value) {
            addCriterion("out_type <", value, "outType");
            return this;
        }

        public Criteria andOutTypeLessThanOrEqualTo(Short value) {
            addCriterion("out_type <=", value, "outType");
            return this;
        }

        public Criteria andOutTypeIn(List<Short> values) {
            addCriterion("out_type in", values, "outType");
            return this;
        }

        public Criteria andOutTypeNotIn(List<Short> values) {
            addCriterion("out_type not in", values, "outType");
            return this;
        }

        public Criteria andOutTypeBetween(Short value1, Short value2) {
            addCriterion("out_type between", value1, value2, "outType");
            return this;
        }

        public Criteria andOutTypeNotBetween(Short value1, Short value2) {
            addCriterion("out_type not between", value1, value2, "outType");
            return this;
        }
    }
}
