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
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.dao.IResourceParametersDAO;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParameters;
import com.qlangtech.tis.manage.biz.dal.pojo.ResourceParametersCriteria;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

import junit.framework.Assert;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ConfigFileParametersAction extends BasicModule {

	private static final long serialVersionUID = 1L;

	private IResourceParametersDAO resourceParametersDAO;

	private static final Pattern PATTERN_KEY_NAME = Pattern.compile("[\\w|_]+");

	public static GlobalParam[] globalParams = new GlobalParam[] { //
			new GlobalParam("zkaddress", "Solr集群zookeeper地址"), //
			new GlobalParam("hdfsaddress", "全量构建分布式文件系统地址"), //
			new GlobalParam("tis_host_address", "TIS中控节点Host地址"), //
			new GlobalParam("tis_assemble_host", "TIS全量控制、日志收集节点"), //
			new GlobalParam("jobtracker_rpc_host", "TIS任务中心入口地址"), //
			new GlobalParam("mq_statistics_host", "TIS实时日志状态收集节点地址"), //
			new GlobalParam("max_db_dump_thread_count", "数据库Dump最大线程数") };

	/**
	 * 初始化系统参数
	 * 
	 * @param context
	 */
	public void doInitParameter(Context context) {

		for (GlobalParam p : globalParams) {

		}

	}

	public static class GlobalParam extends Option {

		private final String desc;

		public GlobalParam(String name, String desc) {
			super(name, null);
			this.desc = desc;
		}

		public String getDesc() {
			return desc;
		}

	}

	/**
	 * 设置参数值
	 *
	 * @param context
	 */
	@Func(PermissionConstant.GLOBAL_PARAMETER_SET)
	public void doSetParameter(Context context) {
		RunEnvironment runtime = RunEnvironment.getEnum(this.getString("runtime"));
		Long rpid = this.getLong("rpid");
		Assert.assertNotNull(rpid);
		Assert.assertNotNull(runtime);
		ResourceParameters param = new ResourceParameters();
		ResourceParametersCriteria criteria = new ResourceParametersCriteria();
		criteria.createCriteria().andRpIdEqualTo(rpid);
		Assert.assertNotNull(runtime);
		String keyvalue = this.getString("keyvalue");
		if (StringUtils.isBlank(keyvalue)) {
			this.addErrorMessage(context, "键值不能为空");
			return;
		}
		switch (runtime) {
		case DAILY:
			param.setDailyValue(keyvalue);
			break;
		case ONLINE:
			param.setOnlineValue(keyvalue);
			break;
		default:
			throw new IllegalArgumentException("runtime:" + runtime + " is invalid");
		}
		this.resourceParametersDAO.updateByExampleSelective(param, criteria);
		this.addActionMessage(context, "已经成功更新");
	}

	@Func(PermissionConstant.GLOBAL_PARAMETER_ADD)
	public void doAddParameter(Context context) {
		String keyName = this.getString("keyname");
		if (StringUtils.isBlank(keyName)) {
			this.addErrorMessage(context, "键名称不能为空");
			return;
		}
		if (!PATTERN_KEY_NAME.matcher(keyName).matches()) {
			this.addErrorMessage(context, "键名键值必须由字母和数字和下划线组成");
			return;
		}
		ResourceParametersCriteria criteria = new ResourceParametersCriteria();
		criteria.createCriteria().andKeyNameEqualTo(keyName);
		if (resourceParametersDAO.countByExample(criteria) > 0) {
			this.addErrorMessage(context, "该键值名" + keyName + "系统中已经存在");
			return;
		}
		ResourceParameters param = new ResourceParameters();
		param.setGmtCreate(new Date());
		param.setDailyValue(StringUtils.trimToNull(this.getString("dailyvalue")));
		param.setReadyValue(StringUtils.trimToNull(this.getString("readyvalue")));
		param.setOnlineValue(StringUtils.trimToNull(this.getString("onlinevalue")));
		param.setKeyName(keyName);
		resourceParametersDAO.insertSelective(param);
		this.addActionMessage(context, "成功添加配置全局变量：" + keyName);
	}

	public IResourceParametersDAO getResourceParametersDAO() {
		return resourceParametersDAO;
	}

	@Autowired
	public void setResourceParametersDAO(IResourceParametersDAO resourceParametersDAO) {
		this.resourceParametersDAO = resourceParametersDAO;
	}
}
