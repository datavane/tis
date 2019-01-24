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
package com.qlangtech.tis.servlet.filter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import com.qlangtech.tis.solrj.extend.AbstractTisCloudSolrClient;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.slf4j.MDC;
import org.slf4j.MDC;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisSolrDispatchFilter extends SolrDispatchFilter {

    private static final Logger logger = LoggerFactory.getLogger(TisSolrDispatchFilter.class);

    static {
        AbstractTisCloudSolrClient.initHashcodeRouter();
    }

    /**
     * @param servletContext
     * @return
     */
    // public static CoreContainer getCoresContainer(ServletContext servletContext)
    // {
    // CoreContainer container = (CoreContainer)
    // servletContext.getAttribute(TIS_SOLR_CORES_CONTAINER);
    // if (container == null) {
    // throw new IllegalStateException("container can not be null in
    // servletContext");
    // }
    // return container;
    // }
    // private final ScheduledExecutorService falconScheduler =
    // Executors.newScheduledThreadPool(1);
    @Override
    public void init(FilterConfig config) throws ServletException {
        logger.info("start TisSolrDispatchFilter.init()");
        super.init(config);
    // final CoreContainer coresContainer = this.getCores();
    // config.getServletContext().setAttribute(TIS_SOLR_CORES_CONTAINER,
    // coresContainer);
    // final String hostName = getHostname();
    // 向小米监控系统发送监控消息
    // falconScheduler.scheduleAtFixedRate(new Runnable() {
    // @Override
    // public void run() {
    // try {
    // sendStatus2Falcon(hostName, coresContainer);
    // } catch (Throwable e) {
    // e.printStackTrace();
    // SendSMSUtils.send("err send falcon:" + e.getMessage(),
    // SendSMSUtils.BAISUI_PHONE);
    // }
    // }
    // }, MonitorSysTagMarker.FalconSendTimeStep,
    // MonitorSysTagMarker.FalconSendTimeStep, TimeUnit.SECONDS);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain, boolean retry) throws IOException, ServletException {
        MDC.put("ip", request.getRemoteHost());
        super.doFilter(request, response, chain, retry);
    }

    private static class CollectionState {

        private long requests;

        private long errors;

        private long timeouts;

        private long totalTime;
    }

    private static String getHostname() {
        try {
            return StringUtils.substringBefore(InetAddress.getLocalHost().getHostName(), ".");
        } catch (UnknownHostException uhe) {
            throw new RuntimeException(uhe);
        }
    }
}
