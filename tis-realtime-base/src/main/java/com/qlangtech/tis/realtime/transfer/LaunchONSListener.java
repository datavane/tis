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
package com.qlangtech.tis.realtime.transfer;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.management.MBeanException;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
// import org.apache.hadoop.net.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.realtime.servlet.JettyTISRunner;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class LaunchONSListener extends BasicTransferTool implements Daemon {

    // http://www.slf4j.org/codes.html#StaticLoggerBinder
    private static final Logger log = LoggerFactory.getLogger(LaunchONSListener.class);

    // private static final String INCR_BIZ_GROUP = "incr_biz_group_order";
    private TransferStatusMBean mbean;

    public static void main(String[] args) throws Exception {
        LaunchONSListener launcher = new LaunchONSListener();
        launcher.start();
        synchronized (launcher) {
            launcher.wait();
        }
    }

    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
    }

    // private JettyTISRunner jettyServer;
    @Override
    public void start() throws Exception {
        Set<String> empty = Collections.emptySet();
        startService(empty);
        List<IOnsListenerStatus> incrChannels = getAllTransferChannel();
        launchMBean(incrChannels);
        log.info("appcontext launch successfull");
    // Collection<IOnsListenerStatus> allIncrListener = incrChannels;
    // RealtimeReportWebSocketServlet realtimeReportServlet = new
    // RealtimeReportWebSocketServlet(
    // allIncrListener);
    // RealtimeStatePageServlet statePageServlet = new
    // RealtimeStatePageServlet(
    // allIncrListener);
    // int retryCount = 0;
    // final int exportPort = NetUtils.getFreeSocketPort();
    // while (true) {
    // 
    // try {
    // this.jettyServer = new JettyTISRunner("/", exportPort);
    // this.prepareJettyServer(jettyServer, exportPort);
    // // this.jettyServer.addServlet(realtimeReportServlet,
    // // "/realtime-status");
    // // this.jettyServer.addServlet(statePageServlet, "/state");
    // this.jettyServer.start();
    // 
    // // BasicONSListener.solrClient;
    // // BasicONSListener.solrClient.getZkClient().create(INCR_BIZ_GROUP,
    // // data, createMode, retryOnConnLoss)
    // 
    // } catch (Throwable e) {
    // // if (++retryCount > 10) {
    // throw new RuntimeException(e);
    // // } else {
    // // log.warn(e.getMessage());
    // // continue;
    // // }
    // }
    // 
    // log.info("jetty server started,export port:" + exportPort);
    // return;
    // }
    }

    protected void prepareJettyServer(JettyTISRunner jettyServer, int exportPort) throws Exception {
    }

    protected void launchMBean(List<IOnsListenerStatus> incrChannels) throws MBeanException, Exception {
        this.mbean = new TransferStatusMBean(this.getIndexNames(), incrChannels);
        mbean.afterPropertiesSet();
    }

    /**
     * @return
     */
    // public static ClassPathXmlApplicationContext createSpringContext() {
    // 
    // }
    @Override
    public void stop() throws Exception {
    }

    @Override
    public void destroy() {
    }
}
