package com.qlangtech.tis.realtime.test.employees.dao;

import com.qlangtech.tis.ibatis.RowMap;
import com.qlangtech.tis.realtime.test.employees.pojo.DeptEmp;
import com.qlangtech.tis.realtime.test.employees.pojo.DeptEmpCriteria;
import java.util.List;

public interface IDeptEmpDAO {
    int countByExample(DeptEmpCriteria example);

    int countFromWriteDB(DeptEmpCriteria example);

    int deleteByExample(DeptEmpCriteria criteria);

    int deleteByPrimaryKey(String deptNo, Integer empNo);

    void insert(DeptEmp record);

    void insertSelective(DeptEmp record);

    List<DeptEmp> selectByExample(DeptEmpCriteria criteria);

    @SuppressWarnings("all")
    List<RowMap> selectColsByExample(DeptEmpCriteria example, int page, int pageSize);

    List<DeptEmp> selectByExample(DeptEmpCriteria example, int page, int pageSize);

    DeptEmp selectByPrimaryKey(String deptNo, Integer empNo);

    int updateByExampleSelective(DeptEmp record, DeptEmpCriteria example);

    int updateByExample(DeptEmp record, DeptEmpCriteria example);

    DeptEmp loadFromWriteDB(String deptNo, Integer empNo);
}