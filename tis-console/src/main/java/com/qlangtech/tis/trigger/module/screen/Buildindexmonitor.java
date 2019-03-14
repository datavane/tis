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
package com.qlangtech.tis.trigger.module.screen;

import java.util.Calendar;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.citrus.turbine.Context;
import com.google.common.collect.Sets;
import com.qlangtech.tis.manage.PermissionConstant;
import com.qlangtech.tis.manage.spring.aop.Func;
import com.qlangtech.tis.manage.yarn.ApplicationReportStatus;
import com.qlangtech.tis.manage.yarn.YarnClient;
import com.qlangtech.tis.runtime.module.screen.BasicScreen;

//
///* 
// * @author 百岁（baisui@qlangtech.com）
// * @date 2019年1月17日
// */
public class Buildindexmonitor extends BasicScreen {

	private static final long serialVersionUID = 1L;
	private YarnClient yarnClient;

	@Override
	@Func(PermissionConstant.APP_BUILD_RESULT_VIEW)
	public void execute(Context context) throws Exception {

		// StringBuffer iframeurl = new
		// StringBuffer(TSearcherConfigFetcher.get().getIndexBuildCenterHost());
		String serviceName = null;
		if (StringUtils.isNotBlank(serviceName = this.getString("serviceName"))) {
			// iframeurl.append("?serviceName=").append(this.getString("serviceName"));
		}
		// context.put("IndexBuildCenterUrl", iframeurl);
		// ApplicationReportStatusSet reportStatus = new
		// ApplicationReportStatusSet();
		// if (StringUtils.isNotBlank(serviceName)) {
		//
		// List<ApplicationReport> reports =
		// yarnClient.getApplications(Sets.newHashSet(serviceName));
		// for (ApplicationReport r : reports) {
		// if (NEW == r.getYarnApplicationState() //
		// || NEW_SAVING == r.getYarnApplicationState() //
		// || SUBMITTED == r.getYarnApplicationState()) {
		// reportStatus.waiting.add(new ApplicationReportStatus(r));
		// } else if (ACCEPTED == r.getYarnApplicationState()) {
		// reportStatus.preparing.add(new ApplicationReportStatus(r));
		// } else if (RUNNING == r.getYarnApplicationState()) {
		// reportStatus.running.add(new ApplicationReportStatus(r));
		// } else if (FINISHED == r.getYarnApplicationState()) {
		// reportStatus.finished.add(new ApplicationReportStatus(r));
		// } else if (FAILED == r.getYarnApplicationState()) {
		// reportStatus.failed.add(new ApplicationReportStatus(r));
		// } else if (KILLED == r.getYarnApplicationState()) {
		// reportStatus.killed.add(new ApplicationReportStatus(r));
		// }
		// }
		//
		// }
		//

		Calendar calender = Calendar.getInstance();
		// 倒推7天
		calender.add(Calendar.DAY_OF_YEAR, -7);
		context.put("rs", yarnClient.getCollectionBuildReports(serviceName, calender.getTime()));
	}

	public YarnClient getYarnClient() {
		return yarnClient;
	}

	@Autowired
	public void setYarnClient(YarnClient yarnClient) {
		this.yarnClient = yarnClient;
	}

	public static class ApplicationReportStatusSet {

		public final Set<ApplicationReportStatus> waiting = Sets.newHashSet();
		public final Set<ApplicationReportStatus> preparing = Sets.newHashSet();
		public final Set<ApplicationReportStatus> running = Sets.newHashSet();
		public final Set<ApplicationReportStatus> finished = Sets.newHashSet();
		public final Set<ApplicationReportStatus> failed = Sets.newHashSet();
		public final Set<ApplicationReportStatus> killed = Sets.newHashSet();

		public final Set<ApplicationReportStatus> getWaiting() {
			return this.waiting;
		}

		public Set<ApplicationReportStatus> getPreparing() {
			return this.preparing;
		}

		public Set<ApplicationReportStatus> getRunning() {
			return this.running;
		}

		public Set<ApplicationReportStatus> getFinished() {
			return this.finished;
		}

		public Set<ApplicationReportStatus> getFailed() {
			return failed;
		}

		public Set<ApplicationReportStatus> getKilled() {
			return this.killed;
		}

	}

	@Override
	public boolean isAppNameAware() {
		return false;
	}

	@Override
	public boolean isEnableDomainView() {
		return true;
	}

}
