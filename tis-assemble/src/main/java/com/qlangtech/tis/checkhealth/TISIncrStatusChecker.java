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
package com.qlangtech.tis.checkhealth;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qlangtech.tis.git.GitUtils;
import com.qlangtech.tis.git.GitUtils.IncrMonitorIndexs;
import com.qlangtech.tis.order.center.AssembleConfig;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;
import com.dihuo.app.common.monitor.ServletContextAware;
import com.dihuo.app.common.monitor.StatusChecker;
import com.dihuo.app.common.monitor.enums.Mode;
import com.dihuo.app.common.monitor.enums.StatusLevel;
import com.dihuo.app.common.monitor.model.StatusModel;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/*
 * 监控增量执行任务是否在执行是否有增量,是否正常执行
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TISIncrStatusChecker implements StatusChecker, ServletContextAware {

    private ServletContext servletContext;

    private IncrMonitorIndexs monitorIndexs;

    private IndexSwapTaskflowLauncher launcherContext;

    // 监控时间一个小时
    private static final long INCR_STATE_MONITOR_INTERVAL = 60 * 60;

    // 最後一次收集數據時間
    private long lastMonitorTimeStampSec;

    private static final Logger logger = LoggerFactory.getLogger("check_health");

    // private TSearcherClusterInfoCollect tisClusterInfoCollect;
    // private ZkStateReader zkStateReader;
    private void refeshLastMonitorTimeStampSec() {
        lastMonitorTimeStampSec = System.currentTimeMillis() / 1000;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public StatusModel check() {
        // logger.info("do check_health");
        StatusModel stateModel = new StatusModel();
        stateModel.level = StatusLevel.OK;
        long current = System.currentTimeMillis() / 1000;
        // 每20分钟监测一次
        if (lastMonitorTimeStampSec + INCR_STATE_MONITOR_INTERVAL < current) {
            // ////////////////////////////////////////////////////////////////
            Map<String, Long> /* node last update timesec */
            nodesLastUpdateTimeSec = null;
            final StringBuffer buffer = new StringBuffer();
            for (String index : this.monitorIndexs.includes) {
                nodesLastUpdateTimeSec = launcherContext.getIncrStatusUmbilicalProtocol().getLastUpdateTimeSec(index);
                if (nodesLastUpdateTimeSec.size() < 1) {
                    // 增量任务已经停止
                    stateModel.message = index + " incr task shutdown";
                    stateModel.level = StatusLevel.FAIL;
                    logger.error(stateModel.message);
                    return stateModel;
                }
                for (Map.Entry<String, Long> /* node last update timesec */
                s : nodesLastUpdateTimeSec.entrySet()) {
                    if ((s.getValue() + INCR_STATE_MONITOR_INTERVAL) < current) {
                        stateModel.message = index + "_incr n:" + s.getKey() + " Run down," + s.getValue();
                        stateModel.level = StatusLevel.FAIL;
                        logger.error(stateModel.message);
                        return stateModel;
                    }
                }
                printLastUpdateTimeSec(buffer, index, nodesLastUpdateTimeSec);
            }
            logger.info(buffer.toString());
            refeshLastMonitorTimeStampSec();
        }
        return stateModel;
    // final long lastCollectTimeStamp =
    // tisClusterInfoCollect.getLastCollectTimeStamp();
    // if (System.currentTimeMillis() > (lastCollectTimeStamp +
    // (COLLECT_STATE_INTERVAL * 2 * 1000))) {
    // stateModel.level = StatusLevel.FAIL;
    // SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    // stateModel.message = "from:" + timeFormat.format(new
    // Date(lastCollectTimeStamp))
    // + " have not collect cluster status";
    // logger.info("cluster collect has error:" + stateModel.message);
    // logger.info("System.currentTimeMillis():" + System.currentTimeMillis());
    // logger.info("lastCollectTimeStamp:" + lastCollectTimeStamp);
    // } else {
    // logger.info("cluster collect is regular");
    // }
    // 
    // return stateModel;
    }

    private static final ThreadLocal<SimpleDateFormat> timeFormat = new ThreadLocal<SimpleDateFormat>() {

        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        }
    };

    private void printLastUpdateTimeSec(StringBuffer buffer, String indexName, Map<String, /* FromAddress */
    Long> nodesLastUpdateTimeSec) {
        // final StringBuffer buffer = new StringBuffer("incr status monitor:" +
        // indexName + "\n");
        buffer.append("\nincr status monitor:" + indexName);
        for (Map.Entry<String, Long> /* node last update timesec */
        entry : nodesLastUpdateTimeSec.entrySet()) {
            buffer.append("\n\t").append(entry.getKey()).append(",lastUpdate:").append(timeFormat.get().format(new Date(entry.getValue() * 1000)));
        }
    }

    @Override
    public void init() {
        refeshLastMonitorTimeStampSec();
        this.launcherContext = IndexSwapTaskflowLauncher.getIndexSwapTaskflowLauncher(this.servletContext);
        try {
            if (AssembleConfig.isDisbleIncrIndexMonitor()) {
                this.monitorIndexs = new IncrMonitorIndexs();
            } else {
                this.monitorIndexs = GitUtils.$().getIncrMonitorIndexs(RunEnvironment.getSysRuntime());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("start monitor index:" + Arrays.toString(this.monitorIndexs.includes.toArray()));
    // ApplicationContext appContext = WebApplicationContextUtils
    // .getRequiredWebApplicationContext(servletContext);
    // this.tisClusterInfoCollect = appContext.getBean("clusterInfoCollect",
    // TSearcherClusterInfoCollect.class);
    // this.zkStateReader = appContext.getBean("solrClient", ZkStateReader.class);
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
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> urls = loader.getResources("META-INF/services/" + StatusChecker.class.getName());
        while (urls.hasMoreElements()) {
            System.out.println(urls.nextElement());
        }
    }
}
