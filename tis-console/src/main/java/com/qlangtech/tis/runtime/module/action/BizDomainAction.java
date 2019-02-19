/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.runtime.module.action;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.manage.biz.dal.pojo.Department;
import com.qlangtech.tis.manage.biz.dal.pojo.DepartmentCriteria;

/* 
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class BizDomainAction extends BasicModule {

	/**
	 */
	private static final long serialVersionUID = 1L;

	private static final Pattern PATTERN_BIZ_NAME = Pattern.compile("[a-z\\d_]{3,20}");

	public static void main(String[] args) {
		Matcher matcher = PATTERN_BIZ_NAME.matcher("baisui");
		if (matcher.matches()) {
			System.out.println("match");
		} else {
			System.out.println("not match");
		}
	}

	// @Func(PermissionConstant.PERMISSION_BASE_DATA_MANAGE)
	// public void doDeleteBizDomain(Context context) {
	//
	// final Integer dptid = this.getInt("dptid");
	//
	// // BizDomain domain = new BizDomain();
	// // domain.setDeleteFlag((int) ManageUtils.DELETE);
	// //
	// // BizDomainCriteria criteria = new BizDomainCriteria();
	// // criteria.createCriteria().andBizIdEqualTo(bizId);
	// // this.getBizDomainDAO().updateByExampleSelective(domain, criteria);
	// final AppsFetcher fetcher = AppsFetcher
	// .create(this.getApplicationDAO());
	//
	// int appcount = fetcher.count(new CriteriaSetter() {
	// @Override
	// public void set(Criteria criteria) {
	// criteria.andDptIdEqualTo(dptid);
	// }
	// });
	// if (appcount > 0) {
	// this.addErrorMessage(context, "该业务下有" + appcount + "个应用与之关联，不能删除");
	// return;
	// }
	// // this.getBizDomainDAO().deleteByPrimaryKey(bizId);
	// DepartmentCriteria dptCriteria = new DepartmentCriteria();
	// dptCriteria.createCriteria().andParentIdEqualTo(dptid);
	// int childCount = this.getDepartmentDAO().countByExample(dptCriteria);
	// if (childCount > 0) {
	// this.addErrorMessage(context, "该部门下有" + childCount + "个子部门，不能删除");
	// return;
	// }
	//
	// this.getDepartmentDAO().deleteByPrimaryKey(dptid);
	//
	// this.addActionMessage(context, "业务线被成功删除");
	//
	// }
	// /**
	// * 取消删除状态
	// *
	// * @param context
	// */
	// @Func(PermissionConstant.PERMISSION_BASE_DATA_MANAGE)
	// public void doRecovery(Context context) {
	//
	// Integer bizId = this.getInt("bizid");
	//
	// BizDomain domain = new BizDomain();
	// domain.setDeleteFlag((int) ManageUtils.UN_DELETE);
	//
	// BizDomainCriteria criteria = new BizDomainCriteria();
	// criteria.createCriteria().andBizIdEqualTo(bizId);
	// this.getBizDomainDAO().updateByExampleSelective(domain, criteria);
	//
	// }
	/*
	 * 添加业务线
	 * 
	 * @param context
	 */
	public void doAdd(Context context) {
		String bizName = this.getString("bizname");
		Department dpt = new Department();
		dpt.setName(bizName);
		context.put("biz", dpt);
		if (!validateParam(context, bizName)) {	
			return;
		}

		DepartmentCriteria dc;
		Department root = getRootDpt();

		
		dpt.setLeaf(true);
		dpt.setGmtCreate(new Date());
		dpt.setGmtModified(new Date());
		
		dpt.setFullName("/" + root.getName() + "/" + bizName);
		dpt.setParentId(root.getDptId());

		dc = new DepartmentCriteria();
		dc.createCriteria().andNameEqualTo(bizName);

		if (getDepartmentDAO().countByExample(dc) > 0) {
			this.addErrorMessage(context, "业务‘" + bizName + "’已经存在,不能重复添加");
			return;
		}

		this.getDepartmentDAO().insertSelective(dpt);

		this.addActionMessage(context, "业务‘" + bizName + "’添加成功");
	}

	private boolean validateParam(Context context, String bizName) {
		if (StringUtils.isBlank(bizName)) {
			this.addErrorMessage(context, "请填写‘名称’");
			return false;
		}

		Matcher m = PATTERN_BIZ_NAME.matcher(bizName);
		if (!m.matches()) {
			this.addErrorMessage(context, "‘名称’不符合规范" + PATTERN_BIZ_NAME);
			return false;
		}

		return true;
	}

	private Department getRootDpt() {
		DepartmentCriteria dc = new DepartmentCriteria();
		dc.createCriteria().andParentIdEqualTo(-1);
		List<Department> parents = this.getDepartmentDAO().selectByExample(dc);
		Department root = null;
		for (Department d : parents) {
			root = d;
			break;
		}
		if (root == null) {
			throw new IllegalStateException("can not find root department");
		}

		if (root.getLeaf() || StringUtils.isBlank(root.getName())) {
			throw new IllegalStateException("neither root attr leaf can not be 'true' nor name can be blank");
		}
		return root;
	}

	// @Func(PermissionConstant.PERMISSION_BASE_DATA_MANAGE)
	public void doUpdate(Context context) {
		Integer bizId = this.getInt("bizid");
		Assert.assertNotNull(bizId);
		Department dpt = new Department();
		dpt.setDptId(bizId);
		String bizName = this.getString("bizname");
		dpt.setName(bizName);
		context.put("biz", dpt);
		if (!validateParam(context, bizName)) {
			return;
		}

		Department root = getRootDpt();

		DepartmentCriteria dc = new DepartmentCriteria();
		dc.createCriteria().andNameEqualTo(bizName);

		if (getDepartmentDAO().countByExample(dc) > 0) {
			this.addErrorMessage(context, "业务‘" + bizName + "’已经存在");
			return;
		}

		
		dpt.setGmtModified(new Date());
		dpt.setName(bizName);
		dpt.setFullName("/" + root.getName() + "/" + bizName);
		dc = new DepartmentCriteria();
		dc.createCriteria().andDptIdEqualTo(bizId);
		getDepartmentDAO().updateByExampleSelective(dpt, dc);

		this.addActionMessage(context, "业务‘" + bizName + "’更新成功");
	}
}
