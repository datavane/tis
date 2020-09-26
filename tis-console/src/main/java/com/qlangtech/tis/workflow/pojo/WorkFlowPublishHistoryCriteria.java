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
package com.qlangtech.tis.workflow.pojo;

import com.qlangtech.tis.ibatis.BasicCriteria;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class WorkFlowPublishHistoryCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    public WorkFlowPublishHistoryCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected WorkFlowPublishHistoryCriteria(WorkFlowPublishHistoryCriteria example) {
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

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
            addCriterion("id not between", value1, value2, "id");
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

        public Criteria andOpUserIdIsNull() {
            addCriterion("op_user_id is null");
            return this;
        }

        public Criteria andOpUserIdIsNotNull() {
            addCriterion("op_user_id is not null");
            return this;
        }

        public Criteria andOpUserIdEqualTo(Integer value) {
            addCriterion("op_user_id =", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotEqualTo(Integer value) {
            addCriterion("op_user_id <>", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdGreaterThan(Integer value) {
            addCriterion("op_user_id >", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("op_user_id >=", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdLessThan(Integer value) {
            addCriterion("op_user_id <", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdLessThanOrEqualTo(Integer value) {
            addCriterion("op_user_id <=", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdIn(List<Integer> values) {
            addCriterion("op_user_id in", values, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotIn(List<Integer> values) {
            addCriterion("op_user_id not in", values, "opUserId");
            return this;
        }

        public Criteria andOpUserIdBetween(Integer value1, Integer value2) {
            addCriterion("op_user_id between", value1, value2, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotBetween(Integer value1, Integer value2) {
            addCriterion("op_user_id not between", value1, value2, "opUserId");
            return this;
        }

        public Criteria andOpUserNameIsNull() {
            addCriterion("op_user_name is null");
            return this;
        }

        public Criteria andOpUserNameIsNotNull() {
            addCriterion("op_user_name is not null");
            return this;
        }

        public Criteria andOpUserNameEqualTo(String value) {
            addCriterion("op_user_name =", value, "opUserName");
            return this;
        }

        public Criteria andOpUserNameNotEqualTo(String value) {
            addCriterion("op_user_name <>", value, "opUserName");
            return this;
        }

        public Criteria andOpUserNameGreaterThan(String value) {
            addCriterion("op_user_name >", value, "opUserName");
            return this;
        }

        public Criteria andOpUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("op_user_name >=", value, "opUserName");
            return this;
        }

        public Criteria andOpUserNameLessThan(String value) {
            addCriterion("op_user_name <", value, "opUserName");
            return this;
        }

        public Criteria andOpUserNameLessThanOrEqualTo(String value) {
            addCriterion("op_user_name <=", value, "opUserName");
            return this;
        }

        public Criteria andOpUserNameLike(String value) {
            addCriterion("op_user_name like", value, "opUserName");
            return this;
        }

        public Criteria andOpUserNameNotLike(String value) {
            addCriterion("op_user_name not like", value, "opUserName");
            return this;
        }

        public Criteria andOpUserNameIn(List<String> values) {
            addCriterion("op_user_name in", values, "opUserName");
            return this;
        }

        public Criteria andOpUserNameNotIn(List<String> values) {
            addCriterion("op_user_name not in", values, "opUserName");
            return this;
        }

        public Criteria andOpUserNameBetween(String value1, String value2) {
            addCriterion("op_user_name between", value1, value2, "opUserName");
            return this;
        }

        public Criteria andOpUserNameNotBetween(String value1, String value2) {
            addCriterion("op_user_name not between", value1, value2, "opUserName");
            return this;
        }

        public Criteria andWorkflowIdIsNull() {
            addCriterion("workflow_id is null");
            return this;
        }

        public Criteria andWorkflowIdIsNotNull() {
            addCriterion("workflow_id is not null");
            return this;
        }

        public Criteria andWorkflowIdEqualTo(Integer value) {
            addCriterion("workflow_id =", value, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdNotEqualTo(Integer value) {
            addCriterion("workflow_id <>", value, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdGreaterThan(Integer value) {
            addCriterion("workflow_id >", value, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("workflow_id >=", value, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdLessThan(Integer value) {
            addCriterion("workflow_id <", value, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdLessThanOrEqualTo(Integer value) {
            addCriterion("workflow_id <=", value, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdIn(List<Integer> values) {
            addCriterion("workflow_id in", values, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdNotIn(List<Integer> values) {
            addCriterion("workflow_id not in", values, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdBetween(Integer value1, Integer value2) {
            addCriterion("workflow_id between", value1, value2, "workflowId");
            return this;
        }

        public Criteria andWorkflowIdNotBetween(Integer value1, Integer value2) {
            addCriterion("workflow_id not between", value1, value2, "workflowId");
            return this;
        }

        public Criteria andWorkflowNameIsNull() {
            addCriterion("workflow_name is null");
            return this;
        }

        public Criteria andWorkflowNameIsNotNull() {
            addCriterion("workflow_name is not null");
            return this;
        }

        public Criteria andWorkflowNameEqualTo(String value) {
            addCriterion("workflow_name =", value, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameNotEqualTo(String value) {
            addCriterion("workflow_name <>", value, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameGreaterThan(String value) {
            addCriterion("workflow_name >", value, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameGreaterThanOrEqualTo(String value) {
            addCriterion("workflow_name >=", value, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameLessThan(String value) {
            addCriterion("workflow_name <", value, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameLessThanOrEqualTo(String value) {
            addCriterion("workflow_name <=", value, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameLike(String value) {
            addCriterion("workflow_name like", value, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameNotLike(String value) {
            addCriterion("workflow_name not like", value, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameIn(List<String> values) {
            addCriterion("workflow_name in", values, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameNotIn(List<String> values) {
            addCriterion("workflow_name not in", values, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameBetween(String value1, String value2) {
            addCriterion("workflow_name between", value1, value2, "workflowName");
            return this;
        }

        public Criteria andWorkflowNameNotBetween(String value1, String value2) {
            addCriterion("workflow_name not between", value1, value2, "workflowName");
            return this;
        }

        public Criteria andPublishStateIsNull() {
            addCriterion("publish_state is null");
            return this;
        }

        public Criteria andPublishStateIsNotNull() {
            addCriterion("publish_state is not null");
            return this;
        }

        public Criteria andPublishStateEqualTo(Byte value) {
            addCriterion("publish_state =", value, "publishState");
            return this;
        }

        public Criteria andPublishStateNotEqualTo(Byte value) {
            addCriterion("publish_state <>", value, "publishState");
            return this;
        }

        public Criteria andPublishStateGreaterThan(Byte value) {
            addCriterion("publish_state >", value, "publishState");
            return this;
        }

        public Criteria andPublishStateGreaterThanOrEqualTo(Byte value) {
            addCriterion("publish_state >=", value, "publishState");
            return this;
        }

        public Criteria andPublishStateLessThan(Byte value) {
            addCriterion("publish_state <", value, "publishState");
            return this;
        }

        public Criteria andPublishStateLessThanOrEqualTo(Byte value) {
            addCriterion("publish_state <=", value, "publishState");
            return this;
        }

        public Criteria andPublishStateIn(List<Byte> values) {
            addCriterion("publish_state in", values, "publishState");
            return this;
        }

        public Criteria andPublishStateNotIn(List<Byte> values) {
            addCriterion("publish_state not in", values, "publishState");
            return this;
        }

        public Criteria andPublishStateBetween(Byte value1, Byte value2) {
            addCriterion("publish_state between", value1, value2, "publishState");
            return this;
        }

        public Criteria andPublishStateNotBetween(Byte value1, Byte value2) {
            addCriterion("publish_state not between", value1, value2, "publishState");
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

        public Criteria andTypeEqualTo(Byte value) {
            addCriterion("type =", value, "type");
            return this;
        }

        public Criteria andTypeNotEqualTo(Byte value) {
            addCriterion("type <>", value, "type");
            return this;
        }

        public Criteria andTypeGreaterThan(Byte value) {
            addCriterion("type >", value, "type");
            return this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("type >=", value, "type");
            return this;
        }

        public Criteria andTypeLessThan(Byte value) {
            addCriterion("type <", value, "type");
            return this;
        }

        public Criteria andTypeLessThanOrEqualTo(Byte value) {
            addCriterion("type <=", value, "type");
            return this;
        }

        public Criteria andTypeIn(List<Byte> values) {
            addCriterion("type in", values, "type");
            return this;
        }

        public Criteria andTypeNotIn(List<Byte> values) {
            addCriterion("type not in", values, "type");
            return this;
        }

        public Criteria andTypeBetween(Byte value1, Byte value2) {
            addCriterion("type between", value1, value2, "type");
            return this;
        }

        public Criteria andTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("type not between", value1, value2, "type");
            return this;
        }

        public Criteria andGitSha1IsNull() {
            addCriterion("git_sha1 is null");
            return this;
        }

        public Criteria andGitSha1IsNotNull() {
            addCriterion("git_sha1 is not null");
            return this;
        }

        public Criteria andGitSha1EqualTo(String value) {
            addCriterion("git_sha1 =", value, "gitSha1");
            return this;
        }

        public Criteria andGitSha1NotEqualTo(String value) {
            addCriterion("git_sha1 <>", value, "gitSha1");
            return this;
        }

        public Criteria andGitSha1GreaterThan(String value) {
            addCriterion("git_sha1 >", value, "gitSha1");
            return this;
        }

        public Criteria andGitSha1GreaterThanOrEqualTo(String value) {
            addCriterion("git_sha1 >=", value, "gitSha1");
            return this;
        }

        public Criteria andGitSha1LessThan(String value) {
            addCriterion("git_sha1 <", value, "gitSha1");
            return this;
        }

        public Criteria andGitSha1LessThanOrEqualTo(String value) {
            addCriterion("git_sha1 <=", value, "gitSha1");
            return this;
        }

        public Criteria andGitSha1Like(String value) {
            addCriterion("git_sha1 like", value, "gitSha1");
            return this;
        }

        public Criteria andGitSha1NotLike(String value) {
            addCriterion("git_sha1 not like", value, "gitSha1");
            return this;
        }

        public Criteria andGitSha1In(List<String> values) {
            addCriterion("git_sha1 in", values, "gitSha1");
            return this;
        }

        public Criteria andGitSha1NotIn(List<String> values) {
            addCriterion("git_sha1 not in", values, "gitSha1");
            return this;
        }

        public Criteria andGitSha1Between(String value1, String value2) {
            addCriterion("git_sha1 between", value1, value2, "gitSha1");
            return this;
        }

        public Criteria andGitSha1NotBetween(String value1, String value2) {
            addCriterion("git_sha1 not between", value1, value2, "gitSha1");
            return this;
        }

        public Criteria andInUseIsNull() {
            addCriterion("in_use is null");
            return this;
        }

        public Criteria andInUseIsNotNull() {
            addCriterion("in_use is not null");
            return this;
        }

        public Criteria andInUseEqualTo(Boolean value) {
            addCriterion("in_use =", value, "inUse");
            return this;
        }

        public Criteria andInUseNotEqualTo(Boolean value) {
            addCriterion("in_use <>", value, "inUse");
            return this;
        }

        public Criteria andInUseGreaterThan(Boolean value) {
            addCriterion("in_use >", value, "inUse");
            return this;
        }

        public Criteria andInUseGreaterThanOrEqualTo(Boolean value) {
            addCriterion("in_use >=", value, "inUse");
            return this;
        }

        public Criteria andInUseLessThan(Boolean value) {
            addCriterion("in_use <", value, "inUse");
            return this;
        }

        public Criteria andInUseLessThanOrEqualTo(Boolean value) {
            addCriterion("in_use <=", value, "inUse");
            return this;
        }

        public Criteria andInUseIn(List<Boolean> values) {
            addCriterion("in_use in", values, "inUse");
            return this;
        }

        public Criteria andInUseNotIn(List<Boolean> values) {
            addCriterion("in_use not in", values, "inUse");
            return this;
        }

        public Criteria andInUseBetween(Boolean value1, Boolean value2) {
            addCriterion("in_use between", value1, value2, "inUse");
            return this;
        }

        public Criteria andInUseNotBetween(Boolean value1, Boolean value2) {
            addCriterion("in_use not between", value1, value2, "inUse");
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

        public Criteria andOpTimeEqualTo(Date value) {
            addCriterion("op_time =", value, "opTime");
            return this;
        }

        public Criteria andOpTimeNotEqualTo(Date value) {
            addCriterion("op_time <>", value, "opTime");
            return this;
        }

        public Criteria andOpTimeGreaterThan(Date value) {
            addCriterion("op_time >", value, "opTime");
            return this;
        }

        public Criteria andOpTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("op_time >=", value, "opTime");
            return this;
        }

        public Criteria andOpTimeLessThan(Date value) {
            addCriterion("op_time <", value, "opTime");
            return this;
        }

        public Criteria andOpTimeLessThanOrEqualTo(Date value) {
            addCriterion("op_time <=", value, "opTime");
            return this;
        }

        public Criteria andOpTimeIn(List<Date> values) {
            addCriterion("op_time in", values, "opTime");
            return this;
        }

        public Criteria andOpTimeNotIn(List<Date> values) {
            addCriterion("op_time not in", values, "opTime");
            return this;
        }

        public Criteria andOpTimeBetween(Date value1, Date value2) {
            addCriterion("op_time between", value1, value2, "opTime");
            return this;
        }

        public Criteria andOpTimeNotBetween(Date value1, Date value2) {
            addCriterion("op_time not between", value1, value2, "opTime");
            return this;
        }
    }
}
