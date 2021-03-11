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
public class WaitingordercridCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<WaitingordercridColEnum> cols = Sets.newHashSet();

    public WaitingordercridCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected WaitingordercridCriteria(WaitingordercridCriteria example) {
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

    public final List<WaitingordercridColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(WaitingordercridColEnum... colName) {
        for (WaitingordercridColEnum c : colName) {
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

        public Criteria andKindIsNull() {
            addCriterion("kind is null");
            return this;
        }

        public Criteria andKindIsNotNull() {
            addCriterion("kind is not null");
            return this;
        }

        public Criteria andKindEqualTo(Short value) {
            addCriterion("kind =", value, "kind");
            return this;
        }

        public Criteria andKindNotEqualTo(Short value) {
            addCriterion("kind <>", value, "kind");
            return this;
        }

        public Criteria andKindGreaterThan(Short value) {
            addCriterion("kind >", value, "kind");
            return this;
        }

        public Criteria andKindGreaterThanOrEqualTo(Short value) {
            addCriterion("kind >=", value, "kind");
            return this;
        }

        public Criteria andKindLessThan(Short value) {
            addCriterion("kind <", value, "kind");
            return this;
        }

        public Criteria andKindLessThanOrEqualTo(Short value) {
            addCriterion("kind <=", value, "kind");
            return this;
        }

        public Criteria andKindIn(List<Short> values) {
            addCriterion("kind in", values, "kind");
            return this;
        }

        public Criteria andKindNotIn(List<Short> values) {
            addCriterion("kind not in", values, "kind");
            return this;
        }

        public Criteria andKindBetween(Short value1, Short value2) {
            addCriterion("kind between", value1, value2, "kind");
            return this;
        }

        public Criteria andKindNotBetween(Short value1, Short value2) {
            addCriterion("kind not between", value1, value2, "kind");
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
    }
}
