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
package com.qlangtech.tis.servlet.filter;

import com.google.common.base.Joiner;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.SolrXmlConfig;
import org.apache.solr.core.TisCoreContainer;
import org.apache.solr.servlet.SolrDispatchFilter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年2月22日
 */
public class TisSolrDispatchFilter extends SolrDispatchFilter {

    private static final String TIS_SOLR_CORES_CONTAINER = "tis_solr_cores_container";

    static {
        AbstractTisCloudSolrClient.initHashcodeRouter();
    }

    // /**
    // * @param servletContext
    // * @return
    // */
    // public static CoreContainer getCoresContainer(ServletContext servletContext) {
    // CoreContainer container = (CoreContainer) servletContext.getAttribute(TIS_SOLR_CORES_CONTAINER);
    // if (container == null) {
    // throw new IllegalStateException("container can not be null in servletContext");
    // }
    // return container;
    // }
    // private final ScheduledExecutorService falconScheduler = Executors.newScheduledThreadPool(1);
    @Override
    public void init(FilterConfig config) throws ServletException {
        super.init(config);
        final CoreContainer coresContainer = this.getCores();
        config.getServletContext().setAttribute(TIS_SOLR_CORES_CONTAINER, coresContainer);
    // final String hostName = getHostname();
    // 向小米监控系统发送监控消息
    // falconScheduler.scheduleAtFixedRate(new Runnable() {
    // @Override
    // public void run() {
    // try {
    // sendStatus2Falcon(hostName, coresContainer);
    // } catch (Throwable e) {
    // e.printStackTrace();
    // SendSMSUtils.send("err send falcon:" + e.getMessage(), SendSMSUtils.BAISUI_PHONE);
    // }
    // }
    // }, MonitorSysTagMarker.FalconSendTimeStep, MonitorSysTagMarker.FalconSendTimeStep, TimeUnit.SECONDS);
    }

    protected CoreContainer createCoreContainer(Path solrHome, Properties extraProperties) {
        NodeConfig nodeConfig = SolrXmlConfig.fromSolrHome(solrHome, extraProperties);
        loadNodeConfig(solrHome, extraProperties);
        final CoreContainer coreContainer = new TisCoreContainer(nodeConfig, true);
        coreContainer.load();
        return coreContainer;
    }

    // private static final String FALCON_MONITOR_KEY_SELECT = "/select";
    // 
    // private static final Joiner joinerWith = Joiner.on("_").skipNulls();
    // private static class CollectionState {
    // 
    // private long requests;
    // 
    // private long errors;
    // 
    // private long timeouts;
    // 
    // private long totalTime;
    // }
    private static String getHostname() {
        try {
            return StringUtils.substringBefore(InetAddress.getLocalHost().getHostName(), ".");
        } catch (UnknownHostException uhe) {
            throw new RuntimeException(uhe);
        }
    }
}
