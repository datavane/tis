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
import com.qlangtech.tis.ibatis.BasicCriteria;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GridFieldCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<GridFieldColEnum> cols = Sets.newHashSet();

    public GridFieldCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected GridFieldCriteria(GridFieldCriteria example) {
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

    public final List<GridFieldColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(GridFieldColEnum... colName) {
        for (GridFieldColEnum c : colName) {
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

        public Criteria andGridNameIsNull() {
            addCriterion("grid_name is null");
            return this;
        }

        public Criteria andGridNameIsNotNull() {
            addCriterion("grid_name is not null");
            return this;
        }

        public Criteria andGridNameEqualTo(String value) {
            addCriterion("grid_name =", value, "gridName");
            return this;
        }

        public Criteria andGridNameNotEqualTo(String value) {
            addCriterion("grid_name <>", value, "gridName");
            return this;
        }

        public Criteria andGridNameGreaterThan(String value) {
            addCriterion("grid_name >", value, "gridName");
            return this;
        }

        public Criteria andGridNameGreaterThanOrEqualTo(String value) {
            addCriterion("grid_name >=", value, "gridName");
            return this;
        }

        public Criteria andGridNameLessThan(String value) {
            addCriterion("grid_name <", value, "gridName");
            return this;
        }

        public Criteria andGridNameLessThanOrEqualTo(String value) {
            addCriterion("grid_name <=", value, "gridName");
            return this;
        }

        public Criteria andGridNameLike(String value) {
            addCriterion("grid_name like", value, "gridName");
            return this;
        }

        public Criteria andGridNameNotLike(String value) {
            addCriterion("grid_name not like", value, "gridName");
            return this;
        }

        public Criteria andGridNameIn(List<String> values) {
            addCriterion("grid_name in", values, "gridName");
            return this;
        }

        public Criteria andGridNameNotIn(List<String> values) {
            addCriterion("grid_name not in", values, "gridName");
            return this;
        }

        public Criteria andGridNameBetween(String value1, String value2) {
            addCriterion("grid_name between", value1, value2, "gridName");
            return this;
        }

        public Criteria andGridNameNotBetween(String value1, String value2) {
            addCriterion("grid_name not between", value1, value2, "gridName");
            return this;
        }

        public Criteria andFieldNameIsNull() {
            addCriterion("field_name is null");
            return this;
        }

        public Criteria andFieldNameIsNotNull() {
            addCriterion("field_name is not null");
            return this;
        }

        public Criteria andFieldNameEqualTo(String value) {
            addCriterion("field_name =", value, "fieldName");
            return this;
        }

        public Criteria andFieldNameNotEqualTo(String value) {
            addCriterion("field_name <>", value, "fieldName");
            return this;
        }

        public Criteria andFieldNameGreaterThan(String value) {
            addCriterion("field_name >", value, "fieldName");
            return this;
        }

        public Criteria andFieldNameGreaterThanOrEqualTo(String value) {
            addCriterion("field_name >=", value, "fieldName");
            return this;
        }

        public Criteria andFieldNameLessThan(String value) {
            addCriterion("field_name <", value, "fieldName");
            return this;
        }

        public Criteria andFieldNameLessThanOrEqualTo(String value) {
            addCriterion("field_name <=", value, "fieldName");
            return this;
        }

        public Criteria andFieldNameLike(String value) {
            addCriterion("field_name like", value, "fieldName");
            return this;
        }

        public Criteria andFieldNameNotLike(String value) {
            addCriterion("field_name not like", value, "fieldName");
            return this;
        }

        public Criteria andFieldNameIn(List<String> values) {
            addCriterion("field_name in", values, "fieldName");
            return this;
        }

        public Criteria andFieldNameNotIn(List<String> values) {
            addCriterion("field_name not in", values, "fieldName");
            return this;
        }

        public Criteria andFieldNameBetween(String value1, String value2) {
            addCriterion("field_name between", value1, value2, "fieldName");
            return this;
        }

        public Criteria andFieldNameNotBetween(String value1, String value2) {
            addCriterion("field_name not between", value1, value2, "fieldName");
            return this;
        }

        public Criteria andFieldCaptionIsNull() {
            addCriterion("field_caption is null");
            return this;
        }

        public Criteria andFieldCaptionIsNotNull() {
            addCriterion("field_caption is not null");
            return this;
        }

        public Criteria andFieldCaptionEqualTo(String value) {
            addCriterion("field_caption =", value, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionNotEqualTo(String value) {
            addCriterion("field_caption <>", value, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionGreaterThan(String value) {
            addCriterion("field_caption >", value, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionGreaterThanOrEqualTo(String value) {
            addCriterion("field_caption >=", value, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionLessThan(String value) {
            addCriterion("field_caption <", value, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionLessThanOrEqualTo(String value) {
            addCriterion("field_caption <=", value, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionLike(String value) {
            addCriterion("field_caption like", value, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionNotLike(String value) {
            addCriterion("field_caption not like", value, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionIn(List<String> values) {
            addCriterion("field_caption in", values, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionNotIn(List<String> values) {
            addCriterion("field_caption not in", values, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionBetween(String value1, String value2) {
            addCriterion("field_caption between", value1, value2, "fieldCaption");
            return this;
        }

        public Criteria andFieldCaptionNotBetween(String value1, String value2) {
            addCriterion("field_caption not between", value1, value2, "fieldCaption");
            return this;
        }

        public Criteria andDisplayOrderIsNull() {
            addCriterion("display_order is null");
            return this;
        }

        public Criteria andDisplayOrderIsNotNull() {
            addCriterion("display_order is not null");
            return this;
        }

        public Criteria andDisplayOrderEqualTo(Integer value) {
            addCriterion("display_order =", value, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderNotEqualTo(Integer value) {
            addCriterion("display_order <>", value, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderGreaterThan(Integer value) {
            addCriterion("display_order >", value, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderGreaterThanOrEqualTo(Integer value) {
            addCriterion("display_order >=", value, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderLessThan(Integer value) {
            addCriterion("display_order <", value, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderLessThanOrEqualTo(Integer value) {
            addCriterion("display_order <=", value, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderIn(List<Integer> values) {
            addCriterion("display_order in", values, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderNotIn(List<Integer> values) {
            addCriterion("display_order not in", values, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderBetween(Integer value1, Integer value2) {
            addCriterion("display_order between", value1, value2, "displayOrder");
            return this;
        }

        public Criteria andDisplayOrderNotBetween(Integer value1, Integer value2) {
            addCriterion("display_order not between", value1, value2, "displayOrder");
            return this;
        }

        public Criteria andFieldDefaultValueIsNull() {
            addCriterion("field_default_value is null");
            return this;
        }

        public Criteria andFieldDefaultValueIsNotNull() {
            addCriterion("field_default_value is not null");
            return this;
        }

        public Criteria andFieldDefaultValueEqualTo(String value) {
            addCriterion("field_default_value =", value, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueNotEqualTo(String value) {
            addCriterion("field_default_value <>", value, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueGreaterThan(String value) {
            addCriterion("field_default_value >", value, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueGreaterThanOrEqualTo(String value) {
            addCriterion("field_default_value >=", value, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueLessThan(String value) {
            addCriterion("field_default_value <", value, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueLessThanOrEqualTo(String value) {
            addCriterion("field_default_value <=", value, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueLike(String value) {
            addCriterion("field_default_value like", value, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueNotLike(String value) {
            addCriterion("field_default_value not like", value, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueIn(List<String> values) {
            addCriterion("field_default_value in", values, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueNotIn(List<String> values) {
            addCriterion("field_default_value not in", values, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueBetween(String value1, String value2) {
            addCriterion("field_default_value between", value1, value2, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldDefaultValueNotBetween(String value1, String value2) {
            addCriterion("field_default_value not between", value1, value2, "fieldDefaultValue");
            return this;
        }

        public Criteria andFieldCanUsedIsNull() {
            addCriterion("field_can_used is null");
            return this;
        }

        public Criteria andFieldCanUsedIsNotNull() {
            addCriterion("field_can_used is not null");
            return this;
        }

        public Criteria andFieldCanUsedEqualTo(String value) {
            addCriterion("field_can_used =", value, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedNotEqualTo(String value) {
            addCriterion("field_can_used <>", value, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedGreaterThan(String value) {
            addCriterion("field_can_used >", value, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedGreaterThanOrEqualTo(String value) {
            addCriterion("field_can_used >=", value, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedLessThan(String value) {
            addCriterion("field_can_used <", value, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedLessThanOrEqualTo(String value) {
            addCriterion("field_can_used <=", value, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedLike(String value) {
            addCriterion("field_can_used like", value, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedNotLike(String value) {
            addCriterion("field_can_used not like", value, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedIn(List<String> values) {
            addCriterion("field_can_used in", values, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedNotIn(List<String> values) {
            addCriterion("field_can_used not in", values, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedBetween(String value1, String value2) {
            addCriterion("field_can_used between", value1, value2, "fieldCanUsed");
            return this;
        }

        public Criteria andFieldCanUsedNotBetween(String value1, String value2) {
            addCriterion("field_can_used not between", value1, value2, "fieldCanUsed");
            return this;
        }

        public Criteria andDicNoIsNull() {
            addCriterion("dic_no is null");
            return this;
        }

        public Criteria andDicNoIsNotNull() {
            addCriterion("dic_no is not null");
            return this;
        }

        public Criteria andDicNoEqualTo(String value) {
            addCriterion("dic_no =", value, "dicNo");
            return this;
        }

        public Criteria andDicNoNotEqualTo(String value) {
            addCriterion("dic_no <>", value, "dicNo");
            return this;
        }

        public Criteria andDicNoGreaterThan(String value) {
            addCriterion("dic_no >", value, "dicNo");
            return this;
        }

        public Criteria andDicNoGreaterThanOrEqualTo(String value) {
            addCriterion("dic_no >=", value, "dicNo");
            return this;
        }

        public Criteria andDicNoLessThan(String value) {
            addCriterion("dic_no <", value, "dicNo");
            return this;
        }

        public Criteria andDicNoLessThanOrEqualTo(String value) {
            addCriterion("dic_no <=", value, "dicNo");
            return this;
        }

        public Criteria andDicNoLike(String value) {
            addCriterion("dic_no like", value, "dicNo");
            return this;
        }

        public Criteria andDicNoNotLike(String value) {
            addCriterion("dic_no not like", value, "dicNo");
            return this;
        }

        public Criteria andDicNoIn(List<String> values) {
            addCriterion("dic_no in", values, "dicNo");
            return this;
        }

        public Criteria andDicNoNotIn(List<String> values) {
            addCriterion("dic_no not in", values, "dicNo");
            return this;
        }

        public Criteria andDicNoBetween(String value1, String value2) {
            addCriterion("dic_no between", value1, value2, "dicNo");
            return this;
        }

        public Criteria andDicNoNotBetween(String value1, String value2) {
            addCriterion("dic_no not between", value1, value2, "dicNo");
            return this;
        }

        public Criteria andFieldTypeIsNull() {
            addCriterion("field_type is null");
            return this;
        }

        public Criteria andFieldTypeIsNotNull() {
            addCriterion("field_type is not null");
            return this;
        }

        public Criteria andFieldTypeEqualTo(Byte value) {
            addCriterion("field_type =", value, "fieldType");
            return this;
        }

        public Criteria andFieldTypeNotEqualTo(Byte value) {
            addCriterion("field_type <>", value, "fieldType");
            return this;
        }

        public Criteria andFieldTypeGreaterThan(Byte value) {
            addCriterion("field_type >", value, "fieldType");
            return this;
        }

        public Criteria andFieldTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("field_type >=", value, "fieldType");
            return this;
        }

        public Criteria andFieldTypeLessThan(Byte value) {
            addCriterion("field_type <", value, "fieldType");
            return this;
        }

        public Criteria andFieldTypeLessThanOrEqualTo(Byte value) {
            addCriterion("field_type <=", value, "fieldType");
            return this;
        }

        public Criteria andFieldTypeIn(List<Byte> values) {
            addCriterion("field_type in", values, "fieldType");
            return this;
        }

        public Criteria andFieldTypeNotIn(List<Byte> values) {
            addCriterion("field_type not in", values, "fieldType");
            return this;
        }

        public Criteria andFieldTypeBetween(Byte value1, Byte value2) {
            addCriterion("field_type between", value1, value2, "fieldType");
            return this;
        }

        public Criteria andFieldTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("field_type not between", value1, value2, "fieldType");
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
    }
}
