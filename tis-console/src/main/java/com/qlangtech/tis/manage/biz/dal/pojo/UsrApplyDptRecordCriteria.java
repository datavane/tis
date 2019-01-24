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
public class UsrApplyDptRecordCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    public UsrApplyDptRecordCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected UsrApplyDptRecordCriteria(UsrApplyDptRecordCriteria example) {
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

        public Criteria andAgreedTimeIsNull() {
            addCriterion("agreed_time is null");
            return this;
        }

        public Criteria andAgreedTimeIsNotNull() {
            addCriterion("agreed_time is not null");
            return this;
        }

        public Criteria andAgreedTimeEqualTo(Date value) {
            addCriterion("agreed_time =", value, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeNotEqualTo(Date value) {
            addCriterion("agreed_time <>", value, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeGreaterThan(Date value) {
            addCriterion("agreed_time >", value, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("agreed_time >=", value, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeLessThan(Date value) {
            addCriterion("agreed_time <", value, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeLessThanOrEqualTo(Date value) {
            addCriterion("agreed_time <=", value, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeIn(List<Date> values) {
            addCriterion("agreed_time in", values, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeNotIn(List<Date> values) {
            addCriterion("agreed_time not in", values, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeBetween(Date value1, Date value2) {
            addCriterion("agreed_time between", value1, value2, "agreedTime");
            return this;
        }

        public Criteria andAgreedTimeNotBetween(Date value1, Date value2) {
            addCriterion("agreed_time not between", value1, value2, "agreedTime");
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

        public Criteria andUsrIdIsNull() {
            addCriterion("usr_id is null");
            return this;
        }

        public Criteria andUsrIdIsNotNull() {
            addCriterion("usr_id is not null");
            return this;
        }

        public Criteria andUsrIdEqualTo(String value) {
            addCriterion("usr_id =", value, "usrId");
            return this;
        }

        public Criteria andUsrIdNotEqualTo(String value) {
            addCriterion("usr_id <>", value, "usrId");
            return this;
        }

        public Criteria andUsrIdGreaterThan(String value) {
            addCriterion("usr_id >", value, "usrId");
            return this;
        }

        public Criteria andUsrIdGreaterThanOrEqualTo(String value) {
            addCriterion("usr_id >=", value, "usrId");
            return this;
        }

        public Criteria andUsrIdLessThan(String value) {
            addCriterion("usr_id <", value, "usrId");
            return this;
        }

        public Criteria andUsrIdLessThanOrEqualTo(String value) {
            addCriterion("usr_id <=", value, "usrId");
            return this;
        }

        public Criteria andUsrIdLike(String value) {
            addCriterion("usr_id like", value, "usrId");
            return this;
        }

        public Criteria andUsrIdNotLike(String value) {
            addCriterion("usr_id not like", value, "usrId");
            return this;
        }

        public Criteria andUsrIdIn(List<String> values) {
            addCriterion("usr_id in", values, "usrId");
            return this;
        }

        public Criteria andUsrIdNotIn(List<String> values) {
            addCriterion("usr_id not in", values, "usrId");
            return this;
        }

        public Criteria andUsrIdBetween(String value1, String value2) {
            addCriterion("usr_id between", value1, value2, "usrId");
            return this;
        }

        public Criteria andUsrIdNotBetween(String value1, String value2) {
            addCriterion("usr_id not between", value1, value2, "usrId");
            return this;
        }

        public Criteria andUsrNameIsNull() {
            addCriterion("usr_name is null");
            return this;
        }

        public Criteria andUsrNameIsNotNull() {
            addCriterion("usr_name is not null");
            return this;
        }

        public Criteria andUsrNameEqualTo(String value) {
            addCriterion("usr_name =", value, "usrName");
            return this;
        }

        public Criteria andUsrNameNotEqualTo(String value) {
            addCriterion("usr_name <>", value, "usrName");
            return this;
        }

        public Criteria andUsrNameGreaterThan(String value) {
            addCriterion("usr_name >", value, "usrName");
            return this;
        }

        public Criteria andUsrNameGreaterThanOrEqualTo(String value) {
            addCriterion("usr_name >=", value, "usrName");
            return this;
        }

        public Criteria andUsrNameLessThan(String value) {
            addCriterion("usr_name <", value, "usrName");
            return this;
        }

        public Criteria andUsrNameLessThanOrEqualTo(String value) {
            addCriterion("usr_name <=", value, "usrName");
            return this;
        }

        public Criteria andUsrNameLike(String value) {
            addCriterion("usr_name like", value, "usrName");
            return this;
        }

        public Criteria andUsrNameNotLike(String value) {
            addCriterion("usr_name not like", value, "usrName");
            return this;
        }

        public Criteria andUsrNameIn(List<String> values) {
            addCriterion("usr_name in", values, "usrName");
            return this;
        }

        public Criteria andUsrNameNotIn(List<String> values) {
            addCriterion("usr_name not in", values, "usrName");
            return this;
        }

        public Criteria andUsrNameBetween(String value1, String value2) {
            addCriterion("usr_name between", value1, value2, "usrName");
            return this;
        }

        public Criteria andUsrNameNotBetween(String value1, String value2) {
            addCriterion("usr_name not between", value1, value2, "usrName");
            return this;
        }

        public Criteria andAgreedUsrIdIsNull() {
            addCriterion("agreed_usr_id is null");
            return this;
        }

        public Criteria andAgreedUsrIdIsNotNull() {
            addCriterion("agreed_usr_id is not null");
            return this;
        }

        public Criteria andAgreedUsrIdEqualTo(String value) {
            addCriterion("agreed_usr_id =", value, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdNotEqualTo(String value) {
            addCriterion("agreed_usr_id <>", value, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdGreaterThan(String value) {
            addCriterion("agreed_usr_id >", value, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdGreaterThanOrEqualTo(String value) {
            addCriterion("agreed_usr_id >=", value, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdLessThan(String value) {
            addCriterion("agreed_usr_id <", value, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdLessThanOrEqualTo(String value) {
            addCriterion("agreed_usr_id <=", value, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdLike(String value) {
            addCriterion("agreed_usr_id like", value, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdNotLike(String value) {
            addCriterion("agreed_usr_id not like", value, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdIn(List<String> values) {
            addCriterion("agreed_usr_id in", values, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdNotIn(List<String> values) {
            addCriterion("agreed_usr_id not in", values, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdBetween(String value1, String value2) {
            addCriterion("agreed_usr_id between", value1, value2, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrIdNotBetween(String value1, String value2) {
            addCriterion("agreed_usr_id not between", value1, value2, "agreedUsrId");
            return this;
        }

        public Criteria andAgreedUsrNameIsNull() {
            addCriterion("agreed_usr_name is null");
            return this;
        }

        public Criteria andAgreedUsrNameIsNotNull() {
            addCriterion("agreed_usr_name is not null");
            return this;
        }

        public Criteria andAgreedUsrNameEqualTo(String value) {
            addCriterion("agreed_usr_name =", value, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameNotEqualTo(String value) {
            addCriterion("agreed_usr_name <>", value, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameGreaterThan(String value) {
            addCriterion("agreed_usr_name >", value, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameGreaterThanOrEqualTo(String value) {
            addCriterion("agreed_usr_name >=", value, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameLessThan(String value) {
            addCriterion("agreed_usr_name <", value, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameLessThanOrEqualTo(String value) {
            addCriterion("agreed_usr_name <=", value, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameLike(String value) {
            addCriterion("agreed_usr_name like", value, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameNotLike(String value) {
            addCriterion("agreed_usr_name not like", value, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameIn(List<String> values) {
            addCriterion("agreed_usr_name in", values, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameNotIn(List<String> values) {
            addCriterion("agreed_usr_name not in", values, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameBetween(String value1, String value2) {
            addCriterion("agreed_usr_name between", value1, value2, "agreedUsrName");
            return this;
        }

        public Criteria andAgreedUsrNameNotBetween(String value1, String value2) {
            addCriterion("agreed_usr_name not between", value1, value2, "agreedUsrName");
            return this;
        }

        public Criteria andGrantedIsNull() {
            addCriterion("granted is null");
            return this;
        }

        public Criteria andGrantedIsNotNull() {
            addCriterion("granted is not null");
            return this;
        }

        public Criteria andGrantedEqualTo(String value) {
            addCriterion("granted =", value, "granted");
            return this;
        }

        public Criteria andGrantedNotEqualTo(String value) {
            addCriterion("granted <>", value, "granted");
            return this;
        }

        public Criteria andGrantedGreaterThan(String value) {
            addCriterion("granted >", value, "granted");
            return this;
        }

        public Criteria andGrantedGreaterThanOrEqualTo(String value) {
            addCriterion("granted >=", value, "granted");
            return this;
        }

        public Criteria andGrantedLessThan(String value) {
            addCriterion("granted <", value, "granted");
            return this;
        }

        public Criteria andGrantedLessThanOrEqualTo(String value) {
            addCriterion("granted <=", value, "granted");
            return this;
        }

        public Criteria andGrantedLike(String value) {
            addCriterion("granted like", value, "granted");
            return this;
        }

        public Criteria andGrantedNotLike(String value) {
            addCriterion("granted not like", value, "granted");
            return this;
        }

        public Criteria andGrantedIn(List<String> values) {
            addCriterion("granted in", values, "granted");
            return this;
        }

        public Criteria andGrantedNotIn(List<String> values) {
            addCriterion("granted not in", values, "granted");
            return this;
        }

        public Criteria andGrantedBetween(String value1, String value2) {
            addCriterion("granted between", value1, value2, "granted");
            return this;
        }

        public Criteria andGrantedNotBetween(String value1, String value2) {
            addCriterion("granted not between", value1, value2, "granted");
            return this;
        }

        public Criteria andUdrIdIsNull() {
            addCriterion("udr_id is null");
            return this;
        }

        public Criteria andUdrIdIsNotNull() {
            addCriterion("udr_id is not null");
            return this;
        }

        public Criteria andUdrIdEqualTo(Long value) {
            addCriterion("udr_id =", value, "udrId");
            return this;
        }

        public Criteria andUdrIdNotEqualTo(Long value) {
            addCriterion("udr_id <>", value, "udrId");
            return this;
        }

        public Criteria andUdrIdGreaterThan(Long value) {
            addCriterion("udr_id >", value, "udrId");
            return this;
        }

        public Criteria andUdrIdGreaterThanOrEqualTo(Long value) {
            addCriterion("udr_id >=", value, "udrId");
            return this;
        }

        public Criteria andUdrIdLessThan(Long value) {
            addCriterion("udr_id <", value, "udrId");
            return this;
        }

        public Criteria andUdrIdLessThanOrEqualTo(Long value) {
            addCriterion("udr_id <=", value, "udrId");
            return this;
        }

        public Criteria andUdrIdIn(List<Long> values) {
            addCriterion("udr_id in", values, "udrId");
            return this;
        }

        public Criteria andUdrIdNotIn(List<Long> values) {
            addCriterion("udr_id not in", values, "udrId");
            return this;
        }

        public Criteria andUdrIdBetween(Long value1, Long value2) {
            addCriterion("udr_id between", value1, value2, "udrId");
            return this;
        }

        public Criteria andUdrIdNotBetween(Long value1, Long value2) {
            addCriterion("udr_id not between", value1, value2, "udrId");
            return this;
        }

        public Criteria andDptUsrListIsNull() {
            addCriterion("dpt_usr_list is null");
            return this;
        }

        public Criteria andDptUsrListIsNotNull() {
            addCriterion("dpt_usr_list is not null");
            return this;
        }

        public Criteria andDptUsrListEqualTo(String value) {
            addCriterion("dpt_usr_list =", value, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListNotEqualTo(String value) {
            addCriterion("dpt_usr_list <>", value, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListGreaterThan(String value) {
            addCriterion("dpt_usr_list >", value, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListGreaterThanOrEqualTo(String value) {
            addCriterion("dpt_usr_list >=", value, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListLessThan(String value) {
            addCriterion("dpt_usr_list <", value, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListLessThanOrEqualTo(String value) {
            addCriterion("dpt_usr_list <=", value, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListLike(String value) {
            addCriterion("dpt_usr_list like", value, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListNotLike(String value) {
            addCriterion("dpt_usr_list not like", value, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListIn(List<String> values) {
            addCriterion("dpt_usr_list in", values, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListNotIn(List<String> values) {
            addCriterion("dpt_usr_list not in", values, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListBetween(String value1, String value2) {
            addCriterion("dpt_usr_list between", value1, value2, "dptUsrList");
            return this;
        }

        public Criteria andDptUsrListNotBetween(String value1, String value2) {
            addCriterion("dpt_usr_list not between", value1, value2, "dptUsrList");
            return this;
        }
    }
}
