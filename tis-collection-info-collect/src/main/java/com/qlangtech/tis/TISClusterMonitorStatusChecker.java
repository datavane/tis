/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis;

import com.qlangtech.tis.health.check.IStatusChecker;
import com.qlangtech.tis.health.check.Mode;
import com.qlangtech.tis.health.check.StatusLevel;
import com.qlangtech.tis.health.check.StatusModel;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.solr.common.cloud.ZkStateReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.support.WebApplicationContextUtils;
import javax.servlet.ServletContext;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 判断tis集群增量统计工作是否正常进行中
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月1日
 */
public class TISClusterMonitorStatusChecker implements IStatusChecker, ServletContextAware {

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
        if (System.currentTimeMillis() > (lastCollectTimeStamp + (TSearcherClusterInfoCollect.COLLECT_STATE_INTERVAL * 2 * 1000))) {
            stateModel.level = StatusLevel.FAIL;
            SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            stateModel.message = "from:" + timeFormat.format(new Date(lastCollectTimeStamp)) + " have not collect cluster status";
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
    // public static void main(String[] args) throws Exception {
    // ClassLoader loader = Thread.currentThread().getContextClassLoader();
    // Enumeration<URL> urls = loader.getResources("META-INF/services/" + StatusChecker.class.getName());
    // while (urls.hasMoreElements()) {
    // System.out.println(urls.nextElement());
    // }
    // }
}
