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
package com.qlangtech.tis.runtime.module.action.jarcontent;

import java.text.SimpleDateFormat;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.biz.dal.pojo.Application;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroup;
import com.qlangtech.tis.manage.biz.dal.pojo.ServerGroupCriteria;
import com.qlangtech.tis.manage.biz.dal.pojo.Snapshot;
import com.qlangtech.tis.manage.biz.dal.pojo.SnapshotCriteria;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.manage.common.DefaultOperationDomainLogger;
import com.qlangtech.tis.manage.common.RunContext;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.manage.spring.aop.OperationIgnore;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.BasicModule;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SnapshotRevsionAction extends BasicModule {

	private static final long serialVersionUID = 1L;

	public SnapshotRevsionAction() {
		super("selectRevsion");
	}

	/*
	 * 与doSelectRevsion的区别在于，源参数是页面传递， doSelectRevsionByContext的参数是有http接口传入
	 */
    @OperationIgnore
	public void doSelectRevsionByContext(Context context) {
		Integer snapshotid = (Integer) context.get("selectedSnapshotid");
		// final Integer groupid = this.getInt("groupid");
		final String memo = "drds配置更新";
		final ServerGroup group = getAppServerGroup();
		Assert.assertNotNull(group);
		final AppDomainInfo domain = this.getAppDomain();
		if (!StringUtils.equals(this.getString("appname"), domain.getAppName())) {
			this.addErrorMessage(context,
					"执行的应用：“" + this.getString("appname") + "”，与当前系统应用“" + domain.getAppName() + "”不一致,请关闭当前页面重新打开");
			return;
		}
		if (group.getPublishSnapshotId() != null && snapshotid == (int) group.getPublishSnapshotId()) {
			this.addErrorMessage(context, "SNAPSHOT已经设置为:" + snapshotid + ",请重新设置");
			return;
		}
		Application app = new Application();
		app.setAppId(domain.getAppid());
		app.setProjectName(domain.getAppName());
		change2newSnapshot(snapshotid, memo, group, app, domain.getRunEnvironment(), this);
		addActionMessage(context,
				"已经将<strong style='background-color:yellow'>" + domain.getRunEnvironment().getDescribe()
						+ "</strong>的应用<strong style='background-color:yellow'>" + domain.getAppName()
						+ "</strong>的第<strong style='background-color:yellow'>" + group.getGroupIndex()
						+ "组</strong>服务器，的发布快照设置成了snapshot：<strong style='background-color:yellow'>" + snapshotid
						+ "</strong>");
	}

    @OperationIgnore
	@Func(PermissionConstant.CONFIG_SNAPSHOT_CHANGE)
	public void doSelectRevsion(Context context) {
		Integer snapshotid = this.getInt("selectedSnapshotid");
		// final Integer groupid = this.getInt("groupid");
		final String memo = this.getString("memo");
		if (StringUtils.isBlank(memo)) {
			this.addErrorMessage(context, "请填写操作日志");
			return;
		}
		final ServerGroup group = getAppServerGroup();
		Assert.assertNotNull(group);
		final AppDomainInfo domain = this.getAppDomain();
		if (!StringUtils.equals(this.getString("appname"), domain.getAppName())) {
			this.addErrorMessage(context,
					"执行的应用：“" + this.getString("appname") + "”，与当前系统应用“" + domain.getAppName() + "”不一致,请关闭当前页面重新打开");
			return;
		}
		if (group.getPublishSnapshotId() != null && snapshotid == (int) group.getPublishSnapshotId()) {
			this.addErrorMessage(context, "SNAPSHOT已经设置为:" + snapshotid + ",请重新设置");
			return;
		}
		Application app = new Application();
		app.setAppId(domain.getAppid());
		app.setProjectName(domain.getAppName());
		change2newSnapshot(snapshotid, memo, group, app, domain.getRunEnvironment(), this);
		addActionMessage(context,
				"已经将<strong style='background-color:yellow'>" + domain.getRunEnvironment().getDescribe()
						+ "</strong>的应用<strong style='background-color:yellow'>" + domain.getAppName()
						+ "</strong>的第<strong style='background-color:yellow'>" + group.getGroupIndex()
						+ "组</strong>服务器，的发布快照设置成了snapshot：<strong style='background-color:yellow'>" + snapshotid
						+ "</strong>");
	}

	/**
	 * 取得最新的快照记录
	 *
	 * @param context
	 */
    @OperationIgnore
	@Func(PermissionConstant.CONFIG_SNAPSHOT_CHANGE)
	public void doGetLatestSnapshot(Context context) throws Exception {
		Integer snapshotId = this.getInt("maxsnapshotid");
		SnapshotCriteria criteria = new SnapshotCriteria();
		criteria.createCriteria().andSnIdGreaterThan(snapshotId).andAppidEqualTo(this.getAppDomain().getAppid());
		final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		JSONArray jsonArray = new JSONArray();
		JSONObject obj = null;
		for (Snapshot snapshot : this.getSnapshotDAO().selectByExample(criteria, 1, 20)) {
			obj = new JSONObject();
			obj.put("snid", snapshot.getSnId());
			obj.put("createtime", format.format(snapshot.getCreateTime()));
			obj.put("creator", snapshot.getCreateUserName());
			obj.put("parent", snapshot.getPreSnId());
			obj.put("memo", StringUtils.trimToEmpty(snapshot.getMemo()));
			jsonArray.put(obj);
			break;
		}
		context.put("query_result", jsonArray.toString(1));
	}

	public static void change2newSnapshot(Integer snapshotid, final String memo, ServerGroup group, Application app, // final
																														// AppDomainInfo
																														// domain,
			RunEnvironment runEnvironment, RunContext runContext) {
		// 更新group的PublishSnapshotId
		ServerGroupCriteria groupCriteria = new ServerGroupCriteria();
		groupCriteria.createCriteria().andAppIdEqualTo(app.getAppId()).andRuntEnvironmentEqualTo(runEnvironment.getId())
				.andGidEqualTo(group.getGid());
		ServerGroup ugroup = new ServerGroup();
		ugroup.setPublishSnapshotId(snapshotid);
		DefaultOperationDomainLogger logger = new DefaultOperationDomainLogger();
		logger.setAppName(app.getProjectName());
		logger.setMemo("将Snapshot由" + group.getPublishSnapshotId() + "切换成" + snapshotid + "," + memo);
		logger.setRuntime(runEnvironment.getId());
		ugroup.setLogger(logger);
		runContext.getServerGroupDAO().updateByExampleSelective(ugroup, groupCriteria);
	}
}
