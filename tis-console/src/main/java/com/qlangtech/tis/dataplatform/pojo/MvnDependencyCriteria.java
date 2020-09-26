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
package com.qlangtech.tis.dataplatform.pojo;

import com.qlangtech.tis.ibatis.BasicCriteria;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MvnDependencyCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    public MvnDependencyCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected MvnDependencyCriteria(MvnDependencyCriteria example) {
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

        public Criteria andNobelAppIdIsNull() {
            addCriterion("nobel_app_id is null");
            return this;
        }

        public Criteria andNobelAppIdIsNotNull() {
            addCriterion("nobel_app_id is not null");
            return this;
        }

        public Criteria andNobelAppIdEqualTo(Long value) {
            addCriterion("nobel_app_id =", value, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdNotEqualTo(Long value) {
            addCriterion("nobel_app_id <>", value, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdGreaterThan(Long value) {
            addCriterion("nobel_app_id >", value, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdGreaterThanOrEqualTo(Long value) {
            addCriterion("nobel_app_id >=", value, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdLessThan(Long value) {
            addCriterion("nobel_app_id <", value, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdLessThanOrEqualTo(Long value) {
            addCriterion("nobel_app_id <=", value, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdIn(List<Long> values) {
            addCriterion("nobel_app_id in", values, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdNotIn(List<Long> values) {
            addCriterion("nobel_app_id not in", values, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdBetween(Long value1, Long value2) {
            addCriterion("nobel_app_id between", value1, value2, "nobelAppId");
            return this;
        }

        public Criteria andNobelAppIdNotBetween(Long value1, Long value2) {
            addCriterion("nobel_app_id not between", value1, value2, "nobelAppId");
            return this;
        }

        public Criteria andTisAppIdIsNull() {
            addCriterion("tis_app_id is null");
            return this;
        }

        public Criteria andTisAppIdIsNotNull() {
            addCriterion("tis_app_id is not null");
            return this;
        }

        public Criteria andTisAppIdEqualTo(Long value) {
            addCriterion("tis_app_id =", value, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdNotEqualTo(Long value) {
            addCriterion("tis_app_id <>", value, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdGreaterThan(Long value) {
            addCriterion("tis_app_id >", value, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdGreaterThanOrEqualTo(Long value) {
            addCriterion("tis_app_id >=", value, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdLessThan(Long value) {
            addCriterion("tis_app_id <", value, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdLessThanOrEqualTo(Long value) {
            addCriterion("tis_app_id <=", value, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdIn(List<Long> values) {
            addCriterion("tis_app_id in", values, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdNotIn(List<Long> values) {
            addCriterion("tis_app_id not in", values, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdBetween(Long value1, Long value2) {
            addCriterion("tis_app_id between", value1, value2, "tisAppId");
            return this;
        }

        public Criteria andTisAppIdNotBetween(Long value1, Long value2) {
            addCriterion("tis_app_id not between", value1, value2, "tisAppId");
            return this;
        }

        public Criteria andGroupIdIsNull() {
            addCriterion("group_id is null");
            return this;
        }

        public Criteria andGroupIdIsNotNull() {
            addCriterion("group_id is not null");
            return this;
        }

        public Criteria andGroupIdEqualTo(String value) {
            addCriterion("group_id =", value, "groupId");
            return this;
        }

        public Criteria andGroupIdNotEqualTo(String value) {
            addCriterion("group_id <>", value, "groupId");
            return this;
        }

        public Criteria andGroupIdGreaterThan(String value) {
            addCriterion("group_id >", value, "groupId");
            return this;
        }

        public Criteria andGroupIdGreaterThanOrEqualTo(String value) {
            addCriterion("group_id >=", value, "groupId");
            return this;
        }

        public Criteria andGroupIdLessThan(String value) {
            addCriterion("group_id <", value, "groupId");
            return this;
        }

        public Criteria andGroupIdLessThanOrEqualTo(String value) {
            addCriterion("group_id <=", value, "groupId");
            return this;
        }

        public Criteria andGroupIdLike(String value) {
            addCriterion("group_id like", value, "groupId");
            return this;
        }

        public Criteria andGroupIdNotLike(String value) {
            addCriterion("group_id not like", value, "groupId");
            return this;
        }

        public Criteria andGroupIdIn(List<String> values) {
            addCriterion("group_id in", values, "groupId");
            return this;
        }

        public Criteria andGroupIdNotIn(List<String> values) {
            addCriterion("group_id not in", values, "groupId");
            return this;
        }

        public Criteria andGroupIdBetween(String value1, String value2) {
            addCriterion("group_id between", value1, value2, "groupId");
            return this;
        }

        public Criteria andGroupIdNotBetween(String value1, String value2) {
            addCriterion("group_id not between", value1, value2, "groupId");
            return this;
        }

        public Criteria andArtifactIdIsNull() {
            addCriterion("artifact_id is null");
            return this;
        }

        public Criteria andArtifactIdIsNotNull() {
            addCriterion("artifact_id is not null");
            return this;
        }

        public Criteria andArtifactIdEqualTo(String value) {
            addCriterion("artifact_id =", value, "artifactId");
            return this;
        }

        public Criteria andArtifactIdNotEqualTo(String value) {
            addCriterion("artifact_id <>", value, "artifactId");
            return this;
        }

        public Criteria andArtifactIdGreaterThan(String value) {
            addCriterion("artifact_id >", value, "artifactId");
            return this;
        }

        public Criteria andArtifactIdGreaterThanOrEqualTo(String value) {
            addCriterion("artifact_id >=", value, "artifactId");
            return this;
        }

        public Criteria andArtifactIdLessThan(String value) {
            addCriterion("artifact_id <", value, "artifactId");
            return this;
        }

        public Criteria andArtifactIdLessThanOrEqualTo(String value) {
            addCriterion("artifact_id <=", value, "artifactId");
            return this;
        }

        public Criteria andArtifactIdLike(String value) {
            addCriterion("artifact_id like", value, "artifactId");
            return this;
        }

        public Criteria andArtifactIdNotLike(String value) {
            addCriterion("artifact_id not like", value, "artifactId");
            return this;
        }

        public Criteria andArtifactIdIn(List<String> values) {
            addCriterion("artifact_id in", values, "artifactId");
            return this;
        }

        public Criteria andArtifactIdNotIn(List<String> values) {
            addCriterion("artifact_id not in", values, "artifactId");
            return this;
        }

        public Criteria andArtifactIdBetween(String value1, String value2) {
            addCriterion("artifact_id between", value1, value2, "artifactId");
            return this;
        }

        public Criteria andArtifactIdNotBetween(String value1, String value2) {
            addCriterion("artifact_id not between", value1, value2, "artifactId");
            return this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return this;
        }

        public Criteria andVersionEqualTo(String value) {
            addCriterion("version =", value, "version");
            return this;
        }

        public Criteria andVersionNotEqualTo(String value) {
            addCriterion("version <>", value, "version");
            return this;
        }

        public Criteria andVersionGreaterThan(String value) {
            addCriterion("version >", value, "version");
            return this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(String value) {
            addCriterion("version >=", value, "version");
            return this;
        }

        public Criteria andVersionLessThan(String value) {
            addCriterion("version <", value, "version");
            return this;
        }

        public Criteria andVersionLessThanOrEqualTo(String value) {
            addCriterion("version <=", value, "version");
            return this;
        }

        public Criteria andVersionLike(String value) {
            addCriterion("version like", value, "version");
            return this;
        }

        public Criteria andVersionNotLike(String value) {
            addCriterion("version not like", value, "version");
            return this;
        }

        public Criteria andVersionIn(List<String> values) {
            addCriterion("version in", values, "version");
            return this;
        }

        public Criteria andVersionNotIn(List<String> values) {
            addCriterion("version not in", values, "version");
            return this;
        }

        public Criteria andVersionBetween(String value1, String value2) {
            addCriterion("version between", value1, value2, "version");
            return this;
        }

        public Criteria andVersionNotBetween(String value1, String value2) {
            addCriterion("version not between", value1, value2, "version");
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

        public Criteria andModifyTimeEqualTo(Date value) {
            addCriterion("modify_time =", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotEqualTo(Date value) {
            addCriterion("modify_time <>", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeGreaterThan(Date value) {
            addCriterion("modify_time >", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("modify_time >=", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeLessThan(Date value) {
            addCriterion("modify_time <", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeLessThanOrEqualTo(Date value) {
            addCriterion("modify_time <=", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeIn(List<Date> values) {
            addCriterion("modify_time in", values, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotIn(List<Date> values) {
            addCriterion("modify_time not in", values, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeBetween(Date value1, Date value2) {
            addCriterion("modify_time between", value1, value2, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotBetween(Date value1, Date value2) {
            addCriterion("modify_time not between", value1, value2, "modifyTime");
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

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return this;
        }

        public Criteria andRuntimeIsNull() {
            addCriterion("runtime is null");
            return this;
        }

        public Criteria andRuntimeIsNotNull() {
            addCriterion("runtime is not null");
            return this;
        }

        public Criteria andRuntimeEqualTo(String value) {
            addCriterion("runtime =", value, " runtime");
            return this;
        }

        public Criteria andRuntimeNotEqualTo(String value) {
            addCriterion("runtime <>", value, "runtime");
            return this;
        }

        public Criteria andRuntimeGreaterThan(String value) {
            addCriterion("runtime >", value, "runtime");
            return this;
        }

        public Criteria andRuntimeGreaterThanOrEqualTo(String value) {
            addCriterion("runtime >=", value, "runtime");
            return this;
        }

        public Criteria andRuntimeLessThan(String value) {
            addCriterion("runtime <", value, "runtime");
            return this;
        }

        public Criteria andRuntimeLessThanOrEqualTo(String value) {
            addCriterion("runtime <=", value, "runtime");
            return this;
        }

        public Criteria andRuntimeLike(String value) {
            addCriterion("runtime like", value, "runtime");
            return this;
        }

        public Criteria andRuntimeNotLike(String value) {
            addCriterion("runtime not like", value, "runtime");
            return this;
        }

        public Criteria andRuntimeIn(List<String> values) {
            addCriterion("runtime in", values, "runtime");
            return this;
        }

        public Criteria andRuntimeNotIn(List<String> values) {
            addCriterion("runtime not in", values, "runtime");
            return this;
        }

        public Criteria andRuntimeBetween(String value1, String value2) {
            addCriterion("runtime between", value1, value2, "runtime");
            return this;
        }

        public Criteria andRuntimeNotBetween(String value1, String value2) {
            addCriterion("runtime not between", value1, value2, "runtime");
            return this;
        }
    }
}
