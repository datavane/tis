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
public class PresellOrderExtraCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<PresellOrderExtraColEnum> cols = Sets.newHashSet();

    public PresellOrderExtraCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected PresellOrderExtraCriteria(PresellOrderExtraCriteria example) {
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

    public final List<PresellOrderExtraColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(PresellOrderExtraColEnum... colName) {
        for (PresellOrderExtraColEnum c : colName) {
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

        public Criteria andStockIdIsNull() {
            addCriterion("stock_id is null");
            return this;
        }

        public Criteria andStockIdIsNotNull() {
            addCriterion("stock_id is not null");
            return this;
        }

        public Criteria andStockIdEqualTo(Long value) {
            addCriterion("stock_id =", value, "stockId");
            return this;
        }

        public Criteria andStockIdNotEqualTo(Long value) {
            addCriterion("stock_id <>", value, "stockId");
            return this;
        }

        public Criteria andStockIdGreaterThan(Long value) {
            addCriterion("stock_id >", value, "stockId");
            return this;
        }

        public Criteria andStockIdGreaterThanOrEqualTo(Long value) {
            addCriterion("stock_id >=", value, "stockId");
            return this;
        }

        public Criteria andStockIdLessThan(Long value) {
            addCriterion("stock_id <", value, "stockId");
            return this;
        }

        public Criteria andStockIdLessThanOrEqualTo(Long value) {
            addCriterion("stock_id <=", value, "stockId");
            return this;
        }

        public Criteria andStockIdIn(List<Long> values) {
            addCriterion("stock_id in", values, "stockId");
            return this;
        }

        public Criteria andStockIdNotIn(List<Long> values) {
            addCriterion("stock_id not in", values, "stockId");
            return this;
        }

        public Criteria andStockIdBetween(Long value1, Long value2) {
            addCriterion("stock_id between", value1, value2, "stockId");
            return this;
        }

        public Criteria andStockIdNotBetween(Long value1, Long value2) {
            addCriterion("stock_id not between", value1, value2, "stockId");
            return this;
        }

        public Criteria andTimeFrameIdIsNull() {
            addCriterion("time_frame_id is null");
            return this;
        }

        public Criteria andTimeFrameIdIsNotNull() {
            addCriterion("time_frame_id is not null");
            return this;
        }

        public Criteria andTimeFrameIdEqualTo(Long value) {
            addCriterion("time_frame_id =", value, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdNotEqualTo(Long value) {
            addCriterion("time_frame_id <>", value, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdGreaterThan(Long value) {
            addCriterion("time_frame_id >", value, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdGreaterThanOrEqualTo(Long value) {
            addCriterion("time_frame_id >=", value, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdLessThan(Long value) {
            addCriterion("time_frame_id <", value, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdLessThanOrEqualTo(Long value) {
            addCriterion("time_frame_id <=", value, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdIn(List<Long> values) {
            addCriterion("time_frame_id in", values, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdNotIn(List<Long> values) {
            addCriterion("time_frame_id not in", values, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdBetween(Long value1, Long value2) {
            addCriterion("time_frame_id between", value1, value2, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameIdNotBetween(Long value1, Long value2) {
            addCriterion("time_frame_id not between", value1, value2, "timeFrameId");
            return this;
        }

        public Criteria andTimeFrameNameIsNull() {
            addCriterion("time_frame_name is null");
            return this;
        }

        public Criteria andTimeFrameNameIsNotNull() {
            addCriterion("time_frame_name is not null");
            return this;
        }

        public Criteria andTimeFrameNameEqualTo(String value) {
            addCriterion("time_frame_name =", value, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameNotEqualTo(String value) {
            addCriterion("time_frame_name <>", value, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameGreaterThan(String value) {
            addCriterion("time_frame_name >", value, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameGreaterThanOrEqualTo(String value) {
            addCriterion("time_frame_name >=", value, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameLessThan(String value) {
            addCriterion("time_frame_name <", value, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameLessThanOrEqualTo(String value) {
            addCriterion("time_frame_name <=", value, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameLike(String value) {
            addCriterion("time_frame_name like", value, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameNotLike(String value) {
            addCriterion("time_frame_name not like", value, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameIn(List<String> values) {
            addCriterion("time_frame_name in", values, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameNotIn(List<String> values) {
            addCriterion("time_frame_name not in", values, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameBetween(String value1, String value2) {
            addCriterion("time_frame_name between", value1, value2, "timeFrameName");
            return this;
        }

        public Criteria andTimeFrameNameNotBetween(String value1, String value2) {
            addCriterion("time_frame_name not between", value1, value2, "timeFrameName");
            return this;
        }

        public Criteria andSeatTypeIdIsNull() {
            addCriterion("seat_type_id is null");
            return this;
        }

        public Criteria andSeatTypeIdIsNotNull() {
            addCriterion("seat_type_id is not null");
            return this;
        }

        public Criteria andSeatTypeIdEqualTo(Long value) {
            addCriterion("seat_type_id =", value, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdNotEqualTo(Long value) {
            addCriterion("seat_type_id <>", value, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdGreaterThan(Long value) {
            addCriterion("seat_type_id >", value, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdGreaterThanOrEqualTo(Long value) {
            addCriterion("seat_type_id >=", value, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdLessThan(Long value) {
            addCriterion("seat_type_id <", value, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdLessThanOrEqualTo(Long value) {
            addCriterion("seat_type_id <=", value, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdIn(List<Long> values) {
            addCriterion("seat_type_id in", values, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdNotIn(List<Long> values) {
            addCriterion("seat_type_id not in", values, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdBetween(Long value1, Long value2) {
            addCriterion("seat_type_id between", value1, value2, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeIdNotBetween(Long value1, Long value2) {
            addCriterion("seat_type_id not between", value1, value2, "seatTypeId");
            return this;
        }

        public Criteria andSeatTypeNameIsNull() {
            addCriterion("seat_type_name is null");
            return this;
        }

        public Criteria andSeatTypeNameIsNotNull() {
            addCriterion("seat_type_name is not null");
            return this;
        }

        public Criteria andSeatTypeNameEqualTo(String value) {
            addCriterion("seat_type_name =", value, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameNotEqualTo(String value) {
            addCriterion("seat_type_name <>", value, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameGreaterThan(String value) {
            addCriterion("seat_type_name >", value, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameGreaterThanOrEqualTo(String value) {
            addCriterion("seat_type_name >=", value, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameLessThan(String value) {
            addCriterion("seat_type_name <", value, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameLessThanOrEqualTo(String value) {
            addCriterion("seat_type_name <=", value, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameLike(String value) {
            addCriterion("seat_type_name like", value, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameNotLike(String value) {
            addCriterion("seat_type_name not like", value, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameIn(List<String> values) {
            addCriterion("seat_type_name in", values, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameNotIn(List<String> values) {
            addCriterion("seat_type_name not in", values, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameBetween(String value1, String value2) {
            addCriterion("seat_type_name between", value1, value2, "seatTypeName");
            return this;
        }

        public Criteria andSeatTypeNameNotBetween(String value1, String value2) {
            addCriterion("seat_type_name not between", value1, value2, "seatTypeName");
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

        public Criteria andDiscountRatioEqualTo(Double value) {
            addCriterion("discount_ratio =", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioNotEqualTo(Double value) {
            addCriterion("discount_ratio <>", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioGreaterThan(Double value) {
            addCriterion("discount_ratio >", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioGreaterThanOrEqualTo(Double value) {
            addCriterion("discount_ratio >=", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioLessThan(Double value) {
            addCriterion("discount_ratio <", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioLessThanOrEqualTo(Double value) {
            addCriterion("discount_ratio <=", value, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioIn(List<Double> values) {
            addCriterion("discount_ratio in", values, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioNotIn(List<Double> values) {
            addCriterion("discount_ratio not in", values, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioBetween(Double value1, Double value2) {
            addCriterion("discount_ratio between", value1, value2, "discountRatio");
            return this;
        }

        public Criteria andDiscountRatioNotBetween(Double value1, Double value2) {
            addCriterion("discount_ratio not between", value1, value2, "discountRatio");
            return this;
        }

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return this;
        }

        public Criteria andStartTimeEqualTo(Long value) {
            addCriterion("start_time =", value, "startTime");
            return this;
        }

        public Criteria andStartTimeNotEqualTo(Long value) {
            addCriterion("start_time <>", value, "startTime");
            return this;
        }

        public Criteria andStartTimeGreaterThan(Long value) {
            addCriterion("start_time >", value, "startTime");
            return this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("start_time >=", value, "startTime");
            return this;
        }

        public Criteria andStartTimeLessThan(Long value) {
            addCriterion("start_time <", value, "startTime");
            return this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(Long value) {
            addCriterion("start_time <=", value, "startTime");
            return this;
        }

        public Criteria andStartTimeIn(List<Long> values) {
            addCriterion("start_time in", values, "startTime");
            return this;
        }

        public Criteria andStartTimeNotIn(List<Long> values) {
            addCriterion("start_time not in", values, "startTime");
            return this;
        }

        public Criteria andStartTimeBetween(Long value1, Long value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return this;
        }

        public Criteria andStartTimeNotBetween(Long value1, Long value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
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

        public Criteria andVerifyTimeIsNull() {
            addCriterion("verify_time is null");
            return this;
        }

        public Criteria andVerifyTimeIsNotNull() {
            addCriterion("verify_time is not null");
            return this;
        }

        public Criteria andVerifyTimeEqualTo(Long value) {
            addCriterion("verify_time =", value, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeNotEqualTo(Long value) {
            addCriterion("verify_time <>", value, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeGreaterThan(Long value) {
            addCriterion("verify_time >", value, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("verify_time >=", value, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeLessThan(Long value) {
            addCriterion("verify_time <", value, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeLessThanOrEqualTo(Long value) {
            addCriterion("verify_time <=", value, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeIn(List<Long> values) {
            addCriterion("verify_time in", values, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeNotIn(List<Long> values) {
            addCriterion("verify_time not in", values, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeBetween(Long value1, Long value2) {
            addCriterion("verify_time between", value1, value2, "verifyTime");
            return this;
        }

        public Criteria andVerifyTimeNotBetween(Long value1, Long value2) {
            addCriterion("verify_time not between", value1, value2, "verifyTime");
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

        public Criteria andOvertimeIsNull() {
            addCriterion("overtime is null");
            return this;
        }

        public Criteria andOvertimeIsNotNull() {
            addCriterion("overtime is not null");
            return this;
        }

        public Criteria andOvertimeEqualTo(Long value) {
            addCriterion("overtime =", value, "overtime");
            return this;
        }

        public Criteria andOvertimeNotEqualTo(Long value) {
            addCriterion("overtime <>", value, "overtime");
            return this;
        }

        public Criteria andOvertimeGreaterThan(Long value) {
            addCriterion("overtime >", value, "overtime");
            return this;
        }

        public Criteria andOvertimeGreaterThanOrEqualTo(Long value) {
            addCriterion("overtime >=", value, "overtime");
            return this;
        }

        public Criteria andOvertimeLessThan(Long value) {
            addCriterion("overtime <", value, "overtime");
            return this;
        }

        public Criteria andOvertimeLessThanOrEqualTo(Long value) {
            addCriterion("overtime <=", value, "overtime");
            return this;
        }

        public Criteria andOvertimeIn(List<Long> values) {
            addCriterion("overtime in", values, "overtime");
            return this;
        }

        public Criteria andOvertimeNotIn(List<Long> values) {
            addCriterion("overtime not in", values, "overtime");
            return this;
        }

        public Criteria andOvertimeBetween(Long value1, Long value2) {
            addCriterion("overtime between", value1, value2, "overtime");
            return this;
        }

        public Criteria andOvertimeNotBetween(Long value1, Long value2) {
            addCriterion("overtime not between", value1, value2, "overtime");
            return this;
        }
    }
}
