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
public class RefundPayItemCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<RefundPayItemColEnum> cols = Sets.newHashSet();

    public RefundPayItemCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected RefundPayItemCriteria(RefundPayItemCriteria example) {
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

    public final List<RefundPayItemColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(RefundPayItemColEnum... colName) {
        for (RefundPayItemColEnum c : colName) {
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

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
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

        public Criteria andOrderRefundIdIsNull() {
            addCriterion("order_refund_id is null");
            return this;
        }

        public Criteria andOrderRefundIdIsNotNull() {
            addCriterion("order_refund_id is not null");
            return this;
        }

        public Criteria andOrderRefundIdEqualTo(String value) {
            addCriterion("order_refund_id =", value, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdNotEqualTo(String value) {
            addCriterion("order_refund_id <>", value, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdGreaterThan(String value) {
            addCriterion("order_refund_id >", value, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdGreaterThanOrEqualTo(String value) {
            addCriterion("order_refund_id >=", value, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdLessThan(String value) {
            addCriterion("order_refund_id <", value, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdLessThanOrEqualTo(String value) {
            addCriterion("order_refund_id <=", value, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdLike(String value) {
            addCriterion("order_refund_id like", value, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdNotLike(String value) {
            addCriterion("order_refund_id not like", value, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdIn(List<String> values) {
            addCriterion("order_refund_id in", values, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdNotIn(List<String> values) {
            addCriterion("order_refund_id not in", values, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdBetween(String value1, String value2) {
            addCriterion("order_refund_id between", value1, value2, "orderRefundId");
            return this;
        }

        public Criteria andOrderRefundIdNotBetween(String value1, String value2) {
            addCriterion("order_refund_id not between", value1, value2, "orderRefundId");
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

        public Criteria andWaitingPayIdIsNull() {
            addCriterion("waiting_pay_id is null");
            return this;
        }

        public Criteria andWaitingPayIdIsNotNull() {
            addCriterion("waiting_pay_id is not null");
            return this;
        }

        public Criteria andWaitingPayIdEqualTo(String value) {
            addCriterion("waiting_pay_id =", value, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdNotEqualTo(String value) {
            addCriterion("waiting_pay_id <>", value, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdGreaterThan(String value) {
            addCriterion("waiting_pay_id >", value, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdGreaterThanOrEqualTo(String value) {
            addCriterion("waiting_pay_id >=", value, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdLessThan(String value) {
            addCriterion("waiting_pay_id <", value, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdLessThanOrEqualTo(String value) {
            addCriterion("waiting_pay_id <=", value, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdLike(String value) {
            addCriterion("waiting_pay_id like", value, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdNotLike(String value) {
            addCriterion("waiting_pay_id not like", value, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdIn(List<String> values) {
            addCriterion("waiting_pay_id in", values, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdNotIn(List<String> values) {
            addCriterion("waiting_pay_id not in", values, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdBetween(String value1, String value2) {
            addCriterion("waiting_pay_id between", value1, value2, "waitingPayId");
            return this;
        }

        public Criteria andWaitingPayIdNotBetween(String value1, String value2) {
            addCriterion("waiting_pay_id not between", value1, value2, "waitingPayId");
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

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return this;
        }

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("status =", value, "status");
            return this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("status <>", value, "status");
            return this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("status >", value, "status");
            return this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("status >=", value, "status");
            return this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("status <", value, "status");
            return this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("status <=", value, "status");
            return this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("status in", values, "status");
            return this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("status not in", values, "status");
            return this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("status between", value1, value2, "status");
            return this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("status not between", value1, value2, "status");
            return this;
        }

        public Criteria andFinishTimeIsNull() {
            addCriterion("finish_time is null");
            return this;
        }

        public Criteria andFinishTimeIsNotNull() {
            addCriterion("finish_time is not null");
            return this;
        }

        public Criteria andFinishTimeEqualTo(Long value) {
            addCriterion("finish_time =", value, "finishTime");
            return this;
        }

        public Criteria andFinishTimeNotEqualTo(Long value) {
            addCriterion("finish_time <>", value, "finishTime");
            return this;
        }

        public Criteria andFinishTimeGreaterThan(Long value) {
            addCriterion("finish_time >", value, "finishTime");
            return this;
        }

        public Criteria andFinishTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("finish_time >=", value, "finishTime");
            return this;
        }

        public Criteria andFinishTimeLessThan(Long value) {
            addCriterion("finish_time <", value, "finishTime");
            return this;
        }

        public Criteria andFinishTimeLessThanOrEqualTo(Long value) {
            addCriterion("finish_time <=", value, "finishTime");
            return this;
        }

        public Criteria andFinishTimeIn(List<Long> values) {
            addCriterion("finish_time in", values, "finishTime");
            return this;
        }

        public Criteria andFinishTimeNotIn(List<Long> values) {
            addCriterion("finish_time not in", values, "finishTime");
            return this;
        }

        public Criteria andFinishTimeBetween(Long value1, Long value2) {
            addCriterion("finish_time between", value1, value2, "finishTime");
            return this;
        }

        public Criteria andFinishTimeNotBetween(Long value1, Long value2) {
            addCriterion("finish_time not between", value1, value2, "finishTime");
            return this;
        }

        public Criteria andMsgIsNull() {
            addCriterion("msg is null");
            return this;
        }

        public Criteria andMsgIsNotNull() {
            addCriterion("msg is not null");
            return this;
        }

        public Criteria andMsgEqualTo(String value) {
            addCriterion("msg =", value, "msg");
            return this;
        }

        public Criteria andMsgNotEqualTo(String value) {
            addCriterion("msg <>", value, "msg");
            return this;
        }

        public Criteria andMsgGreaterThan(String value) {
            addCriterion("msg >", value, "msg");
            return this;
        }

        public Criteria andMsgGreaterThanOrEqualTo(String value) {
            addCriterion("msg >=", value, "msg");
            return this;
        }

        public Criteria andMsgLessThan(String value) {
            addCriterion("msg <", value, "msg");
            return this;
        }

        public Criteria andMsgLessThanOrEqualTo(String value) {
            addCriterion("msg <=", value, "msg");
            return this;
        }

        public Criteria andMsgLike(String value) {
            addCriterion("msg like", value, "msg");
            return this;
        }

        public Criteria andMsgNotLike(String value) {
            addCriterion("msg not like", value, "msg");
            return this;
        }

        public Criteria andMsgIn(List<String> values) {
            addCriterion("msg in", values, "msg");
            return this;
        }

        public Criteria andMsgNotIn(List<String> values) {
            addCriterion("msg not in", values, "msg");
            return this;
        }

        public Criteria andMsgBetween(String value1, String value2) {
            addCriterion("msg between", value1, value2, "msg");
            return this;
        }

        public Criteria andMsgNotBetween(String value1, String value2) {
            addCriterion("msg not between", value1, value2, "msg");
            return this;
        }

        public Criteria andShouldFeeIsNull() {
            addCriterion("should_fee is null");
            return this;
        }

        public Criteria andShouldFeeIsNotNull() {
            addCriterion("should_fee is not null");
            return this;
        }

        public Criteria andShouldFeeEqualTo(Integer value) {
            addCriterion("should_fee =", value, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeNotEqualTo(Integer value) {
            addCriterion("should_fee <>", value, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeGreaterThan(Integer value) {
            addCriterion("should_fee >", value, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("should_fee >=", value, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeLessThan(Integer value) {
            addCriterion("should_fee <", value, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeLessThanOrEqualTo(Integer value) {
            addCriterion("should_fee <=", value, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeIn(List<Integer> values) {
            addCriterion("should_fee in", values, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeNotIn(List<Integer> values) {
            addCriterion("should_fee not in", values, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeBetween(Integer value1, Integer value2) {
            addCriterion("should_fee between", value1, value2, "shouldFee");
            return this;
        }

        public Criteria andShouldFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("should_fee not between", value1, value2, "shouldFee");
            return this;
        }

        public Criteria andActualFeeIsNull() {
            addCriterion("actual_fee is null");
            return this;
        }

        public Criteria andActualFeeIsNotNull() {
            addCriterion("actual_fee is not null");
            return this;
        }

        public Criteria andActualFeeEqualTo(Integer value) {
            addCriterion("actual_fee =", value, "actualFee");
            return this;
        }

        public Criteria andActualFeeNotEqualTo(Integer value) {
            addCriterion("actual_fee <>", value, "actualFee");
            return this;
        }

        public Criteria andActualFeeGreaterThan(Integer value) {
            addCriterion("actual_fee >", value, "actualFee");
            return this;
        }

        public Criteria andActualFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("actual_fee >=", value, "actualFee");
            return this;
        }

        public Criteria andActualFeeLessThan(Integer value) {
            addCriterion("actual_fee <", value, "actualFee");
            return this;
        }

        public Criteria andActualFeeLessThanOrEqualTo(Integer value) {
            addCriterion("actual_fee <=", value, "actualFee");
            return this;
        }

        public Criteria andActualFeeIn(List<Integer> values) {
            addCriterion("actual_fee in", values, "actualFee");
            return this;
        }

        public Criteria andActualFeeNotIn(List<Integer> values) {
            addCriterion("actual_fee not in", values, "actualFee");
            return this;
        }

        public Criteria andActualFeeBetween(Integer value1, Integer value2) {
            addCriterion("actual_fee between", value1, value2, "actualFee");
            return this;
        }

        public Criteria andActualFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("actual_fee not between", value1, value2, "actualFee");
            return this;
        }

        public Criteria andDeductRatioIsNull() {
            addCriterion("deduct_ratio is null");
            return this;
        }

        public Criteria andDeductRatioIsNotNull() {
            addCriterion("deduct_ratio is not null");
            return this;
        }

        public Criteria andDeductRatioEqualTo(Double value) {
            addCriterion("deduct_ratio =", value, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioNotEqualTo(Double value) {
            addCriterion("deduct_ratio <>", value, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioGreaterThan(Double value) {
            addCriterion("deduct_ratio >", value, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioGreaterThanOrEqualTo(Double value) {
            addCriterion("deduct_ratio >=", value, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioLessThan(Double value) {
            addCriterion("deduct_ratio <", value, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioLessThanOrEqualTo(Double value) {
            addCriterion("deduct_ratio <=", value, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioIn(List<Double> values) {
            addCriterion("deduct_ratio in", values, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioNotIn(List<Double> values) {
            addCriterion("deduct_ratio not in", values, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioBetween(Double value1, Double value2) {
            addCriterion("deduct_ratio between", value1, value2, "deductRatio");
            return this;
        }

        public Criteria andDeductRatioNotBetween(Double value1, Double value2) {
            addCriterion("deduct_ratio not between", value1, value2, "deductRatio");
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

        public Criteria andLastVerEqualTo(Integer value) {
            addCriterion("last_ver =", value, "lastVer");
            return this;
        }

        public Criteria andLastVerNotEqualTo(Integer value) {
            addCriterion("last_ver <>", value, "lastVer");
            return this;
        }

        public Criteria andLastVerGreaterThan(Integer value) {
            addCriterion("last_ver >", value, "lastVer");
            return this;
        }

        public Criteria andLastVerGreaterThanOrEqualTo(Integer value) {
            addCriterion("last_ver >=", value, "lastVer");
            return this;
        }

        public Criteria andLastVerLessThan(Integer value) {
            addCriterion("last_ver <", value, "lastVer");
            return this;
        }

        public Criteria andLastVerLessThanOrEqualTo(Integer value) {
            addCriterion("last_ver <=", value, "lastVer");
            return this;
        }

        public Criteria andLastVerIn(List<Integer> values) {
            addCriterion("last_ver in", values, "lastVer");
            return this;
        }

        public Criteria andLastVerNotIn(List<Integer> values) {
            addCriterion("last_ver not in", values, "lastVer");
            return this;
        }

        public Criteria andLastVerBetween(Integer value1, Integer value2) {
            addCriterion("last_ver between", value1, value2, "lastVer");
            return this;
        }

        public Criteria andLastVerNotBetween(Integer value1, Integer value2) {
            addCriterion("last_ver not between", value1, value2, "lastVer");
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

        public Criteria andIsValidEqualTo(Byte value) {
            addCriterion("is_valid =", value, "isValid");
            return this;
        }

        public Criteria andIsValidNotEqualTo(Byte value) {
            addCriterion("is_valid <>", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThan(Byte value) {
            addCriterion("is_valid >", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_valid >=", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThan(Byte value) {
            addCriterion("is_valid <", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThanOrEqualTo(Byte value) {
            addCriterion("is_valid <=", value, "isValid");
            return this;
        }

        public Criteria andIsValidIn(List<Byte> values) {
            addCriterion("is_valid in", values, "isValid");
            return this;
        }

        public Criteria andIsValidNotIn(List<Byte> values) {
            addCriterion("is_valid not in", values, "isValid");
            return this;
        }

        public Criteria andIsValidBetween(Byte value1, Byte value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return this;
        }

        public Criteria andIsValidNotBetween(Byte value1, Byte value2) {
            addCriterion("is_valid not between", value1, value2, "isValid");
            return this;
        }

        public Criteria andExtIsNull() {
            addCriterion("ext is null");
            return this;
        }

        public Criteria andExtIsNotNull() {
            addCriterion("ext is not null");
            return this;
        }

        public Criteria andExtEqualTo(String value) {
            addCriterion("ext =", value, "ext");
            return this;
        }

        public Criteria andExtNotEqualTo(String value) {
            addCriterion("ext <>", value, "ext");
            return this;
        }

        public Criteria andExtGreaterThan(String value) {
            addCriterion("ext >", value, "ext");
            return this;
        }

        public Criteria andExtGreaterThanOrEqualTo(String value) {
            addCriterion("ext >=", value, "ext");
            return this;
        }

        public Criteria andExtLessThan(String value) {
            addCriterion("ext <", value, "ext");
            return this;
        }

        public Criteria andExtLessThanOrEqualTo(String value) {
            addCriterion("ext <=", value, "ext");
            return this;
        }

        public Criteria andExtLike(String value) {
            addCriterion("ext like", value, "ext");
            return this;
        }

        public Criteria andExtNotLike(String value) {
            addCriterion("ext not like", value, "ext");
            return this;
        }

        public Criteria andExtIn(List<String> values) {
            addCriterion("ext in", values, "ext");
            return this;
        }

        public Criteria andExtNotIn(List<String> values) {
            addCriterion("ext not in", values, "ext");
            return this;
        }

        public Criteria andExtBetween(String value1, String value2) {
            addCriterion("ext between", value1, value2, "ext");
            return this;
        }

        public Criteria andExtNotBetween(String value1, String value2) {
            addCriterion("ext not between", value1, value2, "ext");
            return this;
        }

        public Criteria andFromTypeIsNull() {
            addCriterion("from_type is null");
            return this;
        }

        public Criteria andFromTypeIsNotNull() {
            addCriterion("from_type is not null");
            return this;
        }

        public Criteria andFromTypeEqualTo(Byte value) {
            addCriterion("from_type =", value, "fromType");
            return this;
        }

        public Criteria andFromTypeNotEqualTo(Byte value) {
            addCriterion("from_type <>", value, "fromType");
            return this;
        }

        public Criteria andFromTypeGreaterThan(Byte value) {
            addCriterion("from_type >", value, "fromType");
            return this;
        }

        public Criteria andFromTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("from_type >=", value, "fromType");
            return this;
        }

        public Criteria andFromTypeLessThan(Byte value) {
            addCriterion("from_type <", value, "fromType");
            return this;
        }

        public Criteria andFromTypeLessThanOrEqualTo(Byte value) {
            addCriterion("from_type <=", value, "fromType");
            return this;
        }

        public Criteria andFromTypeIn(List<Byte> values) {
            addCriterion("from_type in", values, "fromType");
            return this;
        }

        public Criteria andFromTypeNotIn(List<Byte> values) {
            addCriterion("from_type not in", values, "fromType");
            return this;
        }

        public Criteria andFromTypeBetween(Byte value1, Byte value2) {
            addCriterion("from_type between", value1, value2, "fromType");
            return this;
        }

        public Criteria andFromTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("from_type not between", value1, value2, "fromType");
            return this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("type is null");
            return this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return this;
        }

        public Criteria andTypeEqualTo(Integer value) {
            addCriterion("type =", value, "type");
            return this;
        }

        public Criteria andTypeNotEqualTo(Integer value) {
            addCriterion("type <>", value, "type");
            return this;
        }

        public Criteria andTypeGreaterThan(Integer value) {
            addCriterion("type >", value, "type");
            return this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("type >=", value, "type");
            return this;
        }

        public Criteria andTypeLessThan(Integer value) {
            addCriterion("type <", value, "type");
            return this;
        }

        public Criteria andTypeLessThanOrEqualTo(Integer value) {
            addCriterion("type <=", value, "type");
            return this;
        }

        public Criteria andTypeIn(List<Integer> values) {
            addCriterion("type in", values, "type");
            return this;
        }

        public Criteria andTypeNotIn(List<Integer> values) {
            addCriterion("type not in", values, "type");
            return this;
        }

        public Criteria andTypeBetween(Integer value1, Integer value2) {
            addCriterion("type between", value1, value2, "type");
            return this;
        }

        public Criteria andTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("type not between", value1, value2, "type");
            return this;
        }

        public Criteria andRefundWayIsNull() {
            addCriterion("refund_way is null");
            return this;
        }

        public Criteria andRefundWayIsNotNull() {
            addCriterion("refund_way is not null");
            return this;
        }

        public Criteria andRefundWayEqualTo(Byte value) {
            addCriterion("refund_way =", value, "refundWay");
            return this;
        }

        public Criteria andRefundWayNotEqualTo(Byte value) {
            addCriterion("refund_way <>", value, "refundWay");
            return this;
        }

        public Criteria andRefundWayGreaterThan(Byte value) {
            addCriterion("refund_way >", value, "refundWay");
            return this;
        }

        public Criteria andRefundWayGreaterThanOrEqualTo(Byte value) {
            addCriterion("refund_way >=", value, "refundWay");
            return this;
        }

        public Criteria andRefundWayLessThan(Byte value) {
            addCriterion("refund_way <", value, "refundWay");
            return this;
        }

        public Criteria andRefundWayLessThanOrEqualTo(Byte value) {
            addCriterion("refund_way <=", value, "refundWay");
            return this;
        }

        public Criteria andRefundWayIn(List<Byte> values) {
            addCriterion("refund_way in", values, "refundWay");
            return this;
        }

        public Criteria andRefundWayNotIn(List<Byte> values) {
            addCriterion("refund_way not in", values, "refundWay");
            return this;
        }

        public Criteria andRefundWayBetween(Byte value1, Byte value2) {
            addCriterion("refund_way between", value1, value2, "refundWay");
            return this;
        }

        public Criteria andRefundWayNotBetween(Byte value1, Byte value2) {
            addCriterion("refund_way not between", value1, value2, "refundWay");
            return this;
        }

        public Criteria andRelaWaitingPayIdIsNull() {
            addCriterion("rela_waiting_pay_id is null");
            return this;
        }

        public Criteria andRelaWaitingPayIdIsNotNull() {
            addCriterion("rela_waiting_pay_id is not null");
            return this;
        }

        public Criteria andRelaWaitingPayIdEqualTo(String value) {
            addCriterion("rela_waiting_pay_id =", value, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdNotEqualTo(String value) {
            addCriterion("rela_waiting_pay_id <>", value, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdGreaterThan(String value) {
            addCriterion("rela_waiting_pay_id >", value, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdGreaterThanOrEqualTo(String value) {
            addCriterion("rela_waiting_pay_id >=", value, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdLessThan(String value) {
            addCriterion("rela_waiting_pay_id <", value, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdLessThanOrEqualTo(String value) {
            addCriterion("rela_waiting_pay_id <=", value, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdLike(String value) {
            addCriterion("rela_waiting_pay_id like", value, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdNotLike(String value) {
            addCriterion("rela_waiting_pay_id not like", value, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdIn(List<String> values) {
            addCriterion("rela_waiting_pay_id in", values, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdNotIn(List<String> values) {
            addCriterion("rela_waiting_pay_id not in", values, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdBetween(String value1, String value2) {
            addCriterion("rela_waiting_pay_id between", value1, value2, "relaWaitingPayId");
            return this;
        }

        public Criteria andRelaWaitingPayIdNotBetween(String value1, String value2) {
            addCriterion("rela_waiting_pay_id not between", value1, value2, "relaWaitingPayId");
            return this;
        }

        public Criteria andKindpayIdIsNull() {
            addCriterion("kindpay_id is null");
            return this;
        }

        public Criteria andKindpayIdIsNotNull() {
            addCriterion("kindpay_id is not null");
            return this;
        }

        public Criteria andKindpayIdEqualTo(String value) {
            addCriterion("kindpay_id =", value, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdNotEqualTo(String value) {
            addCriterion("kindpay_id <>", value, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdGreaterThan(String value) {
            addCriterion("kindpay_id >", value, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdGreaterThanOrEqualTo(String value) {
            addCriterion("kindpay_id >=", value, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdLessThan(String value) {
            addCriterion("kindpay_id <", value, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdLessThanOrEqualTo(String value) {
            addCriterion("kindpay_id <=", value, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdLike(String value) {
            addCriterion("kindpay_id like", value, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdNotLike(String value) {
            addCriterion("kindpay_id not like", value, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdIn(List<String> values) {
            addCriterion("kindpay_id in", values, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdNotIn(List<String> values) {
            addCriterion("kindpay_id not in", values, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdBetween(String value1, String value2) {
            addCriterion("kindpay_id between", value1, value2, "kindpayId");
            return this;
        }

        public Criteria andKindpayIdNotBetween(String value1, String value2) {
            addCriterion("kindpay_id not between", value1, value2, "kindpayId");
            return this;
        }
    }
}
