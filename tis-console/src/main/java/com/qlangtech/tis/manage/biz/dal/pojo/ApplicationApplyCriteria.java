/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.manage.biz.dal.pojo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ApplicationApplyCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    public ApplicationApplyCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected ApplicationApplyCriteria(ApplicationApplyCriteria example) {
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

        public Criteria andCreateUsrIdEqualTo(String value) {
            addCriterion("create_usr_id =", value, "createUsrId");
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

        public Criteria andProjectNameIsNull() {
            addCriterion("project_name is null");
            return this;
        }

        public Criteria andProjectNameIsNotNull() {
            addCriterion("project_name is not null");
            return this;
        }

        public Criteria andProjectNameEqualTo(String value) {
            addCriterion("project_name =", value, "projectName");
            return this;
        }

        public Criteria andProjectNameNotEqualTo(String value) {
            addCriterion("project_name <>", value, "projectName");
            return this;
        }

        public Criteria andProjectNameGreaterThan(String value) {
            addCriterion("project_name >", value, "projectName");
            return this;
        }

        public Criteria andProjectNameGreaterThanOrEqualTo(String value) {
            addCriterion("project_name >=", value, "projectName");
            return this;
        }

        public Criteria andProjectNameLessThan(String value) {
            addCriterion("project_name <", value, "projectName");
            return this;
        }

        public Criteria andProjectNameLessThanOrEqualTo(String value) {
            addCriterion("project_name <=", value, "projectName");
            return this;
        }

        public Criteria andProjectNameLike(String value) {
            addCriterion("project_name like", value, "projectName");
            return this;
        }

        public Criteria andProjectNameNotLike(String value) {
            addCriterion("project_name not like", value, "projectName");
            return this;
        }

        public Criteria andProjectNameIn(List<String> values) {
            addCriterion("project_name in", values, "projectName");
            return this;
        }

        public Criteria andProjectNameNotIn(List<String> values) {
            addCriterion("project_name not in", values, "projectName");
            return this;
        }

        public Criteria andProjectNameBetween(String value1, String value2) {
            addCriterion("project_name between", value1, value2, "projectName");
            return this;
        }

        public Criteria andProjectNameNotBetween(String value1, String value2) {
            addCriterion("project_name not between", value1, value2, "projectName");
            return this;
        }

        public Criteria andReceptIsNull() {
            addCriterion("recept is null");
            return this;
        }

        public Criteria andReceptIsNotNull() {
            addCriterion("recept is not null");
            return this;
        }

        public Criteria andReceptEqualTo(String value) {
            addCriterion("recept =", value, "recept");
            return this;
        }

        public Criteria andReceptNotEqualTo(String value) {
            addCriterion("recept <>", value, "recept");
            return this;
        }

        public Criteria andReceptGreaterThan(String value) {
            addCriterion("recept >", value, "recept");
            return this;
        }

        public Criteria andReceptGreaterThanOrEqualTo(String value) {
            addCriterion("recept >=", value, "recept");
            return this;
        }

        public Criteria andReceptLessThan(String value) {
            addCriterion("recept <", value, "recept");
            return this;
        }

        public Criteria andReceptLessThanOrEqualTo(String value) {
            addCriterion("recept <=", value, "recept");
            return this;
        }

        public Criteria andReceptLike(String value) {
            addCriterion("recept like", value, "recept");
            return this;
        }

        public Criteria andReceptNotLike(String value) {
            addCriterion("recept not like", value, "recept");
            return this;
        }

        public Criteria andReceptIn(List<String> values) {
            addCriterion("recept in", values, "recept");
            return this;
        }

        public Criteria andReceptNotIn(List<String> values) {
            addCriterion("recept not in", values, "recept");
            return this;
        }

        public Criteria andReceptBetween(String value1, String value2) {
            addCriterion("recept between", value1, value2, "recept");
            return this;
        }

        public Criteria andReceptNotBetween(String value1, String value2) {
            addCriterion("recept not between", value1, value2, "recept");
            return this;
        }

        public Criteria andManagerIsNull() {
            addCriterion("manager is null");
            return this;
        }

        public Criteria andManagerIsNotNull() {
            addCriterion("manager is not null");
            return this;
        }

        public Criteria andManagerEqualTo(String value) {
            addCriterion("manager =", value, "manager");
            return this;
        }

        public Criteria andManagerNotEqualTo(String value) {
            addCriterion("manager <>", value, "manager");
            return this;
        }

        public Criteria andManagerGreaterThan(String value) {
            addCriterion("manager >", value, "manager");
            return this;
        }

        public Criteria andManagerGreaterThanOrEqualTo(String value) {
            addCriterion("manager >=", value, "manager");
            return this;
        }

        public Criteria andManagerLessThan(String value) {
            addCriterion("manager <", value, "manager");
            return this;
        }

        public Criteria andManagerLessThanOrEqualTo(String value) {
            addCriterion("manager <=", value, "manager");
            return this;
        }

        public Criteria andManagerLike(String value) {
            addCriterion("manager like", value, "manager");
            return this;
        }

        public Criteria andManagerNotLike(String value) {
            addCriterion("manager not like", value, "manager");
            return this;
        }

        public Criteria andManagerIn(List<String> values) {
            addCriterion("manager in", values, "manager");
            return this;
        }

        public Criteria andManagerNotIn(List<String> values) {
            addCriterion("manager not in", values, "manager");
            return this;
        }

        public Criteria andManagerBetween(String value1, String value2) {
            addCriterion("manager between", value1, value2, "manager");
            return this;
        }

        public Criteria andManagerNotBetween(String value1, String value2) {
            addCriterion("manager not between", value1, value2, "manager");
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

        public Criteria andIsAutoDeployIsNull() {
            addCriterion("is_auto_deploy is null");
            return this;
        }

        public Criteria andIsAutoDeployIsNotNull() {
            addCriterion("is_auto_deploy is not null");
            return this;
        }

        public Criteria andIsAutoDeployEqualTo(String value) {
            addCriterion("is_auto_deploy =", value, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployNotEqualTo(String value) {
            addCriterion("is_auto_deploy <>", value, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployGreaterThan(String value) {
            addCriterion("is_auto_deploy >", value, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployGreaterThanOrEqualTo(String value) {
            addCriterion("is_auto_deploy >=", value, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployLessThan(String value) {
            addCriterion("is_auto_deploy <", value, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployLessThanOrEqualTo(String value) {
            addCriterion("is_auto_deploy <=", value, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployLike(String value) {
            addCriterion("is_auto_deploy like", value, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployNotLike(String value) {
            addCriterion("is_auto_deploy not like", value, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployIn(List<String> values) {
            addCriterion("is_auto_deploy in", values, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployNotIn(List<String> values) {
            addCriterion("is_auto_deploy not in", values, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployBetween(String value1, String value2) {
            addCriterion("is_auto_deploy between", value1, value2, "isAutoDeploy");
            return this;
        }

        public Criteria andIsAutoDeployNotBetween(String value1, String value2) {
            addCriterion("is_auto_deploy not between", value1, value2, "isAutoDeploy");
            return this;
        }

        public Criteria andDptIdIsNull() {
            addCriterion("dpt_id is null");
            return this;
        }

        public Criteria andDptIdIsNotNull() {
            addCriterion("dpt_id is not null");
            return this;
        }

        public Criteria andDptIdEqualTo(Integer value) {
            addCriterion("dpt_id =", value, "dptId");
            return this;
        }

        public Criteria andDptIdNotEqualTo(Integer value) {
            addCriterion("dpt_id <>", value, "dptId");
            return this;
        }

        public Criteria andDptIdGreaterThan(Integer value) {
            addCriterion("dpt_id >", value, "dptId");
            return this;
        }

        public Criteria andDptIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("dpt_id >=", value, "dptId");
            return this;
        }

        public Criteria andDptIdLessThan(Integer value) {
            addCriterion("dpt_id <", value, "dptId");
            return this;
        }

        public Criteria andDptIdLessThanOrEqualTo(Integer value) {
            addCriterion("dpt_id <=", value, "dptId");
            return this;
        }

        public Criteria andDptIdIn(List<Integer> values) {
            addCriterion("dpt_id in", values, "dptId");
            return this;
        }

        public Criteria andDptIdNotIn(List<Integer> values) {
            addCriterion("dpt_id not in", values, "dptId");
            return this;
        }

        public Criteria andDptIdBetween(Integer value1, Integer value2) {
            addCriterion("dpt_id between", value1, value2, "dptId");
            return this;
        }

        public Criteria andDptIdNotBetween(Integer value1, Integer value2) {
            addCriterion("dpt_id not between", value1, value2, "dptId");
            return this;
        }

        public Criteria andDptNameIsNull() {
            addCriterion("dpt_name is null");
            return this;
        }

        public Criteria andDptNameIsNotNull() {
            addCriterion("dpt_name is not null");
            return this;
        }

        public Criteria andDptNameEqualTo(String value) {
            addCriterion("dpt_name =", value, "dptName");
            return this;
        }

        public Criteria andDptNameNotEqualTo(String value) {
            addCriterion("dpt_name <>", value, "dptName");
            return this;
        }

        public Criteria andDptNameGreaterThan(String value) {
            addCriterion("dpt_name >", value, "dptName");
            return this;
        }

        public Criteria andDptNameGreaterThanOrEqualTo(String value) {
            addCriterion("dpt_name >=", value, "dptName");
            return this;
        }

        public Criteria andDptNameLessThan(String value) {
            addCriterion("dpt_name <", value, "dptName");
            return this;
        }

        public Criteria andDptNameLessThanOrEqualTo(String value) {
            addCriterion("dpt_name <=", value, "dptName");
            return this;
        }

        public Criteria andDptNameLike(String value) {
            addCriterion("dpt_name like", value, "dptName");
            return this;
        }

        public Criteria andDptNameNotLike(String value) {
            addCriterion("dpt_name not like", value, "dptName");
            return this;
        }

        public Criteria andDptNameIn(List<String> values) {
            addCriterion("dpt_name in", values, "dptName");
            return this;
        }

        public Criteria andDptNameNotIn(List<String> values) {
            addCriterion("dpt_name not in", values, "dptName");
            return this;
        }

        public Criteria andDptNameBetween(String value1, String value2) {
            addCriterion("dpt_name between", value1, value2, "dptName");
            return this;
        }

        public Criteria andDptNameNotBetween(String value1, String value2) {
            addCriterion("dpt_name not between", value1, value2, "dptName");
            return this;
        }

        public Criteria andPvIsNull() {
            addCriterion("pv is null");
            return this;
        }

        public Criteria andPvIsNotNull() {
            addCriterion("pv is not null");
            return this;
        }

        public Criteria andPvEqualTo(Integer value) {
            addCriterion("pv =", value, "pv");
            return this;
        }

        public Criteria andPvNotEqualTo(Integer value) {
            addCriterion("pv <>", value, "pv");
            return this;
        }

        public Criteria andPvGreaterThan(Integer value) {
            addCriterion("pv >", value, "pv");
            return this;
        }

        public Criteria andPvGreaterThanOrEqualTo(Integer value) {
            addCriterion("pv >=", value, "pv");
            return this;
        }

        public Criteria andPvLessThan(Integer value) {
            addCriterion("pv <", value, "pv");
            return this;
        }

        public Criteria andPvLessThanOrEqualTo(Integer value) {
            addCriterion("pv <=", value, "pv");
            return this;
        }

        public Criteria andPvIn(List<Integer> values) {
            addCriterion("pv in", values, "pv");
            return this;
        }

        public Criteria andPvNotIn(List<Integer> values) {
            addCriterion("pv not in", values, "pv");
            return this;
        }

        public Criteria andPvBetween(Integer value1, Integer value2) {
            addCriterion("pv between", value1, value2, "pv");
            return this;
        }

        public Criteria andPvNotBetween(Integer value1, Integer value2) {
            addCriterion("pv not between", value1, value2, "pv");
            return this;
        }

        public Criteria andUvIsNull() {
            addCriterion("uv is null");
            return this;
        }

        public Criteria andUvIsNotNull() {
            addCriterion("uv is not null");
            return this;
        }

        public Criteria andUvEqualTo(Integer value) {
            addCriterion("uv =", value, "uv");
            return this;
        }

        public Criteria andUvNotEqualTo(Integer value) {
            addCriterion("uv <>", value, "uv");
            return this;
        }

        public Criteria andUvGreaterThan(Integer value) {
            addCriterion("uv >", value, "uv");
            return this;
        }

        public Criteria andUvGreaterThanOrEqualTo(Integer value) {
            addCriterion("uv >=", value, "uv");
            return this;
        }

        public Criteria andUvLessThan(Integer value) {
            addCriterion("uv <", value, "uv");
            return this;
        }

        public Criteria andUvLessThanOrEqualTo(Integer value) {
            addCriterion("uv <=", value, "uv");
            return this;
        }

        public Criteria andUvIn(List<Integer> values) {
            addCriterion("uv in", values, "uv");
            return this;
        }

        public Criteria andUvNotIn(List<Integer> values) {
            addCriterion("uv not in", values, "uv");
            return this;
        }

        public Criteria andUvBetween(Integer value1, Integer value2) {
            addCriterion("uv between", value1, value2, "uv");
            return this;
        }

        public Criteria andUvNotBetween(Integer value1, Integer value2) {
            addCriterion("uv not between", value1, value2, "uv");
            return this;
        }

        public Criteria andOnlineServersIsNull() {
            addCriterion("online_servers is null");
            return this;
        }

        public Criteria andOnlineServersIsNotNull() {
            addCriterion("online_servers is not null");
            return this;
        }

        public Criteria andOnlineServersEqualTo(String value) {
            addCriterion("online_servers =", value, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersNotEqualTo(String value) {
            addCriterion("online_servers <>", value, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersGreaterThan(String value) {
            addCriterion("online_servers >", value, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersGreaterThanOrEqualTo(String value) {
            addCriterion("online_servers >=", value, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersLessThan(String value) {
            addCriterion("online_servers <", value, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersLessThanOrEqualTo(String value) {
            addCriterion("online_servers <=", value, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersLike(String value) {
            addCriterion("online_servers like", value, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersNotLike(String value) {
            addCriterion("online_servers not like", value, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersIn(List<String> values) {
            addCriterion("online_servers in", values, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersNotIn(List<String> values) {
            addCriterion("online_servers not in", values, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersBetween(String value1, String value2) {
            addCriterion("online_servers between", value1, value2, "onlineServers");
            return this;
        }

        public Criteria andOnlineServersNotBetween(String value1, String value2) {
            addCriterion("online_servers not between", value1, value2, "onlineServers");
            return this;
        }

        public Criteria andPublishDateIsNull() {
            addCriterion("publish_date is null");
            return this;
        }

        public Criteria andPublishDateIsNotNull() {
            addCriterion("publish_date is not null");
            return this;
        }

        public Criteria andPublishDateEqualTo(Date value) {
            addCriterion("publish_date =", value, "publishDate");
            return this;
        }

        public Criteria andPublishDateNotEqualTo(Date value) {
            addCriterion("publish_date <>", value, "publishDate");
            return this;
        }

        public Criteria andPublishDateGreaterThan(Date value) {
            addCriterion("publish_date >", value, "publishDate");
            return this;
        }

        public Criteria andPublishDateGreaterThanOrEqualTo(Date value) {
            addCriterion("publish_date >=", value, "publishDate");
            return this;
        }

        public Criteria andPublishDateLessThan(Date value) {
            addCriterion("publish_date <", value, "publishDate");
            return this;
        }

        public Criteria andPublishDateLessThanOrEqualTo(Date value) {
            addCriterion("publish_date <=", value, "publishDate");
            return this;
        }

        public Criteria andPublishDateIn(List<Date> values) {
            addCriterion("publish_date in", values, "publishDate");
            return this;
        }

        public Criteria andPublishDateNotIn(List<Date> values) {
            addCriterion("publish_date not in", values, "publishDate");
            return this;
        }

        public Criteria andPublishDateBetween(Date value1, Date value2) {
            addCriterion("publish_date between", value1, value2, "publishDate");
            return this;
        }

        public Criteria andPublishDateNotBetween(Date value1, Date value2) {
            addCriterion("publish_date not between", value1, value2, "publishDate");
            return this;
        }

        public Criteria andIsPassedTestIsNull() {
            addCriterion("is_passed_test is null");
            return this;
        }

        public Criteria andIsPassedTestIsNotNull() {
            addCriterion("is_passed_test is not null");
            return this;
        }

        public Criteria andIsPassedTestEqualTo(String value) {
            addCriterion("is_passed_test =", value, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestNotEqualTo(String value) {
            addCriterion("is_passed_test <>", value, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestGreaterThan(String value) {
            addCriterion("is_passed_test >", value, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestGreaterThanOrEqualTo(String value) {
            addCriterion("is_passed_test >=", value, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestLessThan(String value) {
            addCriterion("is_passed_test <", value, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestLessThanOrEqualTo(String value) {
            addCriterion("is_passed_test <=", value, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestLike(String value) {
            addCriterion("is_passed_test like", value, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestNotLike(String value) {
            addCriterion("is_passed_test not like", value, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestIn(List<String> values) {
            addCriterion("is_passed_test in", values, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestNotIn(List<String> values) {
            addCriterion("is_passed_test not in", values, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestBetween(String value1, String value2) {
            addCriterion("is_passed_test between", value1, value2, "isPassedTest");
            return this;
        }

        public Criteria andIsPassedTestNotBetween(String value1, String value2) {
            addCriterion("is_passed_test not between", value1, value2, "isPassedTest");
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

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("status =", value, "status");
            return this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("status <>", value, "status");
            return this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("status >", value, "status");
            return this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("status >=", value, "status");
            return this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("status <", value, "status");
            return this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("status <=", value, "status");
            return this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("status in", values, "status");
            return this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("status not in", values, "status");
            return this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("status between", value1, value2, "status");
            return this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("status not between", value1, value2, "status");
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

        public Criteria andFullSourceTypeIsNull() {
            addCriterion("full_source_type is null");
            return this;
        }

        public Criteria andFullSourceTypeIsNotNull() {
            addCriterion("full_source_type is not null");
            return this;
        }

        public Criteria andFullSourceTypeEqualTo(Byte value) {
            addCriterion("full_source_type =", value, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeNotEqualTo(Byte value) {
            addCriterion("full_source_type <>", value, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeGreaterThan(Byte value) {
            addCriterion("full_source_type >", value, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("full_source_type >=", value, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeLessThan(Byte value) {
            addCriterion("full_source_type <", value, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeLessThanOrEqualTo(Byte value) {
            addCriterion("full_source_type <=", value, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeIn(List<Byte> values) {
            addCriterion("full_source_type in", values, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeNotIn(List<Byte> values) {
            addCriterion("full_source_type not in", values, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeBetween(Byte value1, Byte value2) {
            addCriterion("full_source_type between", value1, value2, "fullSourceType");
            return this;
        }

        public Criteria andFullSourceTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("full_source_type not between", value1, value2, "fullSourceType");
            return this;
        }

        public Criteria andIncrTypeIsNull() {
            addCriterion("incr_type is null");
            return this;
        }

        public Criteria andIncrTypeIsNotNull() {
            addCriterion("incr_type is not null");
            return this;
        }

        public Criteria andIncrTypeEqualTo(Byte value) {
            addCriterion("incr_type =", value, "incrType");
            return this;
        }

        public Criteria andIncrTypeNotEqualTo(Byte value) {
            addCriterion("incr_type <>", value, "incrType");
            return this;
        }

        public Criteria andIncrTypeGreaterThan(Byte value) {
            addCriterion("incr_type >", value, "incrType");
            return this;
        }

        public Criteria andIncrTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("incr_type >=", value, "incrType");
            return this;
        }

        public Criteria andIncrTypeLessThan(Byte value) {
            addCriterion("incr_type <", value, "incrType");
            return this;
        }

        public Criteria andIncrTypeLessThanOrEqualTo(Byte value) {
            addCriterion("incr_type <=", value, "incrType");
            return this;
        }

        public Criteria andIncrTypeIn(List<Byte> values) {
            addCriterion("incr_type in", values, "incrType");
            return this;
        }

        public Criteria andIncrTypeNotIn(List<Byte> values) {
            addCriterion("incr_type not in", values, "incrType");
            return this;
        }

        public Criteria andIncrTypeBetween(Byte value1, Byte value2) {
            addCriterion("incr_type between", value1, value2, "incrType");
            return this;
        }

        public Criteria andIncrTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("incr_type not between", value1, value2, "incrType");
            return this;
        }
    }
}
