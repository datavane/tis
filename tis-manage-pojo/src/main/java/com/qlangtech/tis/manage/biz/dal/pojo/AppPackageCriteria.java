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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.qlangtech.tis.ibatis.BasicCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.ApplicationCriteria.Criteria;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class AppPackageCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    public AppPackageCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected AppPackageCriteria(AppPackageCriteria example) {
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

        public Criteria andPidIsNull() {
            addCriterion("pid is null");
            return this;
        }

        // baisui add has not been delete start
        public Criteria andNotDelete() {
            addCriterion("app_package.is_deleted =", "N", "app_package.is_deleted");
            return this;
        }

        // baisui add has not been delete end
        public Criteria andPidIsNotNull() {
            addCriterion("pid is not null");
            return this;
        }

        public Criteria andPidEqualTo(Integer value) {
            addCriterion("pid =", value, "pid");
            return this;
        }

        public Criteria andPidNotEqualTo(Integer value) {
            addCriterion("pid <>", value, "pid");
            return this;
        }

        public Criteria andPidGreaterThan(Integer value) {
            addCriterion("pid >", value, "pid");
            return this;
        }

        public Criteria andPidGreaterThanOrEqualTo(Integer value) {
            addCriterion("pid >=", value, "pid");
            return this;
        }

        public Criteria andPidLessThan(Integer value) {
            addCriterion("pid <", value, "pid");
            return this;
        }

        public Criteria andPidLessThanOrEqualTo(Integer value) {
            addCriterion("pid <=", value, "pid");
            return this;
        }

        public Criteria andPidIn(List<Integer> values) {
            addCriterion("pid in", values, "pid");
            return this;
        }

        public Criteria andPidNotIn(List<Integer> values) {
            addCriterion("pid not in", values, "pid");
            return this;
        }

        public Criteria andPidBetween(Integer value1, Integer value2) {
            addCriterion("pid between", value1, value2, "pid");
            return this;
        }

        public Criteria andPidNotBetween(Integer value1, Integer value2) {
            addCriterion("pid not between", value1, value2, "pid");
            return this;
        }

        public Criteria andAppIdIsNull() {
            addCriterion("app_id is null");
            return this;
        }

        public Criteria andAppIdIsNotNull() {
            addCriterion("app_id is not null");
            return this;
        }

        public Criteria andAppIdEqualTo(Integer value) {
            addCriterion("app_id =", value, "appId");
            return this;
        }

        public Criteria andAppIdNotEqualTo(Integer value) {
            addCriterion("app_id <>", value, "appId");
            return this;
        }

        public Criteria andAppIdGreaterThan(Integer value) {
            addCriterion("app_id >", value, "appId");
            return this;
        }

        public Criteria andAppIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("app_id >=", value, "appId");
            return this;
        }

        public Criteria andAppIdLessThan(Integer value) {
            addCriterion("app_id <", value, "appId");
            return this;
        }

        public Criteria andAppIdLessThanOrEqualTo(Integer value) {
            addCriterion("app_id <=", value, "appId");
            return this;
        }

        public Criteria andAppIdIn(List<Integer> values) {
            addCriterion("app_id in", values, "appId");
            return this;
        }

        public Criteria andAppIdNotIn(List<Integer> values) {
            addCriterion("app_id not in", values, "appId");
            return this;
        }

        public Criteria andAppIdBetween(Integer value1, Integer value2) {
            addCriterion("app_id between", value1, value2, "appId");
            return this;
        }

        public Criteria andAppIdNotBetween(Integer value1, Integer value2) {
            addCriterion("app_id not between", value1, value2, "appId");
            return this;
        }

        public Criteria andUploadUserIsNull() {
            addCriterion("upload_user is null");
            return this;
        }

        public Criteria andUploadUserIsNotNull() {
            addCriterion("upload_user is not null");
            return this;
        }

        public Criteria andUploadUserEqualTo(String value) {
            addCriterion("upload_user =", value, "uploadUser");
            return this;
        }

        public Criteria andUploadUserNotEqualTo(String value) {
            addCriterion("upload_user <>", value, "uploadUser");
            return this;
        }

        public Criteria andUploadUserGreaterThan(String value) {
            addCriterion("upload_user >", value, "uploadUser");
            return this;
        }

        public Criteria andUploadUserGreaterThanOrEqualTo(String value) {
            addCriterion("upload_user >=", value, "uploadUser");
            return this;
        }

        public Criteria andUploadUserLessThan(String value) {
            addCriterion("upload_user <", value, "uploadUser");
            return this;
        }

        public Criteria andUploadUserLessThanOrEqualTo(String value) {
            addCriterion("upload_user <=", value, "uploadUser");
            return this;
        }

        public Criteria andUploadUserLike(String value) {
            addCriterion("upload_user like", value, "uploadUser");
            return this;
        }

        public Criteria andUploadUserNotLike(String value) {
            addCriterion("upload_user not like", value, "uploadUser");
            return this;
        }

        public Criteria andUploadUserIn(List<String> values) {
            addCriterion("upload_user in", values, "uploadUser");
            return this;
        }

        public Criteria andUploadUserNotIn(List<String> values) {
            addCriterion("upload_user not in", values, "uploadUser");
            return this;
        }

        public Criteria andUploadUserBetween(String value1, String value2) {
            addCriterion("upload_user between", value1, value2, "uploadUser");
            return this;
        }

        public Criteria andUploadUserNotBetween(String value1, String value2) {
            addCriterion("upload_user not between", value1, value2, "uploadUser");
            return this;
        }

        public Criteria andTestStatusIsNull() {
            addCriterion("test_status is null");
            return this;
        }

        public Criteria andTestStatusIsNotNull() {
            addCriterion("test_status is not null");
            return this;
        }

        public Criteria andTestStatusEqualTo(Short value) {
            addCriterion("test_status =", value, "testStatus");
            return this;
        }

        public Criteria andTestStatusNotEqualTo(Short value) {
            addCriterion("test_status <>", value, "testStatus");
            return this;
        }

        public Criteria andTestStatusGreaterThan(Short value) {
            addCriterion("test_status >", value, "testStatus");
            return this;
        }

        public Criteria andTestStatusGreaterThanOrEqualTo(Short value) {
            addCriterion("test_status >=", value, "testStatus");
            return this;
        }

        public Criteria andTestStatusLessThan(Short value) {
            addCriterion("test_status <", value, "testStatus");
            return this;
        }

        public Criteria andTestStatusLessThanOrEqualTo(Short value) {
            addCriterion("test_status <=", value, "testStatus");
            return this;
        }

        public Criteria andTestStatusIn(List<Short> values) {
            addCriterion("test_status in", values, "testStatus");
            return this;
        }

        public Criteria andTestStatusNotIn(List<Short> values) {
            addCriterion("test_status not in", values, "testStatus");
            return this;
        }

        public Criteria andTestStatusBetween(Short value1, Short value2) {
            addCriterion("test_status between", value1, value2, "testStatus");
            return this;
        }

        public Criteria andTestStatusNotBetween(Short value1, Short value2) {
            addCriterion("test_status not between", value1, value2, "testStatus");
            return this;
        }

        public Criteria andLastTestTimeIsNull() {
            addCriterion("last_test_time is null");
            return this;
        }

        public Criteria andLastTestTimeIsNotNull() {
            addCriterion("last_test_time is not null");
            return this;
        }

        public Criteria andLastTestTimeEqualTo(Date value) {
            addCriterion("last_test_time =", value, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeNotEqualTo(Date value) {
            addCriterion("last_test_time <>", value, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeGreaterThan(Date value) {
            addCriterion("last_test_time >", value, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("last_test_time >=", value, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeLessThan(Date value) {
            addCriterion("last_test_time <", value, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeLessThanOrEqualTo(Date value) {
            addCriterion("last_test_time <=", value, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeIn(List<Date> values) {
            addCriterion("last_test_time in", values, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeNotIn(List<Date> values) {
            addCriterion("last_test_time not in", values, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeBetween(Date value1, Date value2) {
            addCriterion("last_test_time between", value1, value2, "lastTestTime");
            return this;
        }

        public Criteria andLastTestTimeNotBetween(Date value1, Date value2) {
            addCriterion("last_test_time not between", value1, value2, "lastTestTime");
            return this;
        }

        public Criteria andLastTerUserIdIsNull() {
            addCriterion("last_ter_user_id is null");
            return this;
        }

        public Criteria andLastTerUserIdIsNotNull() {
            addCriterion("last_ter_user_id is not null");
            return this;
        }

        public Criteria andLastTerUserIdEqualTo(Integer value) {
            addCriterion("last_ter_user_id =", value, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdNotEqualTo(Integer value) {
            addCriterion("last_ter_user_id <>", value, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdGreaterThan(Integer value) {
            addCriterion("last_ter_user_id >", value, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("last_ter_user_id >=", value, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdLessThan(Integer value) {
            addCriterion("last_ter_user_id <", value, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdLessThanOrEqualTo(Integer value) {
            addCriterion("last_ter_user_id <=", value, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdIn(List<Integer> values) {
            addCriterion("last_ter_user_id in", values, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdNotIn(List<Integer> values) {
            addCriterion("last_ter_user_id not in", values, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdBetween(Integer value1, Integer value2) {
            addCriterion("last_ter_user_id between", value1, value2, "lastTerUserId");
            return this;
        }

        public Criteria andLastTerUserIdNotBetween(Integer value1, Integer value2) {
            addCriterion("last_ter_user_id not between", value1, value2, "lastTerUserId");
            return this;
        }

        public Criteria andLastTestUserIsNull() {
            addCriterion("last_test_user is null");
            return this;
        }

        public Criteria andLastTestUserIsNotNull() {
            addCriterion("last_test_user is not null");
            return this;
        }

        public Criteria andLastTestUserEqualTo(String value) {
            addCriterion("last_test_user =", value, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserNotEqualTo(String value) {
            addCriterion("last_test_user <>", value, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserGreaterThan(String value) {
            addCriterion("last_test_user >", value, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserGreaterThanOrEqualTo(String value) {
            addCriterion("last_test_user >=", value, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserLessThan(String value) {
            addCriterion("last_test_user <", value, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserLessThanOrEqualTo(String value) {
            addCriterion("last_test_user <=", value, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserLike(String value) {
            addCriterion("last_test_user like", value, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserNotLike(String value) {
            addCriterion("last_test_user not like", value, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserIn(List<String> values) {
            addCriterion("last_test_user in", values, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserNotIn(List<String> values) {
            addCriterion("last_test_user not in", values, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserBetween(String value1, String value2) {
            addCriterion("last_test_user between", value1, value2, "lastTestUser");
            return this;
        }

        public Criteria andLastTestUserNotBetween(String value1, String value2) {
            addCriterion("last_test_user not between", value1, value2, "lastTestUser");
            return this;
        }

        public Criteria andSuccessSnapshotIdIsNull() {
            addCriterion("success_snapshot_id is null");
            return this;
        }

        public Criteria andSuccessSnapshotIdIsNotNull() {
            addCriterion("success_snapshot_id is not null");
            return this;
        }

        public Criteria andSuccessSnapshotIdEqualTo(Integer value) {
            addCriterion("success_snapshot_id =", value, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdNotEqualTo(Integer value) {
            addCriterion("success_snapshot_id <>", value, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdGreaterThan(Integer value) {
            addCriterion("success_snapshot_id >", value, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("success_snapshot_id >=", value, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdLessThan(Integer value) {
            addCriterion("success_snapshot_id <", value, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdLessThanOrEqualTo(Integer value) {
            addCriterion("success_snapshot_id <=", value, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdIn(List<Integer> values) {
            addCriterion("success_snapshot_id in", values, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdNotIn(List<Integer> values) {
            addCriterion("success_snapshot_id not in", values, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdBetween(Integer value1, Integer value2) {
            addCriterion("success_snapshot_id between", value1, value2, "successSnapshotId");
            return this;
        }

        public Criteria andSuccessSnapshotIdNotBetween(Integer value1, Integer value2) {
            addCriterion("success_snapshot_id not between", value1, value2, "successSnapshotId");
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

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return this;
        }

        public Criteria andRuntEnvironmentIsNull() {
            addCriterion("runt_environment is null");
            return this;
        }

        public Criteria andRuntEnvironmentIsNotNull() {
            addCriterion("runt_environment is not null");
            return this;
        }

        public Criteria andRuntEnvironmentEqualTo(Short value) {
            addCriterion("runt_environment =", value, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentNotEqualTo(Short value) {
            addCriterion("runt_environment <>", value, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentGreaterThan(Short value) {
            addCriterion("runt_environment >", value, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentGreaterThanOrEqualTo(Short value) {
            addCriterion("runt_environment >=", value, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentLessThan(Short value) {
            addCriterion("runt_environment <", value, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentLessThanOrEqualTo(Short value) {
            addCriterion("runt_environment <=", value, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentIn(List<Short> values) {
            addCriterion("runt_environment in", values, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentNotIn(List<Short> values) {
            addCriterion("runt_environment not in", values, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentBetween(Short value1, Short value2) {
            addCriterion("runt_environment between", value1, value2, "runtEnvironment");
            return this;
        }

        public Criteria andRuntEnvironmentNotBetween(Short value1, Short value2) {
            addCriterion("runt_environment not between", value1, value2, "runtEnvironment");
            return this;
        }
    }
}
