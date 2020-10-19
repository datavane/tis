package com.qlangtech.tis.realtime.test.employees.dao.impl;

import com.qlangtech.tis.ibatis.BasicDAO;
import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.employees.dao.IDeptEmpDAO;
import com.qlangtech.tis.realtime.test.employees.pojo.DeptEmp;
import com.qlangtech.tis.realtime.test.employees.pojo.DeptEmpCriteria;
import java.util.List;

public class DeptEmpDAOImpl extends BasicDAO<DeptEmp,DeptEmpCriteria> implements IDeptEmpDAO {

    public DeptEmpDAOImpl() {
        super();
    }

    public final String getEntityName() {
         return "dept_emp";

    }

    public int countByExample(DeptEmpCriteria example) {
        Integer count = (Integer)  this.count("dept_emp.ibatorgenerated_countByExample",example);
        return count;
    }

    public int countFromWriteDB(DeptEmpCriteria example) {
        Integer count = (Integer)  this.countFromWriterDB("dept_emp.ibatorgenerated_countByExample",example);
        return count;
    }

    public int deleteByExample(DeptEmpCriteria criteria) {
        return  this.deleteRecords("dept_emp.ibatorgenerated_deleteByExample", criteria);

    }

    public int deleteByPrimaryKey(String deptNo, Integer empNo) {
        DeptEmp key = new DeptEmp();
        key.setDeptNo(deptNo);
        key.setEmpNo(empNo);
        return  this.deleteRecords("dept_emp.ibatorgenerated_deleteByPrimaryKey", key);

    }

    public void insert(DeptEmp record) {
        this.insert("dept_emp.ibatorgenerated_insert", record);
    }

    public void insertSelective(DeptEmp record) {
        this.insert("dept_emp.ibatorgenerated_insertSelective", record);
    }

    public List<DeptEmp> selectByExample(DeptEmpCriteria criteria) {
        return this.selectByExample(criteria,1,100);
    }

    @SuppressWarnings("all")
    public final List<RowMap> selectColsByExample(DeptEmpCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        if(example.isTargetColsEmpty()){
              throw new IllegalStateException("criteria com.qlangtech.tis.realtime.test.employees.pojo.DeptEmpCriteria target Cols can not be empty ");
        }
        return (List<RowMap>) this.getSqlMapClientTemplate().queryForList("dept_emp.ibatorgenerated_selectTargetColsByExample", example);
    }

    @SuppressWarnings("unchecked")
    public List<DeptEmp> selectByExample(DeptEmpCriteria example, int page, int pageSize) {
        example.setPage(page);
        example.setPageSize(pageSize);
        List<DeptEmp> list = this.list("dept_emp.ibatorgenerated_selectByExample", example);
        return list;
    }

    public DeptEmp selectByPrimaryKey(String deptNo, Integer empNo) {
        DeptEmp key = new DeptEmp();
        key.setDeptNo(deptNo);
        key.setEmpNo(empNo);
        DeptEmp record = (DeptEmp) this.load("dept_emp.ibatorgenerated_selectByPrimaryKey", key);
        return record;
    }

    public int updateByExampleSelective(DeptEmp record, DeptEmpCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("dept_emp.ibatorgenerated_updateByExampleSelective", parms);
    }

    public int updateByExample(DeptEmp record, DeptEmpCriteria example) {
        UpdateByExampleParms parms = new UpdateByExampleParms(record, example);
        return this.updateRecords("dept_emp.ibatorgenerated_updateByExample", parms);
    }

    public DeptEmp loadFromWriteDB(String deptNo, Integer empNo) {
        DeptEmp key = new DeptEmp();
        key.setDeptNo(deptNo);
        key.setEmpNo(empNo);
        DeptEmp record = (DeptEmp) this.loadFromWriterDB("dept_emp.ibatorgenerated_selectByPrimaryKey",key);
        return record;
    }

    private static class UpdateByExampleParms extends DeptEmpCriteria {
        private Object record;

        public UpdateByExampleParms(Object record, DeptEmpCriteria example) {
            super(example);
            this.record = record;
        }

        public Object getRecord() {
            return record;
        }
    }
}