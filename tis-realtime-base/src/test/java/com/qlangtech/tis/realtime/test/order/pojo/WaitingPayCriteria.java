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
import java.math.BigDecimal;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WaitingPayCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<WaitingPayColEnum> cols = Sets.newHashSet();

    public WaitingPayCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected WaitingPayCriteria(WaitingPayCriteria example) {
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

    public final List<WaitingPayColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(WaitingPayColEnum... colName) {
        for (WaitingPayColEnum c : colName) {
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

        public Criteria andExtIdIsNull() {
            addCriterion("ext_id is null");
            return this;
        }

        public Criteria andExtIdIsNotNull() {
            addCriterion("ext_id is not null");
            return this;
        }

        public Criteria andExtIdEqualTo(String value) {
            addCriterion("ext_id =", value, "extId");
            return this;
        }

        public Criteria andExtIdNotEqualTo(String value) {
            addCriterion("ext_id <>", value, "extId");
            return this;
        }

        public Criteria andExtIdGreaterThan(String value) {
            addCriterion("ext_id >", value, "extId");
            return this;
        }

        public Criteria andExtIdGreaterThanOrEqualTo(String value) {
            addCriterion("ext_id >=", value, "extId");
            return this;
        }

        public Criteria andExtIdLessThan(String value) {
            addCriterion("ext_id <", value, "extId");
            return this;
        }

        public Criteria andExtIdLessThanOrEqualTo(String value) {
            addCriterion("ext_id <=", value, "extId");
            return this;
        }

        public Criteria andExtIdLike(String value) {
            addCriterion("ext_id like", value, "extId");
            return this;
        }

        public Criteria andExtIdNotLike(String value) {
            addCriterion("ext_id not like", value, "extId");
            return this;
        }

        public Criteria andExtIdIn(List<String> values) {
            addCriterion("ext_id in", values, "extId");
            return this;
        }

        public Criteria andExtIdNotIn(List<String> values) {
            addCriterion("ext_id not in", values, "extId");
            return this;
        }

        public Criteria andExtIdBetween(String value1, String value2) {
            addCriterion("ext_id between", value1, value2, "extId");
            return this;
        }

        public Criteria andExtIdNotBetween(String value1, String value2) {
            addCriterion("ext_id not between", value1, value2, "extId");
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

        public Criteria andErrorMessageIsNull() {
            addCriterion("error_message is null");
            return this;
        }

        public Criteria andErrorMessageIsNotNull() {
            addCriterion("error_message is not null");
            return this;
        }

        public Criteria andErrorMessageEqualTo(String value) {
            addCriterion("error_message =", value, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageNotEqualTo(String value) {
            addCriterion("error_message <>", value, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageGreaterThan(String value) {
            addCriterion("error_message >", value, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageGreaterThanOrEqualTo(String value) {
            addCriterion("error_message >=", value, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageLessThan(String value) {
            addCriterion("error_message <", value, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageLessThanOrEqualTo(String value) {
            addCriterion("error_message <=", value, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageLike(String value) {
            addCriterion("error_message like", value, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageNotLike(String value) {
            addCriterion("error_message not like", value, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageIn(List<String> values) {
            addCriterion("error_message in", values, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageNotIn(List<String> values) {
            addCriterion("error_message not in", values, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageBetween(String value1, String value2) {
            addCriterion("error_message between", value1, value2, "errorMessage");
            return this;
        }

        public Criteria andErrorMessageNotBetween(String value1, String value2) {
            addCriterion("error_message not between", value1, value2, "errorMessage");
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

        public Criteria andCustomerRegisterIdIsNull() {
            addCriterion("customer_register_id is null");
            return this;
        }

        public Criteria andCustomerRegisterIdIsNotNull() {
            addCriterion("customer_register_id is not null");
            return this;
        }

        public Criteria andCustomerRegisterIdEqualTo(String value) {
            addCriterion("customer_register_id =", value, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdNotEqualTo(String value) {
            addCriterion("customer_register_id <>", value, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdGreaterThan(String value) {
            addCriterion("customer_register_id >", value, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdGreaterThanOrEqualTo(String value) {
            addCriterion("customer_register_id >=", value, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdLessThan(String value) {
            addCriterion("customer_register_id <", value, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdLessThanOrEqualTo(String value) {
            addCriterion("customer_register_id <=", value, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdLike(String value) {
            addCriterion("customer_register_id like", value, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdNotLike(String value) {
            addCriterion("customer_register_id not like", value, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdIn(List<String> values) {
            addCriterion("customer_register_id in", values, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdNotIn(List<String> values) {
            addCriterion("customer_register_id not in", values, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdBetween(String value1, String value2) {
            addCriterion("customer_register_id between", value1, value2, "customerRegisterId");
            return this;
        }

        public Criteria andCustomerRegisterIdNotBetween(String value1, String value2) {
            addCriterion("customer_register_id not between", value1, value2, "customerRegisterId");
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

        public Criteria andRelationIdIsNull() {
            addCriterion("relation_id is null");
            return this;
        }

        public Criteria andRelationIdIsNotNull() {
            addCriterion("relation_id is not null");
            return this;
        }

        public Criteria andRelationIdEqualTo(String value) {
            addCriterion("relation_id =", value, "relationId");
            return this;
        }

        public Criteria andRelationIdNotEqualTo(String value) {
            addCriterion("relation_id <>", value, "relationId");
            return this;
        }

        public Criteria andRelationIdGreaterThan(String value) {
            addCriterion("relation_id >", value, "relationId");
            return this;
        }

        public Criteria andRelationIdGreaterThanOrEqualTo(String value) {
            addCriterion("relation_id >=", value, "relationId");
            return this;
        }

        public Criteria andRelationIdLessThan(String value) {
            addCriterion("relation_id <", value, "relationId");
            return this;
        }

        public Criteria andRelationIdLessThanOrEqualTo(String value) {
            addCriterion("relation_id <=", value, "relationId");
            return this;
        }

        public Criteria andRelationIdLike(String value) {
            addCriterion("relation_id like", value, "relationId");
            return this;
        }

        public Criteria andRelationIdNotLike(String value) {
            addCriterion("relation_id not like", value, "relationId");
            return this;
        }

        public Criteria andRelationIdIn(List<String> values) {
            addCriterion("relation_id in", values, "relationId");
            return this;
        }

        public Criteria andRelationIdNotIn(List<String> values) {
            addCriterion("relation_id not in", values, "relationId");
            return this;
        }

        public Criteria andRelationIdBetween(String value1, String value2) {
            addCriterion("relation_id between", value1, value2, "relationId");
            return this;
        }

        public Criteria andRelationIdNotBetween(String value1, String value2) {
            addCriterion("relation_id not between", value1, value2, "relationId");
            return this;
        }

        public Criteria andPayStatusIsNull() {
            addCriterion("pay_status is null");
            return this;
        }

        public Criteria andPayStatusIsNotNull() {
            addCriterion("pay_status is not null");
            return this;
        }

        public Criteria andPayStatusEqualTo(Short value) {
            addCriterion("pay_status =", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusNotEqualTo(Short value) {
            addCriterion("pay_status <>", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusGreaterThan(Short value) {
            addCriterion("pay_status >", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("pay_status >=", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusLessThan(Short value) {
            addCriterion("pay_status <", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusLessThanOrEqualTo(Short value) {
            addCriterion("pay_status <=", value, "payStatus");
            return this;
        }

        public Criteria andPayStatusIn(List<Short> values) {
            addCriterion("pay_status in", values, "payStatus");
            return this;
        }

        public Criteria andPayStatusNotIn(List<Short> values) {
            addCriterion("pay_status not in", values, "payStatus");
            return this;
        }

        public Criteria andPayStatusBetween(Short value1, Short value2) {
            addCriterion("pay_status between", value1, value2, "payStatus");
            return this;
        }

        public Criteria andPayStatusNotBetween(Short value1, Short value2) {
            addCriterion("pay_status not between", value1, value2, "payStatus");
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
    }
}
