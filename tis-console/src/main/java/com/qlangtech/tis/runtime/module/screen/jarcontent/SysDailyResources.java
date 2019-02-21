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
package com.qlangtech.tis.runtime.module.screen.jarcontent;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.citrus.turbine.Context;
import com.qlangtech.tis.manage.common.AppDomainInfo;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.runtime.module.action.jarcontent.SaveFileContentAction.SynManagerWorker;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;

import junit.framework.Assert;

/*
 * 将日常的配置同步到线上去
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class SysDailyResources extends BasicScreen {

	private static final long serialVersionUID = 1L;

	public void execute(Context context) throws Exception {
		this.disableNavigationBar(context);
		shallnotShowEnvironment(context);
		Assert.assertNotNull(this.getAppDomain());
		AppDomainInfo app = this.getAppDomain();

		if (StringUtils.isEmpty(RunEnvironment.ONLINE.getPublicRepositoryURL())) {
			this.addErrorMessage(context, "还未设置生产环境服务端地址,请联系系统管理员");
			this.forward("jarcontent/sysDailyResourcesNotSetPublicRepositoryURL.vm");
			return;
		}

		// final AppKey appKey = new AppKey(app.getAppName(), (short) 0,
		// RunEnvironment.DAILY, true);
		final SynManagerWorker synManager = SynManagerWorker.create(app.getAppName(), this.getContext(), this);
		// 日常向线上推送文件
		// SnapshotDomain dailyResDomain = LoadSolrCoreConfigByAppNameServlet
		// .getSnapshotDomain(ConfigFileReader.getConfigList(), appKey, this);
		// if (dailyResDomain != null) {
		// SnapshotDomain onlineRes = ResSynManager
		// .getOnlineResourceConfig(app.getAppName());
		// ResSynManager synManager =
		// ResSynManager.createSynManagerOnlineFromDaily(app.getAppName(),
		// this);
		// new ResSynManager(dailyResDomain,
		// onlineRes);
		context.put("dailyRes", synManager.getDailyRes());
		// context.put("onlineRes", synManager.getOnlineResDomain());
		// context.put("rescompare", synManager.getCompareResult());
		context.put("synManager", synManager);
		// }

	}

	@Override
	public boolean isEnableDomainView() {
		return false;
	}
}
