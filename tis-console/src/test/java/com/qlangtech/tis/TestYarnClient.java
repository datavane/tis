package com.qlangtech.tis;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.utils.Sets;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

import com.qlangtech.tis.yarn.common.YarnConstant;

import junit.framework.TestCase;

/*
 * @author 百岁（baisui@2dfire.com）
 *
 * @date 2019年3月12日
 */
public class TestYarnClient extends TestCase {

	public void testLaunch() throws Exception {
		
		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(getYarnConfig());
		yarnClient.start();

		Set<String> applicationTypes = Sets.newHashSet("YARN");
		List<ApplicationReport> reports = yarnClient.getApplications(applicationTypes);
		for (ApplicationReport report : reports) {
			System.out.println(report.getName() + ",start:" + report.getStartTime() + ",state:"
					+ report.getFinalApplicationStatus());
		}
		
	}

	private YarnConfiguration getYarnConfig() throws IOException {
		YarnConfiguration conf = new YarnConfiguration();
		conf.set("hadoop.job.ugi", "search");
		InputStream yarnsiteStream = this.getClass().getResourceAsStream(YarnConstant.CLASSPATH_YARN_CONFIG_PATH);
		if (yarnsiteStream == null) {
			throw new IllegalStateException(
					"yarn-site.xml is not exist in class path:" + YarnConstant.CLASSPATH_YARN_CONFIG_PATH);
		}

		conf.addResource(yarnsiteStream);
		return conf;
	}
}
