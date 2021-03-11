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

import com.qlangtech.tis.manage.common.TISBaseCriteria;
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
public class SpecialfeeCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<SpecialfeeColEnum> cols = Sets.newHashSet();

    public SpecialfeeCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected SpecialfeeCriteria(SpecialfeeCriteria example) {
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

    public final List<SpecialfeeColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(SpecialfeeColEnum... colName) {
        for (SpecialfeeColEnum c : colName) {
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

        public Criteria andSpecialfeeIdIsNull() {
            addCriterion("specialfee_id is null");
            return this;
        }

        public Criteria andSpecialfeeIdIsNotNull() {
            addCriterion("specialfee_id is not null");
            return this;
        }

        public Criteria andSpecialfeeIdEqualTo(String value) {
            addCriterion("specialfee_id =", value, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdNotEqualTo(String value) {
            addCriterion("specialfee_id <>", value, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdGreaterThan(String value) {
            addCriterion("specialfee_id >", value, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdGreaterThanOrEqualTo(String value) {
            addCriterion("specialfee_id >=", value, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdLessThan(String value) {
            addCriterion("specialfee_id <", value, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdLessThanOrEqualTo(String value) {
            addCriterion("specialfee_id <=", value, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdLike(String value) {
            addCriterion("specialfee_id like", value, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdNotLike(String value) {
            addCriterion("specialfee_id not like", value, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdIn(List<String> values) {
            addCriterion("specialfee_id in", values, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdNotIn(List<String> values) {
            addCriterion("specialfee_id not in", values, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdBetween(String value1, String value2) {
            addCriterion("specialfee_id between", value1, value2, "specialfeeId");
            return this;
        }

        public Criteria andSpecialfeeIdNotBetween(String value1, String value2) {
            addCriterion("specialfee_id not between", value1, value2, "specialfeeId");
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

        public Criteria andKindIsNull() {
            addCriterion("kind is null");
            return this;
        }

        public Criteria andKindIsNotNull() {
            addCriterion("kind is not null");
            return this;
        }

        public Criteria andKindEqualTo(Byte value) {
            addCriterion("kind =", value, "kind");
            return this;
        }

        public Criteria andKindNotEqualTo(Byte value) {
            addCriterion("kind <>", value, "kind");
            return this;
        }

        public Criteria andKindGreaterThan(Byte value) {
            addCriterion("kind >", value, "kind");
            return this;
        }

        public Criteria andKindGreaterThanOrEqualTo(Byte value) {
            addCriterion("kind >=", value, "kind");
            return this;
        }

        public Criteria andKindLessThan(Byte value) {
            addCriterion("kind <", value, "kind");
            return this;
        }

        public Criteria andKindLessThanOrEqualTo(Byte value) {
            addCriterion("kind <=", value, "kind");
            return this;
        }

        public Criteria andKindIn(List<Byte> values) {
            addCriterion("kind in", values, "kind");
            return this;
        }

        public Criteria andKindNotIn(List<Byte> values) {
            addCriterion("kind not in", values, "kind");
            return this;
        }

        public Criteria andKindBetween(Byte value1, Byte value2) {
            addCriterion("kind between", value1, value2, "kind");
            return this;
        }

        public Criteria andKindNotBetween(Byte value1, Byte value2) {
            addCriterion("kind not between", value1, value2, "kind");
            return this;
        }

        public Criteria andFeedetailIdIsNull() {
            addCriterion("feedetail_id is null");
            return this;
        }

        public Criteria andFeedetailIdIsNotNull() {
            addCriterion("feedetail_id is not null");
            return this;
        }

        public Criteria andFeedetailIdEqualTo(String value) {
            addCriterion("feedetail_id =", value, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdNotEqualTo(String value) {
            addCriterion("feedetail_id <>", value, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdGreaterThan(String value) {
            addCriterion("feedetail_id >", value, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdGreaterThanOrEqualTo(String value) {
            addCriterion("feedetail_id >=", value, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdLessThan(String value) {
            addCriterion("feedetail_id <", value, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdLessThanOrEqualTo(String value) {
            addCriterion("feedetail_id <=", value, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdLike(String value) {
            addCriterion("feedetail_id like", value, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdNotLike(String value) {
            addCriterion("feedetail_id not like", value, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdIn(List<String> values) {
            addCriterion("feedetail_id in", values, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdNotIn(List<String> values) {
            addCriterion("feedetail_id not in", values, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdBetween(String value1, String value2) {
            addCriterion("feedetail_id between", value1, value2, "feedetailId");
            return this;
        }

        public Criteria andFeedetailIdNotBetween(String value1, String value2) {
            addCriterion("feedetail_id not between", value1, value2, "feedetailId");
            return this;
        }

        public Criteria andFeeIsNull() {
            addCriterion("fee is null");
            return this;
        }

        public Criteria andFeeIsNotNull() {
            addCriterion("fee is not null");
            return this;
        }

        public Criteria andFeeEqualTo(BigDecimal value) {
            addCriterion("fee =", value, "fee");
            return this;
        }

        public Criteria andFeeNotEqualTo(BigDecimal value) {
            addCriterion("fee <>", value, "fee");
            return this;
        }

        public Criteria andFeeGreaterThan(BigDecimal value) {
            addCriterion("fee >", value, "fee");
            return this;
        }

        public Criteria andFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("fee >=", value, "fee");
            return this;
        }

        public Criteria andFeeLessThan(BigDecimal value) {
            addCriterion("fee <", value, "fee");
            return this;
        }

        public Criteria andFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("fee <=", value, "fee");
            return this;
        }

        public Criteria andFeeIn(List<BigDecimal> values) {
            addCriterion("fee in", values, "fee");
            return this;
        }

        public Criteria andFeeNotIn(List<BigDecimal> values) {
            addCriterion("fee not in", values, "fee");
            return this;
        }

        public Criteria andFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("fee between", value1, value2, "fee");
            return this;
        }

        public Criteria andFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("fee not between", value1, value2, "fee");
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
    }
}
