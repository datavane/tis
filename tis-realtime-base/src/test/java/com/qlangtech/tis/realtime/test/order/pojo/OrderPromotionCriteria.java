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
public class OrderPromotionCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<OrderPromotionColEnum> cols = Sets.newHashSet();

    public OrderPromotionCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected OrderPromotionCriteria(OrderPromotionCriteria example) {
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

    public final List<OrderPromotionColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(OrderPromotionColEnum... colName) {
        for (OrderPromotionColEnum c : colName) {
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

        public Criteria andPromotionIdIsNull() {
            addCriterion("promotion_id is null");
            return this;
        }

        public Criteria andPromotionIdIsNotNull() {
            addCriterion("promotion_id is not null");
            return this;
        }

        public Criteria andPromotionIdEqualTo(String value) {
            addCriterion("promotion_id =", value, "promotionId");
            return this;
        }

        public Criteria andPromotionIdNotEqualTo(String value) {
            addCriterion("promotion_id <>", value, "promotionId");
            return this;
        }

        public Criteria andPromotionIdGreaterThan(String value) {
            addCriterion("promotion_id >", value, "promotionId");
            return this;
        }

        public Criteria andPromotionIdGreaterThanOrEqualTo(String value) {
            addCriterion("promotion_id >=", value, "promotionId");
            return this;
        }

        public Criteria andPromotionIdLessThan(String value) {
            addCriterion("promotion_id <", value, "promotionId");
            return this;
        }

        public Criteria andPromotionIdLessThanOrEqualTo(String value) {
            addCriterion("promotion_id <=", value, "promotionId");
            return this;
        }

        public Criteria andPromotionIdLike(String value) {
            addCriterion("promotion_id like", value, "promotionId");
            return this;
        }

        public Criteria andPromotionIdNotLike(String value) {
            addCriterion("promotion_id not like", value, "promotionId");
            return this;
        }

        public Criteria andPromotionIdIn(List<String> values) {
            addCriterion("promotion_id in", values, "promotionId");
            return this;
        }

        public Criteria andPromotionIdNotIn(List<String> values) {
            addCriterion("promotion_id not in", values, "promotionId");
            return this;
        }

        public Criteria andPromotionIdBetween(String value1, String value2) {
            addCriterion("promotion_id between", value1, value2, "promotionId");
            return this;
        }

        public Criteria andPromotionIdNotBetween(String value1, String value2) {
            addCriterion("promotion_id not between", value1, value2, "promotionId");
            return this;
        }

        public Criteria andPromotionShowNameIsNull() {
            addCriterion("promotion_show_name is null");
            return this;
        }

        public Criteria andPromotionShowNameIsNotNull() {
            addCriterion("promotion_show_name is not null");
            return this;
        }

        public Criteria andPromotionShowNameEqualTo(String value) {
            addCriterion("promotion_show_name =", value, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameNotEqualTo(String value) {
            addCriterion("promotion_show_name <>", value, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameGreaterThan(String value) {
            addCriterion("promotion_show_name >", value, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameGreaterThanOrEqualTo(String value) {
            addCriterion("promotion_show_name >=", value, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameLessThan(String value) {
            addCriterion("promotion_show_name <", value, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameLessThanOrEqualTo(String value) {
            addCriterion("promotion_show_name <=", value, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameLike(String value) {
            addCriterion("promotion_show_name like", value, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameNotLike(String value) {
            addCriterion("promotion_show_name not like", value, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameIn(List<String> values) {
            addCriterion("promotion_show_name in", values, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameNotIn(List<String> values) {
            addCriterion("promotion_show_name not in", values, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameBetween(String value1, String value2) {
            addCriterion("promotion_show_name between", value1, value2, "promotionShowName");
            return this;
        }

        public Criteria andPromotionShowNameNotBetween(String value1, String value2) {
            addCriterion("promotion_show_name not between", value1, value2, "promotionShowName");
            return this;
        }

        public Criteria andPromotionNameIsNull() {
            addCriterion("promotion_name is null");
            return this;
        }

        public Criteria andPromotionNameIsNotNull() {
            addCriterion("promotion_name is not null");
            return this;
        }

        public Criteria andPromotionNameEqualTo(String value) {
            addCriterion("promotion_name =", value, "promotionName");
            return this;
        }

        public Criteria andPromotionNameNotEqualTo(String value) {
            addCriterion("promotion_name <>", value, "promotionName");
            return this;
        }

        public Criteria andPromotionNameGreaterThan(String value) {
            addCriterion("promotion_name >", value, "promotionName");
            return this;
        }

        public Criteria andPromotionNameGreaterThanOrEqualTo(String value) {
            addCriterion("promotion_name >=", value, "promotionName");
            return this;
        }

        public Criteria andPromotionNameLessThan(String value) {
            addCriterion("promotion_name <", value, "promotionName");
            return this;
        }

        public Criteria andPromotionNameLessThanOrEqualTo(String value) {
            addCriterion("promotion_name <=", value, "promotionName");
            return this;
        }

        public Criteria andPromotionNameLike(String value) {
            addCriterion("promotion_name like", value, "promotionName");
            return this;
        }

        public Criteria andPromotionNameNotLike(String value) {
            addCriterion("promotion_name not like", value, "promotionName");
            return this;
        }

        public Criteria andPromotionNameIn(List<String> values) {
            addCriterion("promotion_name in", values, "promotionName");
            return this;
        }

        public Criteria andPromotionNameNotIn(List<String> values) {
            addCriterion("promotion_name not in", values, "promotionName");
            return this;
        }

        public Criteria andPromotionNameBetween(String value1, String value2) {
            addCriterion("promotion_name between", value1, value2, "promotionName");
            return this;
        }

        public Criteria andPromotionNameNotBetween(String value1, String value2) {
            addCriterion("promotion_name not between", value1, value2, "promotionName");
            return this;
        }

        public Criteria andPromotionTypeIsNull() {
            addCriterion("promotion_type is null");
            return this;
        }

        public Criteria andPromotionTypeIsNotNull() {
            addCriterion("promotion_type is not null");
            return this;
        }

        public Criteria andPromotionTypeEqualTo(Integer value) {
            addCriterion("promotion_type =", value, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeNotEqualTo(Integer value) {
            addCriterion("promotion_type <>", value, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeGreaterThan(Integer value) {
            addCriterion("promotion_type >", value, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("promotion_type >=", value, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeLessThan(Integer value) {
            addCriterion("promotion_type <", value, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeLessThanOrEqualTo(Integer value) {
            addCriterion("promotion_type <=", value, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeIn(List<Integer> values) {
            addCriterion("promotion_type in", values, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeNotIn(List<Integer> values) {
            addCriterion("promotion_type not in", values, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeBetween(Integer value1, Integer value2) {
            addCriterion("promotion_type between", value1, value2, "promotionType");
            return this;
        }

        public Criteria andPromotionTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("promotion_type not between", value1, value2, "promotionType");
            return this;
        }

        public Criteria andPromotionSubTypeIsNull() {
            addCriterion("promotion_sub_type is null");
            return this;
        }

        public Criteria andPromotionSubTypeIsNotNull() {
            addCriterion("promotion_sub_type is not null");
            return this;
        }

        public Criteria andPromotionSubTypeEqualTo(Byte value) {
            addCriterion("promotion_sub_type =", value, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeNotEqualTo(Byte value) {
            addCriterion("promotion_sub_type <>", value, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeGreaterThan(Byte value) {
            addCriterion("promotion_sub_type >", value, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("promotion_sub_type >=", value, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeLessThan(Byte value) {
            addCriterion("promotion_sub_type <", value, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeLessThanOrEqualTo(Byte value) {
            addCriterion("promotion_sub_type <=", value, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeIn(List<Byte> values) {
            addCriterion("promotion_sub_type in", values, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeNotIn(List<Byte> values) {
            addCriterion("promotion_sub_type not in", values, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeBetween(Byte value1, Byte value2) {
            addCriterion("promotion_sub_type between", value1, value2, "promotionSubType");
            return this;
        }

        public Criteria andPromotionSubTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("promotion_sub_type not between", value1, value2, "promotionSubType");
            return this;
        }

        public Criteria andPromotionFeeIsNull() {
            addCriterion("promotion_fee is null");
            return this;
        }

        public Criteria andPromotionFeeIsNotNull() {
            addCriterion("promotion_fee is not null");
            return this;
        }

        public Criteria andPromotionFeeEqualTo(BigDecimal value) {
            addCriterion("promotion_fee =", value, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeNotEqualTo(BigDecimal value) {
            addCriterion("promotion_fee <>", value, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeGreaterThan(BigDecimal value) {
            addCriterion("promotion_fee >", value, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("promotion_fee >=", value, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeLessThan(BigDecimal value) {
            addCriterion("promotion_fee <", value, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("promotion_fee <=", value, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeIn(List<BigDecimal> values) {
            addCriterion("promotion_fee in", values, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeNotIn(List<BigDecimal> values) {
            addCriterion("promotion_fee not in", values, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("promotion_fee between", value1, value2, "promotionFee");
            return this;
        }

        public Criteria andPromotionFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("promotion_fee not between", value1, value2, "promotionFee");
            return this;
        }

        public Criteria andPromotionRatioIsNull() {
            addCriterion("promotion_ratio is null");
            return this;
        }

        public Criteria andPromotionRatioIsNotNull() {
            addCriterion("promotion_ratio is not null");
            return this;
        }

        public Criteria andPromotionRatioEqualTo(BigDecimal value) {
            addCriterion("promotion_ratio =", value, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioNotEqualTo(BigDecimal value) {
            addCriterion("promotion_ratio <>", value, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioGreaterThan(BigDecimal value) {
            addCriterion("promotion_ratio >", value, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("promotion_ratio >=", value, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioLessThan(BigDecimal value) {
            addCriterion("promotion_ratio <", value, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioLessThanOrEqualTo(BigDecimal value) {
            addCriterion("promotion_ratio <=", value, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioIn(List<BigDecimal> values) {
            addCriterion("promotion_ratio in", values, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioNotIn(List<BigDecimal> values) {
            addCriterion("promotion_ratio not in", values, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("promotion_ratio between", value1, value2, "promotionRatio");
            return this;
        }

        public Criteria andPromotionRatioNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("promotion_ratio not between", value1, value2, "promotionRatio");
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

        public Criteria andPromotionSourceIsNull() {
            addCriterion("promotion_source is null");
            return this;
        }

        public Criteria andPromotionSourceIsNotNull() {
            addCriterion("promotion_source is not null");
            return this;
        }

        public Criteria andPromotionSourceEqualTo(Integer value) {
            addCriterion("promotion_source =", value, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceNotEqualTo(Integer value) {
            addCriterion("promotion_source <>", value, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceGreaterThan(Integer value) {
            addCriterion("promotion_source >", value, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceGreaterThanOrEqualTo(Integer value) {
            addCriterion("promotion_source >=", value, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceLessThan(Integer value) {
            addCriterion("promotion_source <", value, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceLessThanOrEqualTo(Integer value) {
            addCriterion("promotion_source <=", value, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceIn(List<Integer> values) {
            addCriterion("promotion_source in", values, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceNotIn(List<Integer> values) {
            addCriterion("promotion_source not in", values, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceBetween(Integer value1, Integer value2) {
            addCriterion("promotion_source between", value1, value2, "promotionSource");
            return this;
        }

        public Criteria andPromotionSourceNotBetween(Integer value1, Integer value2) {
            addCriterion("promotion_source not between", value1, value2, "promotionSource");
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
    }
}
