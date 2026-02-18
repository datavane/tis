/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qlangtech.tis.order.center;

import com.qlangtech.tis.cloud.ITISCoordinator;

import com.qlangtech.tis.datax.DataXJobSubmit;
import com.qlangtech.tis.datax.DataXJobSubmitAkkaClusterSupport;
import com.qlangtech.tis.exec.AbstractActionInvocation;
import com.qlangtech.tis.exec.ActionInvocation;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.exec.ExecuteResult;
import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.realtime.transfer.IOnsListenerStatus;
import com.qlangtech.tis.rpc.server.DefaultLoggerAppenderServiceImpl;
import com.qlangtech.tis.rpc.server.FullBuildStatCollectorServer;
import com.qlangtech.tis.rpc.server.IncrStatusServer;
import com.qlangtech.tis.rpc.server.IncrStatusUmbilicalProtocolImpl;
import com.qlangtech.tis.solrj.util.ZkUtils;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年11月5日 下午6:57:19
 */
public class IndexSwapTaskflowLauncher implements Daemon, ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(IndexSwapTaskflowLauncher.class);

    public static final String KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER = "IndexSwapTaskflowLauncher";
    private ITISCoordinator zkClient;


    static {
    }

    public static void initPhaseStatusStatusWriter() {

    }


    public static IndexSwapTaskflowLauncher getIndexSwapTaskflowLauncher(ServletContext context) {
        IndexSwapTaskflowLauncher result =
                (IndexSwapTaskflowLauncher) context.getAttribute(KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER);
        if (result == null) {
            throw new IllegalStateException("IndexSwapTaskflowLauncher can not be null in servletContext");
        }
        return result;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("IndexSwapTaskflowLauncher shutting down...");

        logger.info("IndexSwapTaskflowLauncher shutdown completed");
    }


    public void setZkClient(ITISCoordinator zkClient) {
        this.zkClient = zkClient;
    }

    public ITISCoordinator getZkClient() {
        throw new UnsupportedOperationException();
    }


    private Collection<IOnsListenerStatus> incrChannels;

    public Collection<IOnsListenerStatus> getIncrChannels() {
        return incrChannels;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // AbstractTisCloudSolrClient.initHashcodeRouter();
        // 构建各阶段持久化
        try {
            this.afterPropertiesSet();
            this.incrChannels = initIncrTransferStateCollect();
            // FlumeApplication.startFlume();
            sce.getServletContext().setAttribute(KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER, this);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        DataXJobSubmit dataXJobSubmit = DataXJobSubmit.getDataXJobSubmit();
        if (!(dataXJobSubmit instanceof DataXJobSubmitAkkaClusterSupport)) {
            throw new IllegalStateException("dataXJobSubmit:" + dataXJobSubmit.getClass().getName() + " must be type "
                    + "of " + DataXJobSubmitAkkaClusterSupport.class.getSimpleName());
        }
        ((DataXJobSubmitAkkaClusterSupport) dataXJobSubmit).launchAkkaCluster();
    }

    public void afterPropertiesSet() throws Exception {
        this.setZkClient(ITISCoordinator.create());
    }

    private IncrStatusServer incrStatusServer;

    public IncrStatusServer getIncrStatusUmbilicalProtocol() {
        if (incrStatusServer == null) {
            throw new IllegalStateException("incrStatusUmbilicalProtocolServer can not be null");
        }
        return this.incrStatusServer;
    }

    // 发布增量集群任务收集器
    private Collection<IOnsListenerStatus> initIncrTransferStateCollect() throws Exception {
        // this.incrStatusUmbilicalProtocolServer = new IncrStatusUmbilicalProtocolImpl();
        final int exportPort = ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PORT; //NetUtils.getFreeSocketPort();
        incrStatusServer = new IncrStatusServer(exportPort);
        incrStatusServer.addService(IncrStatusUmbilicalProtocolImpl.getInstance());
        incrStatusServer.addService(DefaultLoggerAppenderServiceImpl.getInstance());
        incrStatusServer.addService(FullBuildStatCollectorServer.getInstance());
        incrStatusServer.start();
        final List<IOnsListenerStatus> result = new ArrayList<>();
        Collection<IOnsListenerStatus> incrChannels = getAllTransferChannel(result);


        ZkUtils.registerAddress2ZK(// "/tis/incr-transfer-group/incr-state-collect"
                this.zkClient, // "/tis/incr-transfer-group/incr-state-collect"
                ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH, exportPort);
        IncrStatusUmbilicalProtocolImpl.getInstance().startLogging();
        return incrChannels;
    }

    public List<IOnsListenerStatus> getAllTransferChannel(final List<IOnsListenerStatus> result) {
        return result;
    }

    /**
     * 由servlet接收到命令之后触发
     *
     * @param execContext
     * @throws Exception
     */
    @SuppressWarnings("all")
    public ExecuteResult startWork(DefaultChainContext chainContext) throws Exception {
        //chainContext.rebindLoggingMDCParams();
        ActionInvocation invoke = null;
        ExecutePhaseRange range = chainContext.getExecutePhaseRange();
        logger.info("start component:" + range.getStart() + ",end component:" + range.getEnd());
        Objects.requireNonNull(this.zkClient, "zkClient can not be null");
        chainContext.setZkClient(this.zkClient);
        invoke = AbstractActionInvocation.createExecChain(chainContext);
        ExecuteResult execResult = invoke.invoke();
        if (!execResult.isSuccess()) {
            logger.warn(execResult.getMessage());
        }
        return execResult;
    }


    @Override
    public void init(DaemonContext context) throws DaemonInitException, Exception {
    }

    @Override
    public void start() throws Exception {
        afterPropertiesSet();
        logger.info("index Swap Task ready");
    }

    public static void main(String[] arg) throws Exception {
        IndexSwapTaskflowLauncher launcher = new IndexSwapTaskflowLauncher();
        launcher.start();
        synchronized (launcher) {
            launcher.wait();
        }
    }

    @Override
    public void stop() throws Exception {
    }

    @Override
    public void destroy() {
    }

    //    /**
    //     * 初始化 TIS Actor System
    //     *
    //     * 实现步骤：
    //     * 1. 获取 Spring ApplicationContext
    //     * 2. 从 Spring 容器中获取 DAO 依赖
    //     * 3. 创建 TISActorSystem 实例
    //     * 4. 初始化 Actor System
    //     * 5. 将实例保存到 ServletContext（供其他组件使用）
    //     *
    //     * @param sce ServletContextEvent
    //     */
    //    private void initializeTISActorSystem(ServletContextEvent sce) {
    //        logger.info("Initializing TIS Actor System...");
    //
    //        try {
    //            // 1. 获取 Spring ApplicationContext
    //            WebApplicationContext applicationContext =
    //                WebApplicationContextUtils.getWebApplicationContext(sce.getServletContext());
    //
    //            if (applicationContext == null) {
    //                logger.warn("Spring ApplicationContext not available, skipping Actor System initialization");
    //                return;
    //            }
    //
    //            // 2. 从 Spring 容器中获取 DAO 依赖
    //            IWorkFlowBuildHistoryDAO workflowBuildHistoryDAO =
    //                applicationContext.getBean(IWorkFlowBuildHistoryDAO.class);
    //            IDAGNodeExecutionDAO dagNodeExecutionDAO =
    //                applicationContext.getBean(IDAGNodeExecutionDAO.class);
    //
    //            if (workflowBuildHistoryDAO == null || dagNodeExecutionDAO == null) {
    //                logger.warn("Required DAO beans not found, skipping Actor System initialization");
    //                return;
    //            }
    //
    //            // 3. 创建 TISActorSystem 实例
    //            tisActorSystem = new TISActorSystem(workflowBuildHistoryDAO, dagNodeExecutionDAO);
    //
    //            // 4. 初始化 Actor System
    //            tisActorSystem.initialize();
    //
    //            // 5. 将实例保存到 ServletContext（供其他组件使用）
    //            sce.getServletContext().setAttribute(TISActorSystemHolder.ATTR_TIS_ACTOR_SYSTEM, tisActorSystem);
    //
    //            logger.info("TIS Actor System initialized successfully");
    //
    //        } catch (Exception e) {
    //            logger.error("Failed to initialize TIS Actor System", e);
    //            // 不抛出异常，允许 TIS 继续启动（Actor System 是可选功能）
    //        }
    //    }
}
