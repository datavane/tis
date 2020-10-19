package com.qlangtech.tis.realtime.test.employees.pojo;

import com.qlangtech.tis.realtime.transfer.AbstractRowValueGetter;

import java.io.Serializable;
import java.util.Date;

public class DeptEmp extends AbstractRowValueGetter implements Serializable {
    private String deptNo;

    private Integer empNo;

    private Date fromDate;

    private Date toDate;

    private static final long serialVersionUID = 1L;

    public String getDeptNo() {
        return deptNo;
    }

    public void setDeptNo(String deptNo) {
        this.deptNo = deptNo == null ? null : deptNo.trim();
    }

    public Integer getEmpNo() {
        return empNo;
    }

    public void setEmpNo(Integer empNo) {
        this.empNo = empNo;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }
}