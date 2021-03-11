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
public class GlobalcodeorderCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<GlobalcodeorderColEnum> cols = Sets.newHashSet();

    public GlobalcodeorderCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected GlobalcodeorderCriteria(GlobalcodeorderCriteria example) {
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

    public final List<GlobalcodeorderColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(GlobalcodeorderColEnum... colName) {
        for (GlobalcodeorderColEnum c : colName) {
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

        public Criteria andGlobalCodeIsNull() {
            addCriterion("global_code is null");
            return this;
        }

        public Criteria andGlobalCodeIsNotNull() {
            addCriterion("global_code is not null");
            return this;
        }

        public Criteria andGlobalCodeEqualTo(String value) {
            addCriterion("global_code =", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeNotEqualTo(String value) {
            addCriterion("global_code <>", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeGreaterThan(String value) {
            addCriterion("global_code >", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeGreaterThanOrEqualTo(String value) {
            addCriterion("global_code >=", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeLessThan(String value) {
            addCriterion("global_code <", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeLessThanOrEqualTo(String value) {
            addCriterion("global_code <=", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeLike(String value) {
            addCriterion("global_code like", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeNotLike(String value) {
            addCriterion("global_code not like", value, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeIn(List<String> values) {
            addCriterion("global_code in", values, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeNotIn(List<String> values) {
            addCriterion("global_code not in", values, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeBetween(String value1, String value2) {
            addCriterion("global_code between", value1, value2, "globalCode");
            return this;
        }

        public Criteria andGlobalCodeNotBetween(String value1, String value2) {
            addCriterion("global_code not between", value1, value2, "globalCode");
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
    }
}
