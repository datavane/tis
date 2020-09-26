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
import java.math.BigDecimal;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TotalpayinfoCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<TotalpayinfoColEnum> cols = Sets.newHashSet();

    public TotalpayinfoCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected TotalpayinfoCriteria(TotalpayinfoCriteria example) {
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

    public final List<TotalpayinfoColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(TotalpayinfoColEnum... colName) {
        for (TotalpayinfoColEnum c : colName) {
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

        public Criteria andTotalpayIdIsNull() {
            addCriterion("totalpay_id is null");
            return this;
        }

        public Criteria andTotalpayIdIsNotNull() {
            addCriterion("totalpay_id is not null");
            return this;
        }

        public Criteria andTotalpayIdEqualTo(String value) {
            addCriterion("totalpay_id =", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdNotEqualTo(String value) {
            addCriterion("totalpay_id <>", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdGreaterThan(String value) {
            addCriterion("totalpay_id >", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdGreaterThanOrEqualTo(String value) {
            addCriterion("totalpay_id >=", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdLessThan(String value) {
            addCriterion("totalpay_id <", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdLessThanOrEqualTo(String value) {
            addCriterion("totalpay_id <=", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdLike(String value) {
            addCriterion("totalpay_id like", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdNotLike(String value) {
            addCriterion("totalpay_id not like", value, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdIn(List<String> values) {
            addCriterion("totalpay_id in", values, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdNotIn(List<String> values) {
            addCriterion("totalpay_id not in", values, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdBetween(String value1, String value2) {
            addCriterion("totalpay_id between", value1, value2, "totalpayId");
            return this;
        }

        public Criteria andTotalpayIdNotBetween(String value1, String value2) {
            addCriterion("totalpay_id not between", value1, value2, "totalpayId");
            return this;
        }

        public Criteria andCurrDateIsNull() {
            addCriterion("curr_date is null");
            return this;
        }

        public Criteria andCurrDateIsNotNull() {
            addCriterion("curr_date is not null");
            return this;
        }

        public Criteria andCurrDateEqualTo(Date value) {
            addCriterionForJDBCDate("curr_date =", value, "currDate");
            return this;
        }

        public Criteria andCurrDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("curr_date <>", value, "currDate");
            return this;
        }

        public Criteria andCurrDateGreaterThan(Date value) {
            addCriterionForJDBCDate("curr_date >", value, "currDate");
            return this;
        }

        public Criteria andCurrDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("curr_date >=", value, "currDate");
            return this;
        }

        public Criteria andCurrDateLessThan(Date value) {
            addCriterionForJDBCDate("curr_date <", value, "currDate");
            return this;
        }

        public Criteria andCurrDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("curr_date <=", value, "currDate");
            return this;
        }

        public Criteria andCurrDateIn(List<Date> values) {
            addCriterionForJDBCDate("curr_date in", values, "currDate");
            return this;
        }

        public Criteria andCurrDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("curr_date not in", values, "currDate");
            return this;
        }

        public Criteria andCurrDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("curr_date between", value1, value2, "currDate");
            return this;
        }

        public Criteria andCurrDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("curr_date not between", value1, value2, "currDate");
            return this;
        }

        public Criteria andOutfeeIsNull() {
            addCriterion("outfee is null");
            return this;
        }

        public Criteria andOutfeeIsNotNull() {
            addCriterion("outfee is not null");
            return this;
        }

        public Criteria andOutfeeEqualTo(BigDecimal value) {
            addCriterion("outfee =", value, "outfee");
            return this;
        }

        public Criteria andOutfeeNotEqualTo(BigDecimal value) {
            addCriterion("outfee <>", value, "outfee");
            return this;
        }

        public Criteria andOutfeeGreaterThan(BigDecimal value) {
            addCriterion("outfee >", value, "outfee");
            return this;
        }

        public Criteria andOutfeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("outfee >=", value, "outfee");
            return this;
        }

        public Criteria andOutfeeLessThan(BigDecimal value) {
            addCriterion("outfee <", value, "outfee");
            return this;
        }

        public Criteria andOutfeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("outfee <=", value, "outfee");
            return this;
        }

        public Criteria andOutfeeIn(List<BigDecimal> values) {
            addCriterion("outfee in", values, "outfee");
            return this;
        }

        public Criteria andOutfeeNotIn(List<BigDecimal> values) {
            addCriterion("outfee not in", values, "outfee");
            return this;
        }

        public Criteria andOutfeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("outfee between", value1, value2, "outfee");
            return this;
        }

        public Criteria andOutfeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("outfee not between", value1, value2, "outfee");
            return this;
        }

        public Criteria andSourceAmountIsNull() {
            addCriterion("source_amount is null");
            return this;
        }

        public Criteria andSourceAmountIsNotNull() {
            addCriterion("source_amount is not null");
            return this;
        }

        public Criteria andSourceAmountEqualTo(BigDecimal value) {
            addCriterion("source_amount =", value, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountNotEqualTo(BigDecimal value) {
            addCriterion("source_amount <>", value, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountGreaterThan(BigDecimal value) {
            addCriterion("source_amount >", value, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("source_amount >=", value, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountLessThan(BigDecimal value) {
            addCriterion("source_amount <", value, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("source_amount <=", value, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountIn(List<BigDecimal> values) {
            addCriterion("source_amount in", values, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountNotIn(List<BigDecimal> values) {
            addCriterion("source_amount not in", values, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("source_amount between", value1, value2, "sourceAmount");
            return this;
        }

        public Criteria andSourceAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("source_amount not between", value1, value2, "sourceAmount");
            return this;
        }

        public Criteria andDiscountAmountIsNull() {
            addCriterion("discount_amount is null");
            return this;
        }

        public Criteria andDiscountAmountIsNotNull() {
            addCriterion("discount_amount is not null");
            return this;
        }

        public Criteria andDiscountAmountEqualTo(BigDecimal value) {
            addCriterion("discount_amount =", value, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountNotEqualTo(BigDecimal value) {
            addCriterion("discount_amount <>", value, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountGreaterThan(BigDecimal value) {
            addCriterion("discount_amount >", value, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_amount >=", value, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountLessThan(BigDecimal value) {
            addCriterion("discount_amount <", value, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_amount <=", value, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountIn(List<BigDecimal> values) {
            addCriterion("discount_amount in", values, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountNotIn(List<BigDecimal> values) {
            addCriterion("discount_amount not in", values, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_amount between", value1, value2, "discountAmount");
            return this;
        }

        public Criteria andDiscountAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_amount not between", value1, value2, "discountAmount");
            return this;
        }

        public Criteria andResultAmountIsNull() {
            addCriterion("result_amount is null");
            return this;
        }

        public Criteria andResultAmountIsNotNull() {
            addCriterion("result_amount is not null");
            return this;
        }

        public Criteria andResultAmountEqualTo(BigDecimal value) {
            addCriterion("result_amount =", value, "resultAmount");
            return this;
        }

        public Criteria andResultAmountNotEqualTo(BigDecimal value) {
            addCriterion("result_amount <>", value, "resultAmount");
            return this;
        }

        public Criteria andResultAmountGreaterThan(BigDecimal value) {
            addCriterion("result_amount >", value, "resultAmount");
            return this;
        }

        public Criteria andResultAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("result_amount >=", value, "resultAmount");
            return this;
        }

        public Criteria andResultAmountLessThan(BigDecimal value) {
            addCriterion("result_amount <", value, "resultAmount");
            return this;
        }

        public Criteria andResultAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("result_amount <=", value, "resultAmount");
            return this;
        }

        public Criteria andResultAmountIn(List<BigDecimal> values) {
            addCriterion("result_amount in", values, "resultAmount");
            return this;
        }

        public Criteria andResultAmountNotIn(List<BigDecimal> values) {
            addCriterion("result_amount not in", values, "resultAmount");
            return this;
        }

        public Criteria andResultAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("result_amount between", value1, value2, "resultAmount");
            return this;
        }

        public Criteria andResultAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("result_amount not between", value1, value2, "resultAmount");
            return this;
        }

        public Criteria andRecieveAmountIsNull() {
            addCriterion("recieve_amount is null");
            return this;
        }

        public Criteria andRecieveAmountIsNotNull() {
            addCriterion("recieve_amount is not null");
            return this;
        }

        public Criteria andRecieveAmountEqualTo(BigDecimal value) {
            addCriterion("recieve_amount =", value, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountNotEqualTo(BigDecimal value) {
            addCriterion("recieve_amount <>", value, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountGreaterThan(BigDecimal value) {
            addCriterion("recieve_amount >", value, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("recieve_amount >=", value, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountLessThan(BigDecimal value) {
            addCriterion("recieve_amount <", value, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("recieve_amount <=", value, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountIn(List<BigDecimal> values) {
            addCriterion("recieve_amount in", values, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountNotIn(List<BigDecimal> values) {
            addCriterion("recieve_amount not in", values, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("recieve_amount between", value1, value2, "recieveAmount");
            return this;
        }

        public Criteria andRecieveAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("recieve_amount not between", value1, value2, "recieveAmount");
            return this;
        }

        public Criteria andRatioIsNull() {
            addCriterion("ratio is null");
            return this;
        }

        public Criteria andRatioIsNotNull() {
            addCriterion("ratio is not null");
            return this;
        }

        public Criteria andRatioEqualTo(BigDecimal value) {
            addCriterion("ratio =", value, "ratio");
            return this;
        }

        public Criteria andRatioNotEqualTo(BigDecimal value) {
            addCriterion("ratio <>", value, "ratio");
            return this;
        }

        public Criteria andRatioGreaterThan(BigDecimal value) {
            addCriterion("ratio >", value, "ratio");
            return this;
        }

        public Criteria andRatioGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("ratio >=", value, "ratio");
            return this;
        }

        public Criteria andRatioLessThan(BigDecimal value) {
            addCriterion("ratio <", value, "ratio");
            return this;
        }

        public Criteria andRatioLessThanOrEqualTo(BigDecimal value) {
            addCriterion("ratio <=", value, "ratio");
            return this;
        }

        public Criteria andRatioIn(List<BigDecimal> values) {
            addCriterion("ratio in", values, "ratio");
            return this;
        }

        public Criteria andRatioNotIn(List<BigDecimal> values) {
            addCriterion("ratio not in", values, "ratio");
            return this;
        }

        public Criteria andRatioBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ratio between", value1, value2, "ratio");
            return this;
        }

        public Criteria andRatioNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("ratio not between", value1, value2, "ratio");
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

        public Criteria andIsValidEqualTo(Byte value) {
            addCriterion("is_valid =", value, "isValid");
            return this;
        }

        public Criteria andIsValidNotEqualTo(Byte value) {
            addCriterion("is_valid <>", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThan(Byte value) {
            addCriterion("is_valid >", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_valid >=", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThan(Byte value) {
            addCriterion("is_valid <", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThanOrEqualTo(Byte value) {
            addCriterion("is_valid <=", value, "isValid");
            return this;
        }

        public Criteria andIsValidIn(List<Byte> values) {
            addCriterion("is_valid in", values, "isValid");
            return this;
        }

        public Criteria andIsValidNotIn(List<Byte> values) {
            addCriterion("is_valid not in", values, "isValid");
            return this;
        }

        public Criteria andIsValidBetween(Byte value1, Byte value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return this;
        }

        public Criteria andIsValidNotBetween(Byte value1, Byte value2) {
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

        public Criteria andOpUserIdIsNull() {
            addCriterion("op_user_id is null");
            return this;
        }

        public Criteria andOpUserIdIsNotNull() {
            addCriterion("op_user_id is not null");
            return this;
        }

        public Criteria andOpUserIdEqualTo(String value) {
            addCriterion("op_user_id =", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotEqualTo(String value) {
            addCriterion("op_user_id <>", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdGreaterThan(String value) {
            addCriterion("op_user_id >", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("op_user_id >=", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdLessThan(String value) {
            addCriterion("op_user_id <", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdLessThanOrEqualTo(String value) {
            addCriterion("op_user_id <=", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdLike(String value) {
            addCriterion("op_user_id like", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotLike(String value) {
            addCriterion("op_user_id not like", value, "opUserId");
            return this;
        }

        public Criteria andOpUserIdIn(List<String> values) {
            addCriterion("op_user_id in", values, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotIn(List<String> values) {
            addCriterion("op_user_id not in", values, "opUserId");
            return this;
        }

        public Criteria andOpUserIdBetween(String value1, String value2) {
            addCriterion("op_user_id between", value1, value2, "opUserId");
            return this;
        }

        public Criteria andOpUserIdNotBetween(String value1, String value2) {
            addCriterion("op_user_id not between", value1, value2, "opUserId");
            return this;
        }

        public Criteria andDiscountPlanIdIsNull() {
            addCriterion("discount_plan_id is null");
            return this;
        }

        public Criteria andDiscountPlanIdIsNotNull() {
            addCriterion("discount_plan_id is not null");
            return this;
        }

        public Criteria andDiscountPlanIdEqualTo(String value) {
            addCriterion("discount_plan_id =", value, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdNotEqualTo(String value) {
            addCriterion("discount_plan_id <>", value, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdGreaterThan(String value) {
            addCriterion("discount_plan_id >", value, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdGreaterThanOrEqualTo(String value) {
            addCriterion("discount_plan_id >=", value, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdLessThan(String value) {
            addCriterion("discount_plan_id <", value, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdLessThanOrEqualTo(String value) {
            addCriterion("discount_plan_id <=", value, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdLike(String value) {
            addCriterion("discount_plan_id like", value, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdNotLike(String value) {
            addCriterion("discount_plan_id not like", value, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdIn(List<String> values) {
            addCriterion("discount_plan_id in", values, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdNotIn(List<String> values) {
            addCriterion("discount_plan_id not in", values, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdBetween(String value1, String value2) {
            addCriterion("discount_plan_id between", value1, value2, "discountPlanId");
            return this;
        }

        public Criteria andDiscountPlanIdNotBetween(String value1, String value2) {
            addCriterion("discount_plan_id not between", value1, value2, "discountPlanId");
            return this;
        }

        public Criteria andOperatorIsNull() {
            addCriterion("operator is null");
            return this;
        }

        public Criteria andOperatorIsNotNull() {
            addCriterion("operator is not null");
            return this;
        }

        public Criteria andOperatorEqualTo(String value) {
            addCriterion("operator =", value, "operator");
            return this;
        }

        public Criteria andOperatorNotEqualTo(String value) {
            addCriterion("operator <>", value, "operator");
            return this;
        }

        public Criteria andOperatorGreaterThan(String value) {
            addCriterion("operator >", value, "operator");
            return this;
        }

        public Criteria andOperatorGreaterThanOrEqualTo(String value) {
            addCriterion("operator >=", value, "operator");
            return this;
        }

        public Criteria andOperatorLessThan(String value) {
            addCriterion("operator <", value, "operator");
            return this;
        }

        public Criteria andOperatorLessThanOrEqualTo(String value) {
            addCriterion("operator <=", value, "operator");
            return this;
        }

        public Criteria andOperatorLike(String value) {
            addCriterion("operator like", value, "operator");
            return this;
        }

        public Criteria andOperatorNotLike(String value) {
            addCriterion("operator not like", value, "operator");
            return this;
        }

        public Criteria andOperatorIn(List<String> values) {
            addCriterion("operator in", values, "operator");
            return this;
        }

        public Criteria andOperatorNotIn(List<String> values) {
            addCriterion("operator not in", values, "operator");
            return this;
        }

        public Criteria andOperatorBetween(String value1, String value2) {
            addCriterion("operator between", value1, value2, "operator");
            return this;
        }

        public Criteria andOperatorNotBetween(String value1, String value2) {
            addCriterion("operator not between", value1, value2, "operator");
            return this;
        }

        public Criteria andOperateDateIsNull() {
            addCriterion("operate_date is null");
            return this;
        }

        public Criteria andOperateDateIsNotNull() {
            addCriterion("operate_date is not null");
            return this;
        }

        public Criteria andOperateDateEqualTo(Long value) {
            addCriterion("operate_date =", value, "operateDate");
            return this;
        }

        public Criteria andOperateDateNotEqualTo(Long value) {
            addCriterion("operate_date <>", value, "operateDate");
            return this;
        }

        public Criteria andOperateDateGreaterThan(Long value) {
            addCriterion("operate_date >", value, "operateDate");
            return this;
        }

        public Criteria andOperateDateGreaterThanOrEqualTo(Long value) {
            addCriterion("operate_date >=", value, "operateDate");
            return this;
        }

        public Criteria andOperateDateLessThan(Long value) {
            addCriterion("operate_date <", value, "operateDate");
            return this;
        }

        public Criteria andOperateDateLessThanOrEqualTo(Long value) {
            addCriterion("operate_date <=", value, "operateDate");
            return this;
        }

        public Criteria andOperateDateIn(List<Long> values) {
            addCriterion("operate_date in", values, "operateDate");
            return this;
        }

        public Criteria andOperateDateNotIn(List<Long> values) {
            addCriterion("operate_date not in", values, "operateDate");
            return this;
        }

        public Criteria andOperateDateBetween(Long value1, Long value2) {
            addCriterion("operate_date between", value1, value2, "operateDate");
            return this;
        }

        public Criteria andOperateDateNotBetween(Long value1, Long value2) {
            addCriterion("operate_date not between", value1, value2, "operateDate");
            return this;
        }

        public Criteria andCardIdIsNull() {
            addCriterion("card_id is null");
            return this;
        }

        public Criteria andCardIdIsNotNull() {
            addCriterion("card_id is not null");
            return this;
        }

        public Criteria andCardIdEqualTo(String value) {
            addCriterion("card_id =", value, "cardId");
            return this;
        }

        public Criteria andCardIdNotEqualTo(String value) {
            addCriterion("card_id <>", value, "cardId");
            return this;
        }

        public Criteria andCardIdGreaterThan(String value) {
            addCriterion("card_id >", value, "cardId");
            return this;
        }

        public Criteria andCardIdGreaterThanOrEqualTo(String value) {
            addCriterion("card_id >=", value, "cardId");
            return this;
        }

        public Criteria andCardIdLessThan(String value) {
            addCriterion("card_id <", value, "cardId");
            return this;
        }

        public Criteria andCardIdLessThanOrEqualTo(String value) {
            addCriterion("card_id <=", value, "cardId");
            return this;
        }

        public Criteria andCardIdLike(String value) {
            addCriterion("card_id like", value, "cardId");
            return this;
        }

        public Criteria andCardIdNotLike(String value) {
            addCriterion("card_id not like", value, "cardId");
            return this;
        }

        public Criteria andCardIdIn(List<String> values) {
            addCriterion("card_id in", values, "cardId");
            return this;
        }

        public Criteria andCardIdNotIn(List<String> values) {
            addCriterion("card_id not in", values, "cardId");
            return this;
        }

        public Criteria andCardIdBetween(String value1, String value2) {
            addCriterion("card_id between", value1, value2, "cardId");
            return this;
        }

        public Criteria andCardIdNotBetween(String value1, String value2) {
            addCriterion("card_id not between", value1, value2, "cardId");
            return this;
        }

        public Criteria andCardIsNull() {
            addCriterion("card is null");
            return this;
        }

        public Criteria andCardIsNotNull() {
            addCriterion("card is not null");
            return this;
        }

        public Criteria andCardEqualTo(String value) {
            addCriterion("card =", value, "card");
            return this;
        }

        public Criteria andCardNotEqualTo(String value) {
            addCriterion("card <>", value, "card");
            return this;
        }

        public Criteria andCardGreaterThan(String value) {
            addCriterion("card >", value, "card");
            return this;
        }

        public Criteria andCardGreaterThanOrEqualTo(String value) {
            addCriterion("card >=", value, "card");
            return this;
        }

        public Criteria andCardLessThan(String value) {
            addCriterion("card <", value, "card");
            return this;
        }

        public Criteria andCardLessThanOrEqualTo(String value) {
            addCriterion("card <=", value, "card");
            return this;
        }

        public Criteria andCardLike(String value) {
            addCriterion("card like", value, "card");
            return this;
        }

        public Criteria andCardNotLike(String value) {
            addCriterion("card not like", value, "card");
            return this;
        }

        public Criteria andCardIn(List<String> values) {
            addCriterion("card in", values, "card");
            return this;
        }

        public Criteria andCardNotIn(List<String> values) {
            addCriterion("card not in", values, "card");
            return this;
        }

        public Criteria andCardBetween(String value1, String value2) {
            addCriterion("card between", value1, value2, "card");
            return this;
        }

        public Criteria andCardNotBetween(String value1, String value2) {
            addCriterion("card not between", value1, value2, "card");
            return this;
        }

        public Criteria andCardEntityIdIsNull() {
            addCriterion("card_entity_id is null");
            return this;
        }

        public Criteria andCardEntityIdIsNotNull() {
            addCriterion("card_entity_id is not null");
            return this;
        }

        public Criteria andCardEntityIdEqualTo(String value) {
            addCriterion("card_entity_id =", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdNotEqualTo(String value) {
            addCriterion("card_entity_id <>", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdGreaterThan(String value) {
            addCriterion("card_entity_id >", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdGreaterThanOrEqualTo(String value) {
            addCriterion("card_entity_id >=", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdLessThan(String value) {
            addCriterion("card_entity_id <", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdLessThanOrEqualTo(String value) {
            addCriterion("card_entity_id <=", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdLike(String value) {
            addCriterion("card_entity_id like", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdNotLike(String value) {
            addCriterion("card_entity_id not like", value, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdIn(List<String> values) {
            addCriterion("card_entity_id in", values, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdNotIn(List<String> values) {
            addCriterion("card_entity_id not in", values, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdBetween(String value1, String value2) {
            addCriterion("card_entity_id between", value1, value2, "cardEntityId");
            return this;
        }

        public Criteria andCardEntityIdNotBetween(String value1, String value2) {
            addCriterion("card_entity_id not between", value1, value2, "cardEntityId");
            return this;
        }

        public Criteria andIsFullRatioIsNull() {
            addCriterion("is_full_ratio is null");
            return this;
        }

        public Criteria andIsFullRatioIsNotNull() {
            addCriterion("is_full_ratio is not null");
            return this;
        }

        public Criteria andIsFullRatioEqualTo(Byte value) {
            addCriterion("is_full_ratio =", value, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioNotEqualTo(Byte value) {
            addCriterion("is_full_ratio <>", value, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioGreaterThan(Byte value) {
            addCriterion("is_full_ratio >", value, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_full_ratio >=", value, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioLessThan(Byte value) {
            addCriterion("is_full_ratio <", value, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioLessThanOrEqualTo(Byte value) {
            addCriterion("is_full_ratio <=", value, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioIn(List<Byte> values) {
            addCriterion("is_full_ratio in", values, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioNotIn(List<Byte> values) {
            addCriterion("is_full_ratio not in", values, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioBetween(Byte value1, Byte value2) {
            addCriterion("is_full_ratio between", value1, value2, "isFullRatio");
            return this;
        }

        public Criteria andIsFullRatioNotBetween(Byte value1, Byte value2) {
            addCriterion("is_full_ratio not between", value1, value2, "isFullRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioIsNull() {
            addCriterion("is_minconsume_ratio is null");
            return this;
        }

        public Criteria andIsMinconsumeRatioIsNotNull() {
            addCriterion("is_minconsume_ratio is not null");
            return this;
        }

        public Criteria andIsMinconsumeRatioEqualTo(Byte value) {
            addCriterion("is_minconsume_ratio =", value, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioNotEqualTo(Byte value) {
            addCriterion("is_minconsume_ratio <>", value, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioGreaterThan(Byte value) {
            addCriterion("is_minconsume_ratio >", value, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_minconsume_ratio >=", value, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioLessThan(Byte value) {
            addCriterion("is_minconsume_ratio <", value, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioLessThanOrEqualTo(Byte value) {
            addCriterion("is_minconsume_ratio <=", value, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioIn(List<Byte> values) {
            addCriterion("is_minconsume_ratio in", values, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioNotIn(List<Byte> values) {
            addCriterion("is_minconsume_ratio not in", values, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioBetween(Byte value1, Byte value2) {
            addCriterion("is_minconsume_ratio between", value1, value2, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsMinconsumeRatioNotBetween(Byte value1, Byte value2) {
            addCriterion("is_minconsume_ratio not between", value1, value2, "isMinconsumeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioIsNull() {
            addCriterion("is_servicefee_ratio is null");
            return this;
        }

        public Criteria andIsServicefeeRatioIsNotNull() {
            addCriterion("is_servicefee_ratio is not null");
            return this;
        }

        public Criteria andIsServicefeeRatioEqualTo(Byte value) {
            addCriterion("is_servicefee_ratio =", value, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioNotEqualTo(Byte value) {
            addCriterion("is_servicefee_ratio <>", value, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioGreaterThan(Byte value) {
            addCriterion("is_servicefee_ratio >", value, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_servicefee_ratio >=", value, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioLessThan(Byte value) {
            addCriterion("is_servicefee_ratio <", value, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioLessThanOrEqualTo(Byte value) {
            addCriterion("is_servicefee_ratio <=", value, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioIn(List<Byte> values) {
            addCriterion("is_servicefee_ratio in", values, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioNotIn(List<Byte> values) {
            addCriterion("is_servicefee_ratio not in", values, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioBetween(Byte value1, Byte value2) {
            addCriterion("is_servicefee_ratio between", value1, value2, "isServicefeeRatio");
            return this;
        }

        public Criteria andIsServicefeeRatioNotBetween(Byte value1, Byte value2) {
            addCriterion("is_servicefee_ratio not between", value1, value2, "isServicefeeRatio");
            return this;
        }

        public Criteria andInvoiceCodeIsNull() {
            addCriterion("invoice_code is null");
            return this;
        }

        public Criteria andInvoiceCodeIsNotNull() {
            addCriterion("invoice_code is not null");
            return this;
        }

        public Criteria andInvoiceCodeEqualTo(String value) {
            addCriterion("invoice_code =", value, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeNotEqualTo(String value) {
            addCriterion("invoice_code <>", value, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeGreaterThan(String value) {
            addCriterion("invoice_code >", value, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeGreaterThanOrEqualTo(String value) {
            addCriterion("invoice_code >=", value, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeLessThan(String value) {
            addCriterion("invoice_code <", value, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeLessThanOrEqualTo(String value) {
            addCriterion("invoice_code <=", value, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeLike(String value) {
            addCriterion("invoice_code like", value, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeNotLike(String value) {
            addCriterion("invoice_code not like", value, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeIn(List<String> values) {
            addCriterion("invoice_code in", values, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeNotIn(List<String> values) {
            addCriterion("invoice_code not in", values, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeBetween(String value1, String value2) {
            addCriterion("invoice_code between", value1, value2, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceCodeNotBetween(String value1, String value2) {
            addCriterion("invoice_code not between", value1, value2, "invoiceCode");
            return this;
        }

        public Criteria andInvoiceMemoIsNull() {
            addCriterion("invoice_memo is null");
            return this;
        }

        public Criteria andInvoiceMemoIsNotNull() {
            addCriterion("invoice_memo is not null");
            return this;
        }

        public Criteria andInvoiceMemoEqualTo(String value) {
            addCriterion("invoice_memo =", value, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoNotEqualTo(String value) {
            addCriterion("invoice_memo <>", value, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoGreaterThan(String value) {
            addCriterion("invoice_memo >", value, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoGreaterThanOrEqualTo(String value) {
            addCriterion("invoice_memo >=", value, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoLessThan(String value) {
            addCriterion("invoice_memo <", value, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoLessThanOrEqualTo(String value) {
            addCriterion("invoice_memo <=", value, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoLike(String value) {
            addCriterion("invoice_memo like", value, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoNotLike(String value) {
            addCriterion("invoice_memo not like", value, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoIn(List<String> values) {
            addCriterion("invoice_memo in", values, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoNotIn(List<String> values) {
            addCriterion("invoice_memo not in", values, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoBetween(String value1, String value2) {
            addCriterion("invoice_memo between", value1, value2, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceMemoNotBetween(String value1, String value2) {
            addCriterion("invoice_memo not between", value1, value2, "invoiceMemo");
            return this;
        }

        public Criteria andInvoiceIsNull() {
            addCriterion("invoice is null");
            return this;
        }

        public Criteria andInvoiceIsNotNull() {
            addCriterion("invoice is not null");
            return this;
        }

        public Criteria andInvoiceEqualTo(BigDecimal value) {
            addCriterion("invoice =", value, "invoice");
            return this;
        }

        public Criteria andInvoiceNotEqualTo(BigDecimal value) {
            addCriterion("invoice <>", value, "invoice");
            return this;
        }

        public Criteria andInvoiceGreaterThan(BigDecimal value) {
            addCriterion("invoice >", value, "invoice");
            return this;
        }

        public Criteria andInvoiceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("invoice >=", value, "invoice");
            return this;
        }

        public Criteria andInvoiceLessThan(BigDecimal value) {
            addCriterion("invoice <", value, "invoice");
            return this;
        }

        public Criteria andInvoiceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("invoice <=", value, "invoice");
            return this;
        }

        public Criteria andInvoiceIn(List<BigDecimal> values) {
            addCriterion("invoice in", values, "invoice");
            return this;
        }

        public Criteria andInvoiceNotIn(List<BigDecimal> values) {
            addCriterion("invoice not in", values, "invoice");
            return this;
        }

        public Criteria andInvoiceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("invoice between", value1, value2, "invoice");
            return this;
        }

        public Criteria andInvoiceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("invoice not between", value1, value2, "invoice");
            return this;
        }

        public Criteria andOverStatusIsNull() {
            addCriterion("over_status is null");
            return this;
        }

        public Criteria andOverStatusIsNotNull() {
            addCriterion("over_status is not null");
            return this;
        }

        public Criteria andOverStatusEqualTo(Byte value) {
            addCriterion("over_status =", value, "overStatus");
            return this;
        }

        public Criteria andOverStatusNotEqualTo(Byte value) {
            addCriterion("over_status <>", value, "overStatus");
            return this;
        }

        public Criteria andOverStatusGreaterThan(Byte value) {
            addCriterion("over_status >", value, "overStatus");
            return this;
        }

        public Criteria andOverStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("over_status >=", value, "overStatus");
            return this;
        }

        public Criteria andOverStatusLessThan(Byte value) {
            addCriterion("over_status <", value, "overStatus");
            return this;
        }

        public Criteria andOverStatusLessThanOrEqualTo(Byte value) {
            addCriterion("over_status <=", value, "overStatus");
            return this;
        }

        public Criteria andOverStatusIn(List<Byte> values) {
            addCriterion("over_status in", values, "overStatus");
            return this;
        }

        public Criteria andOverStatusNotIn(List<Byte> values) {
            addCriterion("over_status not in", values, "overStatus");
            return this;
        }

        public Criteria andOverStatusBetween(Byte value1, Byte value2) {
            addCriterion("over_status between", value1, value2, "overStatus");
            return this;
        }

        public Criteria andOverStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("over_status not between", value1, value2, "overStatus");
            return this;
        }

        public Criteria andIsHideIsNull() {
            addCriterion("is_hide is null");
            return this;
        }

        public Criteria andIsHideIsNotNull() {
            addCriterion("is_hide is not null");
            return this;
        }

        public Criteria andIsHideEqualTo(Byte value) {
            addCriterion("is_hide =", value, "isHide");
            return this;
        }

        public Criteria andIsHideNotEqualTo(Byte value) {
            addCriterion("is_hide <>", value, "isHide");
            return this;
        }

        public Criteria andIsHideGreaterThan(Byte value) {
            addCriterion("is_hide >", value, "isHide");
            return this;
        }

        public Criteria andIsHideGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_hide >=", value, "isHide");
            return this;
        }

        public Criteria andIsHideLessThan(Byte value) {
            addCriterion("is_hide <", value, "isHide");
            return this;
        }

        public Criteria andIsHideLessThanOrEqualTo(Byte value) {
            addCriterion("is_hide <=", value, "isHide");
            return this;
        }

        public Criteria andIsHideIn(List<Byte> values) {
            addCriterion("is_hide in", values, "isHide");
            return this;
        }

        public Criteria andIsHideNotIn(List<Byte> values) {
            addCriterion("is_hide not in", values, "isHide");
            return this;
        }

        public Criteria andIsHideBetween(Byte value1, Byte value2) {
            addCriterion("is_hide between", value1, value2, "isHide");
            return this;
        }

        public Criteria andIsHideNotBetween(Byte value1, Byte value2) {
            addCriterion("is_hide not between", value1, value2, "isHide");
            return this;
        }

        public Criteria andLoadTimeIsNull() {
            addCriterion("load_time is null");
            return this;
        }

        public Criteria andLoadTimeIsNotNull() {
            addCriterion("load_time is not null");
            return this;
        }

        public Criteria andLoadTimeEqualTo(Integer value) {
            addCriterion("load_time =", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeNotEqualTo(Integer value) {
            addCriterion("load_time <>", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeGreaterThan(Integer value) {
            addCriterion("load_time >", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("load_time >=", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeLessThan(Integer value) {
            addCriterion("load_time <", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeLessThanOrEqualTo(Integer value) {
            addCriterion("load_time <=", value, "loadTime");
            return this;
        }

        public Criteria andLoadTimeIn(List<Integer> values) {
            addCriterion("load_time in", values, "loadTime");
            return this;
        }

        public Criteria andLoadTimeNotIn(List<Integer> values) {
            addCriterion("load_time not in", values, "loadTime");
            return this;
        }

        public Criteria andLoadTimeBetween(Integer value1, Integer value2) {
            addCriterion("load_time between", value1, value2, "loadTime");
            return this;
        }

        public Criteria andLoadTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("load_time not between", value1, value2, "loadTime");
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

        public Criteria andModifyTimeEqualTo(Integer value) {
            addCriterion("modify_time =", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotEqualTo(Integer value) {
            addCriterion("modify_time <>", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeGreaterThan(Integer value) {
            addCriterion("modify_time >", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("modify_time >=", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeLessThan(Integer value) {
            addCriterion("modify_time <", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeLessThanOrEqualTo(Integer value) {
            addCriterion("modify_time <=", value, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeIn(List<Integer> values) {
            addCriterion("modify_time in", values, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotIn(List<Integer> values) {
            addCriterion("modify_time not in", values, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeBetween(Integer value1, Integer value2) {
            addCriterion("modify_time between", value1, value2, "modifyTime");
            return this;
        }

        public Criteria andModifyTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("modify_time not between", value1, value2, "modifyTime");
            return this;
        }

        public Criteria andPrintnum1IsNull() {
            addCriterion("printnum1 is null");
            return this;
        }

        public Criteria andPrintnum1IsNotNull() {
            addCriterion("printnum1 is not null");
            return this;
        }

        public Criteria andPrintnum1EqualTo(Integer value) {
            addCriterion("printnum1 =", value, "printnum1");
            return this;
        }

        public Criteria andPrintnum1NotEqualTo(Integer value) {
            addCriterion("printnum1 <>", value, "printnum1");
            return this;
        }

        public Criteria andPrintnum1GreaterThan(Integer value) {
            addCriterion("printnum1 >", value, "printnum1");
            return this;
        }

        public Criteria andPrintnum1GreaterThanOrEqualTo(Integer value) {
            addCriterion("printnum1 >=", value, "printnum1");
            return this;
        }

        public Criteria andPrintnum1LessThan(Integer value) {
            addCriterion("printnum1 <", value, "printnum1");
            return this;
        }

        public Criteria andPrintnum1LessThanOrEqualTo(Integer value) {
            addCriterion("printnum1 <=", value, "printnum1");
            return this;
        }

        public Criteria andPrintnum1In(List<Integer> values) {
            addCriterion("printnum1 in", values, "printnum1");
            return this;
        }

        public Criteria andPrintnum1NotIn(List<Integer> values) {
            addCriterion("printnum1 not in", values, "printnum1");
            return this;
        }

        public Criteria andPrintnum1Between(Integer value1, Integer value2) {
            addCriterion("printnum1 between", value1, value2, "printnum1");
            return this;
        }

        public Criteria andPrintnum1NotBetween(Integer value1, Integer value2) {
            addCriterion("printnum1 not between", value1, value2, "printnum1");
            return this;
        }

        public Criteria andPrintnum2IsNull() {
            addCriterion("printnum2 is null");
            return this;
        }

        public Criteria andPrintnum2IsNotNull() {
            addCriterion("printnum2 is not null");
            return this;
        }

        public Criteria andPrintnum2EqualTo(Integer value) {
            addCriterion("printnum2 =", value, "printnum2");
            return this;
        }

        public Criteria andPrintnum2NotEqualTo(Integer value) {
            addCriterion("printnum2 <>", value, "printnum2");
            return this;
        }

        public Criteria andPrintnum2GreaterThan(Integer value) {
            addCriterion("printnum2 >", value, "printnum2");
            return this;
        }

        public Criteria andPrintnum2GreaterThanOrEqualTo(Integer value) {
            addCriterion("printnum2 >=", value, "printnum2");
            return this;
        }

        public Criteria andPrintnum2LessThan(Integer value) {
            addCriterion("printnum2 <", value, "printnum2");
            return this;
        }

        public Criteria andPrintnum2LessThanOrEqualTo(Integer value) {
            addCriterion("printnum2 <=", value, "printnum2");
            return this;
        }

        public Criteria andPrintnum2In(List<Integer> values) {
            addCriterion("printnum2 in", values, "printnum2");
            return this;
        }

        public Criteria andPrintnum2NotIn(List<Integer> values) {
            addCriterion("printnum2 not in", values, "printnum2");
            return this;
        }

        public Criteria andPrintnum2Between(Integer value1, Integer value2) {
            addCriterion("printnum2 between", value1, value2, "printnum2");
            return this;
        }

        public Criteria andPrintnum2NotBetween(Integer value1, Integer value2) {
            addCriterion("printnum2 not between", value1, value2, "printnum2");
            return this;
        }

        public Criteria andCouponDiscountIsNull() {
            addCriterion("coupon_discount is null");
            return this;
        }

        public Criteria andCouponDiscountIsNotNull() {
            addCriterion("coupon_discount is not null");
            return this;
        }

        public Criteria andCouponDiscountEqualTo(BigDecimal value) {
            addCriterion("coupon_discount =", value, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountNotEqualTo(BigDecimal value) {
            addCriterion("coupon_discount <>", value, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountGreaterThan(BigDecimal value) {
            addCriterion("coupon_discount >", value, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("coupon_discount >=", value, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountLessThan(BigDecimal value) {
            addCriterion("coupon_discount <", value, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("coupon_discount <=", value, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountIn(List<BigDecimal> values) {
            addCriterion("coupon_discount in", values, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountNotIn(List<BigDecimal> values) {
            addCriterion("coupon_discount not in", values, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("coupon_discount between", value1, value2, "couponDiscount");
            return this;
        }

        public Criteria andCouponDiscountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("coupon_discount not between", value1, value2, "couponDiscount");
            return this;
        }

        public Criteria andDiscountAmountReceivablesIsNull() {
            addCriterion("discount_amount_receivables is null");
            return this;
        }

        public Criteria andDiscountAmountReceivablesIsNotNull() {
            addCriterion("discount_amount_receivables is not null");
            return this;
        }

        public Criteria andDiscountAmountReceivablesEqualTo(BigDecimal value) {
            addCriterion("discount_amount_receivables =", value, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesNotEqualTo(BigDecimal value) {
            addCriterion("discount_amount_receivables <>", value, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesGreaterThan(BigDecimal value) {
            addCriterion("discount_amount_receivables >", value, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_amount_receivables >=", value, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesLessThan(BigDecimal value) {
            addCriterion("discount_amount_receivables <", value, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesLessThanOrEqualTo(BigDecimal value) {
            addCriterion("discount_amount_receivables <=", value, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesIn(List<BigDecimal> values) {
            addCriterion("discount_amount_receivables in", values, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesNotIn(List<BigDecimal> values) {
            addCriterion("discount_amount_receivables not in", values, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_amount_receivables between", value1, value2, "discountAmountReceivables");
            return this;
        }

        public Criteria andDiscountAmountReceivablesNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("discount_amount_receivables not between", value1, value2, "discountAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesIsNull() {
            addCriterion("result_amount_receivables is null");
            return this;
        }

        public Criteria andResultAmountReceivablesIsNotNull() {
            addCriterion("result_amount_receivables is not null");
            return this;
        }

        public Criteria andResultAmountReceivablesEqualTo(BigDecimal value) {
            addCriterion("result_amount_receivables =", value, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesNotEqualTo(BigDecimal value) {
            addCriterion("result_amount_receivables <>", value, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesGreaterThan(BigDecimal value) {
            addCriterion("result_amount_receivables >", value, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("result_amount_receivables >=", value, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesLessThan(BigDecimal value) {
            addCriterion("result_amount_receivables <", value, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesLessThanOrEqualTo(BigDecimal value) {
            addCriterion("result_amount_receivables <=", value, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesIn(List<BigDecimal> values) {
            addCriterion("result_amount_receivables in", values, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesNotIn(List<BigDecimal> values) {
            addCriterion("result_amount_receivables not in", values, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("result_amount_receivables between", value1, value2, "resultAmountReceivables");
            return this;
        }

        public Criteria andResultAmountReceivablesNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("result_amount_receivables not between", value1, value2, "resultAmountReceivables");
            return this;
        }
    }
}
