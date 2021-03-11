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
package com.qlangtech.tis.realtime.test.member.pojo;

import com.qlangtech.tis.manage.common.TISBaseCriteria;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class CustomerCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<CustomerColEnum> cols = Sets.newHashSet();

    public CustomerCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected CustomerCriteria(CustomerCriteria example) {
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

    public final List<CustomerColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(CustomerColEnum... colName) {
        for (CustomerColEnum c : colName) {
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

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List<Date> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List<java.sql.Date> dateList = new ArrayList<java.sql.Date>();
            Iterator<Date> iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(iter.next().getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return this;
        }

        public Criteria andIdEqualTo(String value) {
            addCriterion("id =", value, "id");
            return this;
        }

        public Criteria andIdNotEqualTo(String value) {
            addCriterion("id <>", value, "id");
            return this;
        }

        public Criteria andIdGreaterThan(String value) {
            addCriterion("id >", value, "id");
            return this;
        }

        public Criteria andIdGreaterThanOrEqualTo(String value) {
            addCriterion("id >=", value, "id");
            return this;
        }

        public Criteria andIdLessThan(String value) {
            addCriterion("id <", value, "id");
            return this;
        }

        public Criteria andIdLessThanOrEqualTo(String value) {
            addCriterion("id <=", value, "id");
            return this;
        }

        public Criteria andIdLike(String value) {
            addCriterion("id like", value, "id");
            return this;
        }

        public Criteria andIdNotLike(String value) {
            addCriterion("id not like", value, "id");
            return this;
        }

        public Criteria andIdIn(List<String> values) {
            addCriterion("id in", values, "id");
            return this;
        }

        public Criteria andIdNotIn(List<String> values) {
            addCriterion("id not in", values, "id");
            return this;
        }

        public Criteria andIdBetween(String value1, String value2) {
            addCriterion("id between", value1, value2, "id");
            return this;
        }

        public Criteria andIdNotBetween(String value1, String value2) {
            addCriterion("id not between", value1, value2, "id");
            return this;
        }

        public Criteria andMobileIsNull() {
            addCriterion("mobile is null");
            return this;
        }

        public Criteria andMobileIsNotNull() {
            addCriterion("mobile is not null");
            return this;
        }

        public Criteria andMobileEqualTo(String value) {
            addCriterion("mobile =", value, "mobile");
            return this;
        }

        public Criteria andMobileNotEqualTo(String value) {
            addCriterion("mobile <>", value, "mobile");
            return this;
        }

        public Criteria andMobileGreaterThan(String value) {
            addCriterion("mobile >", value, "mobile");
            return this;
        }

        public Criteria andMobileGreaterThanOrEqualTo(String value) {
            addCriterion("mobile >=", value, "mobile");
            return this;
        }

        public Criteria andMobileLessThan(String value) {
            addCriterion("mobile <", value, "mobile");
            return this;
        }

        public Criteria andMobileLessThanOrEqualTo(String value) {
            addCriterion("mobile <=", value, "mobile");
            return this;
        }

        public Criteria andMobileLike(String value) {
            addCriterion("mobile like", value, "mobile");
            return this;
        }

        public Criteria andMobileNotLike(String value) {
            addCriterion("mobile not like", value, "mobile");
            return this;
        }

        public Criteria andMobileIn(List<String> values) {
            addCriterion("mobile in", values, "mobile");
            return this;
        }

        public Criteria andMobileNotIn(List<String> values) {
            addCriterion("mobile not in", values, "mobile");
            return this;
        }

        public Criteria andMobileBetween(String value1, String value2) {
            addCriterion("mobile between", value1, value2, "mobile");
            return this;
        }

        public Criteria andMobileNotBetween(String value1, String value2) {
            addCriterion("mobile not between", value1, value2, "mobile");
            return this;
        }

        public Criteria andPhoneIsNull() {
            addCriterion("phone is null");
            return this;
        }

        public Criteria andPhoneIsNotNull() {
            addCriterion("phone is not null");
            return this;
        }

        public Criteria andPhoneEqualTo(String value) {
            addCriterion("phone =", value, "phone");
            return this;
        }

        public Criteria andPhoneNotEqualTo(String value) {
            addCriterion("phone <>", value, "phone");
            return this;
        }

        public Criteria andPhoneGreaterThan(String value) {
            addCriterion("phone >", value, "phone");
            return this;
        }

        public Criteria andPhoneGreaterThanOrEqualTo(String value) {
            addCriterion("phone >=", value, "phone");
            return this;
        }

        public Criteria andPhoneLessThan(String value) {
            addCriterion("phone <", value, "phone");
            return this;
        }

        public Criteria andPhoneLessThanOrEqualTo(String value) {
            addCriterion("phone <=", value, "phone");
            return this;
        }

        public Criteria andPhoneLike(String value) {
            addCriterion("phone like", value, "phone");
            return this;
        }

        public Criteria andPhoneNotLike(String value) {
            addCriterion("phone not like", value, "phone");
            return this;
        }

        public Criteria andPhoneIn(List<String> values) {
            addCriterion("phone in", values, "phone");
            return this;
        }

        public Criteria andPhoneNotIn(List<String> values) {
            addCriterion("phone not in", values, "phone");
            return this;
        }

        public Criteria andPhoneBetween(String value1, String value2) {
            addCriterion("phone between", value1, value2, "phone");
            return this;
        }

        public Criteria andPhoneNotBetween(String value1, String value2) {
            addCriterion("phone not between", value1, value2, "phone");
            return this;
        }

        public Criteria andSexIsNull() {
            addCriterion("sex is null");
            return this;
        }

        public Criteria andSexIsNotNull() {
            addCriterion("sex is not null");
            return this;
        }

        public Criteria andSexEqualTo(Short value) {
            addCriterion("sex =", value, "sex");
            return this;
        }

        public Criteria andSexNotEqualTo(Short value) {
            addCriterion("sex <>", value, "sex");
            return this;
        }

        public Criteria andSexGreaterThan(Short value) {
            addCriterion("sex >", value, "sex");
            return this;
        }

        public Criteria andSexGreaterThanOrEqualTo(Short value) {
            addCriterion("sex >=", value, "sex");
            return this;
        }

        public Criteria andSexLessThan(Short value) {
            addCriterion("sex <", value, "sex");
            return this;
        }

        public Criteria andSexLessThanOrEqualTo(Short value) {
            addCriterion("sex <=", value, "sex");
            return this;
        }

        public Criteria andSexIn(List<Short> values) {
            addCriterion("sex in", values, "sex");
            return this;
        }

        public Criteria andSexNotIn(List<Short> values) {
            addCriterion("sex not in", values, "sex");
            return this;
        }

        public Criteria andSexBetween(Short value1, Short value2) {
            addCriterion("sex between", value1, value2, "sex");
            return this;
        }

        public Criteria andSexNotBetween(Short value1, Short value2) {
            addCriterion("sex not between", value1, value2, "sex");
            return this;
        }

        public Criteria andBirthdayIsNull() {
            addCriterion("birthday is null");
            return this;
        }

        public Criteria andBirthdayIsNotNull() {
            addCriterion("birthday is not null");
            return this;
        }

        public Criteria andBirthdayEqualTo(Date value) {
            addCriterionForJDBCDate("birthday =", value, "birthday");
            return this;
        }

        public Criteria andBirthdayNotEqualTo(Date value) {
            addCriterionForJDBCDate("birthday <>", value, "birthday");
            return this;
        }

        public Criteria andBirthdayGreaterThan(Date value) {
            addCriterionForJDBCDate("birthday >", value, "birthday");
            return this;
        }

        public Criteria andBirthdayGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("birthday >=", value, "birthday");
            return this;
        }

        public Criteria andBirthdayLessThan(Date value) {
            addCriterionForJDBCDate("birthday <", value, "birthday");
            return this;
        }

        public Criteria andBirthdayLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("birthday <=", value, "birthday");
            return this;
        }

        public Criteria andBirthdayIn(List<Date> values) {
            addCriterionForJDBCDate("birthday in", values, "birthday");
            return this;
        }

        public Criteria andBirthdayNotIn(List<Date> values) {
            addCriterionForJDBCDate("birthday not in", values, "birthday");
            return this;
        }

        public Criteria andBirthdayBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("birthday between", value1, value2, "birthday");
            return this;
        }

        public Criteria andBirthdayNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("birthday not between", value1, value2, "birthday");
            return this;
        }

        public Criteria andCertificateIsNull() {
            addCriterion("certificate is null");
            return this;
        }

        public Criteria andCertificateIsNotNull() {
            addCriterion("certificate is not null");
            return this;
        }

        public Criteria andCertificateEqualTo(String value) {
            addCriterion("certificate =", value, "certificate");
            return this;
        }

        public Criteria andCertificateNotEqualTo(String value) {
            addCriterion("certificate <>", value, "certificate");
            return this;
        }

        public Criteria andCertificateGreaterThan(String value) {
            addCriterion("certificate >", value, "certificate");
            return this;
        }

        public Criteria andCertificateGreaterThanOrEqualTo(String value) {
            addCriterion("certificate >=", value, "certificate");
            return this;
        }

        public Criteria andCertificateLessThan(String value) {
            addCriterion("certificate <", value, "certificate");
            return this;
        }

        public Criteria andCertificateLessThanOrEqualTo(String value) {
            addCriterion("certificate <=", value, "certificate");
            return this;
        }

        public Criteria andCertificateLike(String value) {
            addCriterion("certificate like", value, "certificate");
            return this;
        }

        public Criteria andCertificateNotLike(String value) {
            addCriterion("certificate not like", value, "certificate");
            return this;
        }

        public Criteria andCertificateIn(List<String> values) {
            addCriterion("certificate in", values, "certificate");
            return this;
        }

        public Criteria andCertificateNotIn(List<String> values) {
            addCriterion("certificate not in", values, "certificate");
            return this;
        }

        public Criteria andCertificateBetween(String value1, String value2) {
            addCriterion("certificate between", value1, value2, "certificate");
            return this;
        }

        public Criteria andCertificateNotBetween(String value1, String value2) {
            addCriterion("certificate not between", value1, value2, "certificate");
            return this;
        }

        public Criteria andSpellIsNull() {
            addCriterion("spell is null");
            return this;
        }

        public Criteria andSpellIsNotNull() {
            addCriterion("spell is not null");
            return this;
        }

        public Criteria andSpellEqualTo(String value) {
            addCriterion("spell =", value, "spell");
            return this;
        }

        public Criteria andSpellNotEqualTo(String value) {
            addCriterion("spell <>", value, "spell");
            return this;
        }

        public Criteria andSpellGreaterThan(String value) {
            addCriterion("spell >", value, "spell");
            return this;
        }

        public Criteria andSpellGreaterThanOrEqualTo(String value) {
            addCriterion("spell >=", value, "spell");
            return this;
        }

        public Criteria andSpellLessThan(String value) {
            addCriterion("spell <", value, "spell");
            return this;
        }

        public Criteria andSpellLessThanOrEqualTo(String value) {
            addCriterion("spell <=", value, "spell");
            return this;
        }

        public Criteria andSpellLike(String value) {
            addCriterion("spell like", value, "spell");
            return this;
        }

        public Criteria andSpellNotLike(String value) {
            addCriterion("spell not like", value, "spell");
            return this;
        }

        public Criteria andSpellIn(List<String> values) {
            addCriterion("spell in", values, "spell");
            return this;
        }

        public Criteria andSpellNotIn(List<String> values) {
            addCriterion("spell not in", values, "spell");
            return this;
        }

        public Criteria andSpellBetween(String value1, String value2) {
            addCriterion("spell between", value1, value2, "spell");
            return this;
        }

        public Criteria andSpellNotBetween(String value1, String value2) {
            addCriterion("spell not between", value1, value2, "spell");
            return this;
        }

        public Criteria andNameIsNull() {
            addCriterion("name is null");
            return this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("name is not null");
            return this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("name =", value, "name");
            return this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("name <>", value, "name");
            return this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("name >", value, "name");
            return this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("name >=", value, "name");
            return this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("name <", value, "name");
            return this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("name <=", value, "name");
            return this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("name like", value, "name");
            return this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("name not like", value, "name");
            return this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("name in", values, "name");
            return this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("name not in", values, "name");
            return this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("name between", value1, value2, "name");
            return this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("name not between", value1, value2, "name");
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

        public Criteria andIsValidIsNull() {
            addCriterion("is_valid is null");
            return this;
        }

        public Criteria andIsValidIsNotNull() {
            addCriterion("is_valid is not null");
            return this;
        }

        public Criteria andIsValidEqualTo(Boolean value) {
            addCriterion("is_valid =", value, "isValid");
            return this;
        }

        public Criteria andIsValidNotEqualTo(Boolean value) {
            addCriterion("is_valid <>", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThan(Boolean value) {
            addCriterion("is_valid >", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_valid >=", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThan(Boolean value) {
            addCriterion("is_valid <", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThanOrEqualTo(Boolean value) {
            addCriterion("is_valid <=", value, "isValid");
            return this;
        }

        public Criteria andIsValidIn(List<Boolean> values) {
            addCriterion("is_valid in", values, "isValid");
            return this;
        }

        public Criteria andIsValidNotIn(List<Boolean> values) {
            addCriterion("is_valid not in", values, "isValid");
            return this;
        }

        public Criteria andIsValidBetween(Boolean value1, Boolean value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return this;
        }

        public Criteria andIsValidNotBetween(Boolean value1, Boolean value2) {
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

        public Criteria andContryIdIsNull() {
            addCriterion("contry_id is null");
            return this;
        }

        public Criteria andContryIdIsNotNull() {
            addCriterion("contry_id is not null");
            return this;
        }

        public Criteria andContryIdEqualTo(String value) {
            addCriterion("contry_id =", value, "contryId");
            return this;
        }

        public Criteria andContryIdNotEqualTo(String value) {
            addCriterion("contry_id <>", value, "contryId");
            return this;
        }

        public Criteria andContryIdGreaterThan(String value) {
            addCriterion("contry_id >", value, "contryId");
            return this;
        }

        public Criteria andContryIdGreaterThanOrEqualTo(String value) {
            addCriterion("contry_id >=", value, "contryId");
            return this;
        }

        public Criteria andContryIdLessThan(String value) {
            addCriterion("contry_id <", value, "contryId");
            return this;
        }

        public Criteria andContryIdLessThanOrEqualTo(String value) {
            addCriterion("contry_id <=", value, "contryId");
            return this;
        }

        public Criteria andContryIdLike(String value) {
            addCriterion("contry_id like", value, "contryId");
            return this;
        }

        public Criteria andContryIdNotLike(String value) {
            addCriterion("contry_id not like", value, "contryId");
            return this;
        }

        public Criteria andContryIdIn(List<String> values) {
            addCriterion("contry_id in", values, "contryId");
            return this;
        }

        public Criteria andContryIdNotIn(List<String> values) {
            addCriterion("contry_id not in", values, "contryId");
            return this;
        }

        public Criteria andContryIdBetween(String value1, String value2) {
            addCriterion("contry_id between", value1, value2, "contryId");
            return this;
        }

        public Criteria andContryIdNotBetween(String value1, String value2) {
            addCriterion("contry_id not between", value1, value2, "contryId");
            return this;
        }

        public Criteria andContryCodeIsNull() {
            addCriterion("contry_code is null");
            return this;
        }

        public Criteria andContryCodeIsNotNull() {
            addCriterion("contry_code is not null");
            return this;
        }

        public Criteria andContryCodeEqualTo(String value) {
            addCriterion("contry_code =", value, "contryCode");
            return this;
        }

        public Criteria andContryCodeNotEqualTo(String value) {
            addCriterion("contry_code <>", value, "contryCode");
            return this;
        }

        public Criteria andContryCodeGreaterThan(String value) {
            addCriterion("contry_code >", value, "contryCode");
            return this;
        }

        public Criteria andContryCodeGreaterThanOrEqualTo(String value) {
            addCriterion("contry_code >=", value, "contryCode");
            return this;
        }

        public Criteria andContryCodeLessThan(String value) {
            addCriterion("contry_code <", value, "contryCode");
            return this;
        }

        public Criteria andContryCodeLessThanOrEqualTo(String value) {
            addCriterion("contry_code <=", value, "contryCode");
            return this;
        }

        public Criteria andContryCodeLike(String value) {
            addCriterion("contry_code like", value, "contryCode");
            return this;
        }

        public Criteria andContryCodeNotLike(String value) {
            addCriterion("contry_code not like", value, "contryCode");
            return this;
        }

        public Criteria andContryCodeIn(List<String> values) {
            addCriterion("contry_code in", values, "contryCode");
            return this;
        }

        public Criteria andContryCodeNotIn(List<String> values) {
            addCriterion("contry_code not in", values, "contryCode");
            return this;
        }

        public Criteria andContryCodeBetween(String value1, String value2) {
            addCriterion("contry_code between", value1, value2, "contryCode");
            return this;
        }

        public Criteria andContryCodeNotBetween(String value1, String value2) {
            addCriterion("contry_code not between", value1, value2, "contryCode");
            return this;
        }

        public Criteria andConsumeAmountIsNull() {
            addCriterion("consume_amount is null");
            return this;
        }

        public Criteria andConsumeAmountIsNotNull() {
            addCriterion("consume_amount is not null");
            return this;
        }

        public Criteria andConsumeAmountEqualTo(BigDecimal value) {
            addCriterion("consume_amount =", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountNotEqualTo(BigDecimal value) {
            addCriterion("consume_amount <>", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountGreaterThan(BigDecimal value) {
            addCriterion("consume_amount >", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("consume_amount >=", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountLessThan(BigDecimal value) {
            addCriterion("consume_amount <", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("consume_amount <=", value, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountIn(List<BigDecimal> values) {
            addCriterion("consume_amount in", values, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountNotIn(List<BigDecimal> values) {
            addCriterion("consume_amount not in", values, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("consume_amount between", value1, value2, "consumeAmount");
            return this;
        }

        public Criteria andConsumeAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("consume_amount not between", value1, value2, "consumeAmount");
            return this;
        }

        public Criteria andLastConsumeTimeIsNull() {
            addCriterion("last_consume_time is null");
            return this;
        }

        public Criteria andLastConsumeTimeIsNotNull() {
            addCriterion("last_consume_time is not null");
            return this;
        }

        public Criteria andLastConsumeTimeEqualTo(Long value) {
            addCriterion("last_consume_time =", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeNotEqualTo(Long value) {
            addCriterion("last_consume_time <>", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeGreaterThan(Long value) {
            addCriterion("last_consume_time >", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("last_consume_time >=", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeLessThan(Long value) {
            addCriterion("last_consume_time <", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeLessThanOrEqualTo(Long value) {
            addCriterion("last_consume_time <=", value, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeIn(List<Long> values) {
            addCriterion("last_consume_time in", values, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeNotIn(List<Long> values) {
            addCriterion("last_consume_time not in", values, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeBetween(Long value1, Long value2) {
            addCriterion("last_consume_time between", value1, value2, "lastConsumeTime");
            return this;
        }

        public Criteria andLastConsumeTimeNotBetween(Long value1, Long value2) {
            addCriterion("last_consume_time not between", value1, value2, "lastConsumeTime");
            return this;
        }

        public Criteria andConsumeNumIsNull() {
            addCriterion("consume_num is null");
            return this;
        }

        public Criteria andConsumeNumIsNotNull() {
            addCriterion("consume_num is not null");
            return this;
        }

        public Criteria andConsumeNumEqualTo(Integer value) {
            addCriterion("consume_num =", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumNotEqualTo(Integer value) {
            addCriterion("consume_num <>", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumGreaterThan(Integer value) {
            addCriterion("consume_num >", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("consume_num >=", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumLessThan(Integer value) {
            addCriterion("consume_num <", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumLessThanOrEqualTo(Integer value) {
            addCriterion("consume_num <=", value, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumIn(List<Integer> values) {
            addCriterion("consume_num in", values, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumNotIn(List<Integer> values) {
            addCriterion("consume_num not in", values, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumBetween(Integer value1, Integer value2) {
            addCriterion("consume_num between", value1, value2, "consumeNum");
            return this;
        }

        public Criteria andConsumeNumNotBetween(Integer value1, Integer value2) {
            addCriterion("consume_num not between", value1, value2, "consumeNum");
            return this;
        }

        public Criteria andExtendFieldsIsNull() {
            addCriterion("extend_fields is null");
            return this;
        }

        public Criteria andExtendFieldsIsNotNull() {
            addCriterion("extend_fields is not null");
            return this;
        }

        public Criteria andExtendFieldsEqualTo(String value) {
            addCriterion("extend_fields =", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsNotEqualTo(String value) {
            addCriterion("extend_fields <>", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsGreaterThan(String value) {
            addCriterion("extend_fields >", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsGreaterThanOrEqualTo(String value) {
            addCriterion("extend_fields >=", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsLessThan(String value) {
            addCriterion("extend_fields <", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsLessThanOrEqualTo(String value) {
            addCriterion("extend_fields <=", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsLike(String value) {
            addCriterion("extend_fields like", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsNotLike(String value) {
            addCriterion("extend_fields not like", value, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsIn(List<String> values) {
            addCriterion("extend_fields in", values, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsNotIn(List<String> values) {
            addCriterion("extend_fields not in", values, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsBetween(String value1, String value2) {
            addCriterion("extend_fields between", value1, value2, "extendFields");
            return this;
        }

        public Criteria andExtendFieldsNotBetween(String value1, String value2) {
            addCriterion("extend_fields not between", value1, value2, "extendFields");
            return this;
        }

        public Criteria andCountryCodeIsNull() {
            addCriterion("country_code is null");
            return this;
        }

        public Criteria andCountryCodeIsNotNull() {
            addCriterion("country_code is not null");
            return this;
        }

        public Criteria andCountryCodeEqualTo(String value) {
            addCriterion("country_code =", value, "countryCode");
            return this;
        }

        public Criteria andCountryCodeNotEqualTo(String value) {
            addCriterion("country_code <>", value, "countryCode");
            return this;
        }

        public Criteria andCountryCodeGreaterThan(String value) {
            addCriterion("country_code >", value, "countryCode");
            return this;
        }

        public Criteria andCountryCodeGreaterThanOrEqualTo(String value) {
            addCriterion("country_code >=", value, "countryCode");
            return this;
        }

        public Criteria andCountryCodeLessThan(String value) {
            addCriterion("country_code <", value, "countryCode");
            return this;
        }

        public Criteria andCountryCodeLessThanOrEqualTo(String value) {
            addCriterion("country_code <=", value, "countryCode");
            return this;
        }

        public Criteria andCountryCodeLike(String value) {
            addCriterion("country_code like", value, "countryCode");
            return this;
        }

        public Criteria andCountryCodeNotLike(String value) {
            addCriterion("country_code not like", value, "countryCode");
            return this;
        }

        public Criteria andCountryCodeIn(List<String> values) {
            addCriterion("country_code in", values, "countryCode");
            return this;
        }

        public Criteria andCountryCodeNotIn(List<String> values) {
            addCriterion("country_code not in", values, "countryCode");
            return this;
        }

        public Criteria andCountryCodeBetween(String value1, String value2) {
            addCriterion("country_code between", value1, value2, "countryCode");
            return this;
        }

        public Criteria andCountryCodeNotBetween(String value1, String value2) {
            addCriterion("country_code not between", value1, value2, "countryCode");
            return this;
        }
    }
}
