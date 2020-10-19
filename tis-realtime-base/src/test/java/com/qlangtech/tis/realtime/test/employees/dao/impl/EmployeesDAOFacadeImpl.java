package com.qlangtech.tis.realtime.test.employees.dao.impl;

import com.qlangtech.tis.realtime.test.employees.dao.IDeptEmpDAO;
import com.qlangtech.tis.realtime.test.employees.dao.IEmployeesDAOFacade;

public class EmployeesDAOFacadeImpl implements IEmployeesDAOFacade {
    private final IDeptEmpDAO deptEmpDAO;

    public IDeptEmpDAO getDeptEmpDAO() {
        return this.deptEmpDAO;
    }

    public EmployeesDAOFacadeImpl(IDeptEmpDAO deptEmpDAO) {
        this.deptEmpDAO = deptEmpDAO;
    }
}