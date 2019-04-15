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
package com.qlangtech.tis;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.common.cloud.ZkStateReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.qlangtech.tis.health.check.IStatusChecker;
import com.qlangtech.tis.health.check.Mode;
import com.qlangtech.tis.health.check.StatusLevel;
import com.qlangtech.tis.health.check.StatusModel;
import com.qlangtech.tis.web.start.IServletContextAware;

/*
 * 判断tis集群增量统计工作是否正常进行中
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISClusterMonitorStatusChecker implements IStatusChecker, IServletContextAware {

	private ServletContext servletContext;

	private static final Logger logger = LoggerFactory.getLogger("check_health");

	private TSearcherClusterInfoCollect tisClusterInfoCollect;

	private ZkStateReader zkStateReader;

	@Override
	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public StatusModel check() {
		logger.info("do check_health");
		StatusModel stateModel = new StatusModel();
		stateModel.level = StatusLevel.OK;
		try {
			zkStateReader.getZkClient().getChildren("/", null, true);
			logger.info("zk is regular");
		} catch (Exception e) {
			logger.error("zk is unhealth!!!!!!", e);
			stateModel.level = StatusLevel.FAIL;
			stateModel.message = ExceptionUtils.getMessage(e);
			return stateModel;
		}
		// ////////////////////////////////////////////////////////////////
		final long lastCollectTimeStamp = tisClusterInfoCollect.getLastCollectTimeStamp();
		if (System.currentTimeMillis() > (lastCollectTimeStamp
				+ (TSearcherClusterInfoCollect.COLLECT_STATE_INTERVAL * 2 * 1000))) {
			stateModel.level = StatusLevel.FAIL;
			SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			stateModel.message = "from:" + timeFormat.format(new Date(lastCollectTimeStamp))
					+ " have not collect cluster status";
			logger.info("cluster collect has error:" + stateModel.message);
			logger.info("System.currentTimeMillis():" + System.currentTimeMillis());
			logger.info("lastCollectTimeStamp:" + lastCollectTimeStamp);
		} else {
			logger.info("cluster collect is regular");
		}
		return stateModel;
	}

	@Override
	public void init() {
		ApplicationContext appContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		this.tisClusterInfoCollect = appContext.getBean("clusterInfoCollect", TSearcherClusterInfoCollect.class);
		this.zkStateReader = appContext.getBean("solrClient", ZkStateReader.class);
	}

	@Override
	public Mode mode() {
		return Mode.PUB;
	}

	@Override
	public int order() {
		return 0;
	}

	public static void main(String[] args) throws Exception {
//		ClassLoader loader = Thread.currentThread().getContextClassLoader();
//		Enumeration<URL> urls = loader.getResources("META-INF/services/" + StatusChecker.class.getName());
//		while (urls.hasMoreElements()) {
//			System.out.println(urls.nextElement());
//		}
	}
}
