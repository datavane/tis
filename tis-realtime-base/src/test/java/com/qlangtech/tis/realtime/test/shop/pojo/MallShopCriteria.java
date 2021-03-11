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
package com.qlangtech.tis.realtime.test.shop.pojo;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.manage.common.TISBaseCriteria;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MallShopCriteria extends TISBaseCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<MallShopColEnum> cols = Sets.newHashSet();

    public MallShopCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected MallShopCriteria(MallShopCriteria example) {
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

    public final List<MallShopColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(MallShopColEnum... colName) {
        for (MallShopColEnum c : colName) {
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

        public Criteria andShopEntityIdIsNull() {
            addCriterion("shop_entity_id is null");
            return this;
        }

        public Criteria andShopEntityIdIsNotNull() {
            addCriterion("shop_entity_id is not null");
            return this;
        }

        public Criteria andShopEntityIdEqualTo(String value) {
            addCriterion("shop_entity_id =", value, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdNotEqualTo(String value) {
            addCriterion("shop_entity_id <>", value, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdGreaterThan(String value) {
            addCriterion("shop_entity_id >", value, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdGreaterThanOrEqualTo(String value) {
            addCriterion("shop_entity_id >=", value, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdLessThan(String value) {
            addCriterion("shop_entity_id <", value, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdLessThanOrEqualTo(String value) {
            addCriterion("shop_entity_id <=", value, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdLike(String value) {
            addCriterion("shop_entity_id like", value, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdNotLike(String value) {
            addCriterion("shop_entity_id not like", value, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdIn(List<String> values) {
            addCriterion("shop_entity_id in", values, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdNotIn(List<String> values) {
            addCriterion("shop_entity_id not in", values, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdBetween(String value1, String value2) {
            addCriterion("shop_entity_id between", value1, value2, "shopEntityId");
            return this;
        }

        public Criteria andShopEntityIdNotBetween(String value1, String value2) {
            addCriterion("shop_entity_id not between", value1, value2, "shopEntityId");
            return this;
        }

        public Criteria andMallEntityIdIsNull() {
            addCriterion("mall_entity_id is null");
            return this;
        }

        public Criteria andMallEntityIdIsNotNull() {
            addCriterion("mall_entity_id is not null");
            return this;
        }

        public Criteria andMallEntityIdEqualTo(String value) {
            addCriterion("mall_entity_id =", value, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdNotEqualTo(String value) {
            addCriterion("mall_entity_id <>", value, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdGreaterThan(String value) {
            addCriterion("mall_entity_id >", value, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdGreaterThanOrEqualTo(String value) {
            addCriterion("mall_entity_id >=", value, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdLessThan(String value) {
            addCriterion("mall_entity_id <", value, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdLessThanOrEqualTo(String value) {
            addCriterion("mall_entity_id <=", value, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdLike(String value) {
            addCriterion("mall_entity_id like", value, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdNotLike(String value) {
            addCriterion("mall_entity_id not like", value, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdIn(List<String> values) {
            addCriterion("mall_entity_id in", values, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdNotIn(List<String> values) {
            addCriterion("mall_entity_id not in", values, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdBetween(String value1, String value2) {
            addCriterion("mall_entity_id between", value1, value2, "mallEntityId");
            return this;
        }

        public Criteria andMallEntityIdNotBetween(String value1, String value2) {
            addCriterion("mall_entity_id not between", value1, value2, "mallEntityId");
            return this;
        }

        public Criteria andMallTypeIsNull() {
            addCriterion("mall_type is null");
            return this;
        }

        public Criteria andMallTypeIsNotNull() {
            addCriterion("mall_type is not null");
            return this;
        }

        public Criteria andMallTypeEqualTo(Integer value) {
            addCriterion("mall_type =", value, "mallType");
            return this;
        }

        public Criteria andMallTypeNotEqualTo(Integer value) {
            addCriterion("mall_type <>", value, "mallType");
            return this;
        }

        public Criteria andMallTypeGreaterThan(Integer value) {
            addCriterion("mall_type >", value, "mallType");
            return this;
        }

        public Criteria andMallTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("mall_type >=", value, "mallType");
            return this;
        }

        public Criteria andMallTypeLessThan(Integer value) {
            addCriterion("mall_type <", value, "mallType");
            return this;
        }

        public Criteria andMallTypeLessThanOrEqualTo(Integer value) {
            addCriterion("mall_type <=", value, "mallType");
            return this;
        }

        public Criteria andMallTypeIn(List<Integer> values) {
            addCriterion("mall_type in", values, "mallType");
            return this;
        }

        public Criteria andMallTypeNotIn(List<Integer> values) {
            addCriterion("mall_type not in", values, "mallType");
            return this;
        }

        public Criteria andMallTypeBetween(Integer value1, Integer value2) {
            addCriterion("mall_type between", value1, value2, "mallType");
            return this;
        }

        public Criteria andMallTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("mall_type not between", value1, value2, "mallType");
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

        public Criteria andStatusEqualTo(Boolean value) {
            addCriterion("status =", value, "status");
            return this;
        }

        public Criteria andStatusNotEqualTo(Boolean value) {
            addCriterion("status <>", value, "status");
            return this;
        }

        public Criteria andStatusGreaterThan(Boolean value) {
            addCriterion("status >", value, "status");
            return this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Boolean value) {
            addCriterion("status >=", value, "status");
            return this;
        }

        public Criteria andStatusLessThan(Boolean value) {
            addCriterion("status <", value, "status");
            return this;
        }

        public Criteria andStatusLessThanOrEqualTo(Boolean value) {
            addCriterion("status <=", value, "status");
            return this;
        }

        public Criteria andStatusIn(List<Boolean> values) {
            addCriterion("status in", values, "status");
            return this;
        }

        public Criteria andStatusNotIn(List<Boolean> values) {
            addCriterion("status not in", values, "status");
            return this;
        }

        public Criteria andStatusBetween(Boolean value1, Boolean value2) {
            addCriterion("status between", value1, value2, "status");
            return this;
        }

        public Criteria andStatusNotBetween(Boolean value1, Boolean value2) {
            addCriterion("status not between", value1, value2, "status");
            return this;
        }

        public Criteria andAreaIdIsNull() {
            addCriterion("area_id is null");
            return this;
        }

        public Criteria andAreaIdIsNotNull() {
            addCriterion("area_id is not null");
            return this;
        }

        public Criteria andAreaIdEqualTo(String value) {
            addCriterion("area_id =", value, "areaId");
            return this;
        }

        public Criteria andAreaIdNotEqualTo(String value) {
            addCriterion("area_id <>", value, "areaId");
            return this;
        }

        public Criteria andAreaIdGreaterThan(String value) {
            addCriterion("area_id >", value, "areaId");
            return this;
        }

        public Criteria andAreaIdGreaterThanOrEqualTo(String value) {
            addCriterion("area_id >=", value, "areaId");
            return this;
        }

        public Criteria andAreaIdLessThan(String value) {
            addCriterion("area_id <", value, "areaId");
            return this;
        }

        public Criteria andAreaIdLessThanOrEqualTo(String value) {
            addCriterion("area_id <=", value, "areaId");
            return this;
        }

        public Criteria andAreaIdLike(String value) {
            addCriterion("area_id like", value, "areaId");
            return this;
        }

        public Criteria andAreaIdNotLike(String value) {
            addCriterion("area_id not like", value, "areaId");
            return this;
        }

        public Criteria andAreaIdIn(List<String> values) {
            addCriterion("area_id in", values, "areaId");
            return this;
        }

        public Criteria andAreaIdNotIn(List<String> values) {
            addCriterion("area_id not in", values, "areaId");
            return this;
        }

        public Criteria andAreaIdBetween(String value1, String value2) {
            addCriterion("area_id between", value1, value2, "areaId");
            return this;
        }

        public Criteria andAreaIdNotBetween(String value1, String value2) {
            addCriterion("area_id not between", value1, value2, "areaId");
            return this;
        }

        public Criteria andCashTypeIsNull() {
            addCriterion("cash_type is null");
            return this;
        }

        public Criteria andCashTypeIsNotNull() {
            addCriterion("cash_type is not null");
            return this;
        }

        public Criteria andCashTypeEqualTo(Boolean value) {
            addCriterion("cash_type =", value, "cashType");
            return this;
        }

        public Criteria andCashTypeNotEqualTo(Boolean value) {
            addCriterion("cash_type <>", value, "cashType");
            return this;
        }

        public Criteria andCashTypeGreaterThan(Boolean value) {
            addCriterion("cash_type >", value, "cashType");
            return this;
        }

        public Criteria andCashTypeGreaterThanOrEqualTo(Boolean value) {
            addCriterion("cash_type >=", value, "cashType");
            return this;
        }

        public Criteria andCashTypeLessThan(Boolean value) {
            addCriterion("cash_type <", value, "cashType");
            return this;
        }

        public Criteria andCashTypeLessThanOrEqualTo(Boolean value) {
            addCriterion("cash_type <=", value, "cashType");
            return this;
        }

        public Criteria andCashTypeIn(List<Boolean> values) {
            addCriterion("cash_type in", values, "cashType");
            return this;
        }

        public Criteria andCashTypeNotIn(List<Boolean> values) {
            addCriterion("cash_type not in", values, "cashType");
            return this;
        }

        public Criteria andCashTypeBetween(Boolean value1, Boolean value2) {
            addCriterion("cash_type between", value1, value2, "cashType");
            return this;
        }

        public Criteria andCashTypeNotBetween(Boolean value1, Boolean value2) {
            addCriterion("cash_type not between", value1, value2, "cashType");
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

        public Criteria andCreateTimeEqualTo(Integer value) {
            addCriterion("create_time =", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotEqualTo(Integer value) {
            addCriterion("create_time <>", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeGreaterThan(Integer value) {
            addCriterion("create_time >", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("create_time >=", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeLessThan(Integer value) {
            addCriterion("create_time <", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Integer value) {
            addCriterion("create_time <=", value, "createTime");
            return this;
        }

        public Criteria andCreateTimeIn(List<Integer> values) {
            addCriterion("create_time in", values, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotIn(List<Integer> values) {
            addCriterion("create_time not in", values, "createTime");
            return this;
        }

        public Criteria andCreateTimeBetween(Integer value1, Integer value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return this;
        }

        public Criteria andCreateTimeNotBetween(Integer value1, Integer value2) {
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

        public Criteria andOpTimeEqualTo(Integer value) {
            addCriterion("op_time =", value, "opTime");
            return this;
        }

        public Criteria andOpTimeNotEqualTo(Integer value) {
            addCriterion("op_time <>", value, "opTime");
            return this;
        }

        public Criteria andOpTimeGreaterThan(Integer value) {
            addCriterion("op_time >", value, "opTime");
            return this;
        }

        public Criteria andOpTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("op_time >=", value, "opTime");
            return this;
        }

        public Criteria andOpTimeLessThan(Integer value) {
            addCriterion("op_time <", value, "opTime");
            return this;
        }

        public Criteria andOpTimeLessThanOrEqualTo(Integer value) {
            addCriterion("op_time <=", value, "opTime");
            return this;
        }

        public Criteria andOpTimeIn(List<Integer> values) {
            addCriterion("op_time in", values, "opTime");
            return this;
        }

        public Criteria andOpTimeNotIn(List<Integer> values) {
            addCriterion("op_time not in", values, "opTime");
            return this;
        }

        public Criteria andOpTimeBetween(Integer value1, Integer value2) {
            addCriterion("op_time between", value1, value2, "opTime");
            return this;
        }

        public Criteria andOpTimeNotBetween(Integer value1, Integer value2) {
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
    }
}
