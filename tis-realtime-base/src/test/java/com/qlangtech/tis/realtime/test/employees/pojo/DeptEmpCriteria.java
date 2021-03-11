package com.qlangtech.tis.realtime.test.employees.pojo;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.manage.common.TISBaseCriteria;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DeptEmpCriteria extends TISBaseCriteria {
    protected String orderByClause;

    protected List<Criteria> oredCriteria;

    private final Set<DeptEmpColEnum> cols = Sets.newHashSet();

    public DeptEmpCriteria() {
        oredCriteria = new ArrayList<Criteria>();
    }

    protected DeptEmpCriteria(DeptEmpCriteria example) {
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

    public final List<DeptEmpColEnum> getCols() {
        return Lists.newArrayList(this.cols);
    }

    public final void addSelCol(DeptEmpColEnum... colName) {
        for (DeptEmpColEnum c : colName) {
              if(!c.isPK()){
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
            return criteriaWithoutValue.size() > 0
                || criteriaWithSingleValue.size() > 0
                || criteriaWithListValue.size() > 0
                || criteriaWithBetweenValue.size() > 0;
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

        public Criteria andDeptNoIsNull() {
            addCriterion("dept_no is null");
            return this;
        }

        public Criteria andDeptNoIsNotNull() {
            addCriterion("dept_no is not null");
            return this;
        }

        public Criteria andDeptNoEqualTo(String value) {
            addCriterion("dept_no =", value, "deptNo");
            return this;
        }

        public Criteria andDeptNoNotEqualTo(String value) {
            addCriterion("dept_no <>", value, "deptNo");
            return this;
        }

        public Criteria andDeptNoGreaterThan(String value) {
            addCriterion("dept_no >", value, "deptNo");
            return this;
        }

        public Criteria andDeptNoGreaterThanOrEqualTo(String value) {
            addCriterion("dept_no >=", value, "deptNo");
            return this;
        }

        public Criteria andDeptNoLessThan(String value) {
            addCriterion("dept_no <", value, "deptNo");
            return this;
        }

        public Criteria andDeptNoLessThanOrEqualTo(String value) {
            addCriterion("dept_no <=", value, "deptNo");
            return this;
        }

        public Criteria andDeptNoLike(String value) {
            addCriterion("dept_no like", value, "deptNo");
            return this;
        }

        public Criteria andDeptNoNotLike(String value) {
            addCriterion("dept_no not like", value, "deptNo");
            return this;
        }

        public Criteria andDeptNoIn(List<String> values) {
            addCriterion("dept_no in", values, "deptNo");
            return this;
        }

        public Criteria andDeptNoNotIn(List<String> values) {
            addCriterion("dept_no not in", values, "deptNo");
            return this;
        }

        public Criteria andDeptNoBetween(String value1, String value2) {
            addCriterion("dept_no between", value1, value2, "deptNo");
            return this;
        }

        public Criteria andDeptNoNotBetween(String value1, String value2) {
            addCriterion("dept_no not between", value1, value2, "deptNo");
            return this;
        }

        public Criteria andEmpNoIsNull() {
            addCriterion("emp_no is null");
            return this;
        }

        public Criteria andEmpNoIsNotNull() {
            addCriterion("emp_no is not null");
            return this;
        }

        public Criteria andEmpNoEqualTo(Integer value) {
            addCriterion("emp_no =", value, "empNo");
            return this;
        }

        public Criteria andEmpNoNotEqualTo(Integer value) {
            addCriterion("emp_no <>", value, "empNo");
            return this;
        }

        public Criteria andEmpNoGreaterThan(Integer value) {
            addCriterion("emp_no >", value, "empNo");
            return this;
        }

        public Criteria andEmpNoGreaterThanOrEqualTo(Integer value) {
            addCriterion("emp_no >=", value, "empNo");
            return this;
        }

        public Criteria andEmpNoLessThan(Integer value) {
            addCriterion("emp_no <", value, "empNo");
            return this;
        }

        public Criteria andEmpNoLessThanOrEqualTo(Integer value) {
            addCriterion("emp_no <=", value, "empNo");
            return this;
        }

        public Criteria andEmpNoIn(List<Integer> values) {
            addCriterion("emp_no in", values, "empNo");
            return this;
        }

        public Criteria andEmpNoNotIn(List<Integer> values) {
            addCriterion("emp_no not in", values, "empNo");
            return this;
        }

        public Criteria andEmpNoBetween(Integer value1, Integer value2) {
            addCriterion("emp_no between", value1, value2, "empNo");
            return this;
        }

        public Criteria andEmpNoNotBetween(Integer value1, Integer value2) {
            addCriterion("emp_no not between", value1, value2, "empNo");
            return this;
        }

        public Criteria andFromDateIsNull() {
            addCriterion("from_date is null");
            return this;
        }

        public Criteria andFromDateIsNotNull() {
            addCriterion("from_date is not null");
            return this;
        }

        public Criteria andFromDateEqualTo(Date value) {
            addCriterionForJDBCDate("from_date =", value, "fromDate");
            return this;
        }

        public Criteria andFromDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("from_date <>", value, "fromDate");
            return this;
        }

        public Criteria andFromDateGreaterThan(Date value) {
            addCriterionForJDBCDate("from_date >", value, "fromDate");
            return this;
        }

        public Criteria andFromDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("from_date >=", value, "fromDate");
            return this;
        }

        public Criteria andFromDateLessThan(Date value) {
            addCriterionForJDBCDate("from_date <", value, "fromDate");
            return this;
        }

        public Criteria andFromDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("from_date <=", value, "fromDate");
            return this;
        }

        public Criteria andFromDateIn(List<Date> values) {
            addCriterionForJDBCDate("from_date in", values, "fromDate");
            return this;
        }

        public Criteria andFromDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("from_date not in", values, "fromDate");
            return this;
        }

        public Criteria andFromDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("from_date between", value1, value2, "fromDate");
            return this;
        }

        public Criteria andFromDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("from_date not between", value1, value2, "fromDate");
            return this;
        }

        public Criteria andToDateIsNull() {
            addCriterion("to_date is null");
            return this;
        }

        public Criteria andToDateIsNotNull() {
            addCriterion("to_date is not null");
            return this;
        }

        public Criteria andToDateEqualTo(Date value) {
            addCriterionForJDBCDate("to_date =", value, "toDate");
            return this;
        }

        public Criteria andToDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("to_date <>", value, "toDate");
            return this;
        }

        public Criteria andToDateGreaterThan(Date value) {
            addCriterionForJDBCDate("to_date >", value, "toDate");
            return this;
        }

        public Criteria andToDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("to_date >=", value, "toDate");
            return this;
        }

        public Criteria andToDateLessThan(Date value) {
            addCriterionForJDBCDate("to_date <", value, "toDate");
            return this;
        }

        public Criteria andToDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("to_date <=", value, "toDate");
            return this;
        }

        public Criteria andToDateIn(List<Date> values) {
            addCriterionForJDBCDate("to_date in", values, "toDate");
            return this;
        }

        public Criteria andToDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("to_date not in", values, "toDate");
            return this;
        }

        public Criteria andToDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("to_date between", value1, value2, "toDate");
            return this;
        }

        public Criteria andToDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("to_date not between", value1, value2, "toDate");
            return this;
        }
    }
}