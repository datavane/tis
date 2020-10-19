package com.qlangtech.tis.realtime.test.employees.pojo;

import com.google.common.collect.ImmutableList;
import java.util.List;

public enum DeptEmpColEnum {
    DEPT_NO("dept_no",1,true),
    EMP_NO("emp_no",4,true),
    FROM_DATE("from_date",91,false),
    TO_DATE("to_date",91,false);

    private final String name;

    private final int jdbcType;

    private final boolean pk;

    private static final List<DeptEmpColEnum> pks = (new ImmutableList.Builder<DeptEmpColEnum>()).add(DEPT_NO).add(EMP_NO).build();

    private DeptEmpColEnum(String name, int jdbcType, boolean pk) {
        this.jdbcType = jdbcType;
        this.name = name;
        this.pk = pk;
    }

    public String getName() {
        return this.name;
    }

    public int getJdbcType() {
        return this.jdbcType;
    }

    public boolean isPK() {
        return this.pk;
    }

    public static List<DeptEmpColEnum> getPKs() {
        return pks;
    }
}