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
package com.qlangtech.tis.realtime.test.member.pojo;

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
public class CardCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<CardColEnum> cols = Sets.newHashSet();

    public CardCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected CardCriteria(CardCriteria example) {
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

    public final List<CardColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(CardColEnum... colName) {
        for (CardColEnum c : colName) {
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

        public Criteria andCustomerIdIsNull() {
            addCriterion("customer_id is null");
            return this;
        }

        public Criteria andCustomerIdIsNotNull() {
            addCriterion("customer_id is not null");
            return this;
        }

        public Criteria andCustomerIdEqualTo(String value) {
            addCriterion("customer_id =", value, "customerId");
            return this;
        }

        public Criteria andCustomerIdNotEqualTo(String value) {
            addCriterion("customer_id <>", value, "customerId");
            return this;
        }

        public Criteria andCustomerIdGreaterThan(String value) {
            addCriterion("customer_id >", value, "customerId");
            return this;
        }

        public Criteria andCustomerIdGreaterThanOrEqualTo(String value) {
            addCriterion("customer_id >=", value, "customerId");
            return this;
        }

        public Criteria andCustomerIdLessThan(String value) {
            addCriterion("customer_id <", value, "customerId");
            return this;
        }

        public Criteria andCustomerIdLessThanOrEqualTo(String value) {
            addCriterion("customer_id <=", value, "customerId");
            return this;
        }

        public Criteria andCustomerIdLike(String value) {
            addCriterion("customer_id like", value, "customerId");
            return this;
        }

        public Criteria andCustomerIdNotLike(String value) {
            addCriterion("customer_id not like", value, "customerId");
            return this;
        }

        public Criteria andCustomerIdIn(List<String> values) {
            addCriterion("customer_id in", values, "customerId");
            return this;
        }

        public Criteria andCustomerIdNotIn(List<String> values) {
            addCriterion("customer_id not in", values, "customerId");
            return this;
        }

        public Criteria andCustomerIdBetween(String value1, String value2) {
            addCriterion("customer_id between", value1, value2, "customerId");
            return this;
        }

        public Criteria andCustomerIdNotBetween(String value1, String value2) {
            addCriterion("customer_id not between", value1, value2, "customerId");
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

        public Criteria andPwdIsNull() {
            addCriterion("pwd is null");
            return this;
        }

        public Criteria andPwdIsNotNull() {
            addCriterion("pwd is not null");
            return this;
        }

        public Criteria andPwdEqualTo(String value) {
            addCriterion("pwd =", value, "pwd");
            return this;
        }

        public Criteria andPwdNotEqualTo(String value) {
            addCriterion("pwd <>", value, "pwd");
            return this;
        }

        public Criteria andPwdGreaterThan(String value) {
            addCriterion("pwd >", value, "pwd");
            return this;
        }

        public Criteria andPwdGreaterThanOrEqualTo(String value) {
            addCriterion("pwd >=", value, "pwd");
            return this;
        }

        public Criteria andPwdLessThan(String value) {
            addCriterion("pwd <", value, "pwd");
            return this;
        }

        public Criteria andPwdLessThanOrEqualTo(String value) {
            addCriterion("pwd <=", value, "pwd");
            return this;
        }

        public Criteria andPwdLike(String value) {
            addCriterion("pwd like", value, "pwd");
            return this;
        }

        public Criteria andPwdNotLike(String value) {
            addCriterion("pwd not like", value, "pwd");
            return this;
        }

        public Criteria andPwdIn(List<String> values) {
            addCriterion("pwd in", values, "pwd");
            return this;
        }

        public Criteria andPwdNotIn(List<String> values) {
            addCriterion("pwd not in", values, "pwd");
            return this;
        }

        public Criteria andPwdBetween(String value1, String value2) {
            addCriterion("pwd between", value1, value2, "pwd");
            return this;
        }

        public Criteria andPwdNotBetween(String value1, String value2) {
            addCriterion("pwd not between", value1, value2, "pwd");
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

        public Criteria andActiveDateIsNull() {
            addCriterion("active_date is null");
            return this;
        }

        public Criteria andActiveDateIsNotNull() {
            addCriterion("active_date is not null");
            return this;
        }

        public Criteria andActiveDateEqualTo(Long value) {
            addCriterion("active_date =", value, "activeDate");
            return this;
        }

        public Criteria andActiveDateNotEqualTo(Long value) {
            addCriterion("active_date <>", value, "activeDate");
            return this;
        }

        public Criteria andActiveDateGreaterThan(Long value) {
            addCriterion("active_date >", value, "activeDate");
            return this;
        }

        public Criteria andActiveDateGreaterThanOrEqualTo(Long value) {
            addCriterion("active_date >=", value, "activeDate");
            return this;
        }

        public Criteria andActiveDateLessThan(Long value) {
            addCriterion("active_date <", value, "activeDate");
            return this;
        }

        public Criteria andActiveDateLessThanOrEqualTo(Long value) {
            addCriterion("active_date <=", value, "activeDate");
            return this;
        }

        public Criteria andActiveDateIn(List<Long> values) {
            addCriterion("active_date in", values, "activeDate");
            return this;
        }

        public Criteria andActiveDateNotIn(List<Long> values) {
            addCriterion("active_date not in", values, "activeDate");
            return this;
        }

        public Criteria andActiveDateBetween(Long value1, Long value2) {
            addCriterion("active_date between", value1, value2, "activeDate");
            return this;
        }

        public Criteria andActiveDateNotBetween(Long value1, Long value2) {
            addCriterion("active_date not between", value1, value2, "activeDate");
            return this;
        }

        public Criteria andPreFeeIsNull() {
            addCriterion("pre_fee is null");
            return this;
        }

        public Criteria andPreFeeIsNotNull() {
            addCriterion("pre_fee is not null");
            return this;
        }

        public Criteria andPreFeeEqualTo(BigDecimal value) {
            addCriterion("pre_fee =", value, "preFee");
            return this;
        }

        public Criteria andPreFeeNotEqualTo(BigDecimal value) {
            addCriterion("pre_fee <>", value, "preFee");
            return this;
        }

        public Criteria andPreFeeGreaterThan(BigDecimal value) {
            addCriterion("pre_fee >", value, "preFee");
            return this;
        }

        public Criteria andPreFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("pre_fee >=", value, "preFee");
            return this;
        }

        public Criteria andPreFeeLessThan(BigDecimal value) {
            addCriterion("pre_fee <", value, "preFee");
            return this;
        }

        public Criteria andPreFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("pre_fee <=", value, "preFee");
            return this;
        }

        public Criteria andPreFeeIn(List<BigDecimal> values) {
            addCriterion("pre_fee in", values, "preFee");
            return this;
        }

        public Criteria andPreFeeNotIn(List<BigDecimal> values) {
            addCriterion("pre_fee not in", values, "preFee");
            return this;
        }

        public Criteria andPreFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pre_fee between", value1, value2, "preFee");
            return this;
        }

        public Criteria andPreFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pre_fee not between", value1, value2, "preFee");
            return this;
        }

        public Criteria andBalanceIsNull() {
            addCriterion("balance is null");
            return this;
        }

        public Criteria andBalanceIsNotNull() {
            addCriterion("balance is not null");
            return this;
        }

        public Criteria andBalanceEqualTo(BigDecimal value) {
            addCriterion("balance =", value, "balance");
            return this;
        }

        public Criteria andBalanceNotEqualTo(BigDecimal value) {
            addCriterion("balance <>", value, "balance");
            return this;
        }

        public Criteria andBalanceGreaterThan(BigDecimal value) {
            addCriterion("balance >", value, "balance");
            return this;
        }

        public Criteria andBalanceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("balance >=", value, "balance");
            return this;
        }

        public Criteria andBalanceLessThan(BigDecimal value) {
            addCriterion("balance <", value, "balance");
            return this;
        }

        public Criteria andBalanceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("balance <=", value, "balance");
            return this;
        }

        public Criteria andBalanceIn(List<BigDecimal> values) {
            addCriterion("balance in", values, "balance");
            return this;
        }

        public Criteria andBalanceNotIn(List<BigDecimal> values) {
            addCriterion("balance not in", values, "balance");
            return this;
        }

        public Criteria andBalanceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("balance between", value1, value2, "balance");
            return this;
        }

        public Criteria andBalanceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("balance not between", value1, value2, "balance");
            return this;
        }

        public Criteria andGiftBalanceIsNull() {
            addCriterion("gift_balance is null");
            return this;
        }

        public Criteria andGiftBalanceIsNotNull() {
            addCriterion("gift_balance is not null");
            return this;
        }

        public Criteria andGiftBalanceEqualTo(BigDecimal value) {
            addCriterion("gift_balance =", value, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceNotEqualTo(BigDecimal value) {
            addCriterion("gift_balance <>", value, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceGreaterThan(BigDecimal value) {
            addCriterion("gift_balance >", value, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("gift_balance >=", value, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceLessThan(BigDecimal value) {
            addCriterion("gift_balance <", value, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("gift_balance <=", value, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceIn(List<BigDecimal> values) {
            addCriterion("gift_balance in", values, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceNotIn(List<BigDecimal> values) {
            addCriterion("gift_balance not in", values, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("gift_balance between", value1, value2, "giftBalance");
            return this;
        }

        public Criteria andGiftBalanceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("gift_balance not between", value1, value2, "giftBalance");
            return this;
        }

        public Criteria andRealBalanceIsNull() {
            addCriterion("real_balance is null");
            return this;
        }

        public Criteria andRealBalanceIsNotNull() {
            addCriterion("real_balance is not null");
            return this;
        }

        public Criteria andRealBalanceEqualTo(BigDecimal value) {
            addCriterion("real_balance =", value, "realBalance");
            return this;
        }

        public Criteria andRealBalanceNotEqualTo(BigDecimal value) {
            addCriterion("real_balance <>", value, "realBalance");
            return this;
        }

        public Criteria andRealBalanceGreaterThan(BigDecimal value) {
            addCriterion("real_balance >", value, "realBalance");
            return this;
        }

        public Criteria andRealBalanceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("real_balance >=", value, "realBalance");
            return this;
        }

        public Criteria andRealBalanceLessThan(BigDecimal value) {
            addCriterion("real_balance <", value, "realBalance");
            return this;
        }

        public Criteria andRealBalanceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("real_balance <=", value, "realBalance");
            return this;
        }

        public Criteria andRealBalanceIn(List<BigDecimal> values) {
            addCriterion("real_balance in", values, "realBalance");
            return this;
        }

        public Criteria andRealBalanceNotIn(List<BigDecimal> values) {
            addCriterion("real_balance not in", values, "realBalance");
            return this;
        }

        public Criteria andRealBalanceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("real_balance between", value1, value2, "realBalance");
            return this;
        }

        public Criteria andRealBalanceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("real_balance not between", value1, value2, "realBalance");
            return this;
        }

        public Criteria andDegreeIsNull() {
            addCriterion("degree is null");
            return this;
        }

        public Criteria andDegreeIsNotNull() {
            addCriterion("degree is not null");
            return this;
        }

        public Criteria andDegreeEqualTo(BigDecimal value) {
            addCriterion("degree =", value, "degree");
            return this;
        }

        public Criteria andDegreeNotEqualTo(BigDecimal value) {
            addCriterion("degree <>", value, "degree");
            return this;
        }

        public Criteria andDegreeGreaterThan(BigDecimal value) {
            addCriterion("degree >", value, "degree");
            return this;
        }

        public Criteria andDegreeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("degree >=", value, "degree");
            return this;
        }

        public Criteria andDegreeLessThan(BigDecimal value) {
            addCriterion("degree <", value, "degree");
            return this;
        }

        public Criteria andDegreeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("degree <=", value, "degree");
            return this;
        }

        public Criteria andDegreeIn(List<BigDecimal> values) {
            addCriterion("degree in", values, "degree");
            return this;
        }

        public Criteria andDegreeNotIn(List<BigDecimal> values) {
            addCriterion("degree not in", values, "degree");
            return this;
        }

        public Criteria andDegreeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("degree between", value1, value2, "degree");
            return this;
        }

        public Criteria andDegreeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("degree not between", value1, value2, "degree");
            return this;
        }

        public Criteria andPayAmountIsNull() {
            addCriterion("pay_amount is null");
            return this;
        }

        public Criteria andPayAmountIsNotNull() {
            addCriterion("pay_amount is not null");
            return this;
        }

        public Criteria andPayAmountEqualTo(BigDecimal value) {
            addCriterion("pay_amount =", value, "payAmount");
            return this;
        }

        public Criteria andPayAmountNotEqualTo(BigDecimal value) {
            addCriterion("pay_amount <>", value, "payAmount");
            return this;
        }

        public Criteria andPayAmountGreaterThan(BigDecimal value) {
            addCriterion("pay_amount >", value, "payAmount");
            return this;
        }

        public Criteria andPayAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("pay_amount >=", value, "payAmount");
            return this;
        }

        public Criteria andPayAmountLessThan(BigDecimal value) {
            addCriterion("pay_amount <", value, "payAmount");
            return this;
        }

        public Criteria andPayAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("pay_amount <=", value, "payAmount");
            return this;
        }

        public Criteria andPayAmountIn(List<BigDecimal> values) {
            addCriterion("pay_amount in", values, "payAmount");
            return this;
        }

        public Criteria andPayAmountNotIn(List<BigDecimal> values) {
            addCriterion("pay_amount not in", values, "payAmount");
            return this;
        }

        public Criteria andPayAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pay_amount between", value1, value2, "payAmount");
            return this;
        }

        public Criteria andPayAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("pay_amount not between", value1, value2, "payAmount");
            return this;
        }

        public Criteria andConsumeAmountIsNull() {
            addCriterion("consume_amount is null");
            return this;
        }

        public Criteria andConsumeAmountIsNotNull() {
            addCriterion("consume_amount is not null");
            return this;
        }

        public Criteria andConsumeAmountEqualTo(BigDecimal value) {
            addCriterion("consume_amount =", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountNotEqualTo(BigDecimal value) {
            addCriterion("consume_amount <>", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountGreaterThan(BigDecimal value) {
            addCriterion("consume_amount >", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("consume_amount >=", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountLessThan(BigDecimal value) {
            addCriterion("consume_amount <", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("consume_amount <=", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountIn(List<BigDecimal> values) {
            addCriterion("consume_amount in", values, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountNotIn(List<BigDecimal> values) {
            addCriterion("consume_amount not in", values, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("consume_amount between", value1, value2, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("consume_amount not between", value1, value2, "consumeAmount");
            return this;
        }

        public Criteria andRatioAmountIsNull() {
            addCriterion("ratio_amount is null");
            return this;
        }

        public Criteria andRatioAmountIsNotNull() {
            addCriterion("ratio_amount is not null");
            return this;
        }

        public Criteria andRatioAmountEqualTo(BigDecimal value) {
            addCriterion("ratio_amount =", value, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountNotEqualTo(BigDecimal value) {
            addCriterion("ratio_amount <>", value, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountGreaterThan(BigDecimal value) {
            addCriterion("ratio_amount >", value, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("ratio_amount >=", value, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountLessThan(BigDecimal value) {
            addCriterion("ratio_amount <", value, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("ratio_amount <=", value, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountIn(List<BigDecimal> values) {
            addCriterion("ratio_amount in", values, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountNotIn(List<BigDecimal> values) {
            addCriterion("ratio_amount not in", values, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ratio_amount between", value1, value2, "ratioAmount");
            return this;
        }

        public Criteria andRatioAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ratio_amount not between", value1, value2, "ratioAmount");
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

        public Criteria andGetStatusIsNull() {
            addCriterion("get_status is null");
            return this;
        }

        public Criteria andGetStatusIsNotNull() {
            addCriterion("get_status is not null");
            return this;
        }

        public Criteria andGetStatusEqualTo(Short value) {
            addCriterion("get_status =", value, "getStatus");
            return this;
        }

        public Criteria andGetStatusNotEqualTo(Short value) {
            addCriterion("get_status <>", value, "getStatus");
            return this;
        }

        public Criteria andGetStatusGreaterThan(Short value) {
            addCriterion("get_status >", value, "getStatus");
            return this;
        }

        public Criteria andGetStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("get_status >=", value, "getStatus");
            return this;
        }

        public Criteria andGetStatusLessThan(Short value) {
            addCriterion("get_status <", value, "getStatus");
            return this;
        }

        public Criteria andGetStatusLessThanOrEqualTo(Short value) {
            addCriterion("get_status <=", value, "getStatus");
            return this;
        }

        public Criteria andGetStatusIn(List<Short> values) {
            addCriterion("get_status in", values, "getStatus");
            return this;
        }

        public Criteria andGetStatusNotIn(List<Short> values) {
            addCriterion("get_status not in", values, "getStatus");
            return this;
        }

        public Criteria andGetStatusBetween(Short value1, Short value2) {
            addCriterion("get_status between", value1, value2, "getStatus");
            return this;
        }

        public Criteria andGetStatusNotBetween(Short value1, Short value2) {
            addCriterion("get_status not between", value1, value2, "getStatus");
            return this;
        }

        public Criteria andActiveIdIsNull() {
            addCriterion("active_id is null");
            return this;
        }

        public Criteria andActiveIdIsNotNull() {
            addCriterion("active_id is not null");
            return this;
        }

        public Criteria andActiveIdEqualTo(String value) {
            addCriterion("active_id =", value, "activeId");
            return this;
        }

        public Criteria andActiveIdNotEqualTo(String value) {
            addCriterion("active_id <>", value, "activeId");
            return this;
        }

        public Criteria andActiveIdGreaterThan(String value) {
            addCriterion("active_id >", value, "activeId");
            return this;
        }

        public Criteria andActiveIdGreaterThanOrEqualTo(String value) {
            addCriterion("active_id >=", value, "activeId");
            return this;
        }

        public Criteria andActiveIdLessThan(String value) {
            addCriterion("active_id <", value, "activeId");
            return this;
        }

        public Criteria andActiveIdLessThanOrEqualTo(String value) {
            addCriterion("active_id <=", value, "activeId");
            return this;
        }

        public Criteria andActiveIdLike(String value) {
            addCriterion("active_id like", value, "activeId");
            return this;
        }

        public Criteria andActiveIdNotLike(String value) {
            addCriterion("active_id not like", value, "activeId");
            return this;
        }

        public Criteria andActiveIdIn(List<String> values) {
            addCriterion("active_id in", values, "activeId");
            return this;
        }

        public Criteria andActiveIdNotIn(List<String> values) {
            addCriterion("active_id not in", values, "activeId");
            return this;
        }

        public Criteria andActiveIdBetween(String value1, String value2) {
            addCriterion("active_id between", value1, value2, "activeId");
            return this;
        }

        public Criteria andActiveIdNotBetween(String value1, String value2) {
            addCriterion("active_id not between", value1, value2, "activeId");
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

        public Criteria andIsValidEqualTo(Boolean value) {
            addCriterion("is_valid =", value, "isValid");
            return this;
        }

        public Criteria andIsValidNotEqualTo(Boolean value) {
            addCriterion("is_valid <>", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThan(Boolean value) {
            addCriterion("is_valid >", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_valid >=", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThan(Boolean value) {
            addCriterion("is_valid <", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThanOrEqualTo(Boolean value) {
            addCriterion("is_valid <=", value, "isValid");
            return this;
        }

        public Criteria andIsValidIn(List<Boolean> values) {
            addCriterion("is_valid in", values, "isValid");
            return this;
        }

        public Criteria andIsValidNotIn(List<Boolean> values) {
            addCriterion("is_valid not in", values, "isValid");
            return this;
        }

        public Criteria andIsValidBetween(Boolean value1, Boolean value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return this;
        }

        public Criteria andIsValidNotBetween(Boolean value1, Boolean value2) {
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

        public Criteria andSellerIdIsNull() {
            addCriterion("seller_id is null");
            return this;
        }

        public Criteria andSellerIdIsNotNull() {
            addCriterion("seller_id is not null");
            return this;
        }

        public Criteria andSellerIdEqualTo(String value) {
            addCriterion("seller_id =", value, "sellerId");
            return this;
        }

        public Criteria andSellerIdNotEqualTo(String value) {
            addCriterion("seller_id <>", value, "sellerId");
            return this;
        }

        public Criteria andSellerIdGreaterThan(String value) {
            addCriterion("seller_id >", value, "sellerId");
            return this;
        }

        public Criteria andSellerIdGreaterThanOrEqualTo(String value) {
            addCriterion("seller_id >=", value, "sellerId");
            return this;
        }

        public Criteria andSellerIdLessThan(String value) {
            addCriterion("seller_id <", value, "sellerId");
            return this;
        }

        public Criteria andSellerIdLessThanOrEqualTo(String value) {
            addCriterion("seller_id <=", value, "sellerId");
            return this;
        }

        public Criteria andSellerIdLike(String value) {
            addCriterion("seller_id like", value, "sellerId");
            return this;
        }

        public Criteria andSellerIdNotLike(String value) {
            addCriterion("seller_id not like", value, "sellerId");
            return this;
        }

        public Criteria andSellerIdIn(List<String> values) {
            addCriterion("seller_id in", values, "sellerId");
            return this;
        }

        public Criteria andSellerIdNotIn(List<String> values) {
            addCriterion("seller_id not in", values, "sellerId");
            return this;
        }

        public Criteria andSellerIdBetween(String value1, String value2) {
            addCriterion("seller_id between", value1, value2, "sellerId");
            return this;
        }

        public Criteria andSellerIdNotBetween(String value1, String value2) {
            addCriterion("seller_id not between", value1, value2, "sellerId");
            return this;
        }

        public Criteria andLastConsumeTimeIsNull() {
            addCriterion("last_consume_time is null");
            return this;
        }

        public Criteria andLastConsumeTimeIsNotNull() {
            addCriterion("last_consume_time is not null");
            return this;
        }

        public Criteria andLastConsumeTimeEqualTo(Long value) {
            addCriterion("last_consume_time =", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeNotEqualTo(Long value) {
            addCriterion("last_consume_time <>", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeGreaterThan(Long value) {
            addCriterion("last_consume_time >", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("last_consume_time >=", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeLessThan(Long value) {
            addCriterion("last_consume_time <", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeLessThanOrEqualTo(Long value) {
            addCriterion("last_consume_time <=", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeIn(List<Long> values) {
            addCriterion("last_consume_time in", values, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeNotIn(List<Long> values) {
            addCriterion("last_consume_time not in", values, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeBetween(Long value1, Long value2) {
            addCriterion("last_consume_time between", value1, value2, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeNotBetween(Long value1, Long value2) {
            addCriterion("last_consume_time not between", value1, value2, "lastConsumeTime");
            return this;
        }

        public Criteria andConsumeNumIsNull() {
            addCriterion("consume_num is null");
            return this;
        }

        public Criteria andConsumeNumIsNotNull() {
            addCriterion("consume_num is not null");
            return this;
        }

        public Criteria andConsumeNumEqualTo(Integer value) {
            addCriterion("consume_num =", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumNotEqualTo(Integer value) {
            addCriterion("consume_num <>", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumGreaterThan(Integer value) {
            addCriterion("consume_num >", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("consume_num >=", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumLessThan(Integer value) {
            addCriterion("consume_num <", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumLessThanOrEqualTo(Integer value) {
            addCriterion("consume_num <=", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumIn(List<Integer> values) {
            addCriterion("consume_num in", values, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumNotIn(List<Integer> values) {
            addCriterion("consume_num not in", values, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumBetween(Integer value1, Integer value2) {
            addCriterion("consume_num between", value1, value2, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumNotBetween(Integer value1, Integer value2) {
            addCriterion("consume_num not between", value1, value2, "consumeNum");
            return this;
        }

        public Criteria andExtendFieldsIsNull() {
            addCriterion("extend_fields is null");
            return this;
        }

        public Criteria andExtendFieldsIsNotNull() {
            addCriterion("extend_fields is not null");
            return this;
        }

        public Criteria andExtendFieldsEqualTo(String value) {
            addCriterion("extend_fields =", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsNotEqualTo(String value) {
            addCriterion("extend_fields <>", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsGreaterThan(String value) {
            addCriterion("extend_fields >", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsGreaterThanOrEqualTo(String value) {
            addCriterion("extend_fields >=", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsLessThan(String value) {
            addCriterion("extend_fields <", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsLessThanOrEqualTo(String value) {
            addCriterion("extend_fields <=", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsLike(String value) {
            addCriterion("extend_fields like", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsNotLike(String value) {
            addCriterion("extend_fields not like", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsIn(List<String> values) {
            addCriterion("extend_fields in", values, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsNotIn(List<String> values) {
            addCriterion("extend_fields not in", values, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsBetween(String value1, String value2) {
            addCriterion("extend_fields between", value1, value2, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsNotBetween(String value1, String value2) {
            addCriterion("extend_fields not between", value1, value2, "extendFields");
            return this;
        }

        public Criteria andKindCardTypeIsNull() {
            addCriterion("kind_card_type is null");
            return this;
        }

        public Criteria andKindCardTypeIsNotNull() {
            addCriterion("kind_card_type is not null");
            return this;
        }

        public Criteria andKindCardTypeEqualTo(Boolean value) {
            addCriterion("kind_card_type =", value, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeNotEqualTo(Boolean value) {
            addCriterion("kind_card_type <>", value, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeGreaterThan(Boolean value) {
            addCriterion("kind_card_type >", value, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeGreaterThanOrEqualTo(Boolean value) {
            addCriterion("kind_card_type >=", value, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeLessThan(Boolean value) {
            addCriterion("kind_card_type <", value, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeLessThanOrEqualTo(Boolean value) {
            addCriterion("kind_card_type <=", value, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeIn(List<Boolean> values) {
            addCriterion("kind_card_type in", values, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeNotIn(List<Boolean> values) {
            addCriterion("kind_card_type not in", values, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeBetween(Boolean value1, Boolean value2) {
            addCriterion("kind_card_type between", value1, value2, "kindCardType");
            return this;
        }

        public Criteria andKindCardTypeNotBetween(Boolean value1, Boolean value2) {
            addCriterion("kind_card_type not between", value1, value2, "kindCardType");
            return this;
        }

        public Criteria andGiveBalanceIsNull() {
            addCriterion("give_balance is null");
            return this;
        }

        public Criteria andGiveBalanceIsNotNull() {
            addCriterion("give_balance is not null");
            return this;
        }

        public Criteria andGiveBalanceEqualTo(BigDecimal value) {
            addCriterion("give_balance =", value, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceNotEqualTo(BigDecimal value) {
            addCriterion("give_balance <>", value, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceGreaterThan(BigDecimal value) {
            addCriterion("give_balance >", value, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("give_balance >=", value, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceLessThan(BigDecimal value) {
            addCriterion("give_balance <", value, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("give_balance <=", value, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceIn(List<BigDecimal> values) {
            addCriterion("give_balance in", values, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceNotIn(List<BigDecimal> values) {
            addCriterion("give_balance not in", values, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("give_balance between", value1, value2, "giveBalance");
            return this;
        }

        public Criteria andGiveBalanceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("give_balance not between", value1, value2, "giveBalance");
            return this;
        }

        public Criteria andCardSourceIsNull() {
            addCriterion("card_source is null");
            return this;
        }

        public Criteria andCardSourceIsNotNull() {
            addCriterion("card_source is not null");
            return this;
        }

        public Criteria andCardSourceEqualTo(Short value) {
            addCriterion("card_source =", value, "cardSource");
            return this;
        }

        public Criteria andCardSourceNotEqualTo(Short value) {
            addCriterion("card_source <>", value, "cardSource");
            return this;
        }

        public Criteria andCardSourceGreaterThan(Short value) {
            addCriterion("card_source >", value, "cardSource");
            return this;
        }

        public Criteria andCardSourceGreaterThanOrEqualTo(Short value) {
            addCriterion("card_source >=", value, "cardSource");
            return this;
        }

        public Criteria andCardSourceLessThan(Short value) {
            addCriterion("card_source <", value, "cardSource");
            return this;
        }

        public Criteria andCardSourceLessThanOrEqualTo(Short value) {
            addCriterion("card_source <=", value, "cardSource");
            return this;
        }

        public Criteria andCardSourceIn(List<Short> values) {
            addCriterion("card_source in", values, "cardSource");
            return this;
        }

        public Criteria andCardSourceNotIn(List<Short> values) {
            addCriterion("card_source not in", values, "cardSource");
            return this;
        }

        public Criteria andCardSourceBetween(Short value1, Short value2) {
            addCriterion("card_source between", value1, value2, "cardSource");
            return this;
        }

        public Criteria andCardSourceNotBetween(Short value1, Short value2) {
            addCriterion("card_source not between", value1, value2, "cardSource");
            return this;
        }

        public Criteria andShopMemberSystemIdIsNull() {
            addCriterion("shop_member_system_id is null");
            return this;
        }

        public Criteria andShopMemberSystemIdIsNotNull() {
            addCriterion("shop_member_system_id is not null");
            return this;
        }

        public Criteria andShopMemberSystemIdEqualTo(String value) {
            addCriterion("shop_member_system_id =", value, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdNotEqualTo(String value) {
            addCriterion("shop_member_system_id <>", value, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdGreaterThan(String value) {
            addCriterion("shop_member_system_id >", value, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdGreaterThanOrEqualTo(String value) {
            addCriterion("shop_member_system_id >=", value, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdLessThan(String value) {
            addCriterion("shop_member_system_id <", value, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdLessThanOrEqualTo(String value) {
            addCriterion("shop_member_system_id <=", value, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdLike(String value) {
            addCriterion("shop_member_system_id like", value, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdNotLike(String value) {
            addCriterion("shop_member_system_id not like", value, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdIn(List<String> values) {
            addCriterion("shop_member_system_id in", values, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdNotIn(List<String> values) {
            addCriterion("shop_member_system_id not in", values, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdBetween(String value1, String value2) {
            addCriterion("shop_member_system_id between", value1, value2, "shopMemberSystemId");
            return this;
        }

        public Criteria andShopMemberSystemIdNotBetween(String value1, String value2) {
            addCriterion("shop_member_system_id not between", value1, value2, "shopMemberSystemId");
            return this;
        }

        public Criteria andTransferFlgIsNull() {
            addCriterion("transfer_flg is null");
            return this;
        }

        public Criteria andTransferFlgIsNotNull() {
            addCriterion("transfer_flg is not null");
            return this;
        }

        public Criteria andTransferFlgEqualTo(Boolean value) {
            addCriterion("transfer_flg =", value, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgNotEqualTo(Boolean value) {
            addCriterion("transfer_flg <>", value, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgGreaterThan(Boolean value) {
            addCriterion("transfer_flg >", value, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgGreaterThanOrEqualTo(Boolean value) {
            addCriterion("transfer_flg >=", value, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgLessThan(Boolean value) {
            addCriterion("transfer_flg <", value, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgLessThanOrEqualTo(Boolean value) {
            addCriterion("transfer_flg <=", value, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgIn(List<Boolean> values) {
            addCriterion("transfer_flg in", values, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgNotIn(List<Boolean> values) {
            addCriterion("transfer_flg not in", values, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgBetween(Boolean value1, Boolean value2) {
            addCriterion("transfer_flg between", value1, value2, "transferFlg");
            return this;
        }

        public Criteria andTransferFlgNotBetween(Boolean value1, Boolean value2) {
            addCriterion("transfer_flg not between", value1, value2, "transferFlg");
            return this;
        }

        public Criteria andIsEffectiveIsNull() {
            addCriterion("is_effective is null");
            return this;
        }

        public Criteria andIsEffectiveIsNotNull() {
            addCriterion("is_effective is not null");
            return this;
        }

        public Criteria andIsEffectiveEqualTo(Byte value) {
            addCriterion("is_effective =", value, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveNotEqualTo(Byte value) {
            addCriterion("is_effective <>", value, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveGreaterThan(Byte value) {
            addCriterion("is_effective >", value, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_effective >=", value, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveLessThan(Byte value) {
            addCriterion("is_effective <", value, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveLessThanOrEqualTo(Byte value) {
            addCriterion("is_effective <=", value, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveIn(List<Byte> values) {
            addCriterion("is_effective in", values, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveNotIn(List<Byte> values) {
            addCriterion("is_effective not in", values, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveBetween(Byte value1, Byte value2) {
            addCriterion("is_effective between", value1, value2, "isEffective");
            return this;
        }

        public Criteria andIsEffectiveNotBetween(Byte value1, Byte value2) {
            addCriterion("is_effective not between", value1, value2, "isEffective");
            return this;
        }

        public Criteria andSourceIsNull() {
            addCriterion("source is null");
            return this;
        }

        public Criteria andSourceIsNotNull() {
            addCriterion("source is not null");
            return this;
        }

        public Criteria andSourceEqualTo(String value) {
            addCriterion("source =", value, "source");
            return this;
        }

        public Criteria andSourceNotEqualTo(String value) {
            addCriterion("source <>", value, "source");
            return this;
        }

        public Criteria andSourceGreaterThan(String value) {
            addCriterion("source >", value, "source");
            return this;
        }

        public Criteria andSourceGreaterThanOrEqualTo(String value) {
            addCriterion("source >=", value, "source");
            return this;
        }

        public Criteria andSourceLessThan(String value) {
            addCriterion("source <", value, "source");
            return this;
        }

        public Criteria andSourceLessThanOrEqualTo(String value) {
            addCriterion("source <=", value, "source");
            return this;
        }

        public Criteria andSourceLike(String value) {
            addCriterion("source like", value, "source");
            return this;
        }

        public Criteria andSourceNotLike(String value) {
            addCriterion("source not like", value, "source");
            return this;
        }

        public Criteria andSourceIn(List<String> values) {
            addCriterion("source in", values, "source");
            return this;
        }

        public Criteria andSourceNotIn(List<String> values) {
            addCriterion("source not in", values, "source");
            return this;
        }

        public Criteria andSourceBetween(String value1, String value2) {
            addCriterion("source between", value1, value2, "source");
            return this;
        }

        public Criteria andSourceNotBetween(String value1, String value2) {
            addCriterion("source not between", value1, value2, "source");
            return this;
        }

        public Criteria andActivitySourceIsNull() {
            addCriterion("activity_source is null");
            return this;
        }

        public Criteria andActivitySourceIsNotNull() {
            addCriterion("activity_source is not null");
            return this;
        }

        public Criteria andActivitySourceEqualTo(Short value) {
            addCriterion("activity_source =", value, "activitySource");
            return this;
        }

        public Criteria andActivitySourceNotEqualTo(Short value) {
            addCriterion("activity_source <>", value, "activitySource");
            return this;
        }

        public Criteria andActivitySourceGreaterThan(Short value) {
            addCriterion("activity_source >", value, "activitySource");
            return this;
        }

        public Criteria andActivitySourceGreaterThanOrEqualTo(Short value) {
            addCriterion("activity_source >=", value, "activitySource");
            return this;
        }

        public Criteria andActivitySourceLessThan(Short value) {
            addCriterion("activity_source <", value, "activitySource");
            return this;
        }

        public Criteria andActivitySourceLessThanOrEqualTo(Short value) {
            addCriterion("activity_source <=", value, "activitySource");
            return this;
        }

        public Criteria andActivitySourceIn(List<Short> values) {
            addCriterion("activity_source in", values, "activitySource");
            return this;
        }

        public Criteria andActivitySourceNotIn(List<Short> values) {
            addCriterion("activity_source not in", values, "activitySource");
            return this;
        }

        public Criteria andActivitySourceBetween(Short value1, Short value2) {
            addCriterion("activity_source between", value1, value2, "activitySource");
            return this;
        }

        public Criteria andActivitySourceNotBetween(Short value1, Short value2) {
            addCriterion("activity_source not between", value1, value2, "activitySource");
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

        public Criteria andFreezeBalanceIsNull() {
            addCriterion("freeze_balance is null");
            return this;
        }

        public Criteria andFreezeBalanceIsNotNull() {
            addCriterion("freeze_balance is not null");
            return this;
        }

        public Criteria andFreezeBalanceEqualTo(BigDecimal value) {
            addCriterion("freeze_balance =", value, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceNotEqualTo(BigDecimal value) {
            addCriterion("freeze_balance <>", value, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceGreaterThan(BigDecimal value) {
            addCriterion("freeze_balance >", value, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("freeze_balance >=", value, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceLessThan(BigDecimal value) {
            addCriterion("freeze_balance <", value, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("freeze_balance <=", value, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceIn(List<BigDecimal> values) {
            addCriterion("freeze_balance in", values, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceNotIn(List<BigDecimal> values) {
            addCriterion("freeze_balance not in", values, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("freeze_balance between", value1, value2, "freezeBalance");
            return this;
        }

        public Criteria andFreezeBalanceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("freeze_balance not between", value1, value2, "freezeBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceIsNull() {
            addCriterion("freeze_give_balance is null");
            return this;
        }

        public Criteria andFreezeGiveBalanceIsNotNull() {
            addCriterion("freeze_give_balance is not null");
            return this;
        }

        public Criteria andFreezeGiveBalanceEqualTo(BigDecimal value) {
            addCriterion("freeze_give_balance =", value, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceNotEqualTo(BigDecimal value) {
            addCriterion("freeze_give_balance <>", value, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceGreaterThan(BigDecimal value) {
            addCriterion("freeze_give_balance >", value, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("freeze_give_balance >=", value, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceLessThan(BigDecimal value) {
            addCriterion("freeze_give_balance <", value, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("freeze_give_balance <=", value, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceIn(List<BigDecimal> values) {
            addCriterion("freeze_give_balance in", values, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceNotIn(List<BigDecimal> values) {
            addCriterion("freeze_give_balance not in", values, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("freeze_give_balance between", value1, value2, "freezeGiveBalance");
            return this;
        }

        public Criteria andFreezeGiveBalanceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("freeze_give_balance not between", value1, value2, "freezeGiveBalance");
            return this;
        }
    }
}
