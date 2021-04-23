package com.qlangtech.tis.manage.common.apps;

import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;
import com.qlangtech.tis.manage.common.RunContext;

import java.util.List;

/**
 * @author: baisui 百岁
 * @create: 2021-04-21 12:30
 **/
public interface IDepartmentGetter {
  default List<Department> getDepartmentBelongs(RunContext runcontext) {
    DepartmentCriteria criteria = new DepartmentCriteria();
    criteria.createCriteria().andIsLeaf(true);
    return runcontext.getDepartmentDAO().selectByExample(criteria, 1, 500);
  }
}
