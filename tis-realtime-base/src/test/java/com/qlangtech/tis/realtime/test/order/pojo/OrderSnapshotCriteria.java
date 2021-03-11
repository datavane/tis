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
public class OrderSnapshotCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<OrderSnapshotColEnum> cols = Sets.newHashSet();

    public OrderSnapshotCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected OrderSnapshotCriteria(OrderSnapshotCriteria example) {
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

    public final List<OrderSnapshotColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(OrderSnapshotColEnum... colName) {
        for (OrderSnapshotColEnum c : colName) {
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

        public Criteria andSnapshotIdIsNull() {
            addCriterion("snapshot_id is null");
            return this;
        }

        public Criteria andSnapshotIdIsNotNull() {
            addCriterion("snapshot_id is not null");
            return this;
        }

        public Criteria andSnapshotIdEqualTo(String value) {
            addCriterion("snapshot_id =", value, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdNotEqualTo(String value) {
            addCriterion("snapshot_id <>", value, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdGreaterThan(String value) {
            addCriterion("snapshot_id >", value, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdGreaterThanOrEqualTo(String value) {
            addCriterion("snapshot_id >=", value, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdLessThan(String value) {
            addCriterion("snapshot_id <", value, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdLessThanOrEqualTo(String value) {
            addCriterion("snapshot_id <=", value, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdLike(String value) {
            addCriterion("snapshot_id like", value, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdNotLike(String value) {
            addCriterion("snapshot_id not like", value, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdIn(List<String> values) {
            addCriterion("snapshot_id in", values, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdNotIn(List<String> values) {
            addCriterion("snapshot_id not in", values, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdBetween(String value1, String value2) {
            addCriterion("snapshot_id between", value1, value2, "snapshotId");
            return this;
        }

        public Criteria andSnapshotIdNotBetween(String value1, String value2) {
            addCriterion("snapshot_id not between", value1, value2, "snapshotId");
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

        public Criteria andTotalFeeIsNull() {
            addCriterion("total_fee is null");
            return this;
        }

        public Criteria andTotalFeeIsNotNull() {
            addCriterion("total_fee is not null");
            return this;
        }

        public Criteria andTotalFeeEqualTo(Integer value) {
            addCriterion("total_fee =", value, "totalFee");
            return this;
        }

        public Criteria andTotalFeeNotEqualTo(Integer value) {
            addCriterion("total_fee <>", value, "totalFee");
            return this;
        }

        public Criteria andTotalFeeGreaterThan(Integer value) {
            addCriterion("total_fee >", value, "totalFee");
            return this;
        }

        public Criteria andTotalFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("total_fee >=", value, "totalFee");
            return this;
        }

        public Criteria andTotalFeeLessThan(Integer value) {
            addCriterion("total_fee <", value, "totalFee");
            return this;
        }

        public Criteria andTotalFeeLessThanOrEqualTo(Integer value) {
            addCriterion("total_fee <=", value, "totalFee");
            return this;
        }

        public Criteria andTotalFeeIn(List<Integer> values) {
            addCriterion("total_fee in", values, "totalFee");
            return this;
        }

        public Criteria andTotalFeeNotIn(List<Integer> values) {
            addCriterion("total_fee not in", values, "totalFee");
            return this;
        }

        public Criteria andTotalFeeBetween(Integer value1, Integer value2) {
            addCriterion("total_fee between", value1, value2, "totalFee");
            return this;
        }

        public Criteria andTotalFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("total_fee not between", value1, value2, "totalFee");
            return this;
        }

        public Criteria andNeedFeeIsNull() {
            addCriterion("need_fee is null");
            return this;
        }

        public Criteria andNeedFeeIsNotNull() {
            addCriterion("need_fee is not null");
            return this;
        }

        public Criteria andNeedFeeEqualTo(Integer value) {
            addCriterion("need_fee =", value, "needFee");
            return this;
        }

        public Criteria andNeedFeeNotEqualTo(Integer value) {
            addCriterion("need_fee <>", value, "needFee");
            return this;
        }

        public Criteria andNeedFeeGreaterThan(Integer value) {
            addCriterion("need_fee >", value, "needFee");
            return this;
        }

        public Criteria andNeedFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("need_fee >=", value, "needFee");
            return this;
        }

        public Criteria andNeedFeeLessThan(Integer value) {
            addCriterion("need_fee <", value, "needFee");
            return this;
        }

        public Criteria andNeedFeeLessThanOrEqualTo(Integer value) {
            addCriterion("need_fee <=", value, "needFee");
            return this;
        }

        public Criteria andNeedFeeIn(List<Integer> values) {
            addCriterion("need_fee in", values, "needFee");
            return this;
        }

        public Criteria andNeedFeeNotIn(List<Integer> values) {
            addCriterion("need_fee not in", values, "needFee");
            return this;
        }

        public Criteria andNeedFeeBetween(Integer value1, Integer value2) {
            addCriterion("need_fee between", value1, value2, "needFee");
            return this;
        }

        public Criteria andNeedFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("need_fee not between", value1, value2, "needFee");
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

        public Criteria andDiscountFeeEqualTo(Integer value) {
            addCriterion("discount_fee =", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeNotEqualTo(Integer value) {
            addCriterion("discount_fee <>", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeGreaterThan(Integer value) {
            addCriterion("discount_fee >", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("discount_fee >=", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeLessThan(Integer value) {
            addCriterion("discount_fee <", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeLessThanOrEqualTo(Integer value) {
            addCriterion("discount_fee <=", value, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeIn(List<Integer> values) {
            addCriterion("discount_fee in", values, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeNotIn(List<Integer> values) {
            addCriterion("discount_fee not in", values, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeBetween(Integer value1, Integer value2) {
            addCriterion("discount_fee between", value1, value2, "discountFee");
            return this;
        }

        public Criteria andDiscountFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("discount_fee not between", value1, value2, "discountFee");
            return this;
        }

        public Criteria andServiceFeeIsNull() {
            addCriterion("service_fee is null");
            return this;
        }

        public Criteria andServiceFeeIsNotNull() {
            addCriterion("service_fee is not null");
            return this;
        }

        public Criteria andServiceFeeEqualTo(Integer value) {
            addCriterion("service_fee =", value, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeNotEqualTo(Integer value) {
            addCriterion("service_fee <>", value, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeGreaterThan(Integer value) {
            addCriterion("service_fee >", value, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("service_fee >=", value, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeLessThan(Integer value) {
            addCriterion("service_fee <", value, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeLessThanOrEqualTo(Integer value) {
            addCriterion("service_fee <=", value, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeIn(List<Integer> values) {
            addCriterion("service_fee in", values, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeNotIn(List<Integer> values) {
            addCriterion("service_fee not in", values, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeBetween(Integer value1, Integer value2) {
            addCriterion("service_fee between", value1, value2, "serviceFee");
            return this;
        }

        public Criteria andServiceFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("service_fee not between", value1, value2, "serviceFee");
            return this;
        }

        public Criteria andPayedFeeIsNull() {
            addCriterion("payed_fee is null");
            return this;
        }

        public Criteria andPayedFeeIsNotNull() {
            addCriterion("payed_fee is not null");
            return this;
        }

        public Criteria andPayedFeeEqualTo(Integer value) {
            addCriterion("payed_fee =", value, "payedFee");
            return this;
        }

        public Criteria andPayedFeeNotEqualTo(Integer value) {
            addCriterion("payed_fee <>", value, "payedFee");
            return this;
        }

        public Criteria andPayedFeeGreaterThan(Integer value) {
            addCriterion("payed_fee >", value, "payedFee");
            return this;
        }

        public Criteria andPayedFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("payed_fee >=", value, "payedFee");
            return this;
        }

        public Criteria andPayedFeeLessThan(Integer value) {
            addCriterion("payed_fee <", value, "payedFee");
            return this;
        }

        public Criteria andPayedFeeLessThanOrEqualTo(Integer value) {
            addCriterion("payed_fee <=", value, "payedFee");
            return this;
        }

        public Criteria andPayedFeeIn(List<Integer> values) {
            addCriterion("payed_fee in", values, "payedFee");
            return this;
        }

        public Criteria andPayedFeeNotIn(List<Integer> values) {
            addCriterion("payed_fee not in", values, "payedFee");
            return this;
        }

        public Criteria andPayedFeeBetween(Integer value1, Integer value2) {
            addCriterion("payed_fee between", value1, value2, "payedFee");
            return this;
        }

        public Criteria andPayedFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("payed_fee not between", value1, value2, "payedFee");
            return this;
        }

        public Criteria andPromotionFromIsNull() {
            addCriterion("promotion_from is null");
            return this;
        }

        public Criteria andPromotionFromIsNotNull() {
            addCriterion("promotion_from is not null");
            return this;
        }

        public Criteria andPromotionFromEqualTo(Short value) {
            addCriterion("promotion_from =", value, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromNotEqualTo(Short value) {
            addCriterion("promotion_from <>", value, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromGreaterThan(Short value) {
            addCriterion("promotion_from >", value, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromGreaterThanOrEqualTo(Short value) {
            addCriterion("promotion_from >=", value, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromLessThan(Short value) {
            addCriterion("promotion_from <", value, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromLessThanOrEqualTo(Short value) {
            addCriterion("promotion_from <=", value, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromIn(List<Short> values) {
            addCriterion("promotion_from in", values, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromNotIn(List<Short> values) {
            addCriterion("promotion_from not in", values, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromBetween(Short value1, Short value2) {
            addCriterion("promotion_from between", value1, value2, "promotionFrom");
            return this;
        }

        public Criteria andPromotionFromNotBetween(Short value1, Short value2) {
            addCriterion("promotion_from not between", value1, value2, "promotionFrom");
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

        public Criteria andMd5IsNull() {
            addCriterion("md5 is null");
            return this;
        }

        public Criteria andMd5IsNotNull() {
            addCriterion("md5 is not null");
            return this;
        }

        public Criteria andMd5EqualTo(String value) {
            addCriterion("md5 =", value, "md5");
            return this;
        }

        public Criteria andMd5NotEqualTo(String value) {
            addCriterion("md5 <>", value, "md5");
            return this;
        }

        public Criteria andMd5GreaterThan(String value) {
            addCriterion("md5 >", value, "md5");
            return this;
        }

        public Criteria andMd5GreaterThanOrEqualTo(String value) {
            addCriterion("md5 >=", value, "md5");
            return this;
        }

        public Criteria andMd5LessThan(String value) {
            addCriterion("md5 <", value, "md5");
            return this;
        }

        public Criteria andMd5LessThanOrEqualTo(String value) {
            addCriterion("md5 <=", value, "md5");
            return this;
        }

        public Criteria andMd5Like(String value) {
            addCriterion("md5 like", value, "md5");
            return this;
        }

        public Criteria andMd5NotLike(String value) {
            addCriterion("md5 not like", value, "md5");
            return this;
        }

        public Criteria andMd5In(List<String> values) {
            addCriterion("md5 in", values, "md5");
            return this;
        }

        public Criteria andMd5NotIn(List<String> values) {
            addCriterion("md5 not in", values, "md5");
            return this;
        }

        public Criteria andMd5Between(String value1, String value2) {
            addCriterion("md5 between", value1, value2, "md5");
            return this;
        }

        public Criteria andMd5NotBetween(String value1, String value2) {
            addCriterion("md5 not between", value1, value2, "md5");
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

        public Criteria andOriginFeeIsNull() {
            addCriterion("origin_fee is null");
            return this;
        }

        public Criteria andOriginFeeIsNotNull() {
            addCriterion("origin_fee is not null");
            return this;
        }

        public Criteria andOriginFeeEqualTo(Integer value) {
            addCriterion("origin_fee =", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeNotEqualTo(Integer value) {
            addCriterion("origin_fee <>", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeGreaterThan(Integer value) {
            addCriterion("origin_fee >", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeGreaterThanOrEqualTo(Integer value) {
            addCriterion("origin_fee >=", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeLessThan(Integer value) {
            addCriterion("origin_fee <", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeLessThanOrEqualTo(Integer value) {
            addCriterion("origin_fee <=", value, "originFee");
            return this;
        }

        public Criteria andOriginFeeIn(List<Integer> values) {
            addCriterion("origin_fee in", values, "originFee");
            return this;
        }

        public Criteria andOriginFeeNotIn(List<Integer> values) {
            addCriterion("origin_fee not in", values, "originFee");
            return this;
        }

        public Criteria andOriginFeeBetween(Integer value1, Integer value2) {
            addCriterion("origin_fee between", value1, value2, "originFee");
            return this;
        }

        public Criteria andOriginFeeNotBetween(Integer value1, Integer value2) {
            addCriterion("origin_fee not between", value1, value2, "originFee");
            return this;
        }

        public Criteria andOrderCtimeIsNull() {
            addCriterion("order_ctime is null");
            return this;
        }

        public Criteria andOrderCtimeIsNotNull() {
            addCriterion("order_ctime is not null");
            return this;
        }

        public Criteria andOrderCtimeEqualTo(Long value) {
            addCriterion("order_ctime =", value, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeNotEqualTo(Long value) {
            addCriterion("order_ctime <>", value, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeGreaterThan(Long value) {
            addCriterion("order_ctime >", value, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeGreaterThanOrEqualTo(Long value) {
            addCriterion("order_ctime >=", value, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeLessThan(Long value) {
            addCriterion("order_ctime <", value, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeLessThanOrEqualTo(Long value) {
            addCriterion("order_ctime <=", value, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeIn(List<Long> values) {
            addCriterion("order_ctime in", values, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeNotIn(List<Long> values) {
            addCriterion("order_ctime not in", values, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeBetween(Long value1, Long value2) {
            addCriterion("order_ctime between", value1, value2, "orderCtime");
            return this;
        }

        public Criteria andOrderCtimeNotBetween(Long value1, Long value2) {
            addCriterion("order_ctime not between", value1, value2, "orderCtime");
            return this;
        }

        public Criteria andLeastAmountIsNull() {
            addCriterion("least_amount is null");
            return this;
        }

        public Criteria andLeastAmountIsNotNull() {
            addCriterion("least_amount is not null");
            return this;
        }

        public Criteria andLeastAmountEqualTo(Integer value) {
            addCriterion("least_amount =", value, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountNotEqualTo(Integer value) {
            addCriterion("least_amount <>", value, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountGreaterThan(Integer value) {
            addCriterion("least_amount >", value, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountGreaterThanOrEqualTo(Integer value) {
            addCriterion("least_amount >=", value, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountLessThan(Integer value) {
            addCriterion("least_amount <", value, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountLessThanOrEqualTo(Integer value) {
            addCriterion("least_amount <=", value, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountIn(List<Integer> values) {
            addCriterion("least_amount in", values, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountNotIn(List<Integer> values) {
            addCriterion("least_amount not in", values, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountBetween(Integer value1, Integer value2) {
            addCriterion("least_amount between", value1, value2, "leastAmount");
            return this;
        }

        public Criteria andLeastAmountNotBetween(Integer value1, Integer value2) {
            addCriterion("least_amount not between", value1, value2, "leastAmount");
            return this;
        }
    }
}
