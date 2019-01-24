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
package com.qlangtech.tis.hdfs.client.context.impl;

import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.InitializingBean;
import com.qlangtech.tis.hdfs.TISHdfsUtils;
import com.qlangtech.tis.common.config.IServiceConfig;
import com.qlangtech.tis.common.zk.TerminatorZkClient;
import com.qlangtech.tis.exception.TimeManageException;
import com.qlangtech.tis.hdfs.client.context.TSearcherDumpContext;
import com.qlangtech.tis.hdfs.client.context.TSearcherQueryContext;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherQueryContextImpl.ServiceConfigChangeListener;
import com.qlangtech.tis.hdfs.client.process.BatchDataProcessor;
import com.qlangtech.tis.hdfs.client.router.GroupRouter;
import com.qlangtech.tis.hdfs.client.status.SolrCoreStatusHolder;
import com.qlangtech.tis.hdfs.client.time.FileTimeProvider;
import com.qlangtech.tis.hdfs.util.ServiceNameAware;

/*
 * 数据DUMP上下文实现，（无状态）
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TSearcherDumpContextImpl implements TSearcherDumpContext, ServiceNameAware, InitializingBean {

    private static final String FS_DEFAULT_NAME = "fs.default.name";

    protected static final Log logger = LogFactory.getLog(TSearcherDumpContextImpl.class);

    protected String fsName;

    private TSearcherQueryContext queryContext;

    private int triggerPort = 0;

    public int getTriggerPort() {
        return triggerPort;
    }

    public void setTriggerPort(int triggerPort) {
        this.triggerPort = triggerPort;
    }

    // 执行dump逻辑的时候，是否需要判断本节点是否抢夺到了锁
    private boolean triggerLock;

    @Override
    public void fireServiceConfigChange() {
        queryContext.fireServiceConfigChange();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.initDistributedSystemTaskLock();
    }

    public void addCoreConfigChangeListener(ServiceConfigChangeListener listener) {
        queryContext.addCoreConfigChangeListener(listener);
    }

    // @Override
    private void initDistributedSystemTaskLock() {
        logger.warn("【注意】初始化集群系统分布式锁、HDFS任务阀值锁<<");
    // HdfsJobLock hdfsLock =
    // new HdfsJobLock(getZkClient());
    }

    public IServiceConfig getServiceConfig() {
        return queryContext.getServiceConfig();
    }

    public GroupRouter getGroupRouter() {
        return queryContext.getGroupRouter();
    }

    public SolrCoreStatusHolder getHostStatusHolder() {
        return queryContext.getHostStatusHolder();
    }

    public String getServiceName() {
        return queryContext.getServiceName();
    }

    public TerminatorZkClient getZkClient() {
        return queryContext.getZkClient();
    }

    public void setFsName(String fsName) {
        this.fsName = fsName;
    }

    public TSearcherDumpContextImpl() {
        super();
    // this.logger = new WrapperLogger(
    // LogFactory.getLog(HdfsTerminatorBean.class), this);
    }

    // protected static FileSystem fileSystem;
    public static FileSystem getHdfsFileSystem() {
        // return fileSystem;
        return TISHdfsUtils.getFileSystem();
    }

    @Override
    public FileSystem getDistributeFileSystem() {
        return getHdfsFileSystem();
    // if (fileSystem == null) {
    // synchronized (TSearcherDumpContextImpl.class) {
    // if (fileSystem == null) {
    // 
    // Configuration configuration = new Configuration();
    // FileSystem fileSys = null;
    // if (StringUtils.isEmpty(this.fsName)) {
    // throw new IllegalStateException(
    // "hdfsHost can not be null");
    // }
    // logger.info("hdfsAddress:" + this.fsName);
    // try {
    // configuration.set("fs.default.name", this.fsName);
    // 
    // configuration.addResource("core-site.xml");
    // configuration.addResource("mapred-site.xml");
    // 
    // fileSys = FileSystem.get(configuration);
    // 
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // fileSystem = fileSys;
    // 
    // }
    // }
    // }
    // return fileSystem;
    }

    @Override
    public Set<String> getGroupNameSet() {
        return Collections.emptySet();
    }

    @Override
    public Boolean getIncrOrNot() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalTimeFilePath() {
        return this.localTimeFilePath;
    }

    private String localTimeFilePath;

    public void setLocalTimeFilePath(String localTimeFilePath) {
        this.localTimeFilePath = localTimeFilePath;
    }

    // @Override
    // public Integer getNumSplits() {
    // //return getConfiguration().getInt("import.split.size", 4);
    // return 4;
    // }
    private String currentUserName = null;

    protected FileTimeProvider fileTimeProvider;

    public FileTimeProvider getFileTimeProvider() {
        return fileTimeProvider;
    }

    public void setFileTimeProvider(FileTimeProvider fileTimeProvider) {
        this.fileTimeProvider = fileTimeProvider;
    }

    // private boolean shallInitializeHdfs = true;
    // 
    // public boolean isShallInitializeHdfs() {
    // return shallInitializeHdfs;
    // }
    // @Override
    public FileTimeProvider getTimeProvider() {
        if (fileTimeProvider == null) {
            try {
                if (localTimeFilePath != null)
                    // 
                    fileTimeProvider = new FileTimeProvider(localTimeFilePath);
                else
                    // 本地存储
                    // HDFS存储
                    fileTimeProvider = new FileTimeProvider(this, getCurrentUserName());
            } catch (TimeManageException e) {
                logger.error("[错误]生成时间记录生成器出现错误，将不能正常启动", e);
                throw new RuntimeException("[错误]生成时间记录生成器出现错误，将不能正常启动", e);
            }
        }
        return fileTimeProvider;
    }

    @Override
    public String getFSName() {
        return this.fsName;
    }

    // private void initHadoopConfiguration() throws TerminatorInitException {
    // 
    // // try {
    // //
    // // System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
    // // "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
    // // final Configuration configuration = new Configuration();
    // // // configuration.addResource("core-site.xml");
    // // // configuration.addResource("mapred-site.xml");
    // // configuration.set(FS_DEFAULT_NAME, fsName);
    // // configuration.set("hadoop.job.ugi", "hongzhen.lm,admin");
    // // configuration.set("dfs.web.ugi", "hongzhen.lm,admin");
    // //
    // // UserGroupInformation ugi = UserGroupInformation.getCurrentUser();
    // // currentUserName = ugi.getUserName();
    // // logger.warn("[" + currentUserName + "]");
    // // fileSystem = FileSystem.newInstance(configuration);
    // // } catch (Exception e) {
    // // logger.warn(
    // // "[error]illeagl fsName（Hadoop cluster NameNode address）fsName:"
    // // + fsName + ",currentUserName:" + currentUserName, e);
    // // throw new TerminatorInitException(
    // // "[error]illeagl fsName（Hadoop cluster NameNode address）fsName:"
    // // + fsName + ",currentUserName:" + currentUserName, e);
    // // }
    // 
    // this.fileSystem =
    // }
    // // 百岁添加 用户可以自定义hdfsProviderClass
    // private String hdfsProviderBeanName;
    // 
    // // 百岁添加 start
    // public String getHdfsProviderBeanName() {
    // return this.hdfsProviderBeanName;
    // }
    // 
    // public void setHdfsProviderBeanName(String hdfsProviderClass) {
    // this.hdfsProviderBeanName = hdfsProviderClass;
    // }
    // private ApplicationContext applicationContext;
    // 
    // @Override
    // public void setApplicationContext(ApplicationContext applicationContext)
    // throws BeansException {
    // this.applicationContext = applicationContext;
    // }
    // public void initHDFSDataProvider() {
    // 
    // if (StringUtils.isNotBlank(this.getHdfsProviderBeanName())) {
    // 
    // setFullHdfsProvider((HDFSProvider) this.applicationContext.getBean(
    // this.getHdfsProviderBeanName(), HDFSProvider.class));
    // 
    // } else if (this.getFullDataProvider() != null) {
    // 
    // this.setFullHdfsProvider(ReflectionUtils.newInstance(this
    // .getHdfsDataProvider(), new ImportContext(this,
    // new Boolean(false))));
    // }
    // 
    // // if (StringUtils.isNotBlank(this.getHdfsProviderBeanName())) {
    // //
    // // setIncrHdfsProvider((HDFSProvider) this.applicationContext.getBean(
    // // this.getHdfsProviderBeanName(), HDFSProvider.class));
    // //
    // // } else
    // 
    // if (this.getIncrDataProvider() != null) {
    // 
    // this.setIncrHdfsProvider(ReflectionUtils.newInstance(this
    // .getHdfsDataProvider(), new ImportContext(this,
    // new Boolean(true))));
    // }
    // 
    // }
    @Override
    public int getWirteHdfsThreadCount() {
        return this.writeHdfsThreadCount;
    }

    // @SuppressWarnings("all")
    // public void setHdfsProvider(HDFSProvider hdfsProvider) {
    // this.hdfsProvider = hdfsProvider;
    // }
    public String getCurrentUserName() {
        return StringUtils.defaultIfEmpty(currentUserName, System.getProperty("user.name"));
    }

    // public void setCurrentUserName(String currentUserName) {
    // this.currentUserName = currentUserName;
    // }
    private int writeHdfsThreadCount = 10;

    public int getWriteHdfsThreadCount() {
        return writeHdfsThreadCount;
    }

    public void setWriteHdfsThreadCount(int writeHdfsThreadCount) {
        this.writeHdfsThreadCount = writeHdfsThreadCount;
    }

    public void setQueryContext(TSearcherQueryContext queryContext) {
        this.queryContext = queryContext;
    }

    @SuppressWarnings("all")
    private BatchDataProcessor dataprocessor;

    @SuppressWarnings("all")
    @Override
    public BatchDataProcessor getDataProcessor() {
        return this.dataprocessor;
    }

    @SuppressWarnings("all")
    public void setDataprocessor(BatchDataProcessor dataprocessor) {
        this.dataprocessor = dataprocessor;
    }

    public boolean isTriggerLock() {
        return triggerLock;
    }

    public void setTriggerLock(boolean triggerLock) {
        this.triggerLock = triggerLock;
    }
}
