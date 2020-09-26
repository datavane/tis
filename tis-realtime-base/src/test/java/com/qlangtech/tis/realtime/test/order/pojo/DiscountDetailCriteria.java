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
public class DiscountDetailCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<DiscountDetailColEnum> cols = Sets.newHashSet();

    public DiscountDetailCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected DiscountDetailCriteria(DiscountDetailCriteria example) {
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

    public final List<DiscountDetailColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(DiscountDetailColEnum... colName) {
        for (DiscountDetailColEnum c : colName) {
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

        public Criteria andInstanceIdIsNull() {
            addCriterion("instance_id is null");
            return this;
        }

        public Criteria andInstanceIdIsNotNull() {
            addCriterion("instance_id is not null");
            return this;
        }

        public Criteria andInstanceIdEqualTo(String value) {
            addCriterion("instance_id =", value, "instanceId");
            return this;
        }

        public Criteria andInstanceIdNotEqualTo(String value) {
            addCriterion("instance_id <>", value, "instanceId");
            return this;
        }

        public Criteria andInstanceIdGreaterThan(String value) {
            addCriterion("instance_id >", value, "instanceId");
            return this;
        }

        public Criteria andInstanceIdGreaterThanOrEqualTo(String value) {
            addCriterion("instance_id >=", value, "instanceId");
            return this;
        }

        public Criteria andInstanceIdLessThan(String value) {
            addCriterion("instance_id <", value, "instanceId");
            return this;
        }

        public Criteria andInstanceIdLessThanOrEqualTo(String value) {
            addCriterion("instance_id <=", value, "instanceId");
            return this;
        }

        public Criteria andInstanceIdLike(String value) {
            addCriterion("instance_id like", value, "instanceId");
            return this;
        }

        public Criteria andInstanceIdNotLike(String value) {
            addCriterion("instance_id not like", value, "instanceId");
            return this;
        }

        public Criteria andInstanceIdIn(List<String> values) {
            addCriterion("instance_id in", values, "instanceId");
            return this;
        }

        public Criteria andInstanceIdNotIn(List<String> values) {
            addCriterion("instance_id not in", values, "instanceId");
            return this;
        }

        public Criteria andInstanceIdBetween(String value1, String value2) {
            addCriterion("instance_id between", value1, value2, "instanceId");
            return this;
        }

        public Criteria andInstanceIdNotBetween(String value1, String value2) {
            addCriterion("instance_id not between", value1, value2, "instanceId");
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

        public Criteria andDiscountIdIsNull() {
            addCriterion("discount_id is null");
            return this;
        }

        public Criteria andDiscountIdIsNotNull() {
            addCriterion("discount_id is not null");
            return this;
        }

        public Criteria andDiscountIdEqualTo(String value) {
            addCriterion("discount_id =", value, "discountId");
            return this;
        }

        public Criteria andDiscountIdNotEqualTo(String value) {
            addCriterion("discount_id <>", value, "discountId");
            return this;
        }

        public Criteria andDiscountIdGreaterThan(String value) {
            addCriterion("discount_id >", value, "discountId");
            return this;
        }

        public Criteria andDiscountIdGreaterThanOrEqualTo(String value) {
            addCriterion("discount_id >=", value, "discountId");
            return this;
        }

        public Criteria andDiscountIdLessThan(String value) {
            addCriterion("discount_id <", value, "discountId");
            return this;
        }

        public Criteria andDiscountIdLessThanOrEqualTo(String value) {
            addCriterion("discount_id <=", value, "discountId");
            return this;
        }

        public Criteria andDiscountIdLike(String value) {
            addCriterion("discount_id like", value, "discountId");
            return this;
        }

        public Criteria andDiscountIdNotLike(String value) {
            addCriterion("discount_id not like", value, "discountId");
            return this;
        }

        public Criteria andDiscountIdIn(List<String> values) {
            addCriterion("discount_id in", values, "discountId");
            return this;
        }

        public Criteria andDiscountIdNotIn(List<String> values) {
            addCriterion("discount_id not in", values, "discountId");
            return this;
        }

        public Criteria andDiscountIdBetween(String value1, String value2) {
            addCriterion("discount_id between", value1, value2, "discountId");
            return this;
        }

        public Criteria andDiscountIdNotBetween(String value1, String value2) {
            addCriterion("discount_id not between", value1, value2, "discountId");
            return this;
        }

        public Criteria andDiscountNameIsNull() {
            addCriterion("discount_name is null");
            return this;
        }

        public Criteria andDiscountNameIsNotNull() {
            addCriterion("discount_name is not null");
            return this;
        }

        public Criteria andDiscountNameEqualTo(String value) {
            addCriterion("discount_name =", value, "discountName");
            return this;
        }

        public Criteria andDiscountNameNotEqualTo(String value) {
            addCriterion("discount_name <>", value, "discountName");
            return this;
        }

        public Criteria andDiscountNameGreaterThan(String value) {
            addCriterion("discount_name >", value, "discountName");
            return this;
        }

        public Criteria andDiscountNameGreaterThanOrEqualTo(String value) {
            addCriterion("discount_name >=", value, "discountName");
            return this;
        }

        public Criteria andDiscountNameLessThan(String value) {
            addCriterion("discount_name <", value, "discountName");
            return this;
        }

        public Criteria andDiscountNameLessThanOrEqualTo(String value) {
            addCriterion("discount_name <=", value, "discountName");
            return this;
        }

        public Criteria andDiscountNameLike(String value) {
            addCriterion("discount_name like", value, "discountName");
            return this;
        }

        public Criteria andDiscountNameNotLike(String value) {
            addCriterion("discount_name not like", value, "discountName");
            return this;
        }

        public Criteria andDiscountNameIn(List<String> values) {
            addCriterion("discount_name in", values, "discountName");
            return this;
        }

        public Criteria andDiscountNameNotIn(List<String> values) {
            addCriterion("discount_name not in", values, "discountName");
            return this;
        }

        public Criteria andDiscountNameBetween(String value1, String value2) {
            addCriterion("discount_name between", value1, value2, "discountName");
            return this;
        }

        public Criteria andDiscountNameNotBetween(String value1, String value2) {
            addCriterion("discount_name not between", value1, value2, "discountName");
            return this;
        }

        public Criteria andDiscountTypeIsNull() {
            addCriterion("discount_type is null");
            return this;
        }

        public Criteria andDiscountTypeIsNotNull() {
            addCriterion("discount_type is not null");
            return this;
        }

        public Criteria andDiscountTypeEqualTo(Integer value) {
            addCriterion("discount_type =", value, "discountType");
            return this;
        }

        public Criteria andDiscountTypeNotEqualTo(Integer value) {
            addCriterion("discount_type <>", value, "discountType");
            return this;
        }

        public Criteria andDiscountTypeGreaterThan(Integer value) {
            addCriterion("discount_type >", value, "discountType");
            return this;
        }

        public Criteria andDiscountTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("discount_type >=", value, "discountType");
            return this;
        }

        public Criteria andDiscountTypeLessThan(Integer value) {
            addCriterion("discount_type <", value, "discountType");
            return this;
        }

        public Criteria andDiscountTypeLessThanOrEqualTo(Integer value) {
            addCriterion("discount_type <=", value, "discountType");
            return this;
        }

        public Criteria andDiscountTypeIn(List<Integer> values) {
            addCriterion("discount_type in", values, "discountType");
            return this;
        }

        public Criteria andDiscountTypeNotIn(List<Integer> values) {
            addCriterion("discount_type not in", values, "discountType");
            return this;
        }

        public Criteria andDiscountTypeBetween(Integer value1, Integer value2) {
            addCriterion("discount_type between", value1, value2, "discountType");
            return this;
        }

        public Criteria andDiscountTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("discount_type not between", value1, value2, "discountType");
            return this;
        }

        public Criteria andDiscountSubTypeIsNull() {
            addCriterion("discount_sub_type is null");
            return this;
        }

        public Criteria andDiscountSubTypeIsNotNull() {
            addCriterion("discount_sub_type is not null");
            return this;
        }

        public Criteria andDiscountSubTypeEqualTo(Integer value) {
            addCriterion("discount_sub_type =", value, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeNotEqualTo(Integer value) {
            addCriterion("discount_sub_type <>", value, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeGreaterThan(Integer value) {
            addCriterion("discount_sub_type >", value, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("discount_sub_type >=", value, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeLessThan(Integer value) {
            addCriterion("discount_sub_type <", value, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeLessThanOrEqualTo(Integer value) {
            addCriterion("discount_sub_type <=", value, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeIn(List<Integer> values) {
            addCriterion("discount_sub_type in", values, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeNotIn(List<Integer> values) {
            addCriterion("discount_sub_type not in", values, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeBetween(Integer value1, Integer value2) {
            addCriterion("discount_sub_type between", value1, value2, "discountSubType");
            return this;
        }

        public Criteria andDiscountSubTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("discount_sub_type not between", value1, value2, "discountSubType");
            return this;
        }

        public Criteria andDiscountFeeIsNull() {
            addCriterion("discount_fee is null");
            return this;
        }

        public Criteria andDiscountFeeIsNotNull() {
            addCriterion("discount_fee is not null");
            return this;
        }

        public Criteria andDiscountFeeEqualTo(BigDecimal value) {
            addCriterion("discount_fee =", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeNotEqualTo(BigDecimal value) {
            addCriterion("discount_fee <>", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeGreaterThan(BigDecimal value) {
            addCriterion("discount_fee >", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_fee >=", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeLessThan(BigDecimal value) {
            addCriterion("discount_fee <", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_fee <=", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeIn(List<BigDecimal> values) {
            addCriterion("discount_fee in", values, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeNotIn(List<BigDecimal> values) {
            addCriterion("discount_fee not in", values, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_fee between", value1, value2, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_fee not between", value1, value2, "discountFee");
            return this;
        }

        public Criteria andDiscountRatioIsNull() {
            addCriterion("discount_ratio is null");
            return this;
        }

        public Criteria andDiscountRatioIsNotNull() {
            addCriterion("discount_ratio is not null");
            return this;
        }

        public Criteria andDiscountRatioEqualTo(BigDecimal value) {
            addCriterion("discount_ratio =", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioNotEqualTo(BigDecimal value) {
            addCriterion("discount_ratio <>", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioGreaterThan(BigDecimal value) {
            addCriterion("discount_ratio >", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_ratio >=", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioLessThan(BigDecimal value) {
            addCriterion("discount_ratio <", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioLessThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_ratio <=", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioIn(List<BigDecimal> values) {
            addCriterion("discount_ratio in", values, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioNotIn(List<BigDecimal> values) {
            addCriterion("discount_ratio not in", values, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_ratio between", value1, value2, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_ratio not between", value1, value2, "discountRatio");
            return this;
        }

        public Criteria andOrderDiscountFeeIsNull() {
            addCriterion("order_discount_fee is null");
            return this;
        }

        public Criteria andOrderDiscountFeeIsNotNull() {
            addCriterion("order_discount_fee is not null");
            return this;
        }

        public Criteria andOrderDiscountFeeEqualTo(BigDecimal value) {
            addCriterion("order_discount_fee =", value, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeNotEqualTo(BigDecimal value) {
            addCriterion("order_discount_fee <>", value, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeGreaterThan(BigDecimal value) {
            addCriterion("order_discount_fee >", value, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("order_discount_fee >=", value, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeLessThan(BigDecimal value) {
            addCriterion("order_discount_fee <", value, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("order_discount_fee <=", value, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeIn(List<BigDecimal> values) {
            addCriterion("order_discount_fee in", values, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeNotIn(List<BigDecimal> values) {
            addCriterion("order_discount_fee not in", values, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("order_discount_fee between", value1, value2, "orderDiscountFee");
            return this;
        }

        public Criteria andOrderDiscountFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("order_discount_fee not between", value1, value2, "orderDiscountFee");
            return this;
        }

        public Criteria andRatioFeeIsNull() {
            addCriterion("ratio_fee is null");
            return this;
        }

        public Criteria andRatioFeeIsNotNull() {
            addCriterion("ratio_fee is not null");
            return this;
        }

        public Criteria andRatioFeeEqualTo(BigDecimal value) {
            addCriterion("ratio_fee =", value, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeNotEqualTo(BigDecimal value) {
            addCriterion("ratio_fee <>", value, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeGreaterThan(BigDecimal value) {
            addCriterion("ratio_fee >", value, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("ratio_fee >=", value, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeLessThan(BigDecimal value) {
            addCriterion("ratio_fee <", value, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("ratio_fee <=", value, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeIn(List<BigDecimal> values) {
            addCriterion("ratio_fee in", values, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeNotIn(List<BigDecimal> values) {
            addCriterion("ratio_fee not in", values, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ratio_fee between", value1, value2, "ratioFee");
            return this;
        }

        public Criteria andRatioFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ratio_fee not between", value1, value2, "ratioFee");
            return this;
        }

        public Criteria andOriginFeeIsNull() {
            addCriterion("origin_fee is null");
            return this;
        }

        public Criteria andOriginFeeIsNotNull() {
            addCriterion("origin_fee is not null");
            return this;
        }

        public Criteria andOriginFeeEqualTo(BigDecimal value) {
            addCriterion("origin_fee =", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeNotEqualTo(BigDecimal value) {
            addCriterion("origin_fee <>", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeGreaterThan(BigDecimal value) {
            addCriterion("origin_fee >", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_fee >=", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeLessThan(BigDecimal value) {
            addCriterion("origin_fee <", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_fee <=", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeIn(List<BigDecimal> values) {
            addCriterion("origin_fee in", values, "originFee");
            return this;
        }

        public Criteria andOriginFeeNotIn(List<BigDecimal> values) {
            addCriterion("origin_fee not in", values, "originFee");
            return this;
        }

        public Criteria andOriginFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_fee between", value1, value2, "originFee");
            return this;
        }

        public Criteria andOriginFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_fee not between", value1, value2, "originFee");
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

        public Criteria andIsValidEqualTo(Integer value) {
            addCriterion("is_valid =", value, "isValid");
            return this;
        }

        public Criteria andIsValidNotEqualTo(Integer value) {
            addCriterion("is_valid <>", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThan(Integer value) {
            addCriterion("is_valid >", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_valid >=", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThan(Integer value) {
            addCriterion("is_valid <", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThanOrEqualTo(Integer value) {
            addCriterion("is_valid <=", value, "isValid");
            return this;
        }

        public Criteria andIsValidIn(List<Integer> values) {
            addCriterion("is_valid in", values, "isValid");
            return this;
        }

        public Criteria andIsValidNotIn(List<Integer> values) {
            addCriterion("is_valid not in", values, "isValid");
            return this;
        }

        public Criteria andIsValidBetween(Integer value1, Integer value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return this;
        }

        public Criteria andIsValidNotBetween(Integer value1, Integer value2) {
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

        public Criteria andActivityIdIsNull() {
            addCriterion("activity_id is null");
            return this;
        }

        public Criteria andActivityIdIsNotNull() {
            addCriterion("activity_id is not null");
            return this;
        }

        public Criteria andActivityIdEqualTo(String value) {
            addCriterion("activity_id =", value, "activityId");
            return this;
        }

        public Criteria andActivityIdNotEqualTo(String value) {
            addCriterion("activity_id <>", value, "activityId");
            return this;
        }

        public Criteria andActivityIdGreaterThan(String value) {
            addCriterion("activity_id >", value, "activityId");
            return this;
        }

        public Criteria andActivityIdGreaterThanOrEqualTo(String value) {
            addCriterion("activity_id >=", value, "activityId");
            return this;
        }

        public Criteria andActivityIdLessThan(String value) {
            addCriterion("activity_id <", value, "activityId");
            return this;
        }

        public Criteria andActivityIdLessThanOrEqualTo(String value) {
            addCriterion("activity_id <=", value, "activityId");
            return this;
        }

        public Criteria andActivityIdLike(String value) {
            addCriterion("activity_id like", value, "activityId");
            return this;
        }

        public Criteria andActivityIdNotLike(String value) {
            addCriterion("activity_id not like", value, "activityId");
            return this;
        }

        public Criteria andActivityIdIn(List<String> values) {
            addCriterion("activity_id in", values, "activityId");
            return this;
        }

        public Criteria andActivityIdNotIn(List<String> values) {
            addCriterion("activity_id not in", values, "activityId");
            return this;
        }

        public Criteria andActivityIdBetween(String value1, String value2) {
            addCriterion("activity_id between", value1, value2, "activityId");
            return this;
        }

        public Criteria andActivityIdNotBetween(String value1, String value2) {
            addCriterion("activity_id not between", value1, value2, "activityId");
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

        public Criteria andOrderPromotionIdIsNull() {
            addCriterion("order_promotion_id is null");
            return this;
        }

        public Criteria andOrderPromotionIdIsNotNull() {
            addCriterion("order_promotion_id is not null");
            return this;
        }

        public Criteria andOrderPromotionIdEqualTo(String value) {
            addCriterion("order_promotion_id =", value, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdNotEqualTo(String value) {
            addCriterion("order_promotion_id <>", value, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdGreaterThan(String value) {
            addCriterion("order_promotion_id >", value, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdGreaterThanOrEqualTo(String value) {
            addCriterion("order_promotion_id >=", value, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdLessThan(String value) {
            addCriterion("order_promotion_id <", value, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdLessThanOrEqualTo(String value) {
            addCriterion("order_promotion_id <=", value, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdLike(String value) {
            addCriterion("order_promotion_id like", value, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdNotLike(String value) {
            addCriterion("order_promotion_id not like", value, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdIn(List<String> values) {
            addCriterion("order_promotion_id in", values, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdNotIn(List<String> values) {
            addCriterion("order_promotion_id not in", values, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdBetween(String value1, String value2) {
            addCriterion("order_promotion_id between", value1, value2, "orderPromotionId");
            return this;
        }

        public Criteria andOrderPromotionIdNotBetween(String value1, String value2) {
            addCriterion("order_promotion_id not between", value1, value2, "orderPromotionId");
            return this;
        }
    }
}
