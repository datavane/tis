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
public class PayinfoCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<PayinfoColEnum> cols = Sets.newHashSet();

    public PayinfoCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected PayinfoCriteria(PayinfoCriteria example) {
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

    public final List<PayinfoColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(PayinfoColEnum... colName) {
        for (PayinfoColEnum c : colName) {
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

        public Criteria andKindpaynameIsNull() {
            addCriterion("kindpayname is null");
            return this;
        }

        public Criteria andKindpaynameIsNotNull() {
            addCriterion("kindpayname is not null");
            return this;
        }

        public Criteria andKindpaynameEqualTo(String value) {
            addCriterion("kindpayname =", value, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameNotEqualTo(String value) {
            addCriterion("kindpayname <>", value, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameGreaterThan(String value) {
            addCriterion("kindpayname >", value, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameGreaterThanOrEqualTo(String value) {
            addCriterion("kindpayname >=", value, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameLessThan(String value) {
            addCriterion("kindpayname <", value, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameLessThanOrEqualTo(String value) {
            addCriterion("kindpayname <=", value, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameLike(String value) {
            addCriterion("kindpayname like", value, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameNotLike(String value) {
            addCriterion("kindpayname not like", value, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameIn(List<String> values) {
            addCriterion("kindpayname in", values, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameNotIn(List<String> values) {
            addCriterion("kindpayname not in", values, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameBetween(String value1, String value2) {
            addCriterion("kindpayname between", value1, value2, "kindpayname");
            return this;
        }

        public Criteria andKindpaynameNotBetween(String value1, String value2) {
            addCriterion("kindpayname not between", value1, value2, "kindpayname");
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

        public Criteria andOperatorIsNull() {
            addCriterion("operator is null");
            return this;
        }

        public Criteria andOperatorIsNotNull() {
            addCriterion("operator is not null");
            return this;
        }

        public Criteria andOperatorEqualTo(String value) {
            addCriterion("operator =", value, "operator");
            return this;
        }

        public Criteria andOperatorNotEqualTo(String value) {
            addCriterion("operator <>", value, "operator");
            return this;
        }

        public Criteria andOperatorGreaterThan(String value) {
            addCriterion("operator >", value, "operator");
            return this;
        }

        public Criteria andOperatorGreaterThanOrEqualTo(String value) {
            addCriterion("operator >=", value, "operator");
            return this;
        }

        public Criteria andOperatorLessThan(String value) {
            addCriterion("operator <", value, "operator");
            return this;
        }

        public Criteria andOperatorLessThanOrEqualTo(String value) {
            addCriterion("operator <=", value, "operator");
            return this;
        }

        public Criteria andOperatorLike(String value) {
            addCriterion("operator like", value, "operator");
            return this;
        }

        public Criteria andOperatorNotLike(String value) {
            addCriterion("operator not like", value, "operator");
            return this;
        }

        public Criteria andOperatorIn(List<String> values) {
            addCriterion("operator in", values, "operator");
            return this;
        }

        public Criteria andOperatorNotIn(List<String> values) {
            addCriterion("operator not in", values, "operator");
            return this;
        }

        public Criteria andOperatorBetween(String value1, String value2) {
            addCriterion("operator between", value1, value2, "operator");
            return this;
        }

        public Criteria andOperatorNotBetween(String value1, String value2) {
            addCriterion("operator not between", value1, value2, "operator");
            return this;
        }

        public Criteria andOperatorNameIsNull() {
            addCriterion("operator_name is null");
            return this;
        }

        public Criteria andOperatorNameIsNotNull() {
            addCriterion("operator_name is not null");
            return this;
        }

        public Criteria andOperatorNameEqualTo(String value) {
            addCriterion("operator_name =", value, "operatorName");
            return this;
        }

        public Criteria andOperatorNameNotEqualTo(String value) {
            addCriterion("operator_name <>", value, "operatorName");
            return this;
        }

        public Criteria andOperatorNameGreaterThan(String value) {
            addCriterion("operator_name >", value, "operatorName");
            return this;
        }

        public Criteria andOperatorNameGreaterThanOrEqualTo(String value) {
            addCriterion("operator_name >=", value, "operatorName");
            return this;
        }

        public Criteria andOperatorNameLessThan(String value) {
            addCriterion("operator_name <", value, "operatorName");
            return this;
        }

        public Criteria andOperatorNameLessThanOrEqualTo(String value) {
            addCriterion("operator_name <=", value, "operatorName");
            return this;
        }

        public Criteria andOperatorNameLike(String value) {
            addCriterion("operator_name like", value, "operatorName");
            return this;
        }

        public Criteria andOperatorNameNotLike(String value) {
            addCriterion("operator_name not like", value, "operatorName");
            return this;
        }

        public Criteria andOperatorNameIn(List<String> values) {
            addCriterion("operator_name in", values, "operatorName");
            return this;
        }

        public Criteria andOperatorNameNotIn(List<String> values) {
            addCriterion("operator_name not in", values, "operatorName");
            return this;
        }

        public Criteria andOperatorNameBetween(String value1, String value2) {
            addCriterion("operator_name between", value1, value2, "operatorName");
            return this;
        }

        public Criteria andOperatorNameNotBetween(String value1, String value2) {
            addCriterion("operator_name not between", value1, value2, "operatorName");
            return this;
        }

        public Criteria andPayTimeIsNull() {
            addCriterion("pay_time is null");
            return this;
        }

        public Criteria andPayTimeIsNotNull() {
            addCriterion("pay_time is not null");
            return this;
        }

        public Criteria andPayTimeEqualTo(Long value) {
            addCriterion("pay_time =", value, "payTime");
            return this;
        }

        public Criteria andPayTimeNotEqualTo(Long value) {
            addCriterion("pay_time <>", value, "payTime");
            return this;
        }

        public Criteria andPayTimeGreaterThan(Long value) {
            addCriterion("pay_time >", value, "payTime");
            return this;
        }

        public Criteria andPayTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("pay_time >=", value, "payTime");
            return this;
        }

        public Criteria andPayTimeLessThan(Long value) {
            addCriterion("pay_time <", value, "payTime");
            return this;
        }

        public Criteria andPayTimeLessThanOrEqualTo(Long value) {
            addCriterion("pay_time <=", value, "payTime");
            return this;
        }

        public Criteria andPayTimeIn(List<Long> values) {
            addCriterion("pay_time in", values, "payTime");
            return this;
        }

        public Criteria andPayTimeNotIn(List<Long> values) {
            addCriterion("pay_time not in", values, "payTime");
            return this;
        }

        public Criteria andPayTimeBetween(Long value1, Long value2) {
            addCriterion("pay_time between", value1, value2, "payTime");
            return this;
        }

        public Criteria andPayTimeNotBetween(Long value1, Long value2) {
            addCriterion("pay_time not between", value1, value2, "payTime");
            return this;
        }

        public Criteria andPayIsNull() {
            addCriterion("pay is null");
            return this;
        }

        public Criteria andPayIsNotNull() {
            addCriterion("pay is not null");
            return this;
        }

        public Criteria andPayEqualTo(BigDecimal value) {
            addCriterion("pay =", value, "pay");
            return this;
        }

        public Criteria andPayNotEqualTo(BigDecimal value) {
            addCriterion("pay <>", value, "pay");
            return this;
        }

        public Criteria andPayGreaterThan(BigDecimal value) {
            addCriterion("pay >", value, "pay");
            return this;
        }

        public Criteria andPayGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("pay >=", value, "pay");
            return this;
        }

        public Criteria andPayLessThan(BigDecimal value) {
            addCriterion("pay <", value, "pay");
            return this;
        }

        public Criteria andPayLessThanOrEqualTo(BigDecimal value) {
            addCriterion("pay <=", value, "pay");
            return this;
        }

        public Criteria andPayIn(List<BigDecimal> values) {
            addCriterion("pay in", values, "pay");
            return this;
        }

        public Criteria andPayNotIn(List<BigDecimal> values) {
            addCriterion("pay not in", values, "pay");
            return this;
        }

        public Criteria andPayBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pay between", value1, value2, "pay");
            return this;
        }

        public Criteria andPayNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pay not between", value1, value2, "pay");
            return this;
        }

        public Criteria andChargeIsNull() {
            addCriterion("charge is null");
            return this;
        }

        public Criteria andChargeIsNotNull() {
            addCriterion("charge is not null");
            return this;
        }

        public Criteria andChargeEqualTo(BigDecimal value) {
            addCriterion("charge =", value, "charge");
            return this;
        }

        public Criteria andChargeNotEqualTo(BigDecimal value) {
            addCriterion("charge <>", value, "charge");
            return this;
        }

        public Criteria andChargeGreaterThan(BigDecimal value) {
            addCriterion("charge >", value, "charge");
            return this;
        }

        public Criteria andChargeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("charge >=", value, "charge");
            return this;
        }

        public Criteria andChargeLessThan(BigDecimal value) {
            addCriterion("charge <", value, "charge");
            return this;
        }

        public Criteria andChargeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("charge <=", value, "charge");
            return this;
        }

        public Criteria andChargeIn(List<BigDecimal> values) {
            addCriterion("charge in", values, "charge");
            return this;
        }

        public Criteria andChargeNotIn(List<BigDecimal> values) {
            addCriterion("charge not in", values, "charge");
            return this;
        }

        public Criteria andChargeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("charge between", value1, value2, "charge");
            return this;
        }

        public Criteria andChargeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("charge not between", value1, value2, "charge");
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

        public Criteria andOpuserIdIsNull() {
            addCriterion("opuser_id is null");
            return this;
        }

        public Criteria andOpuserIdIsNotNull() {
            addCriterion("opuser_id is not null");
            return this;
        }

        public Criteria andOpuserIdEqualTo(String value) {
            addCriterion("opuser_id =", value, "opuserId");
            return this;
        }

        public Criteria andOpuserIdNotEqualTo(String value) {
            addCriterion("opuser_id <>", value, "opuserId");
            return this;
        }

        public Criteria andOpuserIdGreaterThan(String value) {
            addCriterion("opuser_id >", value, "opuserId");
            return this;
        }

        public Criteria andOpuserIdGreaterThanOrEqualTo(String value) {
            addCriterion("opuser_id >=", value, "opuserId");
            return this;
        }

        public Criteria andOpuserIdLessThan(String value) {
            addCriterion("opuser_id <", value, "opuserId");
            return this;
        }

        public Criteria andOpuserIdLessThanOrEqualTo(String value) {
            addCriterion("opuser_id <=", value, "opuserId");
            return this;
        }

        public Criteria andOpuserIdLike(String value) {
            addCriterion("opuser_id like", value, "opuserId");
            return this;
        }

        public Criteria andOpuserIdNotLike(String value) {
            addCriterion("opuser_id not like", value, "opuserId");
            return this;
        }

        public Criteria andOpuserIdIn(List<String> values) {
            addCriterion("opuser_id in", values, "opuserId");
            return this;
        }

        public Criteria andOpuserIdNotIn(List<String> values) {
            addCriterion("opuser_id not in", values, "opuserId");
            return this;
        }

        public Criteria andOpuserIdBetween(String value1, String value2) {
            addCriterion("opuser_id between", value1, value2, "opuserId");
            return this;
        }

        public Criteria andOpuserIdNotBetween(String value1, String value2) {
            addCriterion("opuser_id not between", value1, value2, "opuserId");
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

        public Criteria andOnlineBillIdIsNull() {
            addCriterion("online_bill_id is null");
            return this;
        }

        public Criteria andOnlineBillIdIsNotNull() {
            addCriterion("online_bill_id is not null");
            return this;
        }

        public Criteria andOnlineBillIdEqualTo(String value) {
            addCriterion("online_bill_id =", value, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdNotEqualTo(String value) {
            addCriterion("online_bill_id <>", value, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdGreaterThan(String value) {
            addCriterion("online_bill_id >", value, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdGreaterThanOrEqualTo(String value) {
            addCriterion("online_bill_id >=", value, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdLessThan(String value) {
            addCriterion("online_bill_id <", value, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdLessThanOrEqualTo(String value) {
            addCriterion("online_bill_id <=", value, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdLike(String value) {
            addCriterion("online_bill_id like", value, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdNotLike(String value) {
            addCriterion("online_bill_id not like", value, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdIn(List<String> values) {
            addCriterion("online_bill_id in", values, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdNotIn(List<String> values) {
            addCriterion("online_bill_id not in", values, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdBetween(String value1, String value2) {
            addCriterion("online_bill_id between", value1, value2, "onlineBillId");
            return this;
        }

        public Criteria andOnlineBillIdNotBetween(String value1, String value2) {
            addCriterion("online_bill_id not between", value1, value2, "onlineBillId");
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

        public Criteria andTypeEqualTo(Short value) {
            addCriterion("type =", value, "type");
            return this;
        }

        public Criteria andTypeNotEqualTo(Short value) {
            addCriterion("type <>", value, "type");
            return this;
        }

        public Criteria andTypeGreaterThan(Short value) {
            addCriterion("type >", value, "type");
            return this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(Short value) {
            addCriterion("type >=", value, "type");
            return this;
        }

        public Criteria andTypeLessThan(Short value) {
            addCriterion("type <", value, "type");
            return this;
        }

        public Criteria andTypeLessThanOrEqualTo(Short value) {
            addCriterion("type <=", value, "type");
            return this;
        }

        public Criteria andTypeIn(List<Short> values) {
            addCriterion("type in", values, "type");
            return this;
        }

        public Criteria andTypeNotIn(List<Short> values) {
            addCriterion("type not in", values, "type");
            return this;
        }

        public Criteria andTypeBetween(Short value1, Short value2) {
            addCriterion("type between", value1, value2, "type");
            return this;
        }

        public Criteria andTypeNotBetween(Short value1, Short value2) {
            addCriterion("type not between", value1, value2, "type");
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

        public Criteria andWaitingpayIdIsNull() {
            addCriterion("waitingpay_id is null");
            return this;
        }

        public Criteria andWaitingpayIdIsNotNull() {
            addCriterion("waitingpay_id is not null");
            return this;
        }

        public Criteria andWaitingpayIdEqualTo(String value) {
            addCriterion("waitingpay_id =", value, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdNotEqualTo(String value) {
            addCriterion("waitingpay_id <>", value, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdGreaterThan(String value) {
            addCriterion("waitingpay_id >", value, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdGreaterThanOrEqualTo(String value) {
            addCriterion("waitingpay_id >=", value, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdLessThan(String value) {
            addCriterion("waitingpay_id <", value, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdLessThanOrEqualTo(String value) {
            addCriterion("waitingpay_id <=", value, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdLike(String value) {
            addCriterion("waitingpay_id like", value, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdNotLike(String value) {
            addCriterion("waitingpay_id not like", value, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdIn(List<String> values) {
            addCriterion("waitingpay_id in", values, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdNotIn(List<String> values) {
            addCriterion("waitingpay_id not in", values, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdBetween(String value1, String value2) {
            addCriterion("waitingpay_id between", value1, value2, "waitingpayId");
            return this;
        }

        public Criteria andWaitingpayIdNotBetween(String value1, String value2) {
            addCriterion("waitingpay_id not between", value1, value2, "waitingpayId");
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

        public Criteria andIsDealedIsNull() {
            addCriterion("is_dealed is null");
            return this;
        }

        public Criteria andIsDealedIsNotNull() {
            addCriterion("is_dealed is not null");
            return this;
        }

        public Criteria andIsDealedEqualTo(Byte value) {
            addCriterion("is_dealed =", value, "isDealed");
            return this;
        }

        public Criteria andIsDealedNotEqualTo(Byte value) {
            addCriterion("is_dealed <>", value, "isDealed");
            return this;
        }

        public Criteria andIsDealedGreaterThan(Byte value) {
            addCriterion("is_dealed >", value, "isDealed");
            return this;
        }

        public Criteria andIsDealedGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_dealed >=", value, "isDealed");
            return this;
        }

        public Criteria andIsDealedLessThan(Byte value) {
            addCriterion("is_dealed <", value, "isDealed");
            return this;
        }

        public Criteria andIsDealedLessThanOrEqualTo(Byte value) {
            addCriterion("is_dealed <=", value, "isDealed");
            return this;
        }

        public Criteria andIsDealedIn(List<Byte> values) {
            addCriterion("is_dealed in", values, "isDealed");
            return this;
        }

        public Criteria andIsDealedNotIn(List<Byte> values) {
            addCriterion("is_dealed not in", values, "isDealed");
            return this;
        }

        public Criteria andIsDealedBetween(Byte value1, Byte value2) {
            addCriterion("is_dealed between", value1, value2, "isDealed");
            return this;
        }

        public Criteria andIsDealedNotBetween(Byte value1, Byte value2) {
            addCriterion("is_dealed not between", value1, value2, "isDealed");
            return this;
        }

        public Criteria andTypeNameIsNull() {
            addCriterion("type_name is null");
            return this;
        }

        public Criteria andTypeNameIsNotNull() {
            addCriterion("type_name is not null");
            return this;
        }

        public Criteria andTypeNameEqualTo(String value) {
            addCriterion("type_name =", value, "typeName");
            return this;
        }

        public Criteria andTypeNameNotEqualTo(String value) {
            addCriterion("type_name <>", value, "typeName");
            return this;
        }

        public Criteria andTypeNameGreaterThan(String value) {
            addCriterion("type_name >", value, "typeName");
            return this;
        }

        public Criteria andTypeNameGreaterThanOrEqualTo(String value) {
            addCriterion("type_name >=", value, "typeName");
            return this;
        }

        public Criteria andTypeNameLessThan(String value) {
            addCriterion("type_name <", value, "typeName");
            return this;
        }

        public Criteria andTypeNameLessThanOrEqualTo(String value) {
            addCriterion("type_name <=", value, "typeName");
            return this;
        }

        public Criteria andTypeNameLike(String value) {
            addCriterion("type_name like", value, "typeName");
            return this;
        }

        public Criteria andTypeNameNotLike(String value) {
            addCriterion("type_name not like", value, "typeName");
            return this;
        }

        public Criteria andTypeNameIn(List<String> values) {
            addCriterion("type_name in", values, "typeName");
            return this;
        }

        public Criteria andTypeNameNotIn(List<String> values) {
            addCriterion("type_name not in", values, "typeName");
            return this;
        }

        public Criteria andTypeNameBetween(String value1, String value2) {
            addCriterion("type_name between", value1, value2, "typeName");
            return this;
        }

        public Criteria andTypeNameNotBetween(String value1, String value2) {
            addCriterion("type_name not between", value1, value2, "typeName");
            return this;
        }

        public Criteria andCouponFeeIsNull() {
            addCriterion("coupon_fee is null");
            return this;
        }

        public Criteria andCouponFeeIsNotNull() {
            addCriterion("coupon_fee is not null");
            return this;
        }

        public Criteria andCouponFeeEqualTo(BigDecimal value) {
            addCriterion("coupon_fee =", value, "couponFee");
            return this;
        }

        public Criteria andCouponFeeNotEqualTo(BigDecimal value) {
            addCriterion("coupon_fee <>", value, "couponFee");
            return this;
        }

        public Criteria andCouponFeeGreaterThan(BigDecimal value) {
            addCriterion("coupon_fee >", value, "couponFee");
            return this;
        }

        public Criteria andCouponFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("coupon_fee >=", value, "couponFee");
            return this;
        }

        public Criteria andCouponFeeLessThan(BigDecimal value) {
            addCriterion("coupon_fee <", value, "couponFee");
            return this;
        }

        public Criteria andCouponFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("coupon_fee <=", value, "couponFee");
            return this;
        }

        public Criteria andCouponFeeIn(List<BigDecimal> values) {
            addCriterion("coupon_fee in", values, "couponFee");
            return this;
        }

        public Criteria andCouponFeeNotIn(List<BigDecimal> values) {
            addCriterion("coupon_fee not in", values, "couponFee");
            return this;
        }

        public Criteria andCouponFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("coupon_fee between", value1, value2, "couponFee");
            return this;
        }

        public Criteria andCouponFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("coupon_fee not between", value1, value2, "couponFee");
            return this;
        }

        public Criteria andCouponCostIsNull() {
            addCriterion("coupon_cost is null");
            return this;
        }

        public Criteria andCouponCostIsNotNull() {
            addCriterion("coupon_cost is not null");
            return this;
        }

        public Criteria andCouponCostEqualTo(BigDecimal value) {
            addCriterion("coupon_cost =", value, "couponCost");
            return this;
        }

        public Criteria andCouponCostNotEqualTo(BigDecimal value) {
            addCriterion("coupon_cost <>", value, "couponCost");
            return this;
        }

        public Criteria andCouponCostGreaterThan(BigDecimal value) {
            addCriterion("coupon_cost >", value, "couponCost");
            return this;
        }

        public Criteria andCouponCostGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("coupon_cost >=", value, "couponCost");
            return this;
        }

        public Criteria andCouponCostLessThan(BigDecimal value) {
            addCriterion("coupon_cost <", value, "couponCost");
            return this;
        }

        public Criteria andCouponCostLessThanOrEqualTo(BigDecimal value) {
            addCriterion("coupon_cost <=", value, "couponCost");
            return this;
        }

        public Criteria andCouponCostIn(List<BigDecimal> values) {
            addCriterion("coupon_cost in", values, "couponCost");
            return this;
        }

        public Criteria andCouponCostNotIn(List<BigDecimal> values) {
            addCriterion("coupon_cost not in", values, "couponCost");
            return this;
        }

        public Criteria andCouponCostBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("coupon_cost between", value1, value2, "couponCost");
            return this;
        }

        public Criteria andCouponCostNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("coupon_cost not between", value1, value2, "couponCost");
            return this;
        }

        public Criteria andCouponNumIsNull() {
            addCriterion("coupon_num is null");
            return this;
        }

        public Criteria andCouponNumIsNotNull() {
            addCriterion("coupon_num is not null");
            return this;
        }

        public Criteria andCouponNumEqualTo(Short value) {
            addCriterion("coupon_num =", value, "couponNum");
            return this;
        }

        public Criteria andCouponNumNotEqualTo(Short value) {
            addCriterion("coupon_num <>", value, "couponNum");
            return this;
        }

        public Criteria andCouponNumGreaterThan(Short value) {
            addCriterion("coupon_num >", value, "couponNum");
            return this;
        }

        public Criteria andCouponNumGreaterThanOrEqualTo(Short value) {
            addCriterion("coupon_num >=", value, "couponNum");
            return this;
        }

        public Criteria andCouponNumLessThan(Short value) {
            addCriterion("coupon_num <", value, "couponNum");
            return this;
        }

        public Criteria andCouponNumLessThanOrEqualTo(Short value) {
            addCriterion("coupon_num <=", value, "couponNum");
            return this;
        }

        public Criteria andCouponNumIn(List<Short> values) {
            addCriterion("coupon_num in", values, "couponNum");
            return this;
        }

        public Criteria andCouponNumNotIn(List<Short> values) {
            addCriterion("coupon_num not in", values, "couponNum");
            return this;
        }

        public Criteria andCouponNumBetween(Short value1, Short value2) {
            addCriterion("coupon_num between", value1, value2, "couponNum");
            return this;
        }

        public Criteria andCouponNumNotBetween(Short value1, Short value2) {
            addCriterion("coupon_num not between", value1, value2, "couponNum");
            return this;
        }

        public Criteria andPayFromIsNull() {
            addCriterion("pay_from is null");
            return this;
        }

        public Criteria andPayFromIsNotNull() {
            addCriterion("pay_from is not null");
            return this;
        }

        public Criteria andPayFromEqualTo(Short value) {
            addCriterion("pay_from =", value, "payFrom");
            return this;
        }

        public Criteria andPayFromNotEqualTo(Short value) {
            addCriterion("pay_from <>", value, "payFrom");
            return this;
        }

        public Criteria andPayFromGreaterThan(Short value) {
            addCriterion("pay_from >", value, "payFrom");
            return this;
        }

        public Criteria andPayFromGreaterThanOrEqualTo(Short value) {
            addCriterion("pay_from >=", value, "payFrom");
            return this;
        }

        public Criteria andPayFromLessThan(Short value) {
            addCriterion("pay_from <", value, "payFrom");
            return this;
        }

        public Criteria andPayFromLessThanOrEqualTo(Short value) {
            addCriterion("pay_from <=", value, "payFrom");
            return this;
        }

        public Criteria andPayFromIn(List<Short> values) {
            addCriterion("pay_from in", values, "payFrom");
            return this;
        }

        public Criteria andPayFromNotIn(List<Short> values) {
            addCriterion("pay_from not in", values, "payFrom");
            return this;
        }

        public Criteria andPayFromBetween(Short value1, Short value2) {
            addCriterion("pay_from between", value1, value2, "payFrom");
            return this;
        }

        public Criteria andPayFromNotBetween(Short value1, Short value2) {
            addCriterion("pay_from not between", value1, value2, "payFrom");
            return this;
        }

        public Criteria andParentEntityIdIsNull() {
            addCriterion("parent_entity_id is null");
            return this;
        }

        public Criteria andParentEntityIdIsNotNull() {
            addCriterion("parent_entity_id is not null");
            return this;
        }

        public Criteria andParentEntityIdEqualTo(String value) {
            addCriterion("parent_entity_id =", value, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdNotEqualTo(String value) {
            addCriterion("parent_entity_id <>", value, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdGreaterThan(String value) {
            addCriterion("parent_entity_id >", value, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdGreaterThanOrEqualTo(String value) {
            addCriterion("parent_entity_id >=", value, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdLessThan(String value) {
            addCriterion("parent_entity_id <", value, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdLessThanOrEqualTo(String value) {
            addCriterion("parent_entity_id <=", value, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdLike(String value) {
            addCriterion("parent_entity_id like", value, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdNotLike(String value) {
            addCriterion("parent_entity_id not like", value, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdIn(List<String> values) {
            addCriterion("parent_entity_id in", values, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdNotIn(List<String> values) {
            addCriterion("parent_entity_id not in", values, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdBetween(String value1, String value2) {
            addCriterion("parent_entity_id between", value1, value2, "parentEntityId");
            return this;
        }

        public Criteria andParentEntityIdNotBetween(String value1, String value2) {
            addCriterion("parent_entity_id not between", value1, value2, "parentEntityId");
            return this;
        }

        public Criteria andParentIdIsNull() {
            addCriterion("parent_id is null");
            return this;
        }

        public Criteria andParentIdIsNotNull() {
            addCriterion("parent_id is not null");
            return this;
        }

        public Criteria andParentIdEqualTo(String value) {
            addCriterion("parent_id =", value, "parentId");
            return this;
        }

        public Criteria andParentIdNotEqualTo(String value) {
            addCriterion("parent_id <>", value, "parentId");
            return this;
        }

        public Criteria andParentIdGreaterThan(String value) {
            addCriterion("parent_id >", value, "parentId");
            return this;
        }

        public Criteria andParentIdGreaterThanOrEqualTo(String value) {
            addCriterion("parent_id >=", value, "parentId");
            return this;
        }

        public Criteria andParentIdLessThan(String value) {
            addCriterion("parent_id <", value, "parentId");
            return this;
        }

        public Criteria andParentIdLessThanOrEqualTo(String value) {
            addCriterion("parent_id <=", value, "parentId");
            return this;
        }

        public Criteria andParentIdLike(String value) {
            addCriterion("parent_id like", value, "parentId");
            return this;
        }

        public Criteria andParentIdNotLike(String value) {
            addCriterion("parent_id not like", value, "parentId");
            return this;
        }

        public Criteria andParentIdIn(List<String> values) {
            addCriterion("parent_id in", values, "parentId");
            return this;
        }

        public Criteria andParentIdNotIn(List<String> values) {
            addCriterion("parent_id not in", values, "parentId");
            return this;
        }

        public Criteria andParentIdBetween(String value1, String value2) {
            addCriterion("parent_id between", value1, value2, "parentId");
            return this;
        }

        public Criteria andParentIdNotBetween(String value1, String value2) {
            addCriterion("parent_id not between", value1, value2, "parentId");
            return this;
        }

        public Criteria andParentCodeIsNull() {
            addCriterion("parent_code is null");
            return this;
        }

        public Criteria andParentCodeIsNotNull() {
            addCriterion("parent_code is not null");
            return this;
        }

        public Criteria andParentCodeEqualTo(String value) {
            addCriterion("parent_code =", value, "parentCode");
            return this;
        }

        public Criteria andParentCodeNotEqualTo(String value) {
            addCriterion("parent_code <>", value, "parentCode");
            return this;
        }

        public Criteria andParentCodeGreaterThan(String value) {
            addCriterion("parent_code >", value, "parentCode");
            return this;
        }

        public Criteria andParentCodeGreaterThanOrEqualTo(String value) {
            addCriterion("parent_code >=", value, "parentCode");
            return this;
        }

        public Criteria andParentCodeLessThan(String value) {
            addCriterion("parent_code <", value, "parentCode");
            return this;
        }

        public Criteria andParentCodeLessThanOrEqualTo(String value) {
            addCriterion("parent_code <=", value, "parentCode");
            return this;
        }

        public Criteria andParentCodeLike(String value) {
            addCriterion("parent_code like", value, "parentCode");
            return this;
        }

        public Criteria andParentCodeNotLike(String value) {
            addCriterion("parent_code not like", value, "parentCode");
            return this;
        }

        public Criteria andParentCodeIn(List<String> values) {
            addCriterion("parent_code in", values, "parentCode");
            return this;
        }

        public Criteria andParentCodeNotIn(List<String> values) {
            addCriterion("parent_code not in", values, "parentCode");
            return this;
        }

        public Criteria andParentCodeBetween(String value1, String value2) {
            addCriterion("parent_code between", value1, value2, "parentCode");
            return this;
        }

        public Criteria andParentCodeNotBetween(String value1, String value2) {
            addCriterion("parent_code not between", value1, value2, "parentCode");
            return this;
        }

        public Criteria andKindCardIdIsNull() {
            addCriterion("kind_card_id is null");
            return this;
        }

        public Criteria andKindCardIdIsNotNull() {
            addCriterion("kind_card_id is not null");
            return this;
        }

        public Criteria andKindCardIdEqualTo(String value) {
            addCriterion("kind_card_id =", value, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdNotEqualTo(String value) {
            addCriterion("kind_card_id <>", value, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdGreaterThan(String value) {
            addCriterion("kind_card_id >", value, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdGreaterThanOrEqualTo(String value) {
            addCriterion("kind_card_id >=", value, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdLessThan(String value) {
            addCriterion("kind_card_id <", value, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdLessThanOrEqualTo(String value) {
            addCriterion("kind_card_id <=", value, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdLike(String value) {
            addCriterion("kind_card_id like", value, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdNotLike(String value) {
            addCriterion("kind_card_id not like", value, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdIn(List<String> values) {
            addCriterion("kind_card_id in", values, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdNotIn(List<String> values) {
            addCriterion("kind_card_id not in", values, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdBetween(String value1, String value2) {
            addCriterion("kind_card_id between", value1, value2, "kindCardId");
            return this;
        }

        public Criteria andKindCardIdNotBetween(String value1, String value2) {
            addCriterion("kind_card_id not between", value1, value2, "kindCardId");
            return this;
        }
    }
}
