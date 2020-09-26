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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class ApplicationExtendCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    public ApplicationExtendCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected ApplicationExtendCriteria(ApplicationExtendCriteria example) {
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

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
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

        public Criteria andGmtModifiedIsNull() {
            addCriterion("gmt_modified is null");
            return this;
        }

        public Criteria andGmtModifiedIsNotNull() {
            addCriterion("gmt_modified is not null");
            return this;
        }

        public Criteria andGmtModifiedEqualTo(Date value) {
            addCriterion("gmt_modified =", value, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedNotEqualTo(Date value) {
            addCriterion("gmt_modified <>", value, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedGreaterThan(Date value) {
            addCriterion("gmt_modified >", value, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedGreaterThanOrEqualTo(Date value) {
            addCriterion("gmt_modified >=", value, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedLessThan(Date value) {
            addCriterion("gmt_modified <", value, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedLessThanOrEqualTo(Date value) {
            addCriterion("gmt_modified <=", value, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedIn(List<Date> values) {
            addCriterion("gmt_modified in", values, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedNotIn(List<Date> values) {
            addCriterion("gmt_modified not in", values, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedBetween(Date value1, Date value2) {
            addCriterion("gmt_modified between", value1, value2, "gmtModified");
            return this;
        }

        public Criteria andGmtModifiedNotBetween(Date value1, Date value2) {
            addCriterion("gmt_modified not between", value1, value2, "gmtModified");
            return this;
        }

        public Criteria andAIdIsNull() {
            addCriterion("a_id is null");
            return this;
        }

        public Criteria andAIdIsNotNull() {
            addCriterion("a_id is not null");
            return this;
        }

        public Criteria andAIdEqualTo(Long value) {
            addCriterion("a_id =", value, "aId");
            return this;
        }

        public Criteria andAIdNotEqualTo(Long value) {
            addCriterion("a_id <>", value, "aId");
            return this;
        }

        public Criteria andAIdGreaterThan(Long value) {
            addCriterion("a_id >", value, "aId");
            return this;
        }

        public Criteria andAIdGreaterThanOrEqualTo(Long value) {
            addCriterion("a_id >=", value, "aId");
            return this;
        }

        public Criteria andAIdLessThan(Long value) {
            addCriterion("a_id <", value, "aId");
            return this;
        }

        public Criteria andAIdLessThanOrEqualTo(Long value) {
            addCriterion("a_id <=", value, "aId");
            return this;
        }

        public Criteria andAIdIn(List<Long> values) {
            addCriterion("a_id in", values, "aId");
            return this;
        }

        public Criteria andAIdNotIn(List<Long> values) {
            addCriterion("a_id not in", values, "aId");
            return this;
        }

        public Criteria andAIdBetween(Long value1, Long value2) {
            addCriterion("a_id between", value1, value2, "aId");
            return this;
        }

        public Criteria andAIdNotBetween(Long value1, Long value2) {
            addCriterion("a_id not between", value1, value2, "aId");
            return this;
        }

        public Criteria andIsLockIsNull() {
            addCriterion("is_lock is null");
            return this;
        }

        public Criteria andIsLockIsNotNull() {
            addCriterion("is_lock is not null");
            return this;
        }

        public Criteria andIsLockEqualTo(String value) {
            addCriterion("is_lock =", value, "isLock");
            return this;
        }

        public Criteria andIsLockNotEqualTo(String value) {
            addCriterion("is_lock <>", value, "isLock");
            return this;
        }

        public Criteria andIsLockGreaterThan(String value) {
            addCriterion("is_lock >", value, "isLock");
            return this;
        }

        public Criteria andIsLockGreaterThanOrEqualTo(String value) {
            addCriterion("is_lock >=", value, "isLock");
            return this;
        }

        public Criteria andIsLockLessThan(String value) {
            addCriterion("is_lock <", value, "isLock");
            return this;
        }

        public Criteria andIsLockLessThanOrEqualTo(String value) {
            addCriterion("is_lock <=", value, "isLock");
            return this;
        }

        public Criteria andIsLockLike(String value) {
            addCriterion("is_lock like", value, "isLock");
            return this;
        }

        public Criteria andIsLockNotLike(String value) {
            addCriterion("is_lock not like", value, "isLock");
            return this;
        }

        public Criteria andIsLockIn(List<String> values) {
            addCriterion("is_lock in", values, "isLock");
            return this;
        }

        public Criteria andIsLockNotIn(List<String> values) {
            addCriterion("is_lock not in", values, "isLock");
            return this;
        }

        public Criteria andIsLockBetween(String value1, String value2) {
            addCriterion("is_lock between", value1, value2, "isLock");
            return this;
        }

        public Criteria andIsLockNotBetween(String value1, String value2) {
            addCriterion("is_lock not between", value1, value2, "isLock");
            return this;
        }

        public Criteria andIsReleaseIsNull() {
            addCriterion("is_release is null");
            return this;
        }

        public Criteria andIsReleaseIsNotNull() {
            addCriterion("is_release is not null");
            return this;
        }

        public Criteria andIsReleaseEqualTo(String value) {
            addCriterion("is_release =", value, "isRelease");
            return this;
        }

        public Criteria andIsReleaseNotEqualTo(String value) {
            addCriterion("is_release <>", value, "isRelease");
            return this;
        }

        public Criteria andIsReleaseGreaterThan(String value) {
            addCriterion("is_release >", value, "isRelease");
            return this;
        }

        public Criteria andIsReleaseGreaterThanOrEqualTo(String value) {
            addCriterion("is_release >=", value, "isRelease");
            return this;
        }

        public Criteria andIsReleaseLessThan(String value) {
            addCriterion("is_release <", value, "isRelease");
            return this;
        }

        public Criteria andIsReleaseLessThanOrEqualTo(String value) {
            addCriterion("is_release <=", value, "isRelease");
            return this;
        }

        public Criteria andIsReleaseLike(String value) {
            addCriterion("is_release like", value, "isRelease");
            return this;
        }

        public Criteria andIsReleaseNotLike(String value) {
            addCriterion("is_release not like", value, "isRelease");
            return this;
        }

        public Criteria andIsReleaseIn(List<String> values) {
            addCriterion("is_release in", values, "isRelease");
            return this;
        }

        public Criteria andIsReleaseNotIn(List<String> values) {
            addCriterion("is_release not in", values, "isRelease");
            return this;
        }

        public Criteria andIsReleaseBetween(String value1, String value2) {
            addCriterion("is_release between", value1, value2, "isRelease");
            return this;
        }

        public Criteria andIsReleaseNotBetween(String value1, String value2) {
            addCriterion("is_release not between", value1, value2, "isRelease");
            return this;
        }

        public Criteria andOdpsTableIsNull() {
            addCriterion("odps_table is null");
            return this;
        }

        public Criteria andOdpsTableIsNotNull() {
            addCriterion("odps_table is not null");
            return this;
        }

        public Criteria andOdpsTableEqualTo(Long value) {
            addCriterion("odps_table =", value, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableNotEqualTo(Long value) {
            addCriterion("odps_table <>", value, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableGreaterThan(Long value) {
            addCriterion("odps_table >", value, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableGreaterThanOrEqualTo(Long value) {
            addCriterion("odps_table >=", value, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableLessThan(Long value) {
            addCriterion("odps_table <", value, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableLessThanOrEqualTo(Long value) {
            addCriterion("odps_table <=", value, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableIn(List<Long> values) {
            addCriterion("odps_table in", values, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableNotIn(List<Long> values) {
            addCriterion("odps_table not in", values, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableBetween(Long value1, Long value2) {
            addCriterion("odps_table between", value1, value2, "odpsTable");
            return this;
        }

        public Criteria andOdpsTableNotBetween(Long value1, Long value2) {
            addCriterion("odps_table not between", value1, value2, "odpsTable");
            return this;
        }

        public Criteria andColumnsMd5IsNull() {
            addCriterion("columns_md5 is null");
            return this;
        }

        public Criteria andColumnsMd5IsNotNull() {
            addCriterion("columns_md5 is not null");
            return this;
        }

        public Criteria andColumnsMd5EqualTo(String value) {
            addCriterion("columns_md5 =", value, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5NotEqualTo(String value) {
            addCriterion("columns_md5 <>", value, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5GreaterThan(String value) {
            addCriterion("columns_md5 >", value, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5GreaterThanOrEqualTo(String value) {
            addCriterion("columns_md5 >=", value, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5LessThan(String value) {
            addCriterion("columns_md5 <", value, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5LessThanOrEqualTo(String value) {
            addCriterion("columns_md5 <=", value, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5Like(String value) {
            addCriterion("columns_md5 like", value, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5NotLike(String value) {
            addCriterion("columns_md5 not like", value, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5In(List<String> values) {
            addCriterion("columns_md5 in", values, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5NotIn(List<String> values) {
            addCriterion("columns_md5 not in", values, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5Between(String value1, String value2) {
            addCriterion("columns_md5 between", value1, value2, "columnsMd5");
            return this;
        }

        public Criteria andColumnsMd5NotBetween(String value1, String value2) {
            addCriterion("columns_md5 not between", value1, value2, "columnsMd5");
            return this;
        }

        public Criteria andAnnotationIsNull() {
            addCriterion("annotation is null");
            return this;
        }

        public Criteria andAnnotationIsNotNull() {
            addCriterion("annotation is not null");
            return this;
        }

        public Criteria andAnnotationEqualTo(String value) {
            addCriterion("annotation =", value, "annotation");
            return this;
        }

        public Criteria andAnnotationNotEqualTo(String value) {
            addCriterion("annotation <>", value, "annotation");
            return this;
        }

        public Criteria andAnnotationGreaterThan(String value) {
            addCriterion("annotation >", value, "annotation");
            return this;
        }

        public Criteria andAnnotationGreaterThanOrEqualTo(String value) {
            addCriterion("annotation >=", value, "annotation");
            return this;
        }

        public Criteria andAnnotationLessThan(String value) {
            addCriterion("annotation <", value, "annotation");
            return this;
        }

        public Criteria andAnnotationLessThanOrEqualTo(String value) {
            addCriterion("annotation <=", value, "annotation");
            return this;
        }

        public Criteria andAnnotationLike(String value) {
            addCriterion("annotation like", value, "annotation");
            return this;
        }

        public Criteria andAnnotationNotLike(String value) {
            addCriterion("annotation not like", value, "annotation");
            return this;
        }

        public Criteria andAnnotationIn(List<String> values) {
            addCriterion("annotation in", values, "annotation");
            return this;
        }

        public Criteria andAnnotationNotIn(List<String> values) {
            addCriterion("annotation not in", values, "annotation");
            return this;
        }

        public Criteria andAnnotationBetween(String value1, String value2) {
            addCriterion("annotation between", value1, value2, "annotation");
            return this;
        }

        public Criteria andAnnotationNotBetween(String value1, String value2) {
            addCriterion("annotation not between", value1, value2, "annotation");
            return this;
        }

        public Criteria andUserIdIsNull() {
            addCriterion("user_id is null");
            return this;
        }

        public Criteria andUserIdIsNotNull() {
            addCriterion("user_id is not null");
            return this;
        }

        public Criteria andUserIdEqualTo(Long value) {
            addCriterion("user_id =", value, "userId");
            return this;
        }

        public Criteria andUserIdNotEqualTo(Long value) {
            addCriterion("user_id <>", value, "userId");
            return this;
        }

        public Criteria andUserIdGreaterThan(Long value) {
            addCriterion("user_id >", value, "userId");
            return this;
        }

        public Criteria andUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("user_id >=", value, "userId");
            return this;
        }

        public Criteria andUserIdLessThan(Long value) {
            addCriterion("user_id <", value, "userId");
            return this;
        }

        public Criteria andUserIdLessThanOrEqualTo(Long value) {
            addCriterion("user_id <=", value, "userId");
            return this;
        }

        public Criteria andUserIdIn(List<Long> values) {
            addCriterion("user_id in", values, "userId");
            return this;
        }

        public Criteria andUserIdNotIn(List<Long> values) {
            addCriterion("user_id not in", values, "userId");
            return this;
        }

        public Criteria andUserIdBetween(Long value1, Long value2) {
            addCriterion("user_id between", value1, value2, "userId");
            return this;
        }

        public Criteria andUserIdNotBetween(Long value1, Long value2) {
            addCriterion("user_id not between", value1, value2, "userId");
            return this;
        }
    }
}
