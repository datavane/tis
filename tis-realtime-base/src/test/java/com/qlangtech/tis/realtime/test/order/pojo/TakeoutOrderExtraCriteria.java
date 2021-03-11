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
public class TakeoutOrderExtraCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<TakeoutOrderExtraColEnum> cols = Sets.newHashSet();

    public TakeoutOrderExtraCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected TakeoutOrderExtraCriteria(TakeoutOrderExtraCriteria example) {
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

    public final List<TakeoutOrderExtraColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(TakeoutOrderExtraColEnum... colName) {
        for (TakeoutOrderExtraColEnum c : colName) {
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

        public Criteria andViewIdIsNull() {
            addCriterion("view_id is null");
            return this;
        }

        public Criteria andViewIdIsNotNull() {
            addCriterion("view_id is not null");
            return this;
        }

        public Criteria andViewIdEqualTo(String value) {
            addCriterion("view_id =", value, "viewId");
            return this;
        }

        public Criteria andViewIdNotEqualTo(String value) {
            addCriterion("view_id <>", value, "viewId");
            return this;
        }

        public Criteria andViewIdGreaterThan(String value) {
            addCriterion("view_id >", value, "viewId");
            return this;
        }

        public Criteria andViewIdGreaterThanOrEqualTo(String value) {
            addCriterion("view_id >=", value, "viewId");
            return this;
        }

        public Criteria andViewIdLessThan(String value) {
            addCriterion("view_id <", value, "viewId");
            return this;
        }

        public Criteria andViewIdLessThanOrEqualTo(String value) {
            addCriterion("view_id <=", value, "viewId");
            return this;
        }

        public Criteria andViewIdLike(String value) {
            addCriterion("view_id like", value, "viewId");
            return this;
        }

        public Criteria andViewIdNotLike(String value) {
            addCriterion("view_id not like", value, "viewId");
            return this;
        }

        public Criteria andViewIdIn(List<String> values) {
            addCriterion("view_id in", values, "viewId");
            return this;
        }

        public Criteria andViewIdNotIn(List<String> values) {
            addCriterion("view_id not in", values, "viewId");
            return this;
        }

        public Criteria andViewIdBetween(String value1, String value2) {
            addCriterion("view_id between", value1, value2, "viewId");
            return this;
        }

        public Criteria andViewIdNotBetween(String value1, String value2) {
            addCriterion("view_id not between", value1, value2, "viewId");
            return this;
        }

        public Criteria andHasInvoicedIsNull() {
            addCriterion("has_invoiced is null");
            return this;
        }

        public Criteria andHasInvoicedIsNotNull() {
            addCriterion("has_invoiced is not null");
            return this;
        }

        public Criteria andHasInvoicedEqualTo(Byte value) {
            addCriterion("has_invoiced =", value, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedNotEqualTo(Byte value) {
            addCriterion("has_invoiced <>", value, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedGreaterThan(Byte value) {
            addCriterion("has_invoiced >", value, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedGreaterThanOrEqualTo(Byte value) {
            addCriterion("has_invoiced >=", value, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedLessThan(Byte value) {
            addCriterion("has_invoiced <", value, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedLessThanOrEqualTo(Byte value) {
            addCriterion("has_invoiced <=", value, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedIn(List<Byte> values) {
            addCriterion("has_invoiced in", values, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedNotIn(List<Byte> values) {
            addCriterion("has_invoiced not in", values, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedBetween(Byte value1, Byte value2) {
            addCriterion("has_invoiced between", value1, value2, "hasInvoiced");
            return this;
        }

        public Criteria andHasInvoicedNotBetween(Byte value1, Byte value2) {
            addCriterion("has_invoiced not between", value1, value2, "hasInvoiced");
            return this;
        }

        public Criteria andInvoiceTitleIsNull() {
            addCriterion("invoice_title is null");
            return this;
        }

        public Criteria andInvoiceTitleIsNotNull() {
            addCriterion("invoice_title is not null");
            return this;
        }

        public Criteria andInvoiceTitleEqualTo(String value) {
            addCriterion("invoice_title =", value, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleNotEqualTo(String value) {
            addCriterion("invoice_title <>", value, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleGreaterThan(String value) {
            addCriterion("invoice_title >", value, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleGreaterThanOrEqualTo(String value) {
            addCriterion("invoice_title >=", value, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleLessThan(String value) {
            addCriterion("invoice_title <", value, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleLessThanOrEqualTo(String value) {
            addCriterion("invoice_title <=", value, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleLike(String value) {
            addCriterion("invoice_title like", value, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleNotLike(String value) {
            addCriterion("invoice_title not like", value, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleIn(List<String> values) {
            addCriterion("invoice_title in", values, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleNotIn(List<String> values) {
            addCriterion("invoice_title not in", values, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleBetween(String value1, String value2) {
            addCriterion("invoice_title between", value1, value2, "invoiceTitle");
            return this;
        }

        public Criteria andInvoiceTitleNotBetween(String value1, String value2) {
            addCriterion("invoice_title not between", value1, value2, "invoiceTitle");
            return this;
        }

        public Criteria andIsThirdShippingIsNull() {
            addCriterion("is_third_shipping is null");
            return this;
        }

        public Criteria andIsThirdShippingIsNotNull() {
            addCriterion("is_third_shipping is not null");
            return this;
        }

        public Criteria andIsThirdShippingEqualTo(Byte value) {
            addCriterion("is_third_shipping =", value, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingNotEqualTo(Byte value) {
            addCriterion("is_third_shipping <>", value, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingGreaterThan(Byte value) {
            addCriterion("is_third_shipping >", value, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_third_shipping >=", value, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingLessThan(Byte value) {
            addCriterion("is_third_shipping <", value, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingLessThanOrEqualTo(Byte value) {
            addCriterion("is_third_shipping <=", value, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingIn(List<Byte> values) {
            addCriterion("is_third_shipping in", values, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingNotIn(List<Byte> values) {
            addCriterion("is_third_shipping not in", values, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingBetween(Byte value1, Byte value2) {
            addCriterion("is_third_shipping between", value1, value2, "isThirdShipping");
            return this;
        }

        public Criteria andIsThirdShippingNotBetween(Byte value1, Byte value2) {
            addCriterion("is_third_shipping not between", value1, value2, "isThirdShipping");
            return this;
        }

        public Criteria andDaySeqIsNull() {
            addCriterion("day_seq is null");
            return this;
        }

        public Criteria andDaySeqIsNotNull() {
            addCriterion("day_seq is not null");
            return this;
        }

        public Criteria andDaySeqEqualTo(String value) {
            addCriterion("day_seq =", value, "daySeq");
            return this;
        }

        public Criteria andDaySeqNotEqualTo(String value) {
            addCriterion("day_seq <>", value, "daySeq");
            return this;
        }

        public Criteria andDaySeqGreaterThan(String value) {
            addCriterion("day_seq >", value, "daySeq");
            return this;
        }

        public Criteria andDaySeqGreaterThanOrEqualTo(String value) {
            addCriterion("day_seq >=", value, "daySeq");
            return this;
        }

        public Criteria andDaySeqLessThan(String value) {
            addCriterion("day_seq <", value, "daySeq");
            return this;
        }

        public Criteria andDaySeqLessThanOrEqualTo(String value) {
            addCriterion("day_seq <=", value, "daySeq");
            return this;
        }

        public Criteria andDaySeqLike(String value) {
            addCriterion("day_seq like", value, "daySeq");
            return this;
        }

        public Criteria andDaySeqNotLike(String value) {
            addCriterion("day_seq not like", value, "daySeq");
            return this;
        }

        public Criteria andDaySeqIn(List<String> values) {
            addCriterion("day_seq in", values, "daySeq");
            return this;
        }

        public Criteria andDaySeqNotIn(List<String> values) {
            addCriterion("day_seq not in", values, "daySeq");
            return this;
        }

        public Criteria andDaySeqBetween(String value1, String value2) {
            addCriterion("day_seq between", value1, value2, "daySeq");
            return this;
        }

        public Criteria andDaySeqNotBetween(String value1, String value2) {
            addCriterion("day_seq not between", value1, value2, "daySeq");
            return this;
        }

        public Criteria andCourierNameIsNull() {
            addCriterion("courier_name is null");
            return this;
        }

        public Criteria andCourierNameIsNotNull() {
            addCriterion("courier_name is not null");
            return this;
        }

        public Criteria andCourierNameEqualTo(String value) {
            addCriterion("courier_name =", value, "courierName");
            return this;
        }

        public Criteria andCourierNameNotEqualTo(String value) {
            addCriterion("courier_name <>", value, "courierName");
            return this;
        }

        public Criteria andCourierNameGreaterThan(String value) {
            addCriterion("courier_name >", value, "courierName");
            return this;
        }

        public Criteria andCourierNameGreaterThanOrEqualTo(String value) {
            addCriterion("courier_name >=", value, "courierName");
            return this;
        }

        public Criteria andCourierNameLessThan(String value) {
            addCriterion("courier_name <", value, "courierName");
            return this;
        }

        public Criteria andCourierNameLessThanOrEqualTo(String value) {
            addCriterion("courier_name <=", value, "courierName");
            return this;
        }

        public Criteria andCourierNameLike(String value) {
            addCriterion("courier_name like", value, "courierName");
            return this;
        }

        public Criteria andCourierNameNotLike(String value) {
            addCriterion("courier_name not like", value, "courierName");
            return this;
        }

        public Criteria andCourierNameIn(List<String> values) {
            addCriterion("courier_name in", values, "courierName");
            return this;
        }

        public Criteria andCourierNameNotIn(List<String> values) {
            addCriterion("courier_name not in", values, "courierName");
            return this;
        }

        public Criteria andCourierNameBetween(String value1, String value2) {
            addCriterion("courier_name between", value1, value2, "courierName");
            return this;
        }

        public Criteria andCourierNameNotBetween(String value1, String value2) {
            addCriterion("courier_name not between", value1, value2, "courierName");
            return this;
        }

        public Criteria andCourierPhoneIsNull() {
            addCriterion("courier_phone is null");
            return this;
        }

        public Criteria andCourierPhoneIsNotNull() {
            addCriterion("courier_phone is not null");
            return this;
        }

        public Criteria andCourierPhoneEqualTo(String value) {
            addCriterion("courier_phone =", value, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneNotEqualTo(String value) {
            addCriterion("courier_phone <>", value, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneGreaterThan(String value) {
            addCriterion("courier_phone >", value, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneGreaterThanOrEqualTo(String value) {
            addCriterion("courier_phone >=", value, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneLessThan(String value) {
            addCriterion("courier_phone <", value, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneLessThanOrEqualTo(String value) {
            addCriterion("courier_phone <=", value, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneLike(String value) {
            addCriterion("courier_phone like", value, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneNotLike(String value) {
            addCriterion("courier_phone not like", value, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneIn(List<String> values) {
            addCriterion("courier_phone in", values, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneNotIn(List<String> values) {
            addCriterion("courier_phone not in", values, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneBetween(String value1, String value2) {
            addCriterion("courier_phone between", value1, value2, "courierPhone");
            return this;
        }

        public Criteria andCourierPhoneNotBetween(String value1, String value2) {
            addCriterion("courier_phone not between", value1, value2, "courierPhone");
            return this;
        }

        public Criteria andCancelReasonIsNull() {
            addCriterion("cancel_reason is null");
            return this;
        }

        public Criteria andCancelReasonIsNotNull() {
            addCriterion("cancel_reason is not null");
            return this;
        }

        public Criteria andCancelReasonEqualTo(String value) {
            addCriterion("cancel_reason =", value, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonNotEqualTo(String value) {
            addCriterion("cancel_reason <>", value, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonGreaterThan(String value) {
            addCriterion("cancel_reason >", value, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonGreaterThanOrEqualTo(String value) {
            addCriterion("cancel_reason >=", value, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonLessThan(String value) {
            addCriterion("cancel_reason <", value, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonLessThanOrEqualTo(String value) {
            addCriterion("cancel_reason <=", value, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonLike(String value) {
            addCriterion("cancel_reason like", value, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonNotLike(String value) {
            addCriterion("cancel_reason not like", value, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonIn(List<String> values) {
            addCriterion("cancel_reason in", values, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonNotIn(List<String> values) {
            addCriterion("cancel_reason not in", values, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonBetween(String value1, String value2) {
            addCriterion("cancel_reason between", value1, value2, "cancelReason");
            return this;
        }

        public Criteria andCancelReasonNotBetween(String value1, String value2) {
            addCriterion("cancel_reason not between", value1, value2, "cancelReason");
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

        public Criteria andOutIdIsNull() {
            addCriterion("out_id is null");
            return this;
        }

        public Criteria andOutIdIsNotNull() {
            addCriterion("out_id is not null");
            return this;
        }

        public Criteria andOutIdEqualTo(String value) {
            addCriterion("out_id =", value, "outId");
            return this;
        }

        public Criteria andOutIdNotEqualTo(String value) {
            addCriterion("out_id <>", value, "outId");
            return this;
        }

        public Criteria andOutIdGreaterThan(String value) {
            addCriterion("out_id >", value, "outId");
            return this;
        }

        public Criteria andOutIdGreaterThanOrEqualTo(String value) {
            addCriterion("out_id >=", value, "outId");
            return this;
        }

        public Criteria andOutIdLessThan(String value) {
            addCriterion("out_id <", value, "outId");
            return this;
        }

        public Criteria andOutIdLessThanOrEqualTo(String value) {
            addCriterion("out_id <=", value, "outId");
            return this;
        }

        public Criteria andOutIdLike(String value) {
            addCriterion("out_id like", value, "outId");
            return this;
        }

        public Criteria andOutIdNotLike(String value) {
            addCriterion("out_id not like", value, "outId");
            return this;
        }

        public Criteria andOutIdIn(List<String> values) {
            addCriterion("out_id in", values, "outId");
            return this;
        }

        public Criteria andOutIdNotIn(List<String> values) {
            addCriterion("out_id not in", values, "outId");
            return this;
        }

        public Criteria andOutIdBetween(String value1, String value2) {
            addCriterion("out_id between", value1, value2, "outId");
            return this;
        }

        public Criteria andOutIdNotBetween(String value1, String value2) {
            addCriterion("out_id not between", value1, value2, "outId");
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

        public Criteria andBeginExpectDateIsNull() {
            addCriterion("begin_expect_date is null");
            return this;
        }

        public Criteria andBeginExpectDateIsNotNull() {
            addCriterion("begin_expect_date is not null");
            return this;
        }

        public Criteria andBeginExpectDateEqualTo(Long value) {
            addCriterion("begin_expect_date =", value, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateNotEqualTo(Long value) {
            addCriterion("begin_expect_date <>", value, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateGreaterThan(Long value) {
            addCriterion("begin_expect_date >", value, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateGreaterThanOrEqualTo(Long value) {
            addCriterion("begin_expect_date >=", value, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateLessThan(Long value) {
            addCriterion("begin_expect_date <", value, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateLessThanOrEqualTo(Long value) {
            addCriterion("begin_expect_date <=", value, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateIn(List<Long> values) {
            addCriterion("begin_expect_date in", values, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateNotIn(List<Long> values) {
            addCriterion("begin_expect_date not in", values, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateBetween(Long value1, Long value2) {
            addCriterion("begin_expect_date between", value1, value2, "beginExpectDate");
            return this;
        }

        public Criteria andBeginExpectDateNotBetween(Long value1, Long value2) {
            addCriterion("begin_expect_date not between", value1, value2, "beginExpectDate");
            return this;
        }

        public Criteria andEndExpectDateIsNull() {
            addCriterion("end_expect_date is null");
            return this;
        }

        public Criteria andEndExpectDateIsNotNull() {
            addCriterion("end_expect_date is not null");
            return this;
        }

        public Criteria andEndExpectDateEqualTo(Long value) {
            addCriterion("end_expect_date =", value, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateNotEqualTo(Long value) {
            addCriterion("end_expect_date <>", value, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateGreaterThan(Long value) {
            addCriterion("end_expect_date >", value, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateGreaterThanOrEqualTo(Long value) {
            addCriterion("end_expect_date >=", value, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateLessThan(Long value) {
            addCriterion("end_expect_date <", value, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateLessThanOrEqualTo(Long value) {
            addCriterion("end_expect_date <=", value, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateIn(List<Long> values) {
            addCriterion("end_expect_date in", values, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateNotIn(List<Long> values) {
            addCriterion("end_expect_date not in", values, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateBetween(Long value1, Long value2) {
            addCriterion("end_expect_date between", value1, value2, "endExpectDate");
            return this;
        }

        public Criteria andEndExpectDateNotBetween(Long value1, Long value2) {
            addCriterion("end_expect_date not between", value1, value2, "endExpectDate");
            return this;
        }

        public Criteria andReserveDateNameIsNull() {
            addCriterion("reserve_date_name is null");
            return this;
        }

        public Criteria andReserveDateNameIsNotNull() {
            addCriterion("reserve_date_name is not null");
            return this;
        }

        public Criteria andReserveDateNameEqualTo(String value) {
            addCriterion("reserve_date_name =", value, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameNotEqualTo(String value) {
            addCriterion("reserve_date_name <>", value, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameGreaterThan(String value) {
            addCriterion("reserve_date_name >", value, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameGreaterThanOrEqualTo(String value) {
            addCriterion("reserve_date_name >=", value, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameLessThan(String value) {
            addCriterion("reserve_date_name <", value, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameLessThanOrEqualTo(String value) {
            addCriterion("reserve_date_name <=", value, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameLike(String value) {
            addCriterion("reserve_date_name like", value, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameNotLike(String value) {
            addCriterion("reserve_date_name not like", value, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameIn(List<String> values) {
            addCriterion("reserve_date_name in", values, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameNotIn(List<String> values) {
            addCriterion("reserve_date_name not in", values, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameBetween(String value1, String value2) {
            addCriterion("reserve_date_name between", value1, value2, "reserveDateName");
            return this;
        }

        public Criteria andReserveDateNameNotBetween(String value1, String value2) {
            addCriterion("reserve_date_name not between", value1, value2, "reserveDateName");
            return this;
        }
    }
}
