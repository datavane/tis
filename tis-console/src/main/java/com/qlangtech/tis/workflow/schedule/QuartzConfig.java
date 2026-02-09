package com.qlangtech.tis.workflow.schedule;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Quartz 调度器配置类
 * 负责初始化 Quartz Scheduler 和配置持久化
 *
 * 核心职责：
 * 1. 初始化 Quartz Scheduler
 * 2. 配置 JobStore（内存或数据库持久化）
 * 3. 配置线程池
 * 4. 提供 Scheduler 实例
 * 5. 优雅关闭
 *
 * @author 百岁(baisui@qlangtech.com)
 * @date 2026-01-29
 */
public class QuartzConfig {

    private static final Logger logger = LoggerFactory.getLogger(QuartzConfig.class);

    /**
     * Quartz Scheduler 实例
     */
    private Scheduler scheduler;

    /**
     * 是否使用数据库持久化
     * true: 使用 JDBC JobStore（集群模式）
     * false: 使用 RAMJobStore（单机模式）
     */
    private final boolean usePersistence;

    public QuartzConfig(boolean usePersistence) {
        this.usePersistence = usePersistence;
    }

    /**
     * 初始化 Quartz Scheduler
     *
     * 实现步骤：
     * 1. 创建 Quartz 配置 Properties
     * 2. 配置 Scheduler 名称和实例 ID
     * 3. 配置 JobStore（内存或数据库）
     * 4. 配置线程池
     * 5. 创建 Scheduler 实例
     * 6. 启动 Scheduler
     *
     * @throws SchedulerException 初始化失败
     */
    public void initialize() throws SchedulerException {
        logger.info("Initializing Quartz Scheduler (usePersistence={})", usePersistence);

        try {
            // TODO: 实现 Scheduler 初始化
            // 1. 创建配置
            // Properties props = createQuartzProperties();

            // 2. 创建 SchedulerFactory
            // StdSchedulerFactory factory = new StdSchedulerFactory(props);

            // 3. 获取 Scheduler 实例
            // scheduler = factory.getScheduler();

            // 4. 启动 Scheduler
            // scheduler.start();

            // 临时实现：使用默认配置
            StdSchedulerFactory factory = new StdSchedulerFactory();
            scheduler = factory.getScheduler();
            scheduler.start();

            logger.info("Quartz Scheduler initialized successfully");

        } catch (SchedulerException e) {
            logger.error("Failed to initialize Quartz Scheduler", e);
            throw e;
        }
    }

    /**
     * 创建 Quartz 配置
     *
     * @return Quartz 配置 Properties
     */
    private Properties createQuartzProperties() {
        Properties props = new Properties();

        // TODO: 实现配置创建
        // 基础配置
        // props.setProperty("org.quartz.scheduler.instanceName", "TIS-Workflow-Scheduler");
        // props.setProperty("org.quartz.scheduler.instanceId", "AUTO");

        // 线程池配置
        // props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        // props.setProperty("org.quartz.threadPool.threadCount", "10");
        // props.setProperty("org.quartz.threadPool.threadPriority", "5");

        // if (usePersistence) {
        //     // 数据库持久化配置（集群模式）
        //     props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        //     props.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        //     props.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");
        //     props.setProperty("org.quartz.jobStore.isClustered", "true");
        //     props.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");
        //     props.setProperty("org.quartz.jobStore.dataSource", "tisDS");
        //
        //     // 数据源配置（使用 TIS 现有数据源）
        //     // 需要从 TIS 配置中获取数据库连接信息
        //     props.setProperty("org.quartz.dataSource.tisDS.driver", "com.mysql.jdbc.Driver");
        //     props.setProperty("org.quartz.dataSource.tisDS.URL", "jdbc:mysql://localhost:3306/tis");
        //     props.setProperty("org.quartz.dataSource.tisDS.user", "root");
        //     props.setProperty("org.quartz.dataSource.tisDS.password", "");
        //     props.setProperty("org.quartz.dataSource.tisDS.maxConnections", "5");
        // } else {
        //     // 内存存储配置（单机模式）
        //     props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        // }

        return props;
    }

    /**
     * 获取 Scheduler 实例
     *
     * @return Scheduler 实例
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * 优雅关闭 Scheduler
     */
    public void shutdown() {
        logger.info("Shutting down Quartz Scheduler...");

        if (scheduler != null) {
            try {
                // TODO: 实现优雅关闭
                // waitForJobsToComplete = true 表示等待正在执行的任务完成
                // scheduler.shutdown(true);

                scheduler.shutdown(false);
                logger.info("Quartz Scheduler shutdown completed");

            } catch (SchedulerException e) {
                logger.error("Failed to shutdown Quartz Scheduler", e);
            }
        }
    }

    /**
     * 检查 Scheduler 是否已启动
     *
     * @return true 如果已启动
     */
    public boolean isStarted() {
        try {
            return scheduler != null && scheduler.isStarted();
        } catch (SchedulerException e) {
            logger.error("Failed to check scheduler status", e);
            return false;
        }
    }
}
