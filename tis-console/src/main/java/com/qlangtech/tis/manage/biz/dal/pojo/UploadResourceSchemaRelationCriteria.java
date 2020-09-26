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
package com.qlangtech.tis.manage.biz.dal.pojo;

import com.qlangtech.tis.ibatis.BasicCriteria;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class UploadResourceSchemaRelationCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    public UploadResourceSchemaRelationCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected UploadResourceSchemaRelationCriteria(UploadResourceSchemaRelationCriteria example) {
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

        public Criteria andUsIdIsNull() {
            addCriterion("us_id is null");
            return this;
        }

        public Criteria andUsIdIsNotNull() {
            addCriterion("us_id is not null");
            return this;
        }

        public Criteria andUsIdEqualTo(Integer value) {
            addCriterion("us_id =", value, "usId");
            return this;
        }

        public Criteria andUsIdNotEqualTo(Integer value) {
            addCriterion("us_id <>", value, "usId");
            return this;
        }

        public Criteria andUsIdGreaterThan(Integer value) {
            addCriterion("us_id >", value, "usId");
            return this;
        }

        public Criteria andUsIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("us_id >=", value, "usId");
            return this;
        }

        public Criteria andUsIdLessThan(Integer value) {
            addCriterion("us_id <", value, "usId");
            return this;
        }

        public Criteria andUsIdLessThanOrEqualTo(Integer value) {
            addCriterion("us_id <=", value, "usId");
            return this;
        }

        public Criteria andUsIdIn(List<Integer> values) {
            addCriterion("us_id in", values, "usId");
            return this;
        }

        public Criteria andUsIdNotIn(List<Integer> values) {
            addCriterion("us_id not in", values, "usId");
            return this;
        }

        public Criteria andUsIdBetween(Integer value1, Integer value2) {
            addCriterion("us_id between", value1, value2, "usId");
            return this;
        }

        public Criteria andUsIdNotBetween(Integer value1, Integer value2) {
            addCriterion("us_id not between", value1, value2, "usId");
            return this;
        }

        public Criteria andUrIdIsNull() {
            addCriterion("ur_id is null");
            return this;
        }

        public Criteria andUrIdIsNotNull() {
            addCriterion("ur_id is not null");
            return this;
        }

        public Criteria andUrIdEqualTo(Long value) {
            addCriterion("ur_id =", value, "urId");
            return this;
        }

        public Criteria andUrIdNotEqualTo(Long value) {
            addCriterion("ur_id <>", value, "urId");
            return this;
        }

        public Criteria andUrIdGreaterThan(Long value) {
            addCriterion("ur_id >", value, "urId");
            return this;
        }

        public Criteria andUrIdGreaterThanOrEqualTo(Long value) {
            addCriterion("ur_id >=", value, "urId");
            return this;
        }

        public Criteria andUrIdLessThan(Long value) {
            addCriterion("ur_id <", value, "urId");
            return this;
        }

        public Criteria andUrIdLessThanOrEqualTo(Long value) {
            addCriterion("ur_id <=", value, "urId");
            return this;
        }

        public Criteria andUrIdIn(List<Long> values) {
            addCriterion("ur_id in", values, "urId");
            return this;
        }

        public Criteria andUrIdNotIn(List<Long> values) {
            addCriterion("ur_id not in", values, "urId");
            return this;
        }

        public Criteria andUrIdBetween(Long value1, Long value2) {
            addCriterion("ur_id between", value1, value2, "urId");
            return this;
        }

        public Criteria andUrIdNotBetween(Long value1, Long value2) {
            addCriterion("ur_id not between", value1, value2, "urId");
            return this;
        }

        public Criteria andShmIdIsNull() {
            addCriterion("shm_id is null");
            return this;
        }

        public Criteria andShmIdIsNotNull() {
            addCriterion("shm_id is not null");
            return this;
        }

        public Criteria andShmIdEqualTo(Long value) {
            addCriterion("shm_id =", value, "shmId");
            return this;
        }

        public Criteria andShmIdNotEqualTo(Long value) {
            addCriterion("shm_id <>", value, "shmId");
            return this;
        }

        public Criteria andShmIdGreaterThan(Long value) {
            addCriterion("shm_id >", value, "shmId");
            return this;
        }

        public Criteria andShmIdGreaterThanOrEqualTo(Long value) {
            addCriterion("shm_id >=", value, "shmId");
            return this;
        }

        public Criteria andShmIdLessThan(Long value) {
            addCriterion("shm_id <", value, "shmId");
            return this;
        }

        public Criteria andShmIdLessThanOrEqualTo(Long value) {
            addCriterion("shm_id <=", value, "shmId");
            return this;
        }

        public Criteria andShmIdIn(List<Long> values) {
            addCriterion("shm_id in", values, "shmId");
            return this;
        }

        public Criteria andShmIdNotIn(List<Long> values) {
            addCriterion("shm_id not in", values, "shmId");
            return this;
        }

        public Criteria andShmIdBetween(Long value1, Long value2) {
            addCriterion("shm_id between", value1, value2, "shmId");
            return this;
        }

        public Criteria andShmIdNotBetween(Long value1, Long value2) {
            addCriterion("shm_id not between", value1, value2, "shmId");
            return this;
        }

        public Criteria andGmtCreateIsNull() {
            addCriterion("gmt_create is null");
            return this;
        }

        public Criteria andGmtCreateIsNotNull() {
            addCriterion("gmt_create is not null");
            return this;
        }

        public Criteria andGmtCreateEqualTo(Date value) {
            addCriterion("gmt_create =", value, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateNotEqualTo(Date value) {
            addCriterion("gmt_create <>", value, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateGreaterThan(Date value) {
            addCriterion("gmt_create >", value, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateGreaterThanOrEqualTo(Date value) {
            addCriterion("gmt_create >=", value, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateLessThan(Date value) {
            addCriterion("gmt_create <", value, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateLessThanOrEqualTo(Date value) {
            addCriterion("gmt_create <=", value, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateIn(List<Date> values) {
            addCriterion("gmt_create in", values, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateNotIn(List<Date> values) {
            addCriterion("gmt_create not in", values, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateBetween(Date value1, Date value2) {
            addCriterion("gmt_create between", value1, value2, "gmtCreate");
            return this;
        }

        public Criteria andGmtCreateNotBetween(Date value1, Date value2) {
            addCriterion("gmt_create not between", value1, value2, "gmtCreate");
            return this;
        }
    }
}
