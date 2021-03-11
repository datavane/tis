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
import com.qlangtech.tis.manage.common.TISBaseCriteria;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class OrderRefundCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<OrderRefundColEnum> cols = Sets.newHashSet();

    public OrderRefundCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected OrderRefundCriteria(OrderRefundCriteria example) {
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

    public final List<OrderRefundColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(OrderRefundColEnum... colName) {
        for (OrderRefundColEnum c : colName) {
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

        public Criteria andRefundFromIsNull() {
            addCriterion("refund_from is null");
            return this;
        }

        public Criteria andRefundFromIsNotNull() {
            addCriterion("refund_from is not null");
            return this;
        }

        public Criteria andRefundFromEqualTo(Byte value) {
            addCriterion("refund_from =", value, "refundFrom");
            return this;
        }

        public Criteria andRefundFromNotEqualTo(Byte value) {
            addCriterion("refund_from <>", value, "refundFrom");
            return this;
        }

        public Criteria andRefundFromGreaterThan(Byte value) {
            addCriterion("refund_from >", value, "refundFrom");
            return this;
        }

        public Criteria andRefundFromGreaterThanOrEqualTo(Byte value) {
            addCriterion("refund_from >=", value, "refundFrom");
            return this;
        }

        public Criteria andRefundFromLessThan(Byte value) {
            addCriterion("refund_from <", value, "refundFrom");
            return this;
        }

        public Criteria andRefundFromLessThanOrEqualTo(Byte value) {
            addCriterion("refund_from <=", value, "refundFrom");
            return this;
        }

        public Criteria andRefundFromIn(List<Byte> values) {
            addCriterion("refund_from in", values, "refundFrom");
            return this;
        }

        public Criteria andRefundFromNotIn(List<Byte> values) {
            addCriterion("refund_from not in", values, "refundFrom");
            return this;
        }

        public Criteria andRefundFromBetween(Byte value1, Byte value2) {
            addCriterion("refund_from between", value1, value2, "refundFrom");
            return this;
        }

        public Criteria andRefundFromNotBetween(Byte value1, Byte value2) {
            addCriterion("refund_from not between", value1, value2, "refundFrom");
            return this;
        }

        public Criteria andReasonIsNull() {
            addCriterion("reason is null");
            return this;
        }

        public Criteria andReasonIsNotNull() {
            addCriterion("reason is not null");
            return this;
        }

        public Criteria andReasonEqualTo(String value) {
            addCriterion("reason =", value, "reason");
            return this;
        }

        public Criteria andReasonNotEqualTo(String value) {
            addCriterion("reason <>", value, "reason");
            return this;
        }

        public Criteria andReasonGreaterThan(String value) {
            addCriterion("reason >", value, "reason");
            return this;
        }

        public Criteria andReasonGreaterThanOrEqualTo(String value) {
            addCriterion("reason >=", value, "reason");
            return this;
        }

        public Criteria andReasonLessThan(String value) {
            addCriterion("reason <", value, "reason");
            return this;
        }

        public Criteria andReasonLessThanOrEqualTo(String value) {
            addCriterion("reason <=", value, "reason");
            return this;
        }

        public Criteria andReasonLike(String value) {
            addCriterion("reason like", value, "reason");
            return this;
        }

        public Criteria andReasonNotLike(String value) {
            addCriterion("reason not like", value, "reason");
            return this;
        }

        public Criteria andReasonIn(List<String> values) {
            addCriterion("reason in", values, "reason");
            return this;
        }

        public Criteria andReasonNotIn(List<String> values) {
            addCriterion("reason not in", values, "reason");
            return this;
        }

        public Criteria andReasonBetween(String value1, String value2) {
            addCriterion("reason between", value1, value2, "reason");
            return this;
        }

        public Criteria andReasonNotBetween(String value1, String value2) {
            addCriterion("reason not between", value1, value2, "reason");
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

        public Criteria andSubStatusIsNull() {
            addCriterion("sub_status is null");
            return this;
        }

        public Criteria andSubStatusIsNotNull() {
            addCriterion("sub_status is not null");
            return this;
        }

        public Criteria andSubStatusEqualTo(Byte value) {
            addCriterion("sub_status =", value, "subStatus");
            return this;
        }

        public Criteria andSubStatusNotEqualTo(Byte value) {
            addCriterion("sub_status <>", value, "subStatus");
            return this;
        }

        public Criteria andSubStatusGreaterThan(Byte value) {
            addCriterion("sub_status >", value, "subStatus");
            return this;
        }

        public Criteria andSubStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("sub_status >=", value, "subStatus");
            return this;
        }

        public Criteria andSubStatusLessThan(Byte value) {
            addCriterion("sub_status <", value, "subStatus");
            return this;
        }

        public Criteria andSubStatusLessThanOrEqualTo(Byte value) {
            addCriterion("sub_status <=", value, "subStatus");
            return this;
        }

        public Criteria andSubStatusIn(List<Byte> values) {
            addCriterion("sub_status in", values, "subStatus");
            return this;
        }

        public Criteria andSubStatusNotIn(List<Byte> values) {
            addCriterion("sub_status not in", values, "subStatus");
            return this;
        }

        public Criteria andSubStatusBetween(Byte value1, Byte value2) {
            addCriterion("sub_status between", value1, value2, "subStatus");
            return this;
        }

        public Criteria andSubStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("sub_status not between", value1, value2, "subStatus");
            return this;
        }

        public Criteria andRefundCodeIsNull() {
            addCriterion("refund_code is null");
            return this;
        }

        public Criteria andRefundCodeIsNotNull() {
            addCriterion("refund_code is not null");
            return this;
        }

        public Criteria andRefundCodeEqualTo(String value) {
            addCriterion("refund_code =", value, "refundCode");
            return this;
        }

        public Criteria andRefundCodeNotEqualTo(String value) {
            addCriterion("refund_code <>", value, "refundCode");
            return this;
        }

        public Criteria andRefundCodeGreaterThan(String value) {
            addCriterion("refund_code >", value, "refundCode");
            return this;
        }

        public Criteria andRefundCodeGreaterThanOrEqualTo(String value) {
            addCriterion("refund_code >=", value, "refundCode");
            return this;
        }

        public Criteria andRefundCodeLessThan(String value) {
            addCriterion("refund_code <", value, "refundCode");
            return this;
        }

        public Criteria andRefundCodeLessThanOrEqualTo(String value) {
            addCriterion("refund_code <=", value, "refundCode");
            return this;
        }

        public Criteria andRefundCodeLike(String value) {
            addCriterion("refund_code like", value, "refundCode");
            return this;
        }

        public Criteria andRefundCodeNotLike(String value) {
            addCriterion("refund_code not like", value, "refundCode");
            return this;
        }

        public Criteria andRefundCodeIn(List<String> values) {
            addCriterion("refund_code in", values, "refundCode");
            return this;
        }

        public Criteria andRefundCodeNotIn(List<String> values) {
            addCriterion("refund_code not in", values, "refundCode");
            return this;
        }

        public Criteria andRefundCodeBetween(String value1, String value2) {
            addCriterion("refund_code between", value1, value2, "refundCode");
            return this;
        }

        public Criteria andRefundCodeNotBetween(String value1, String value2) {
            addCriterion("refund_code not between", value1, value2, "refundCode");
            return this;
        }

        public Criteria andMaxRefundFeeIsNull() {
            addCriterion("max_refund_fee is null");
            return this;
        }

        public Criteria andMaxRefundFeeIsNotNull() {
            addCriterion("max_refund_fee is not null");
            return this;
        }

        public Criteria andMaxRefundFeeEqualTo(Integer value) {
            addCriterion("max_refund_fee =", value, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeNotEqualTo(Integer value) {
            addCriterion("max_refund_fee <>", value, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeGreaterThan(Integer value) {
            addCriterion("max_refund_fee >", value, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("max_refund_fee >=", value, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeLessThan(Integer value) {
            addCriterion("max_refund_fee <", value, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeLessThanOrEqualTo(Integer value) {
            addCriterion("max_refund_fee <=", value, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeIn(List<Integer> values) {
            addCriterion("max_refund_fee in", values, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeNotIn(List<Integer> values) {
            addCriterion("max_refund_fee not in", values, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeBetween(Integer value1, Integer value2) {
            addCriterion("max_refund_fee between", value1, value2, "maxRefundFee");
            return this;
        }

        public Criteria andMaxRefundFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("max_refund_fee not between", value1, value2, "maxRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeIsNull() {
            addCriterion("apply_refund_fee is null");
            return this;
        }

        public Criteria andApplyRefundFeeIsNotNull() {
            addCriterion("apply_refund_fee is not null");
            return this;
        }

        public Criteria andApplyRefundFeeEqualTo(Integer value) {
            addCriterion("apply_refund_fee =", value, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeNotEqualTo(Integer value) {
            addCriterion("apply_refund_fee <>", value, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeGreaterThan(Integer value) {
            addCriterion("apply_refund_fee >", value, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("apply_refund_fee >=", value, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeLessThan(Integer value) {
            addCriterion("apply_refund_fee <", value, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeLessThanOrEqualTo(Integer value) {
            addCriterion("apply_refund_fee <=", value, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeIn(List<Integer> values) {
            addCriterion("apply_refund_fee in", values, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeNotIn(List<Integer> values) {
            addCriterion("apply_refund_fee not in", values, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeBetween(Integer value1, Integer value2) {
            addCriterion("apply_refund_fee between", value1, value2, "applyRefundFee");
            return this;
        }

        public Criteria andApplyRefundFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("apply_refund_fee not between", value1, value2, "applyRefundFee");
            return this;
        }

        public Criteria andReasonTypeIsNull() {
            addCriterion("reason_type is null");
            return this;
        }

        public Criteria andReasonTypeIsNotNull() {
            addCriterion("reason_type is not null");
            return this;
        }

        public Criteria andReasonTypeEqualTo(Byte value) {
            addCriterion("reason_type =", value, "reasonType");
            return this;
        }

        public Criteria andReasonTypeNotEqualTo(Byte value) {
            addCriterion("reason_type <>", value, "reasonType");
            return this;
        }

        public Criteria andReasonTypeGreaterThan(Byte value) {
            addCriterion("reason_type >", value, "reasonType");
            return this;
        }

        public Criteria andReasonTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("reason_type >=", value, "reasonType");
            return this;
        }

        public Criteria andReasonTypeLessThan(Byte value) {
            addCriterion("reason_type <", value, "reasonType");
            return this;
        }

        public Criteria andReasonTypeLessThanOrEqualTo(Byte value) {
            addCriterion("reason_type <=", value, "reasonType");
            return this;
        }

        public Criteria andReasonTypeIn(List<Byte> values) {
            addCriterion("reason_type in", values, "reasonType");
            return this;
        }

        public Criteria andReasonTypeNotIn(List<Byte> values) {
            addCriterion("reason_type not in", values, "reasonType");
            return this;
        }

        public Criteria andReasonTypeBetween(Byte value1, Byte value2) {
            addCriterion("reason_type between", value1, value2, "reasonType");
            return this;
        }

        public Criteria andReasonTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("reason_type not between", value1, value2, "reasonType");
            return this;
        }

        public Criteria andApplyDescIsNull() {
            addCriterion("apply_desc is null");
            return this;
        }

        public Criteria andApplyDescIsNotNull() {
            addCriterion("apply_desc is not null");
            return this;
        }

        public Criteria andApplyDescEqualTo(String value) {
            addCriterion("apply_desc =", value, "applyDesc");
            return this;
        }

        public Criteria andApplyDescNotEqualTo(String value) {
            addCriterion("apply_desc <>", value, "applyDesc");
            return this;
        }

        public Criteria andApplyDescGreaterThan(String value) {
            addCriterion("apply_desc >", value, "applyDesc");
            return this;
        }

        public Criteria andApplyDescGreaterThanOrEqualTo(String value) {
            addCriterion("apply_desc >=", value, "applyDesc");
            return this;
        }

        public Criteria andApplyDescLessThan(String value) {
            addCriterion("apply_desc <", value, "applyDesc");
            return this;
        }

        public Criteria andApplyDescLessThanOrEqualTo(String value) {
            addCriterion("apply_desc <=", value, "applyDesc");
            return this;
        }

        public Criteria andApplyDescLike(String value) {
            addCriterion("apply_desc like", value, "applyDesc");
            return this;
        }

        public Criteria andApplyDescNotLike(String value) {
            addCriterion("apply_desc not like", value, "applyDesc");
            return this;
        }

        public Criteria andApplyDescIn(List<String> values) {
            addCriterion("apply_desc in", values, "applyDesc");
            return this;
        }

        public Criteria andApplyDescNotIn(List<String> values) {
            addCriterion("apply_desc not in", values, "applyDesc");
            return this;
        }

        public Criteria andApplyDescBetween(String value1, String value2) {
            addCriterion("apply_desc between", value1, value2, "applyDesc");
            return this;
        }

        public Criteria andApplyDescNotBetween(String value1, String value2) {
            addCriterion("apply_desc not between", value1, value2, "applyDesc");
            return this;
        }

        public Criteria andRejectDescIsNull() {
            addCriterion("reject_desc is null");
            return this;
        }

        public Criteria andRejectDescIsNotNull() {
            addCriterion("reject_desc is not null");
            return this;
        }

        public Criteria andRejectDescEqualTo(String value) {
            addCriterion("reject_desc =", value, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescNotEqualTo(String value) {
            addCriterion("reject_desc <>", value, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescGreaterThan(String value) {
            addCriterion("reject_desc >", value, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescGreaterThanOrEqualTo(String value) {
            addCriterion("reject_desc >=", value, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescLessThan(String value) {
            addCriterion("reject_desc <", value, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescLessThanOrEqualTo(String value) {
            addCriterion("reject_desc <=", value, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescLike(String value) {
            addCriterion("reject_desc like", value, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescNotLike(String value) {
            addCriterion("reject_desc not like", value, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescIn(List<String> values) {
            addCriterion("reject_desc in", values, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescNotIn(List<String> values) {
            addCriterion("reject_desc not in", values, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescBetween(String value1, String value2) {
            addCriterion("reject_desc between", value1, value2, "rejectDesc");
            return this;
        }

        public Criteria andRejectDescNotBetween(String value1, String value2) {
            addCriterion("reject_desc not between", value1, value2, "rejectDesc");
            return this;
        }

        public Criteria andPicEvidenceIsNull() {
            addCriterion("pic_evidence is null");
            return this;
        }

        public Criteria andPicEvidenceIsNotNull() {
            addCriterion("pic_evidence is not null");
            return this;
        }

        public Criteria andPicEvidenceEqualTo(String value) {
            addCriterion("pic_evidence =", value, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceNotEqualTo(String value) {
            addCriterion("pic_evidence <>", value, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceGreaterThan(String value) {
            addCriterion("pic_evidence >", value, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceGreaterThanOrEqualTo(String value) {
            addCriterion("pic_evidence >=", value, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceLessThan(String value) {
            addCriterion("pic_evidence <", value, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceLessThanOrEqualTo(String value) {
            addCriterion("pic_evidence <=", value, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceLike(String value) {
            addCriterion("pic_evidence like", value, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceNotLike(String value) {
            addCriterion("pic_evidence not like", value, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceIn(List<String> values) {
            addCriterion("pic_evidence in", values, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceNotIn(List<String> values) {
            addCriterion("pic_evidence not in", values, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceBetween(String value1, String value2) {
            addCriterion("pic_evidence between", value1, value2, "picEvidence");
            return this;
        }

        public Criteria andPicEvidenceNotBetween(String value1, String value2) {
            addCriterion("pic_evidence not between", value1, value2, "picEvidence");
            return this;
        }

        public Criteria andApplyUserIdIsNull() {
            addCriterion("apply_user_id is null");
            return this;
        }

        public Criteria andApplyUserIdIsNotNull() {
            addCriterion("apply_user_id is not null");
            return this;
        }

        public Criteria andApplyUserIdEqualTo(String value) {
            addCriterion("apply_user_id =", value, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdNotEqualTo(String value) {
            addCriterion("apply_user_id <>", value, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdGreaterThan(String value) {
            addCriterion("apply_user_id >", value, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("apply_user_id >=", value, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdLessThan(String value) {
            addCriterion("apply_user_id <", value, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdLessThanOrEqualTo(String value) {
            addCriterion("apply_user_id <=", value, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdLike(String value) {
            addCriterion("apply_user_id like", value, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdNotLike(String value) {
            addCriterion("apply_user_id not like", value, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdIn(List<String> values) {
            addCriterion("apply_user_id in", values, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdNotIn(List<String> values) {
            addCriterion("apply_user_id not in", values, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdBetween(String value1, String value2) {
            addCriterion("apply_user_id between", value1, value2, "applyUserId");
            return this;
        }

        public Criteria andApplyUserIdNotBetween(String value1, String value2) {
            addCriterion("apply_user_id not between", value1, value2, "applyUserId");
            return this;
        }

        public Criteria andTimedTaskJsonIsNull() {
            addCriterion("timed_task_json is null");
            return this;
        }

        public Criteria andTimedTaskJsonIsNotNull() {
            addCriterion("timed_task_json is not null");
            return this;
        }

        public Criteria andTimedTaskJsonEqualTo(String value) {
            addCriterion("timed_task_json =", value, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonNotEqualTo(String value) {
            addCriterion("timed_task_json <>", value, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonGreaterThan(String value) {
            addCriterion("timed_task_json >", value, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonGreaterThanOrEqualTo(String value) {
            addCriterion("timed_task_json >=", value, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonLessThan(String value) {
            addCriterion("timed_task_json <", value, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonLessThanOrEqualTo(String value) {
            addCriterion("timed_task_json <=", value, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonLike(String value) {
            addCriterion("timed_task_json like", value, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonNotLike(String value) {
            addCriterion("timed_task_json not like", value, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonIn(List<String> values) {
            addCriterion("timed_task_json in", values, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonNotIn(List<String> values) {
            addCriterion("timed_task_json not in", values, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonBetween(String value1, String value2) {
            addCriterion("timed_task_json between", value1, value2, "timedTaskJson");
            return this;
        }

        public Criteria andTimedTaskJsonNotBetween(String value1, String value2) {
            addCriterion("timed_task_json not between", value1, value2, "timedTaskJson");
            return this;
        }

        public Criteria andNeedAuditIsNull() {
            addCriterion("need_audit is null");
            return this;
        }

        public Criteria andNeedAuditIsNotNull() {
            addCriterion("need_audit is not null");
            return this;
        }

        public Criteria andNeedAuditEqualTo(Byte value) {
            addCriterion("need_audit =", value, "needAudit");
            return this;
        }

        public Criteria andNeedAuditNotEqualTo(Byte value) {
            addCriterion("need_audit <>", value, "needAudit");
            return this;
        }

        public Criteria andNeedAuditGreaterThan(Byte value) {
            addCriterion("need_audit >", value, "needAudit");
            return this;
        }

        public Criteria andNeedAuditGreaterThanOrEqualTo(Byte value) {
            addCriterion("need_audit >=", value, "needAudit");
            return this;
        }

        public Criteria andNeedAuditLessThan(Byte value) {
            addCriterion("need_audit <", value, "needAudit");
            return this;
        }

        public Criteria andNeedAuditLessThanOrEqualTo(Byte value) {
            addCriterion("need_audit <=", value, "needAudit");
            return this;
        }

        public Criteria andNeedAuditIn(List<Byte> values) {
            addCriterion("need_audit in", values, "needAudit");
            return this;
        }

        public Criteria andNeedAuditNotIn(List<Byte> values) {
            addCriterion("need_audit not in", values, "needAudit");
            return this;
        }

        public Criteria andNeedAuditBetween(Byte value1, Byte value2) {
            addCriterion("need_audit between", value1, value2, "needAudit");
            return this;
        }

        public Criteria andNeedAuditNotBetween(Byte value1, Byte value2) {
            addCriterion("need_audit not between", value1, value2, "needAudit");
            return this;
        }

        public Criteria andRefundSceneIsNull() {
            addCriterion("refund_scene is null");
            return this;
        }

        public Criteria andRefundSceneIsNotNull() {
            addCriterion("refund_scene is not null");
            return this;
        }

        public Criteria andRefundSceneEqualTo(Short value) {
            addCriterion("refund_scene =", value, "refundScene");
            return this;
        }

        public Criteria andRefundSceneNotEqualTo(Short value) {
            addCriterion("refund_scene <>", value, "refundScene");
            return this;
        }

        public Criteria andRefundSceneGreaterThan(Short value) {
            addCriterion("refund_scene >", value, "refundScene");
            return this;
        }

        public Criteria andRefundSceneGreaterThanOrEqualTo(Short value) {
            addCriterion("refund_scene >=", value, "refundScene");
            return this;
        }

        public Criteria andRefundSceneLessThan(Short value) {
            addCriterion("refund_scene <", value, "refundScene");
            return this;
        }

        public Criteria andRefundSceneLessThanOrEqualTo(Short value) {
            addCriterion("refund_scene <=", value, "refundScene");
            return this;
        }

        public Criteria andRefundSceneIn(List<Short> values) {
            addCriterion("refund_scene in", values, "refundScene");
            return this;
        }

        public Criteria andRefundSceneNotIn(List<Short> values) {
            addCriterion("refund_scene not in", values, "refundScene");
            return this;
        }

        public Criteria andRefundSceneBetween(Short value1, Short value2) {
            addCriterion("refund_scene between", value1, value2, "refundScene");
            return this;
        }

        public Criteria andRefundSceneNotBetween(Short value1, Short value2) {
            addCriterion("refund_scene not between", value1, value2, "refundScene");
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

        public Criteria andLiquidatedDamagesFeeIsNull() {
            addCriterion("liquidated_damages_fee is null");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeIsNotNull() {
            addCriterion("liquidated_damages_fee is not null");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeEqualTo(Integer value) {
            addCriterion("liquidated_damages_fee =", value, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeNotEqualTo(Integer value) {
            addCriterion("liquidated_damages_fee <>", value, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeGreaterThan(Integer value) {
            addCriterion("liquidated_damages_fee >", value, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("liquidated_damages_fee >=", value, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeLessThan(Integer value) {
            addCriterion("liquidated_damages_fee <", value, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeLessThanOrEqualTo(Integer value) {
            addCriterion("liquidated_damages_fee <=", value, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeIn(List<Integer> values) {
            addCriterion("liquidated_damages_fee in", values, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeNotIn(List<Integer> values) {
            addCriterion("liquidated_damages_fee not in", values, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeBetween(Integer value1, Integer value2) {
            addCriterion("liquidated_damages_fee between", value1, value2, "liquidatedDamagesFee");
            return this;
        }

        public Criteria andLiquidatedDamagesFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("liquidated_damages_fee not between", value1, value2, "liquidatedDamagesFee");
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
    }
}
