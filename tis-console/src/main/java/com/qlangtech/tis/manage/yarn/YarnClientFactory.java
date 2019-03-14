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
package com.qlangtech.tis.manage.yarn;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

//import java.io.IOException;
//import java.io.InputStream;
//
//import org.apache.hadoop.yarn.conf.YarnConfiguration;
//import org.springframework.beans.factory.FactoryBean;
//import org.springframework.beans.factory.InitializingBean;
//
//import com.qlangtech.tis.yarn.common.YarnConstant;

//import java.io.IOException;
//import java.io.InputStream;
//
//import org.apache.hadoop.yarn.client.api.YarnClient;
//import org.apache.hadoop.yarn.conf.YarnConfiguration;
//import org.springframework.beans.factory.FactoryBean;
//import org.springframework.beans.factory.InitializingBean;
//
//import com.qlangtech.tis.yarn.common.YarnConstant;

/**
 * @author 百岁
 *
 * @date 2019年3月12日
 */
public class YarnClientFactory implements FactoryBean<YarnClient>, InitializingBean {
	private YarnClient yarnClient;

	//
	@Override
	public void afterPropertiesSet() throws Exception {
		this.yarnClient = new YarnClient();
		// this.yarnClient = YarnClient.createYarnClient();
		// this.yarnClient.init(getYarnConfig());
		// this.yarnClient.start();

		// Set<String> applicationTypes = Sets.newHashSet("YARN");
		// List<ApplicationReport> reports =
		// yarnClient.getApplications(applicationTypes);
		// for (ApplicationReport report : reports) {
		// System.out.println(report.getName() + ",start:" +
		// report.getStartTime() + ",state:"
		// + report.getFinalApplicationStatus());
		// }
	}
	//
	// private YarnConfiguration getYarnConfig() throws IOException {
	// YarnConfiguration conf = new YarnConfiguration();
	// conf.set("hadoop.job.ugi", "search");
	// InputStream yarnsiteStream =
	// this.getClass().getResourceAsStream(YarnConstant.CLASSPATH_YARN_CONFIG_PATH);
	// if (yarnsiteStream == null) {
	// throw new IllegalStateException(
	// "yarn-site.xml is not exist in class path:" +
	// YarnConstant.CLASSPATH_YARN_CONFIG_PATH);
	// }
	//
	// conf.addResource(yarnsiteStream);
	// return conf;
	// }

	@Override
	public YarnClient getObject() throws Exception {
		return this.yarnClient;
	}

	//
	@Override
	public Class<?> getObjectType() {
		return YarnClient.class;
	}

	//
	@Override
	public boolean isSingleton() {
		return true;
	}

}
