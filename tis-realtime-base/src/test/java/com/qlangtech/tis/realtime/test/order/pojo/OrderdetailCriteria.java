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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.ibatis.BasicCriteria;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderdetailCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<OrderdetailColEnum> cols = Sets.newHashSet();

    public OrderdetailCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected OrderdetailCriteria(OrderdetailCriteria example) {
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

    public final List<OrderdetailColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(OrderdetailColEnum... colName) {
        for (OrderdetailColEnum c : colName) {
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

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List<Date> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List<java.sql.Date> dateList = new ArrayList<java.sql.Date>();
            Iterator<Date> iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(iter.next().getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
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

        public Criteria andGlobalCodeIsNull() {
            addCriterion("global_code is null");
            return this;
        }

        public Criteria andGlobalCodeIsNotNull() {
            addCriterion("global_code is not null");
            return this;
        }

        public Criteria andGlobalCodeEqualTo(String value) {
            addCriterion("global_code =", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeNotEqualTo(String value) {
            addCriterion("global_code <>", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeGreaterThan(String value) {
            addCriterion("global_code >", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeGreaterThanOrEqualTo(String value) {
            addCriterion("global_code >=", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeLessThan(String value) {
            addCriterion("global_code <", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeLessThanOrEqualTo(String value) {
            addCriterion("global_code <=", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeLike(String value) {
            addCriterion("global_code like", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeNotLike(String value) {
            addCriterion("global_code not like", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeIn(List<String> values) {
            addCriterion("global_code in", values, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeNotIn(List<String> values) {
            addCriterion("global_code not in", values, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeBetween(String value1, String value2) {
            addCriterion("global_code between", value1, value2, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeNotBetween(String value1, String value2) {
            addCriterion("global_code not between", value1, value2, "globalCode");
            return this;
        }

        public Criteria andSimpleCodeIsNull() {
            addCriterion("simple_code is null");
            return this;
        }

        public Criteria andSimpleCodeIsNotNull() {
            addCriterion("simple_code is not null");
            return this;
        }

        public Criteria andSimpleCodeEqualTo(String value) {
            addCriterion("simple_code =", value, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeNotEqualTo(String value) {
            addCriterion("simple_code <>", value, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeGreaterThan(String value) {
            addCriterion("simple_code >", value, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeGreaterThanOrEqualTo(String value) {
            addCriterion("simple_code >=", value, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeLessThan(String value) {
            addCriterion("simple_code <", value, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeLessThanOrEqualTo(String value) {
            addCriterion("simple_code <=", value, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeLike(String value) {
            addCriterion("simple_code like", value, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeNotLike(String value) {
            addCriterion("simple_code not like", value, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeIn(List<String> values) {
            addCriterion("simple_code in", values, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeNotIn(List<String> values) {
            addCriterion("simple_code not in", values, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeBetween(String value1, String value2) {
            addCriterion("simple_code between", value1, value2, "simpleCode");
            return this;
        }

        public Criteria andSimpleCodeNotBetween(String value1, String value2) {
            addCriterion("simple_code not between", value1, value2, "simpleCode");
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

        public Criteria andCodeIsNull() {
            addCriterion("code is null");
            return this;
        }

        public Criteria andCodeIsNotNull() {
            addCriterion("code is not null");
            return this;
        }

        public Criteria andCodeEqualTo(Integer value) {
            addCriterion("code =", value, "code");
            return this;
        }

        public Criteria andCodeNotEqualTo(Integer value) {
            addCriterion("code <>", value, "code");
            return this;
        }

        public Criteria andCodeGreaterThan(Integer value) {
            addCriterion("code >", value, "code");
            return this;
        }

        public Criteria andCodeGreaterThanOrEqualTo(Integer value) {
            addCriterion("code >=", value, "code");
            return this;
        }

        public Criteria andCodeLessThan(Integer value) {
            addCriterion("code <", value, "code");
            return this;
        }

        public Criteria andCodeLessThanOrEqualTo(Integer value) {
            addCriterion("code <=", value, "code");
            return this;
        }

        public Criteria andCodeIn(List<Integer> values) {
            addCriterion("code in", values, "code");
            return this;
        }

        public Criteria andCodeNotIn(List<Integer> values) {
            addCriterion("code not in", values, "code");
            return this;
        }

        public Criteria andCodeBetween(Integer value1, Integer value2) {
            addCriterion("code between", value1, value2, "code");
            return this;
        }

        public Criteria andCodeNotBetween(Integer value1, Integer value2) {
            addCriterion("code not between", value1, value2, "code");
            return this;
        }

        public Criteria andCurrDateIsNull() {
            addCriterion("curr_date is null");
            return this;
        }

        public Criteria andCurrDateIsNotNull() {
            addCriterion("curr_date is not null");
            return this;
        }

        public Criteria andCurrDateEqualTo(Date value) {
            addCriterionForJDBCDate("curr_date =", value, "currDate");
            return this;
        }

        public Criteria andCurrDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("curr_date <>", value, "currDate");
            return this;
        }

        public Criteria andCurrDateGreaterThan(Date value) {
            addCriterionForJDBCDate("curr_date >", value, "currDate");
            return this;
        }

        public Criteria andCurrDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("curr_date >=", value, "currDate");
            return this;
        }

        public Criteria andCurrDateLessThan(Date value) {
            addCriterionForJDBCDate("curr_date <", value, "currDate");
            return this;
        }

        public Criteria andCurrDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("curr_date <=", value, "currDate");
            return this;
        }

        public Criteria andCurrDateIn(List<Date> values) {
            addCriterionForJDBCDate("curr_date in", values, "currDate");
            return this;
        }

        public Criteria andCurrDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("curr_date not in", values, "currDate");
            return this;
        }

        public Criteria andCurrDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("curr_date between", value1, value2, "currDate");
            return this;
        }

        public Criteria andCurrDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("curr_date not between", value1, value2, "currDate");
            return this;
        }

        public Criteria andTotalpayIdIsNull() {
            addCriterion("totalpay_id is null");
            return this;
        }

        public Criteria andTotalpayIdIsNotNull() {
            addCriterion("totalpay_id is not null");
            return this;
        }

        public Criteria andTotalpayIdEqualTo(String value) {
            addCriterion("totalpay_id =", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdNotEqualTo(String value) {
            addCriterion("totalpay_id <>", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdGreaterThan(String value) {
            addCriterion("totalpay_id >", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdGreaterThanOrEqualTo(String value) {
            addCriterion("totalpay_id >=", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdLessThan(String value) {
            addCriterion("totalpay_id <", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdLessThanOrEqualTo(String value) {
            addCriterion("totalpay_id <=", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdLike(String value) {
            addCriterion("totalpay_id like", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdNotLike(String value) {
            addCriterion("totalpay_id not like", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdIn(List<String> values) {
            addCriterion("totalpay_id in", values, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdNotIn(List<String> values) {
            addCriterion("totalpay_id not in", values, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdBetween(String value1, String value2) {
            addCriterion("totalpay_id between", value1, value2, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdNotBetween(String value1, String value2) {
            addCriterion("totalpay_id not between", value1, value2, "totalpayId");
            return this;
        }

        public Criteria andSeatIdIsNull() {
            addCriterion("seat_id is null");
            return this;
        }

        public Criteria andSeatIdIsNotNull() {
            addCriterion("seat_id is not null");
            return this;
        }

        public Criteria andSeatIdEqualTo(String value) {
            addCriterion("seat_id =", value, "seatId");
            return this;
        }

        public Criteria andSeatIdNotEqualTo(String value) {
            addCriterion("seat_id <>", value, "seatId");
            return this;
        }

        public Criteria andSeatIdGreaterThan(String value) {
            addCriterion("seat_id >", value, "seatId");
            return this;
        }

        public Criteria andSeatIdGreaterThanOrEqualTo(String value) {
            addCriterion("seat_id >=", value, "seatId");
            return this;
        }

        public Criteria andSeatIdLessThan(String value) {
            addCriterion("seat_id <", value, "seatId");
            return this;
        }

        public Criteria andSeatIdLessThanOrEqualTo(String value) {
            addCriterion("seat_id <=", value, "seatId");
            return this;
        }

        public Criteria andSeatIdLike(String value) {
            addCriterion("seat_id like", value, "seatId");
            return this;
        }

        public Criteria andSeatIdNotLike(String value) {
            addCriterion("seat_id not like", value, "seatId");
            return this;
        }

        public Criteria andSeatIdIn(List<String> values) {
            addCriterion("seat_id in", values, "seatId");
            return this;
        }

        public Criteria andSeatIdNotIn(List<String> values) {
            addCriterion("seat_id not in", values, "seatId");
            return this;
        }

        public Criteria andSeatIdBetween(String value1, String value2) {
            addCriterion("seat_id between", value1, value2, "seatId");
            return this;
        }

        public Criteria andSeatIdNotBetween(String value1, String value2) {
            addCriterion("seat_id not between", value1, value2, "seatId");
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

        public Criteria andPeopleCountEqualTo(Long value) {
            addCriterion("people_count =", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountNotEqualTo(Long value) {
            addCriterion("people_count <>", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountGreaterThan(Long value) {
            addCriterion("people_count >", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountGreaterThanOrEqualTo(Long value) {
            addCriterion("people_count >=", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountLessThan(Long value) {
            addCriterion("people_count <", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountLessThanOrEqualTo(Long value) {
            addCriterion("people_count <=", value, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountIn(List<Long> values) {
            addCriterion("people_count in", values, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountNotIn(List<Long> values) {
            addCriterion("people_count not in", values, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountBetween(Long value1, Long value2) {
            addCriterion("people_count between", value1, value2, "peopleCount");
            return this;
        }

        public Criteria andPeopleCountNotBetween(Long value1, Long value2) {
            addCriterion("people_count not between", value1, value2, "peopleCount");
            return this;
        }

        public Criteria andOpenTimeIsNull() {
            addCriterion("open_time is null");
            return this;
        }

        public Criteria andOpenTimeIsNotNull() {
            addCriterion("open_time is not null");
            return this;
        }

        public Criteria andOpenTimeEqualTo(Long value) {
            addCriterion("open_time =", value, "openTime");
            return this;
        }

        public Criteria andOpenTimeNotEqualTo(Long value) {
            addCriterion("open_time <>", value, "openTime");
            return this;
        }

        public Criteria andOpenTimeGreaterThan(Long value) {
            addCriterion("open_time >", value, "openTime");
            return this;
        }

        public Criteria andOpenTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("open_time >=", value, "openTime");
            return this;
        }

        public Criteria andOpenTimeLessThan(Long value) {
            addCriterion("open_time <", value, "openTime");
            return this;
        }

        public Criteria andOpenTimeLessThanOrEqualTo(Long value) {
            addCriterion("open_time <=", value, "openTime");
            return this;
        }

        public Criteria andOpenTimeIn(List<Long> values) {
            addCriterion("open_time in", values, "openTime");
            return this;
        }

        public Criteria andOpenTimeNotIn(List<Long> values) {
            addCriterion("open_time not in", values, "openTime");
            return this;
        }

        public Criteria andOpenTimeBetween(Long value1, Long value2) {
            addCriterion("open_time between", value1, value2, "openTime");
            return this;
        }

        public Criteria andOpenTimeNotBetween(Long value1, Long value2) {
            addCriterion("open_time not between", value1, value2, "openTime");
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

        public Criteria andInnerCodeIsNull() {
            addCriterion("inner_code is null");
            return this;
        }

        public Criteria andInnerCodeIsNotNull() {
            addCriterion("inner_code is not null");
            return this;
        }

        public Criteria andInnerCodeEqualTo(String value) {
            addCriterion("inner_code =", value, "innerCode");
            return this;
        }

        public Criteria andInnerCodeNotEqualTo(String value) {
            addCriterion("inner_code <>", value, "innerCode");
            return this;
        }

        public Criteria andInnerCodeGreaterThan(String value) {
            addCriterion("inner_code >", value, "innerCode");
            return this;
        }

        public Criteria andInnerCodeGreaterThanOrEqualTo(String value) {
            addCriterion("inner_code >=", value, "innerCode");
            return this;
        }

        public Criteria andInnerCodeLessThan(String value) {
            addCriterion("inner_code <", value, "innerCode");
            return this;
        }

        public Criteria andInnerCodeLessThanOrEqualTo(String value) {
            addCriterion("inner_code <=", value, "innerCode");
            return this;
        }

        public Criteria andInnerCodeLike(String value) {
            addCriterion("inner_code like", value, "innerCode");
            return this;
        }

        public Criteria andInnerCodeNotLike(String value) {
            addCriterion("inner_code not like", value, "innerCode");
            return this;
        }

        public Criteria andInnerCodeIn(List<String> values) {
            addCriterion("inner_code in", values, "innerCode");
            return this;
        }

        public Criteria andInnerCodeNotIn(List<String> values) {
            addCriterion("inner_code not in", values, "innerCode");
            return this;
        }

        public Criteria andInnerCodeBetween(String value1, String value2) {
            addCriterion("inner_code between", value1, value2, "innerCode");
            return this;
        }

        public Criteria andInnerCodeNotBetween(String value1, String value2) {
            addCriterion("inner_code not between", value1, value2, "innerCode");
            return this;
        }

        public Criteria andMenutimeIdIsNull() {
            addCriterion("menutime_id is null");
            return this;
        }

        public Criteria andMenutimeIdIsNotNull() {
            addCriterion("menutime_id is not null");
            return this;
        }

        public Criteria andMenutimeIdEqualTo(String value) {
            addCriterion("menutime_id =", value, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdNotEqualTo(String value) {
            addCriterion("menutime_id <>", value, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdGreaterThan(String value) {
            addCriterion("menutime_id >", value, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdGreaterThanOrEqualTo(String value) {
            addCriterion("menutime_id >=", value, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdLessThan(String value) {
            addCriterion("menutime_id <", value, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdLessThanOrEqualTo(String value) {
            addCriterion("menutime_id <=", value, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdLike(String value) {
            addCriterion("menutime_id like", value, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdNotLike(String value) {
            addCriterion("menutime_id not like", value, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdIn(List<String> values) {
            addCriterion("menutime_id in", values, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdNotIn(List<String> values) {
            addCriterion("menutime_id not in", values, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdBetween(String value1, String value2) {
            addCriterion("menutime_id between", value1, value2, "menutimeId");
            return this;
        }

        public Criteria andMenutimeIdNotBetween(String value1, String value2) {
            addCriterion("menutime_id not between", value1, value2, "menutimeId");
            return this;
        }

        public Criteria andWorkerIdIsNull() {
            addCriterion("worker_id is null");
            return this;
        }

        public Criteria andWorkerIdIsNotNull() {
            addCriterion("worker_id is not null");
            return this;
        }

        public Criteria andWorkerIdEqualTo(String value) {
            addCriterion("worker_id =", value, "workerId");
            return this;
        }

        public Criteria andWorkerIdNotEqualTo(String value) {
            addCriterion("worker_id <>", value, "workerId");
            return this;
        }

        public Criteria andWorkerIdGreaterThan(String value) {
            addCriterion("worker_id >", value, "workerId");
            return this;
        }

        public Criteria andWorkerIdGreaterThanOrEqualTo(String value) {
            addCriterion("worker_id >=", value, "workerId");
            return this;
        }

        public Criteria andWorkerIdLessThan(String value) {
            addCriterion("worker_id <", value, "workerId");
            return this;
        }

        public Criteria andWorkerIdLessThanOrEqualTo(String value) {
            addCriterion("worker_id <=", value, "workerId");
            return this;
        }

        public Criteria andWorkerIdLike(String value) {
            addCriterion("worker_id like", value, "workerId");
            return this;
        }

        public Criteria andWorkerIdNotLike(String value) {
            addCriterion("worker_id not like", value, "workerId");
            return this;
        }

        public Criteria andWorkerIdIn(List<String> values) {
            addCriterion("worker_id in", values, "workerId");
            return this;
        }

        public Criteria andWorkerIdNotIn(List<String> values) {
            addCriterion("worker_id not in", values, "workerId");
            return this;
        }

        public Criteria andWorkerIdBetween(String value1, String value2) {
            addCriterion("worker_id between", value1, value2, "workerId");
            return this;
        }

        public Criteria andWorkerIdNotBetween(String value1, String value2) {
            addCriterion("worker_id not between", value1, value2, "workerId");
            return this;
        }

        public Criteria andEndTimeIsNull() {
            addCriterion("end_time is null");
            return this;
        }

        public Criteria andEndTimeIsNotNull() {
            addCriterion("end_time is not null");
            return this;
        }

        public Criteria andEndTimeEqualTo(Long value) {
            addCriterion("end_time =", value, "endTime");
            return this;
        }

        public Criteria andEndTimeNotEqualTo(Long value) {
            addCriterion("end_time <>", value, "endTime");
            return this;
        }

        public Criteria andEndTimeGreaterThan(Long value) {
            addCriterion("end_time >", value, "endTime");
            return this;
        }

        public Criteria andEndTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("end_time >=", value, "endTime");
            return this;
        }

        public Criteria andEndTimeLessThan(Long value) {
            addCriterion("end_time <", value, "endTime");
            return this;
        }

        public Criteria andEndTimeLessThanOrEqualTo(Long value) {
            addCriterion("end_time <=", value, "endTime");
            return this;
        }

        public Criteria andEndTimeIn(List<Long> values) {
            addCriterion("end_time in", values, "endTime");
            return this;
        }

        public Criteria andEndTimeNotIn(List<Long> values) {
            addCriterion("end_time not in", values, "endTime");
            return this;
        }

        public Criteria andEndTimeBetween(Long value1, Long value2) {
            addCriterion("end_time between", value1, value2, "endTime");
            return this;
        }

        public Criteria andEndTimeNotBetween(Long value1, Long value2) {
            addCriterion("end_time not between", value1, value2, "endTime");
            return this;
        }

        public Criteria andFeeplanIdIsNull() {
            addCriterion("feeplan_id is null");
            return this;
        }

        public Criteria andFeeplanIdIsNotNull() {
            addCriterion("feeplan_id is not null");
            return this;
        }

        public Criteria andFeeplanIdEqualTo(String value) {
            addCriterion("feeplan_id =", value, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdNotEqualTo(String value) {
            addCriterion("feeplan_id <>", value, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdGreaterThan(String value) {
            addCriterion("feeplan_id >", value, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdGreaterThanOrEqualTo(String value) {
            addCriterion("feeplan_id >=", value, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdLessThan(String value) {
            addCriterion("feeplan_id <", value, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdLessThanOrEqualTo(String value) {
            addCriterion("feeplan_id <=", value, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdLike(String value) {
            addCriterion("feeplan_id like", value, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdNotLike(String value) {
            addCriterion("feeplan_id not like", value, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdIn(List<String> values) {
            addCriterion("feeplan_id in", values, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdNotIn(List<String> values) {
            addCriterion("feeplan_id not in", values, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdBetween(String value1, String value2) {
            addCriterion("feeplan_id between", value1, value2, "feeplanId");
            return this;
        }

        public Criteria andFeeplanIdNotBetween(String value1, String value2) {
            addCriterion("feeplan_id not between", value1, value2, "feeplanId");
            return this;
        }

        public Criteria andOpUserIdIsNull() {
            addCriterion("op_user_id is null");
            return this;
        }

        public Criteria andOpUserIdIsNotNull() {
            addCriterion("op_user_id is not null");
            return this;
        }

        public Criteria andOpUserIdEqualTo(String value) {
            addCriterion("op_user_id =", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotEqualTo(String value) {
            addCriterion("op_user_id <>", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdGreaterThan(String value) {
            addCriterion("op_user_id >", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("op_user_id >=", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdLessThan(String value) {
            addCriterion("op_user_id <", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdLessThanOrEqualTo(String value) {
            addCriterion("op_user_id <=", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdLike(String value) {
            addCriterion("op_user_id like", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotLike(String value) {
            addCriterion("op_user_id not like", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdIn(List<String> values) {
            addCriterion("op_user_id in", values, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotIn(List<String> values) {
            addCriterion("op_user_id not in", values, "opUserId");
            return this;
        }

        public Criteria andOpUserIdBetween(String value1, String value2) {
            addCriterion("op_user_id between", value1, value2, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotBetween(String value1, String value2) {
            addCriterion("op_user_id not between", value1, value2, "opUserId");
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

        public Criteria andOrderKindIsNull() {
            addCriterion("order_kind is null");
            return this;
        }

        public Criteria andOrderKindIsNotNull() {
            addCriterion("order_kind is not null");
            return this;
        }

        public Criteria andOrderKindEqualTo(Short value) {
            addCriterion("order_kind =", value, "orderKind");
            return this;
        }

        public Criteria andOrderKindNotEqualTo(Short value) {
            addCriterion("order_kind <>", value, "orderKind");
            return this;
        }

        public Criteria andOrderKindGreaterThan(Short value) {
            addCriterion("order_kind >", value, "orderKind");
            return this;
        }

        public Criteria andOrderKindGreaterThanOrEqualTo(Short value) {
            addCriterion("order_kind >=", value, "orderKind");
            return this;
        }

        public Criteria andOrderKindLessThan(Short value) {
            addCriterion("order_kind <", value, "orderKind");
            return this;
        }

        public Criteria andOrderKindLessThanOrEqualTo(Short value) {
            addCriterion("order_kind <=", value, "orderKind");
            return this;
        }

        public Criteria andOrderKindIn(List<Short> values) {
            addCriterion("order_kind in", values, "orderKind");
            return this;
        }

        public Criteria andOrderKindNotIn(List<Short> values) {
            addCriterion("order_kind not in", values, "orderKind");
            return this;
        }

        public Criteria andOrderKindBetween(Short value1, Short value2) {
            addCriterion("order_kind between", value1, value2, "orderKind");
            return this;
        }

        public Criteria andOrderKindNotBetween(Short value1, Short value2) {
            addCriterion("order_kind not between", value1, value2, "orderKind");
            return this;
        }

        public Criteria andAreaIdIsNull() {
            addCriterion("area_id is null");
            return this;
        }

        public Criteria andAreaIdIsNotNull() {
            addCriterion("area_id is not null");
            return this;
        }

        public Criteria andAreaIdEqualTo(String value) {
            addCriterion("area_id =", value, "areaId");
            return this;
        }

        public Criteria andAreaIdNotEqualTo(String value) {
            addCriterion("area_id <>", value, "areaId");
            return this;
        }

        public Criteria andAreaIdGreaterThan(String value) {
            addCriterion("area_id >", value, "areaId");
            return this;
        }

        public Criteria andAreaIdGreaterThanOrEqualTo(String value) {
            addCriterion("area_id >=", value, "areaId");
            return this;
        }

        public Criteria andAreaIdLessThan(String value) {
            addCriterion("area_id <", value, "areaId");
            return this;
        }

        public Criteria andAreaIdLessThanOrEqualTo(String value) {
            addCriterion("area_id <=", value, "areaId");
            return this;
        }

        public Criteria andAreaIdLike(String value) {
            addCriterion("area_id like", value, "areaId");
            return this;
        }

        public Criteria andAreaIdNotLike(String value) {
            addCriterion("area_id not like", value, "areaId");
            return this;
        }

        public Criteria andAreaIdIn(List<String> values) {
            addCriterion("area_id in", values, "areaId");
            return this;
        }

        public Criteria andAreaIdNotIn(List<String> values) {
            addCriterion("area_id not in", values, "areaId");
            return this;
        }

        public Criteria andAreaIdBetween(String value1, String value2) {
            addCriterion("area_id between", value1, value2, "areaId");
            return this;
        }

        public Criteria andAreaIdNotBetween(String value1, String value2) {
            addCriterion("area_id not between", value1, value2, "areaId");
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

        public Criteria andIsAutocommitIsNull() {
            addCriterion("is_autocommit is null");
            return this;
        }

        public Criteria andIsAutocommitIsNotNull() {
            addCriterion("is_autocommit is not null");
            return this;
        }

        public Criteria andIsAutocommitEqualTo(Short value) {
            addCriterion("is_autocommit =", value, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitNotEqualTo(Short value) {
            addCriterion("is_autocommit <>", value, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitGreaterThan(Short value) {
            addCriterion("is_autocommit >", value, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitGreaterThanOrEqualTo(Short value) {
            addCriterion("is_autocommit >=", value, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitLessThan(Short value) {
            addCriterion("is_autocommit <", value, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitLessThanOrEqualTo(Short value) {
            addCriterion("is_autocommit <=", value, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitIn(List<Short> values) {
            addCriterion("is_autocommit in", values, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitNotIn(List<Short> values) {
            addCriterion("is_autocommit not in", values, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitBetween(Short value1, Short value2) {
            addCriterion("is_autocommit between", value1, value2, "isAutocommit");
            return this;
        }

        public Criteria andIsAutocommitNotBetween(Short value1, Short value2) {
            addCriterion("is_autocommit not between", value1, value2, "isAutocommit");
            return this;
        }

        public Criteria andSendTimeIsNull() {
            addCriterion("send_time is null");
            return this;
        }

        public Criteria andSendTimeIsNotNull() {
            addCriterion("send_time is not null");
            return this;
        }

        public Criteria andSendTimeEqualTo(Long value) {
            addCriterion("send_time =", value, "sendTime");
            return this;
        }

        public Criteria andSendTimeNotEqualTo(Long value) {
            addCriterion("send_time <>", value, "sendTime");
            return this;
        }

        public Criteria andSendTimeGreaterThan(Long value) {
            addCriterion("send_time >", value, "sendTime");
            return this;
        }

        public Criteria andSendTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("send_time >=", value, "sendTime");
            return this;
        }

        public Criteria andSendTimeLessThan(Long value) {
            addCriterion("send_time <", value, "sendTime");
            return this;
        }

        public Criteria andSendTimeLessThanOrEqualTo(Long value) {
            addCriterion("send_time <=", value, "sendTime");
            return this;
        }

        public Criteria andSendTimeIn(List<Long> values) {
            addCriterion("send_time in", values, "sendTime");
            return this;
        }

        public Criteria andSendTimeNotIn(List<Long> values) {
            addCriterion("send_time not in", values, "sendTime");
            return this;
        }

        public Criteria andSendTimeBetween(Long value1, Long value2) {
            addCriterion("send_time between", value1, value2, "sendTime");
            return this;
        }

        public Criteria andSendTimeNotBetween(Long value1, Long value2) {
            addCriterion("send_time not between", value1, value2, "sendTime");
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

        public Criteria andPaymodeIsNull() {
            addCriterion("paymode is null");
            return this;
        }

        public Criteria andPaymodeIsNotNull() {
            addCriterion("paymode is not null");
            return this;
        }

        public Criteria andPaymodeEqualTo(Short value) {
            addCriterion("paymode =", value, "paymode");
            return this;
        }

        public Criteria andPaymodeNotEqualTo(Short value) {
            addCriterion("paymode <>", value, "paymode");
            return this;
        }

        public Criteria andPaymodeGreaterThan(Short value) {
            addCriterion("paymode >", value, "paymode");
            return this;
        }

        public Criteria andPaymodeGreaterThanOrEqualTo(Short value) {
            addCriterion("paymode >=", value, "paymode");
            return this;
        }

        public Criteria andPaymodeLessThan(Short value) {
            addCriterion("paymode <", value, "paymode");
            return this;
        }

        public Criteria andPaymodeLessThanOrEqualTo(Short value) {
            addCriterion("paymode <=", value, "paymode");
            return this;
        }

        public Criteria andPaymodeIn(List<Short> values) {
            addCriterion("paymode in", values, "paymode");
            return this;
        }

        public Criteria andPaymodeNotIn(List<Short> values) {
            addCriterion("paymode not in", values, "paymode");
            return this;
        }

        public Criteria andPaymodeBetween(Short value1, Short value2) {
            addCriterion("paymode between", value1, value2, "paymode");
            return this;
        }

        public Criteria andPaymodeNotBetween(Short value1, Short value2) {
            addCriterion("paymode not between", value1, value2, "paymode");
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

        public Criteria andSendStatusIsNull() {
            addCriterion("send_status is null");
            return this;
        }

        public Criteria andSendStatusIsNotNull() {
            addCriterion("send_status is not null");
            return this;
        }

        public Criteria andSendStatusEqualTo(Short value) {
            addCriterion("send_status =", value, "sendStatus");
            return this;
        }

        public Criteria andSendStatusNotEqualTo(Short value) {
            addCriterion("send_status <>", value, "sendStatus");
            return this;
        }

        public Criteria andSendStatusGreaterThan(Short value) {
            addCriterion("send_status >", value, "sendStatus");
            return this;
        }

        public Criteria andSendStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("send_status >=", value, "sendStatus");
            return this;
        }

        public Criteria andSendStatusLessThan(Short value) {
            addCriterion("send_status <", value, "sendStatus");
            return this;
        }

        public Criteria andSendStatusLessThanOrEqualTo(Short value) {
            addCriterion("send_status <=", value, "sendStatus");
            return this;
        }

        public Criteria andSendStatusIn(List<Short> values) {
            addCriterion("send_status in", values, "sendStatus");
            return this;
        }

        public Criteria andSendStatusNotIn(List<Short> values) {
            addCriterion("send_status not in", values, "sendStatus");
            return this;
        }

        public Criteria andSendStatusBetween(Short value1, Short value2) {
            addCriterion("send_status between", value1, value2, "sendStatus");
            return this;
        }

        public Criteria andSendStatusNotBetween(Short value1, Short value2) {
            addCriterion("send_status not between", value1, value2, "sendStatus");
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

        public Criteria andIsHideIsNull() {
            addCriterion("is_hide is null");
            return this;
        }

        public Criteria andIsHideIsNotNull() {
            addCriterion("is_hide is not null");
            return this;
        }

        public Criteria andIsHideEqualTo(Byte value) {
            addCriterion("is_hide =", value, "isHide");
            return this;
        }

        public Criteria andIsHideNotEqualTo(Byte value) {
            addCriterion("is_hide <>", value, "isHide");
            return this;
        }

        public Criteria andIsHideGreaterThan(Byte value) {
            addCriterion("is_hide >", value, "isHide");
            return this;
        }

        public Criteria andIsHideGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_hide >=", value, "isHide");
            return this;
        }

        public Criteria andIsHideLessThan(Byte value) {
            addCriterion("is_hide <", value, "isHide");
            return this;
        }

        public Criteria andIsHideLessThanOrEqualTo(Byte value) {
            addCriterion("is_hide <=", value, "isHide");
            return this;
        }

        public Criteria andIsHideIn(List<Byte> values) {
            addCriterion("is_hide in", values, "isHide");
            return this;
        }

        public Criteria andIsHideNotIn(List<Byte> values) {
            addCriterion("is_hide not in", values, "isHide");
            return this;
        }

        public Criteria andIsHideBetween(Byte value1, Byte value2) {
            addCriterion("is_hide between", value1, value2, "isHide");
            return this;
        }

        public Criteria andIsHideNotBetween(Byte value1, Byte value2) {
            addCriterion("is_hide not between", value1, value2, "isHide");
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

        public Criteria andLoadTimeIsNull() {
            addCriterion("load_time is null");
            return this;
        }

        public Criteria andLoadTimeIsNotNull() {
            addCriterion("load_time is not null");
            return this;
        }

        public Criteria andLoadTimeEqualTo(Integer value) {
            addCriterion("load_time =", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeNotEqualTo(Integer value) {
            addCriterion("load_time <>", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeGreaterThan(Integer value) {
            addCriterion("load_time >", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("load_time >=", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeLessThan(Integer value) {
            addCriterion("load_time <", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeLessThanOrEqualTo(Integer value) {
            addCriterion("load_time <=", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeIn(List<Integer> values) {
            addCriterion("load_time in", values, "loadTime");
            return this;
        }

        public Criteria andLoadTimeNotIn(List<Integer> values) {
            addCriterion("load_time not in", values, "loadTime");
            return this;
        }

        public Criteria andLoadTimeBetween(Integer value1, Integer value2) {
            addCriterion("load_time between", value1, value2, "loadTime");
            return this;
        }

        public Criteria andLoadTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("load_time not between", value1, value2, "loadTime");
            return this;
        }

        public Criteria andModifyTimeIsNull() {
            addCriterion("modify_time is null");
            return this;
        }

        public Criteria andModifyTimeIsNotNull() {
            addCriterion("modify_time is not null");
            return this;
        }

        public Criteria andModifyTimeEqualTo(Integer value) {
            addCriterion("modify_time =", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotEqualTo(Integer value) {
            addCriterion("modify_time <>", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeGreaterThan(Integer value) {
            addCriterion("modify_time >", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("modify_time >=", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeLessThan(Integer value) {
            addCriterion("modify_time <", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeLessThanOrEqualTo(Integer value) {
            addCriterion("modify_time <=", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeIn(List<Integer> values) {
            addCriterion("modify_time in", values, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotIn(List<Integer> values) {
            addCriterion("modify_time not in", values, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeBetween(Integer value1, Integer value2) {
            addCriterion("modify_time between", value1, value2, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("modify_time not between", value1, value2, "modifyTime");
            return this;
        }

        public Criteria andIsLimittimeIsNull() {
            addCriterion("is_limittime is null");
            return this;
        }

        public Criteria andIsLimittimeIsNotNull() {
            addCriterion("is_limittime is not null");
            return this;
        }

        public Criteria andIsLimittimeEqualTo(Boolean value) {
            addCriterion("is_limittime =", value, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeNotEqualTo(Boolean value) {
            addCriterion("is_limittime <>", value, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeGreaterThan(Boolean value) {
            addCriterion("is_limittime >", value, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_limittime >=", value, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeLessThan(Boolean value) {
            addCriterion("is_limittime <", value, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeLessThanOrEqualTo(Boolean value) {
            addCriterion("is_limittime <=", value, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeIn(List<Boolean> values) {
            addCriterion("is_limittime in", values, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeNotIn(List<Boolean> values) {
            addCriterion("is_limittime not in", values, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeBetween(Boolean value1, Boolean value2) {
            addCriterion("is_limittime between", value1, value2, "isLimittime");
            return this;
        }

        public Criteria andIsLimittimeNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_limittime not between", value1, value2, "isLimittime");
            return this;
        }

        public Criteria andScanUrlIsNull() {
            addCriterion("scan_url is null");
            return this;
        }

        public Criteria andScanUrlIsNotNull() {
            addCriterion("scan_url is not null");
            return this;
        }

        public Criteria andScanUrlEqualTo(String value) {
            addCriterion("scan_url =", value, "scanUrl");
            return this;
        }

        public Criteria andScanUrlNotEqualTo(String value) {
            addCriterion("scan_url <>", value, "scanUrl");
            return this;
        }

        public Criteria andScanUrlGreaterThan(String value) {
            addCriterion("scan_url >", value, "scanUrl");
            return this;
        }

        public Criteria andScanUrlGreaterThanOrEqualTo(String value) {
            addCriterion("scan_url >=", value, "scanUrl");
            return this;
        }

        public Criteria andScanUrlLessThan(String value) {
            addCriterion("scan_url <", value, "scanUrl");
            return this;
        }

        public Criteria andScanUrlLessThanOrEqualTo(String value) {
            addCriterion("scan_url <=", value, "scanUrl");
            return this;
        }

        public Criteria andScanUrlLike(String value) {
            addCriterion("scan_url like", value, "scanUrl");
            return this;
        }

        public Criteria andScanUrlNotLike(String value) {
            addCriterion("scan_url not like", value, "scanUrl");
            return this;
        }

        public Criteria andScanUrlIn(List<String> values) {
            addCriterion("scan_url in", values, "scanUrl");
            return this;
        }

        public Criteria andScanUrlNotIn(List<String> values) {
            addCriterion("scan_url not in", values, "scanUrl");
            return this;
        }

        public Criteria andScanUrlBetween(String value1, String value2) {
            addCriterion("scan_url between", value1, value2, "scanUrl");
            return this;
        }

        public Criteria andScanUrlNotBetween(String value1, String value2) {
            addCriterion("scan_url not between", value1, value2, "scanUrl");
            return this;
        }

        public Criteria andSeatMarkIsNull() {
            addCriterion("seat_mark is null");
            return this;
        }

        public Criteria andSeatMarkIsNotNull() {
            addCriterion("seat_mark is not null");
            return this;
        }

        public Criteria andSeatMarkEqualTo(String value) {
            addCriterion("seat_mark =", value, "seatMark");
            return this;
        }

        public Criteria andSeatMarkNotEqualTo(String value) {
            addCriterion("seat_mark <>", value, "seatMark");
            return this;
        }

        public Criteria andSeatMarkGreaterThan(String value) {
            addCriterion("seat_mark >", value, "seatMark");
            return this;
        }

        public Criteria andSeatMarkGreaterThanOrEqualTo(String value) {
            addCriterion("seat_mark >=", value, "seatMark");
            return this;
        }

        public Criteria andSeatMarkLessThan(String value) {
            addCriterion("seat_mark <", value, "seatMark");
            return this;
        }

        public Criteria andSeatMarkLessThanOrEqualTo(String value) {
            addCriterion("seat_mark <=", value, "seatMark");
            return this;
        }

        public Criteria andSeatMarkLike(String value) {
            addCriterion("seat_mark like", value, "seatMark");
            return this;
        }

        public Criteria andSeatMarkNotLike(String value) {
            addCriterion("seat_mark not like", value, "seatMark");
            return this;
        }

        public Criteria andSeatMarkIn(List<String> values) {
            addCriterion("seat_mark in", values, "seatMark");
            return this;
        }

        public Criteria andSeatMarkNotIn(List<String> values) {
            addCriterion("seat_mark not in", values, "seatMark");
            return this;
        }

        public Criteria andSeatMarkBetween(String value1, String value2) {
            addCriterion("seat_mark between", value1, value2, "seatMark");
            return this;
        }

        public Criteria andSeatMarkNotBetween(String value1, String value2) {
            addCriterion("seat_mark not between", value1, value2, "seatMark");
            return this;
        }

        public Criteria andReservetimeIdIsNull() {
            addCriterion("reservetime_id is null");
            return this;
        }

        public Criteria andReservetimeIdIsNotNull() {
            addCriterion("reservetime_id is not null");
            return this;
        }

        public Criteria andReservetimeIdEqualTo(String value) {
            addCriterion("reservetime_id =", value, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdNotEqualTo(String value) {
            addCriterion("reservetime_id <>", value, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdGreaterThan(String value) {
            addCriterion("reservetime_id >", value, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdGreaterThanOrEqualTo(String value) {
            addCriterion("reservetime_id >=", value, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdLessThan(String value) {
            addCriterion("reservetime_id <", value, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdLessThanOrEqualTo(String value) {
            addCriterion("reservetime_id <=", value, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdLike(String value) {
            addCriterion("reservetime_id like", value, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdNotLike(String value) {
            addCriterion("reservetime_id not like", value, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdIn(List<String> values) {
            addCriterion("reservetime_id in", values, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdNotIn(List<String> values) {
            addCriterion("reservetime_id not in", values, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdBetween(String value1, String value2) {
            addCriterion("reservetime_id between", value1, value2, "reservetimeId");
            return this;
        }

        public Criteria andReservetimeIdNotBetween(String value1, String value2) {
            addCriterion("reservetime_id not between", value1, value2, "reservetimeId");
            return this;
        }

        public Criteria andIsWaitIsNull() {
            addCriterion("is_wait is null");
            return this;
        }

        public Criteria andIsWaitIsNotNull() {
            addCriterion("is_wait is not null");
            return this;
        }

        public Criteria andIsWaitEqualTo(Byte value) {
            addCriterion("is_wait =", value, "isWait");
            return this;
        }

        public Criteria andIsWaitNotEqualTo(Byte value) {
            addCriterion("is_wait <>", value, "isWait");
            return this;
        }

        public Criteria andIsWaitGreaterThan(Byte value) {
            addCriterion("is_wait >", value, "isWait");
            return this;
        }

        public Criteria andIsWaitGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_wait >=", value, "isWait");
            return this;
        }

        public Criteria andIsWaitLessThan(Byte value) {
            addCriterion("is_wait <", value, "isWait");
            return this;
        }

        public Criteria andIsWaitLessThanOrEqualTo(Byte value) {
            addCriterion("is_wait <=", value, "isWait");
            return this;
        }

        public Criteria andIsWaitIn(List<Byte> values) {
            addCriterion("is_wait in", values, "isWait");
            return this;
        }

        public Criteria andIsWaitNotIn(List<Byte> values) {
            addCriterion("is_wait not in", values, "isWait");
            return this;
        }

        public Criteria andIsWaitBetween(Byte value1, Byte value2) {
            addCriterion("is_wait between", value1, value2, "isWait");
            return this;
        }

        public Criteria andIsWaitNotBetween(Byte value1, Byte value2) {
            addCriterion("is_wait not between", value1, value2, "isWait");
            return this;
        }

        public Criteria andIsPrintIsNull() {
            addCriterion("is_print is null");
            return this;
        }

        public Criteria andIsPrintIsNotNull() {
            addCriterion("is_print is not null");
            return this;
        }

        public Criteria andIsPrintEqualTo(Byte value) {
            addCriterion("is_print =", value, "isPrint");
            return this;
        }

        public Criteria andIsPrintNotEqualTo(Byte value) {
            addCriterion("is_print <>", value, "isPrint");
            return this;
        }

        public Criteria andIsPrintGreaterThan(Byte value) {
            addCriterion("is_print >", value, "isPrint");
            return this;
        }

        public Criteria andIsPrintGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_print >=", value, "isPrint");
            return this;
        }

        public Criteria andIsPrintLessThan(Byte value) {
            addCriterion("is_print <", value, "isPrint");
            return this;
        }

        public Criteria andIsPrintLessThanOrEqualTo(Byte value) {
            addCriterion("is_print <=", value, "isPrint");
            return this;
        }

        public Criteria andIsPrintIn(List<Byte> values) {
            addCriterion("is_print in", values, "isPrint");
            return this;
        }

        public Criteria andIsPrintNotIn(List<Byte> values) {
            addCriterion("is_print not in", values, "isPrint");
            return this;
        }

        public Criteria andIsPrintBetween(Byte value1, Byte value2) {
            addCriterion("is_print between", value1, value2, "isPrint");
            return this;
        }

        public Criteria andIsPrintNotBetween(Byte value1, Byte value2) {
            addCriterion("is_print not between", value1, value2, "isPrint");
            return this;
        }

        public Criteria andBookIdIsNull() {
            addCriterion("book_id is null");
            return this;
        }

        public Criteria andBookIdIsNotNull() {
            addCriterion("book_id is not null");
            return this;
        }

        public Criteria andBookIdEqualTo(String value) {
            addCriterion("book_id =", value, "bookId");
            return this;
        }

        public Criteria andBookIdNotEqualTo(String value) {
            addCriterion("book_id <>", value, "bookId");
            return this;
        }

        public Criteria andBookIdGreaterThan(String value) {
            addCriterion("book_id >", value, "bookId");
            return this;
        }

        public Criteria andBookIdGreaterThanOrEqualTo(String value) {
            addCriterion("book_id >=", value, "bookId");
            return this;
        }

        public Criteria andBookIdLessThan(String value) {
            addCriterion("book_id <", value, "bookId");
            return this;
        }

        public Criteria andBookIdLessThanOrEqualTo(String value) {
            addCriterion("book_id <=", value, "bookId");
            return this;
        }

        public Criteria andBookIdLike(String value) {
            addCriterion("book_id like", value, "bookId");
            return this;
        }

        public Criteria andBookIdNotLike(String value) {
            addCriterion("book_id not like", value, "bookId");
            return this;
        }

        public Criteria andBookIdIn(List<String> values) {
            addCriterion("book_id in", values, "bookId");
            return this;
        }

        public Criteria andBookIdNotIn(List<String> values) {
            addCriterion("book_id not in", values, "bookId");
            return this;
        }

        public Criteria andBookIdBetween(String value1, String value2) {
            addCriterion("book_id between", value1, value2, "bookId");
            return this;
        }

        public Criteria andBookIdNotBetween(String value1, String value2) {
            addCriterion("book_id not between", value1, value2, "bookId");
            return this;
        }

        public Criteria andReserveIdIsNull() {
            addCriterion("reserve_id is null");
            return this;
        }

        public Criteria andReserveIdIsNotNull() {
            addCriterion("reserve_id is not null");
            return this;
        }

        public Criteria andReserveIdEqualTo(String value) {
            addCriterion("reserve_id =", value, "reserveId");
            return this;
        }

        public Criteria andReserveIdNotEqualTo(String value) {
            addCriterion("reserve_id <>", value, "reserveId");
            return this;
        }

        public Criteria andReserveIdGreaterThan(String value) {
            addCriterion("reserve_id >", value, "reserveId");
            return this;
        }

        public Criteria andReserveIdGreaterThanOrEqualTo(String value) {
            addCriterion("reserve_id >=", value, "reserveId");
            return this;
        }

        public Criteria andReserveIdLessThan(String value) {
            addCriterion("reserve_id <", value, "reserveId");
            return this;
        }

        public Criteria andReserveIdLessThanOrEqualTo(String value) {
            addCriterion("reserve_id <=", value, "reserveId");
            return this;
        }

        public Criteria andReserveIdLike(String value) {
            addCriterion("reserve_id like", value, "reserveId");
            return this;
        }

        public Criteria andReserveIdNotLike(String value) {
            addCriterion("reserve_id not like", value, "reserveId");
            return this;
        }

        public Criteria andReserveIdIn(List<String> values) {
            addCriterion("reserve_id in", values, "reserveId");
            return this;
        }

        public Criteria andReserveIdNotIn(List<String> values) {
            addCriterion("reserve_id not in", values, "reserveId");
            return this;
        }

        public Criteria andReserveIdBetween(String value1, String value2) {
            addCriterion("reserve_id between", value1, value2, "reserveId");
            return this;
        }

        public Criteria andReserveIdNotBetween(String value1, String value2) {
            addCriterion("reserve_id not between", value1, value2, "reserveId");
            return this;
        }

        public Criteria andOrignIdIsNull() {
            addCriterion("orign_id is null");
            return this;
        }

        public Criteria andOrignIdIsNotNull() {
            addCriterion("orign_id is not null");
            return this;
        }

        public Criteria andOrignIdEqualTo(String value) {
            addCriterion("orign_id =", value, "orignId");
            return this;
        }

        public Criteria andOrignIdNotEqualTo(String value) {
            addCriterion("orign_id <>", value, "orignId");
            return this;
        }

        public Criteria andOrignIdGreaterThan(String value) {
            addCriterion("orign_id >", value, "orignId");
            return this;
        }

        public Criteria andOrignIdGreaterThanOrEqualTo(String value) {
            addCriterion("orign_id >=", value, "orignId");
            return this;
        }

        public Criteria andOrignIdLessThan(String value) {
            addCriterion("orign_id <", value, "orignId");
            return this;
        }

        public Criteria andOrignIdLessThanOrEqualTo(String value) {
            addCriterion("orign_id <=", value, "orignId");
            return this;
        }

        public Criteria andOrignIdLike(String value) {
            addCriterion("orign_id like", value, "orignId");
            return this;
        }

        public Criteria andOrignIdNotLike(String value) {
            addCriterion("orign_id not like", value, "orignId");
            return this;
        }

        public Criteria andOrignIdIn(List<String> values) {
            addCriterion("orign_id in", values, "orignId");
            return this;
        }

        public Criteria andOrignIdNotIn(List<String> values) {
            addCriterion("orign_id not in", values, "orignId");
            return this;
        }

        public Criteria andOrignIdBetween(String value1, String value2) {
            addCriterion("orign_id between", value1, value2, "orignId");
            return this;
        }

        public Criteria andOrignIdNotBetween(String value1, String value2) {
            addCriterion("orign_id not between", value1, value2, "orignId");
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

        public Criteria andReserveStatusEqualTo(Byte value) {
            addCriterion("reserve_status =", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusNotEqualTo(Byte value) {
            addCriterion("reserve_status <>", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusGreaterThan(Byte value) {
            addCriterion("reserve_status >", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("reserve_status >=", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusLessThan(Byte value) {
            addCriterion("reserve_status <", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusLessThanOrEqualTo(Byte value) {
            addCriterion("reserve_status <=", value, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusIn(List<Byte> values) {
            addCriterion("reserve_status in", values, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusNotIn(List<Byte> values) {
            addCriterion("reserve_status not in", values, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusBetween(Byte value1, Byte value2) {
            addCriterion("reserve_status between", value1, value2, "reserveStatus");
            return this;
        }

        public Criteria andReserveStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("reserve_status not between", value1, value2, "reserveStatus");
            return this;
        }
    }
}
