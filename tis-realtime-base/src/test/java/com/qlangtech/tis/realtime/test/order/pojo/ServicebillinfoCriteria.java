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
public class ServicebillinfoCriteria extends BasicCriteria {

    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<ServicebillinfoColEnum> cols = Sets.newHashSet();

    public ServicebillinfoCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected ServicebillinfoCriteria(ServicebillinfoCriteria example) {
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

    public final List<ServicebillinfoColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(ServicebillinfoColEnum... colName) {
        for (ServicebillinfoColEnum c : colName) {
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

        public Criteria andServicebillIdIsNull() {
            addCriterion("servicebill_id is null");
            return this;
        }

        public Criteria andServicebillIdIsNotNull() {
            addCriterion("servicebill_id is not null");
            return this;
        }

        public Criteria andServicebillIdEqualTo(String value) {
            addCriterion("servicebill_id =", value, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdNotEqualTo(String value) {
            addCriterion("servicebill_id <>", value, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdGreaterThan(String value) {
            addCriterion("servicebill_id >", value, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdGreaterThanOrEqualTo(String value) {
            addCriterion("servicebill_id >=", value, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdLessThan(String value) {
            addCriterion("servicebill_id <", value, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdLessThanOrEqualTo(String value) {
            addCriterion("servicebill_id <=", value, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdLike(String value) {
            addCriterion("servicebill_id like", value, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdNotLike(String value) {
            addCriterion("servicebill_id not like", value, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdIn(List<String> values) {
            addCriterion("servicebill_id in", values, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdNotIn(List<String> values) {
            addCriterion("servicebill_id not in", values, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdBetween(String value1, String value2) {
            addCriterion("servicebill_id between", value1, value2, "servicebillId");
            return this;
        }

        public Criteria andServicebillIdNotBetween(String value1, String value2) {
            addCriterion("servicebill_id not between", value1, value2, "servicebillId");
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

        public Criteria andOriginAmountIsNull() {
            addCriterion("origin_amount is null");
            return this;
        }

        public Criteria andOriginAmountIsNotNull() {
            addCriterion("origin_amount is not null");
            return this;
        }

        public Criteria andOriginAmountEqualTo(BigDecimal value) {
            addCriterion("origin_amount =", value, "originAmount");
            return this;
        }

        public Criteria andOriginAmountNotEqualTo(BigDecimal value) {
            addCriterion("origin_amount <>", value, "originAmount");
            return this;
        }

        public Criteria andOriginAmountGreaterThan(BigDecimal value) {
            addCriterion("origin_amount >", value, "originAmount");
            return this;
        }

        public Criteria andOriginAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_amount >=", value, "originAmount");
            return this;
        }

        public Criteria andOriginAmountLessThan(BigDecimal value) {
            addCriterion("origin_amount <", value, "originAmount");
            return this;
        }

        public Criteria andOriginAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_amount <=", value, "originAmount");
            return this;
        }

        public Criteria andOriginAmountIn(List<BigDecimal> values) {
            addCriterion("origin_amount in", values, "originAmount");
            return this;
        }

        public Criteria andOriginAmountNotIn(List<BigDecimal> values) {
            addCriterion("origin_amount not in", values, "originAmount");
            return this;
        }

        public Criteria andOriginAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_amount between", value1, value2, "originAmount");
            return this;
        }

        public Criteria andOriginAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_amount not between", value1, value2, "originAmount");
            return this;
        }

        public Criteria andOriginServiceChargeIsNull() {
            addCriterion("origin_service_charge is null");
            return this;
        }

        public Criteria andOriginServiceChargeIsNotNull() {
            addCriterion("origin_service_charge is not null");
            return this;
        }

        public Criteria andOriginServiceChargeEqualTo(BigDecimal value) {
            addCriterion("origin_service_charge =", value, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeNotEqualTo(BigDecimal value) {
            addCriterion("origin_service_charge <>", value, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeGreaterThan(BigDecimal value) {
            addCriterion("origin_service_charge >", value, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_service_charge >=", value, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeLessThan(BigDecimal value) {
            addCriterion("origin_service_charge <", value, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_service_charge <=", value, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeIn(List<BigDecimal> values) {
            addCriterion("origin_service_charge in", values, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeNotIn(List<BigDecimal> values) {
            addCriterion("origin_service_charge not in", values, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_service_charge between", value1, value2, "originServiceCharge");
            return this;
        }

        public Criteria andOriginServiceChargeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_service_charge not between", value1, value2, "originServiceCharge");
            return this;
        }

        public Criteria andOriginLeastAmountIsNull() {
            addCriterion("origin_least_amount is null");
            return this;
        }

        public Criteria andOriginLeastAmountIsNotNull() {
            addCriterion("origin_least_amount is not null");
            return this;
        }

        public Criteria andOriginLeastAmountEqualTo(BigDecimal value) {
            addCriterion("origin_least_amount =", value, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountNotEqualTo(BigDecimal value) {
            addCriterion("origin_least_amount <>", value, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountGreaterThan(BigDecimal value) {
            addCriterion("origin_least_amount >", value, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_least_amount >=", value, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountLessThan(BigDecimal value) {
            addCriterion("origin_least_amount <", value, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_least_amount <=", value, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountIn(List<BigDecimal> values) {
            addCriterion("origin_least_amount in", values, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountNotIn(List<BigDecimal> values) {
            addCriterion("origin_least_amount not in", values, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_least_amount between", value1, value2, "originLeastAmount");
            return this;
        }

        public Criteria andOriginLeastAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_least_amount not between", value1, value2, "originLeastAmount");
            return this;
        }

        public Criteria andAgioAmountIsNull() {
            addCriterion("agio_amount is null");
            return this;
        }

        public Criteria andAgioAmountIsNotNull() {
            addCriterion("agio_amount is not null");
            return this;
        }

        public Criteria andAgioAmountEqualTo(BigDecimal value) {
            addCriterion("agio_amount =", value, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountNotEqualTo(BigDecimal value) {
            addCriterion("agio_amount <>", value, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountGreaterThan(BigDecimal value) {
            addCriterion("agio_amount >", value, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_amount >=", value, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountLessThan(BigDecimal value) {
            addCriterion("agio_amount <", value, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_amount <=", value, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountIn(List<BigDecimal> values) {
            addCriterion("agio_amount in", values, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountNotIn(List<BigDecimal> values) {
            addCriterion("agio_amount not in", values, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_amount between", value1, value2, "agioAmount");
            return this;
        }

        public Criteria andAgioAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_amount not between", value1, value2, "agioAmount");
            return this;
        }

        public Criteria andAgioServiceChargeIsNull() {
            addCriterion("agio_service_charge is null");
            return this;
        }

        public Criteria andAgioServiceChargeIsNotNull() {
            addCriterion("agio_service_charge is not null");
            return this;
        }

        public Criteria andAgioServiceChargeEqualTo(BigDecimal value) {
            addCriterion("agio_service_charge =", value, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeNotEqualTo(BigDecimal value) {
            addCriterion("agio_service_charge <>", value, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeGreaterThan(BigDecimal value) {
            addCriterion("agio_service_charge >", value, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_service_charge >=", value, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeLessThan(BigDecimal value) {
            addCriterion("agio_service_charge <", value, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_service_charge <=", value, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeIn(List<BigDecimal> values) {
            addCriterion("agio_service_charge in", values, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeNotIn(List<BigDecimal> values) {
            addCriterion("agio_service_charge not in", values, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_service_charge between", value1, value2, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioServiceChargeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_service_charge not between", value1, value2, "agioServiceCharge");
            return this;
        }

        public Criteria andAgioLeastAmountIsNull() {
            addCriterion("agio_least_amount is null");
            return this;
        }

        public Criteria andAgioLeastAmountIsNotNull() {
            addCriterion("agio_least_amount is not null");
            return this;
        }

        public Criteria andAgioLeastAmountEqualTo(BigDecimal value) {
            addCriterion("agio_least_amount =", value, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountNotEqualTo(BigDecimal value) {
            addCriterion("agio_least_amount <>", value, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountGreaterThan(BigDecimal value) {
            addCriterion("agio_least_amount >", value, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_least_amount >=", value, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountLessThan(BigDecimal value) {
            addCriterion("agio_least_amount <", value, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_least_amount <=", value, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountIn(List<BigDecimal> values) {
            addCriterion("agio_least_amount in", values, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountNotIn(List<BigDecimal> values) {
            addCriterion("agio_least_amount not in", values, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_least_amount between", value1, value2, "agioLeastAmount");
            return this;
        }

        public Criteria andAgioLeastAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_least_amount not between", value1, value2, "agioLeastAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountIsNull() {
            addCriterion("origin_receivables_amount is null");
            return this;
        }

        public Criteria andOriginReceivablesAmountIsNotNull() {
            addCriterion("origin_receivables_amount is not null");
            return this;
        }

        public Criteria andOriginReceivablesAmountEqualTo(BigDecimal value) {
            addCriterion("origin_receivables_amount =", value, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountNotEqualTo(BigDecimal value) {
            addCriterion("origin_receivables_amount <>", value, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountGreaterThan(BigDecimal value) {
            addCriterion("origin_receivables_amount >", value, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_receivables_amount >=", value, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountLessThan(BigDecimal value) {
            addCriterion("origin_receivables_amount <", value, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_receivables_amount <=", value, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountIn(List<BigDecimal> values) {
            addCriterion("origin_receivables_amount in", values, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountNotIn(List<BigDecimal> values) {
            addCriterion("origin_receivables_amount not in", values, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_receivables_amount between", value1, value2, "originReceivablesAmount");
            return this;
        }

        public Criteria andOriginReceivablesAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_receivables_amount not between", value1, value2, "originReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountIsNull() {
            addCriterion("agio_receivables_amount is null");
            return this;
        }

        public Criteria andAgioReceivablesAmountIsNotNull() {
            addCriterion("agio_receivables_amount is not null");
            return this;
        }

        public Criteria andAgioReceivablesAmountEqualTo(BigDecimal value) {
            addCriterion("agio_receivables_amount =", value, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountNotEqualTo(BigDecimal value) {
            addCriterion("agio_receivables_amount <>", value, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountGreaterThan(BigDecimal value) {
            addCriterion("agio_receivables_amount >", value, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_receivables_amount >=", value, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountLessThan(BigDecimal value) {
            addCriterion("agio_receivables_amount <", value, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_receivables_amount <=", value, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountIn(List<BigDecimal> values) {
            addCriterion("agio_receivables_amount in", values, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountNotIn(List<BigDecimal> values) {
            addCriterion("agio_receivables_amount not in", values, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_receivables_amount between", value1, value2, "agioReceivablesAmount");
            return this;
        }

        public Criteria andAgioReceivablesAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_receivables_amount not between", value1, value2, "agioReceivablesAmount");
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

        public Criteria andIsValidEqualTo(Short value) {
            addCriterion("is_valid =", value, "isValid");
            return this;
        }

        public Criteria andIsValidNotEqualTo(Short value) {
            addCriterion("is_valid <>", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThan(Short value) {
            addCriterion("is_valid >", value, "isValid");
            return this;
        }

        public Criteria andIsValidGreaterThanOrEqualTo(Short value) {
            addCriterion("is_valid >=", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThan(Short value) {
            addCriterion("is_valid <", value, "isValid");
            return this;
        }

        public Criteria andIsValidLessThanOrEqualTo(Short value) {
            addCriterion("is_valid <=", value, "isValid");
            return this;
        }

        public Criteria andIsValidIn(List<Short> values) {
            addCriterion("is_valid in", values, "isValid");
            return this;
        }

        public Criteria andIsValidNotIn(List<Short> values) {
            addCriterion("is_valid not in", values, "isValid");
            return this;
        }

        public Criteria andIsValidBetween(Short value1, Short value2) {
            addCriterion("is_valid between", value1, value2, "isValid");
            return this;
        }

        public Criteria andIsValidNotBetween(Short value1, Short value2) {
            addCriterion("is_valid not between", value1, value2, "isValid");
            return this;
        }

        public Criteria andAgioTotalIsNull() {
            addCriterion("agio_total is null");
            return this;
        }

        public Criteria andAgioTotalIsNotNull() {
            addCriterion("agio_total is not null");
            return this;
        }

        public Criteria andAgioTotalEqualTo(BigDecimal value) {
            addCriterion("agio_total =", value, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalNotEqualTo(BigDecimal value) {
            addCriterion("agio_total <>", value, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalGreaterThan(BigDecimal value) {
            addCriterion("agio_total >", value, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_total >=", value, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalLessThan(BigDecimal value) {
            addCriterion("agio_total <", value, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalLessThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_total <=", value, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalIn(List<BigDecimal> values) {
            addCriterion("agio_total in", values, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalNotIn(List<BigDecimal> values) {
            addCriterion("agio_total not in", values, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_total between", value1, value2, "agioTotal");
            return this;
        }

        public Criteria andAgioTotalNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_total not between", value1, value2, "agioTotal");
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

        public Criteria andReserveAmountIsNull() {
            addCriterion("reserve_amount is null");
            return this;
        }

        public Criteria andReserveAmountIsNotNull() {
            addCriterion("reserve_amount is not null");
            return this;
        }

        public Criteria andReserveAmountEqualTo(BigDecimal value) {
            addCriterion("reserve_amount =", value, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountNotEqualTo(BigDecimal value) {
            addCriterion("reserve_amount <>", value, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountGreaterThan(BigDecimal value) {
            addCriterion("reserve_amount >", value, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("reserve_amount >=", value, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountLessThan(BigDecimal value) {
            addCriterion("reserve_amount <", value, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("reserve_amount <=", value, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountIn(List<BigDecimal> values) {
            addCriterion("reserve_amount in", values, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountNotIn(List<BigDecimal> values) {
            addCriterion("reserve_amount not in", values, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("reserve_amount between", value1, value2, "reserveAmount");
            return this;
        }

        public Criteria andReserveAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("reserve_amount not between", value1, value2, "reserveAmount");
            return this;
        }

        public Criteria andOriginTotalIsNull() {
            addCriterion("origin_total is null");
            return this;
        }

        public Criteria andOriginTotalIsNotNull() {
            addCriterion("origin_total is not null");
            return this;
        }

        public Criteria andOriginTotalEqualTo(BigDecimal value) {
            addCriterion("origin_total =", value, "originTotal");
            return this;
        }

        public Criteria andOriginTotalNotEqualTo(BigDecimal value) {
            addCriterion("origin_total <>", value, "originTotal");
            return this;
        }

        public Criteria andOriginTotalGreaterThan(BigDecimal value) {
            addCriterion("origin_total >", value, "originTotal");
            return this;
        }

        public Criteria andOriginTotalGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_total >=", value, "originTotal");
            return this;
        }

        public Criteria andOriginTotalLessThan(BigDecimal value) {
            addCriterion("origin_total <", value, "originTotal");
            return this;
        }

        public Criteria andOriginTotalLessThanOrEqualTo(BigDecimal value) {
            addCriterion("origin_total <=", value, "originTotal");
            return this;
        }

        public Criteria andOriginTotalIn(List<BigDecimal> values) {
            addCriterion("origin_total in", values, "originTotal");
            return this;
        }

        public Criteria andOriginTotalNotIn(List<BigDecimal> values) {
            addCriterion("origin_total not in", values, "originTotal");
            return this;
        }

        public Criteria andOriginTotalBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_total between", value1, value2, "originTotal");
            return this;
        }

        public Criteria andOriginTotalNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("origin_total not between", value1, value2, "originTotal");
            return this;
        }

        public Criteria andFinalAmountIsNull() {
            addCriterion("final_amount is null");
            return this;
        }

        public Criteria andFinalAmountIsNotNull() {
            addCriterion("final_amount is not null");
            return this;
        }

        public Criteria andFinalAmountEqualTo(BigDecimal value) {
            addCriterion("final_amount =", value, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountNotEqualTo(BigDecimal value) {
            addCriterion("final_amount <>", value, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountGreaterThan(BigDecimal value) {
            addCriterion("final_amount >", value, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("final_amount >=", value, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountLessThan(BigDecimal value) {
            addCriterion("final_amount <", value, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountLessThanOrEqualTo(BigDecimal value) {
            addCriterion("final_amount <=", value, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountIn(List<BigDecimal> values) {
            addCriterion("final_amount in", values, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountNotIn(List<BigDecimal> values) {
            addCriterion("final_amount not in", values, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("final_amount between", value1, value2, "finalAmount");
            return this;
        }

        public Criteria andFinalAmountNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("final_amount not between", value1, value2, "finalAmount");
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

        public Criteria andUseCashPromotionIsNull() {
            addCriterion("use_cash_promotion is null");
            return this;
        }

        public Criteria andUseCashPromotionIsNotNull() {
            addCriterion("use_cash_promotion is not null");
            return this;
        }

        public Criteria andUseCashPromotionEqualTo(Byte value) {
            addCriterion("use_cash_promotion =", value, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionNotEqualTo(Byte value) {
            addCriterion("use_cash_promotion <>", value, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionGreaterThan(Byte value) {
            addCriterion("use_cash_promotion >", value, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionGreaterThanOrEqualTo(Byte value) {
            addCriterion("use_cash_promotion >=", value, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionLessThan(Byte value) {
            addCriterion("use_cash_promotion <", value, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionLessThanOrEqualTo(Byte value) {
            addCriterion("use_cash_promotion <=", value, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionIn(List<Byte> values) {
            addCriterion("use_cash_promotion in", values, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionNotIn(List<Byte> values) {
            addCriterion("use_cash_promotion not in", values, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionBetween(Byte value1, Byte value2) {
            addCriterion("use_cash_promotion between", value1, value2, "useCashPromotion");
            return this;
        }

        public Criteria andUseCashPromotionNotBetween(Byte value1, Byte value2) {
            addCriterion("use_cash_promotion not between", value1, value2, "useCashPromotion");
            return this;
        }

        public Criteria andTaxFeeIsNull() {
            addCriterion("tax_fee is null");
            return this;
        }

        public Criteria andTaxFeeIsNotNull() {
            addCriterion("tax_fee is not null");
            return this;
        }

        public Criteria andTaxFeeEqualTo(BigDecimal value) {
            addCriterion("tax_fee =", value, "taxFee");
            return this;
        }

        public Criteria andTaxFeeNotEqualTo(BigDecimal value) {
            addCriterion("tax_fee <>", value, "taxFee");
            return this;
        }

        public Criteria andTaxFeeGreaterThan(BigDecimal value) {
            addCriterion("tax_fee >", value, "taxFee");
            return this;
        }

        public Criteria andTaxFeeGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("tax_fee >=", value, "taxFee");
            return this;
        }

        public Criteria andTaxFeeLessThan(BigDecimal value) {
            addCriterion("tax_fee <", value, "taxFee");
            return this;
        }

        public Criteria andTaxFeeLessThanOrEqualTo(BigDecimal value) {
            addCriterion("tax_fee <=", value, "taxFee");
            return this;
        }

        public Criteria andTaxFeeIn(List<BigDecimal> values) {
            addCriterion("tax_fee in", values, "taxFee");
            return this;
        }

        public Criteria andTaxFeeNotIn(List<BigDecimal> values) {
            addCriterion("tax_fee not in", values, "taxFee");
            return this;
        }

        public Criteria andTaxFeeBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("tax_fee between", value1, value2, "taxFee");
            return this;
        }

        public Criteria andTaxFeeNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("tax_fee not between", value1, value2, "taxFee");
            return this;
        }

        public Criteria andAgioTotalReceivablesIsNull() {
            addCriterion("agio_total_receivables is null");
            return this;
        }

        public Criteria andAgioTotalReceivablesIsNotNull() {
            addCriterion("agio_total_receivables is not null");
            return this;
        }

        public Criteria andAgioTotalReceivablesEqualTo(BigDecimal value) {
            addCriterion("agio_total_receivables =", value, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesNotEqualTo(BigDecimal value) {
            addCriterion("agio_total_receivables <>", value, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesGreaterThan(BigDecimal value) {
            addCriterion("agio_total_receivables >", value, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_total_receivables >=", value, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesLessThan(BigDecimal value) {
            addCriterion("agio_total_receivables <", value, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesLessThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_total_receivables <=", value, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesIn(List<BigDecimal> values) {
            addCriterion("agio_total_receivables in", values, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesNotIn(List<BigDecimal> values) {
            addCriterion("agio_total_receivables not in", values, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_total_receivables between", value1, value2, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioTotalReceivablesNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_total_receivables not between", value1, value2, "agioTotalReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesIsNull() {
            addCriterion("agio_receivables_amount_receivables is null");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesIsNotNull() {
            addCriterion("agio_receivables_amount_receivables is not null");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesEqualTo(BigDecimal value) {
            addCriterion("agio_receivables_amount_receivables =", value, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesNotEqualTo(BigDecimal value) {
            addCriterion("agio_receivables_amount_receivables <>", value, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesGreaterThan(BigDecimal value) {
            addCriterion("agio_receivables_amount_receivables >", value, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_receivables_amount_receivables >=", value, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesLessThan(BigDecimal value) {
            addCriterion("agio_receivables_amount_receivables <", value, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesLessThanOrEqualTo(BigDecimal value) {
            addCriterion("agio_receivables_amount_receivables <=", value, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesIn(List<BigDecimal> values) {
            addCriterion("agio_receivables_amount_receivables in", values, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesNotIn(List<BigDecimal> values) {
            addCriterion("agio_receivables_amount_receivables not in", values, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_receivables_amount_receivables between", value1, value2, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andAgioReceivablesAmountReceivablesNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("agio_receivables_amount_receivables not between", value1, value2, "agioReceivablesAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesIsNull() {
            addCriterion("final_amount_receivables is null");
            return this;
        }

        public Criteria andFinalAmountReceivablesIsNotNull() {
            addCriterion("final_amount_receivables is not null");
            return this;
        }

        public Criteria andFinalAmountReceivablesEqualTo(BigDecimal value) {
            addCriterion("final_amount_receivables =", value, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesNotEqualTo(BigDecimal value) {
            addCriterion("final_amount_receivables <>", value, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesGreaterThan(BigDecimal value) {
            addCriterion("final_amount_receivables >", value, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("final_amount_receivables >=", value, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesLessThan(BigDecimal value) {
            addCriterion("final_amount_receivables <", value, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesLessThanOrEqualTo(BigDecimal value) {
            addCriterion("final_amount_receivables <=", value, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesIn(List<BigDecimal> values) {
            addCriterion("final_amount_receivables in", values, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesNotIn(List<BigDecimal> values) {
            addCriterion("final_amount_receivables not in", values, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("final_amount_receivables between", value1, value2, "finalAmountReceivables");
            return this;
        }

        public Criteria andFinalAmountReceivablesNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("final_amount_receivables not between", value1, value2, "finalAmountReceivables");
            return this;
        }
    }
}
