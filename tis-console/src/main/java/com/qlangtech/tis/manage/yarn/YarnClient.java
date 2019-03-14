package com.qlangtech.tis.manage.yarn;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.ConfigFileContext.Header;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.runtime.module.action.BasicModule;
import com.qlangtech.tis.trigger.module.screen.Buildindexmonitor.ApplicationReportStatusSet;

/**
 * @author 百岁
 * @date 2019年3月13日
 */
public class YarnClient {

	private static final List<Header> HEADER_TEXT_JSON = Lists.newArrayList(new Header("Accept", "application/json"));

	// http://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/ResourceManagerRest.html
	public ApplicationReportStatusSet getCollectionBuildReports(String collectionName, Date start) throws Exception {

		StringBuffer buffer = new StringBuffer(
				"http://" + Config.getYarnResourceManagerHost() + ":8088/ws/v1/cluster/apps?deSelects=resouceRequests");

		buffer.append("&startedTimeBegin=").append(start.getTime());
		buffer.append("&startedTimeEnd=").append((new Date()).getTime());

		if (StringUtils.isNotBlank(collectionName)) {
			buffer.append("&applicationTypes=").append(collectionName);
		}

		final URL url = new URL(buffer.toString());
		return HttpUtils.processContent(url, new StreamProcess<ApplicationReportStatusSet>() {

			@Override
			public ApplicationReportStatusSet p(int status, InputStream stream, String md5) {
				ApplicationReportStatusSet reportStatusSet = new ApplicationReportStatusSet();
				try {

					ApplicationReportStatus r = null;
					JSONObject jobj = JSON.parseObject(IOUtils.toString(stream, BasicModule.getEncode()));
					JSONArray apps = jobj.getJSONObject("apps").getJSONArray("app");

					for (int i = 0; i < apps.size(); i++) {
						r = apps.getObject(i, ApplicationReportStatus.class);

						if ("FAILED".equals(r.getState()) || "FAILED".equals(r.getFinalStatus())) {
							reportStatusSet.failed.add((r));
						} else if ("NEW".equals(r.getState()) //
								|| "NEW_SAVING".equals(r.getState()) //
								|| "SUBMITTED".equals(r.getState())) {
							reportStatusSet.waiting.add(r);
						} else if ("ACCEPTED".equals(r.getState())) {
							reportStatusSet.preparing.add((r));
						} else if ("RUNNING".equals(r.getState())) {
							reportStatusSet.running.add((r));
						} else if ("FINISHED".equals(r.getState())) {
							reportStatusSet.finished.add((r));
						} else if ("KILLED".equals(r.getState())) {
							reportStatusSet.killed.add((r));
						}

					}

				} catch (IOException e) {
					throw new IllegalStateException(e);
				}

				return reportStatusSet;
			}

			@Override
			public List<Header> getHeaders() {
				return HEADER_TEXT_JSON;
			}
		});

	}

	public static void main(String[] args) throws Exception {
		YarnClient client = new YarnClient();
		// client.getCollectionBuildReports("");
	}
}
