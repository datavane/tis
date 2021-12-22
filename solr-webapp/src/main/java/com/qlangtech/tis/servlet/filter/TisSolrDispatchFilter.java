/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.servlet.filter;

import com.qlangtech.tis.solrextend.core.TISCoresLocator;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.NodeConfig;
import org.apache.solr.core.SolrXmlConfig;
import org.apache.solr.servlet.SolrDispatchFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年2月22日
 */
public class TisSolrDispatchFilter extends SolrDispatchFilter {

    private static final String TIS_SOLR_CORES_CONTAINER = "tis_solr_cores_container";

    static {
       // AbstractTisCloudSolrClient.initHashcodeRouter();
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

    @Override
    protected CoreContainer createCoreContainer(Path solrHome, Properties extraProperties) {
        NodeConfig nodeConfig = SolrXmlConfig.fromSolrHome(solrHome, extraProperties);
        loadNodeConfig(solrHome, extraProperties);
        final CoreContainer coreContainer = new CoreContainer(nodeConfig, new TISCoresLocator(nodeConfig.getCoreRootDirectory()), true);
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
//    private static String getHostname() {
//        try {
//            return StringUtils.substringBefore(InetAddress.getLocalHost().getHostName(), ".");
//        } catch (UnknownHostException uhe) {
//            throw new RuntimeException(uhe);
//        }
//    }
}
