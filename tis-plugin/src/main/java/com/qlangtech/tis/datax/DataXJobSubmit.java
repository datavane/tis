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

package com.qlangtech.tis.datax;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.datax.common.element.QueryCriteria;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.annotation.Public;
import com.qlangtech.tis.build.task.IBuildHistory;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.coredefine.module.action.TriggerBuildResult;
import com.qlangtech.tis.datax.job.DataXJobWorker;
import com.qlangtech.tis.datax.preview.IPreviewRowsDataService;
import com.qlangtech.tis.datax.preview.PreviewRowsData;
import com.qlangtech.tis.exec.IExecChainContext;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.extension.TISExtensible;
import com.qlangtech.tis.fullbuild.IFullBuildContext;
import com.qlangtech.tis.fullbuild.indexbuild.IRemoteTaskTrigger;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.order.center.IJoinTaskContext;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DBIdentity;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import com.qlangtech.tis.plugin.ds.IReaderSource;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.TableInDB;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.util.RobustReflectionConverter2;
import com.qlangtech.tis.web.start.TisAppLaunch;
import com.qlangtech.tis.workflow.pojo.IWorkflow;
import com.qlangtech.tis.workflow.pojo.WorkFlowBuildHistory;
import com.tis.hadoop.rpc.RpcServiceReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-04-27 17:03
 **/
@TISExtensible
@Public
public abstract class DataXJobSubmit implements IPreviewRowsDataService {
    private static final Logger logger = LoggerFactory.getLogger(DataXJobSubmit.class);
    public static final String KEY_DATAX_READERS = "dataX_readers";
    public static final int MAX_TABS_NUM_IN_PER_JOB = 40;
    public static final int DEFAULT_PARALLELISM_IN_VM = 1;// parallelism

    public static Callable<DataXJobSubmit> mockGetter;

    public static void main(String[] args) throws Exception {
        Enumeration<URL> resources =
                Thread.currentThread().getContextClassLoader().getResources("com/google/common" + "/base" +
                        "/Preconditions.class");
        while (resources.hasMoreElements()) {
            System.out.println(resources.nextElement());
        }
        // System.out.println(  DataXJobSubmit.class("com/google/common/base/Preconditions.class"));
    }

//    @FormField(ordinal = 0, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
//    public Integer parallelism;

    public static DataXJobSubmit.InstanceType getDataXTriggerType() {

        DataXJobWorker jobWorker = DataXJobWorker.getJobWorker(TargetResName.K8S_DATAX_INSTANCE_NAME
                , Optional.of(DataXJobWorker.K8SWorkerCptType.Server));
        boolean dataXWorkerServiceOnDuty = jobWorker != null && jobWorker.inService();
        DataXJobSubmit.InstanceType execType
                = dataXWorkerServiceOnDuty ? DataXJobSubmit.InstanceType.DISTRIBUTE : DataXJobSubmit.InstanceType.LOCAL;

        if (execType == DataXJobSubmit.InstanceType.LOCAL && TisAppLaunch.isTestMock()) {
            return InstanceType.EMBEDDED;
        }
        return execType;
    }

    public static DataXJobSubmit getDataXJobSubmit() {
        Optional<DataXJobSubmit> dataXJobSubmit = DataXJobSubmit.getDataXJobSubmit(false, DataXJobSubmit.getDataXTriggerType());
        DataXJobSubmit jobSubmit = dataXJobSubmit.orElseThrow(() -> new IllegalStateException("dataXJobSubmit must be present"));
        return jobSubmit;
    }

    public static Optional<IDataXPowerJobSubmit> getPowerJobSubmit() {
        DataXJobSubmit dataXJobSubmit = getDataXJobSubmit();
        logger.info("get dataXJobSubmit instanceof :" + dataXJobSubmit.getClass().getName()
                + ",triggerType:" + DataXJobSubmit.getDataXTriggerType());

        if (dataXJobSubmit instanceof IDataXPowerJobSubmit) {
            return Optional.of((IDataXPowerJobSubmit) dataXJobSubmit);
        }
        return Optional.empty();
    }


    public static Optional<DataXJobSubmit> getDataXJobSubmit(boolean dryRun,
                                                             DataXJobSubmit.InstanceType expectDataXJobSumit) {
        try {
            if (mockGetter != null) {
                return Optional.ofNullable(mockGetter.call());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 如果是DryRun则只需要在内部执行
        final DataXJobSubmit.InstanceType targetType = dryRun ? InstanceType.EMBEDDED : expectDataXJobSumit;
        //        if (joinTaskContext.isDryRun()) {
        //            expectDataXJobSumit = InstanceType.EMBEDDED;
        //        }

        ExtensionList<DataXJobSubmit> jobSumits = Objects.requireNonNull(TIS.get(), "tis instance can not be null")
                .getExtensionList(DataXJobSubmit.class);
        Optional<DataXJobSubmit> jobSubmit =
                jobSumits.stream().filter((jsubmit) -> (targetType) == jsubmit.getType()).findFirst();
        return jobSubmit;
    }

    public static Optional<DataXJobSubmit> getDataXJobSubmit(IJoinTaskContext joinTaskContext,
                                                             DataXJobSubmit.InstanceType expectDataXJobSumit) {
        return getDataXJobSubmit(joinTaskContext.isDryRun(), expectDataXJobSumit);
    }


    public enum InstanceType {
        DS("dolphinscheduler") {
            /**
             * 支持将TIS中的数据同步通道任务，或者ETL任务同步到dolphinscheduler系统中，后由DS端发起任务触发任务
             */
            @Override
            public boolean validate(IControlMsgHandler controlMsgHandler, Context context,
                                    List<DataXCfgFile> cfgFileNames) {
                return true;
            }
        },
        DISTRIBUTE("distribute") {
            @Override
            public boolean validate(IControlMsgHandler controlMsgHandler, Context context,
                                    List<DataXCfgFile> cfgFileNames) {
                return true;
            }
        }, EMBEDDED("embedded") {
            @Override
            public boolean validate(IControlMsgHandler controlMsgHandler, Context context,
                                    List<DataXCfgFile> cfgFileNames) {
                return true;
            }
        }
        //
        , LOCAL("local") {
            @Override
            public boolean validate(IControlMsgHandler controlMsgHandler, Context context,
                                    List<DataXCfgFile> cfgFileNames) {
                DataXJobSubmitParams submitParams = DataXJobSubmitParams.getDftIfEmpty();
                if (cfgFileNames.size() > submitParams.maxJobs) {
                    controlMsgHandler.addErrorMessage(context, "单机版，单次表导入不能超过" + submitParams.maxJobs +
                            "张，如需要导入更多表，请使用分布式K8S DataX执行期");
                    return false;
                }
                return true;
            }
        };
        public final String literia;
        public static final String KEY_TYPE = IFullBuildContext.KEY_TRIGGER_TYPE;

        public static InstanceType parse(String val) {
            for (InstanceType t : InstanceType.values()) {
                if (t.literia.equals(val)) {
                    return t;
                }
            }
            throw new IllegalArgumentException("value:" + val + " is not illegal");
        }

        private InstanceType(String val) {
            this.literia = val;
        }

        public abstract boolean validate(IControlMsgHandler controlMsgHandler, Context context,
                                         List<DataXCfgFile> cfgFileNames);
    }

    /**
     * 预览数据，每次查看只能向前翻页或者向后翻页，不能随机翻页
     *
     * @param dataXName
     * @param tableName
     * @param queryCriteria
     * @return
     */
    @Override
    public PreviewRowsData previewRowsData(DataXName dataXName, String tableName, QueryCriteria queryCriteria) {
        throw new UnsupportedOperationException();
    }

    public abstract InstanceType getType();


    public CuratorDataXTaskMessage getDataXJobDTO(IDataXJobContext jobContext, DataXJobInfo dataXJobInfo,
                                                  IDataxProcessor processor) {

        IJoinTaskContext taskContext = jobContext.getTaskContext();
        if (processor.getResType() == null) {
            throw new NullPointerException("dataXJobDTO.getResType() can not be null");
        }
        CuratorDataXTaskMessage msg = new CuratorDataXTaskMessage();
        if (taskContext.hasIndexName()) {
            msg.setDataXName(taskContext.getIndexName());
        } else {
            msg.setDataXName(processor.identityValue());
        }
        msg.setTaskSerializeNum(jobContext.getTaskSerializeNum());
        msg.setJobId(taskContext.getTaskId());
        msg.setJobName(dataXJobInfo.serialize());
        msg.setExecTimeStamp(taskContext.getPartitionTimestampWithMillis());
        msg.setResType(processor.getResType());

        if (jobContext.getSpecifiedLocalLoggerPath() != null) {
            msg.setLocalLoggerPath(jobContext.getSpecifiedLocalLoggerPath().getAbsolutePath());
        }
        msg.setDisableGrpcRemoteServerConnect(jobContext.isDisableGrpcRemoteServerConnect());

        PhaseStatusCollection preTaskStatus = taskContext.loadPhaseStatusFromLatest();
        logger.info("preTaskStatus is{} null", preTaskStatus != null ? " not" : StringUtils.EMPTY);
        DumpPhaseStatus.TableDumpStatus dataXJob = null;
        if (preTaskStatus != null
                && (dataXJob = preTaskStatus.getDumpPhase().getTable(dataXJobInfo.jobFileName)) != null
                && dataXJob.getAllRows() > 0) {
            msg.setAllRowsApproximately(dataXJob.getReadRows());
        } else {
            msg.setAllRowsApproximately(-1);
        }
        logger.info("job:{} relevant DataXJob:{}", dataXJobInfo.jobFileName, dataXJob != null ? "{" + dataXJob.getReadRows() + "}" : " is null");
        return msg;
    }

    /**
     * 从TIS Console组件中触发构建全量任务
     *
     * @param module
     * @param context
     * @param appName
     * @param powerJobWorkflowInstanceIdOpt 如果是手动触发则为空,如果是定时触发的，例如在powerjob系统中已经生成了powerjob 的workflowInstanceId
     * @param latestWorkflowHistory         最近一次成功执行的历史记录
     * @return
     */
    public abstract TriggerBuildResult triggerJob(
            IControlMsgHandler module, final Context context, DataXName appName
            , Optional<Long> powerJobWorkflowInstanceIdOpt, Optional<WorkFlowBuildHistory> latestWorkflowHistory);

    /**
     * 触发workflow执行
     *
     * @param module
     * @param context
     * @param workflow
     * @param dryRun
     * @param powerJobWorkflowInstanceIdOpt powerJobWorkflowInstanceIdOpt 如果是手动触发则为空,如果是定时触发的，例如在powerjob系统中已经生成了powerjob 的workflowInstanceId
     * @param latestSuccessWorkflowHistory  最近一次成功执行的workflow history记录
     * @return
     */
    public abstract TriggerBuildResult triggerWorkflowJob(
            IControlMsgHandler module, final Context context, IWorkflow workflow, Boolean dryRun
            , Optional<Long> powerJobWorkflowInstanceIdOpt, Optional<WorkFlowBuildHistory> latestSuccessWorkflowHistory);


    /**
     * 终止正在执行的任务
     *
     * @param buildHistory
     */
    public abstract boolean cancelTask(IControlMsgHandler module, final Context context, IBuildHistory buildHistory);

//    /**
//     * 创建数据同步 pre，post hook
//     *
//     * @param taskContext
//     * @param statusRpc
//     * @param processor
//     * @param lifeCycleHookInfo
//     * @return
//     */
//    public abstract IRemoteTaskTrigger createDataXJob(
//            IDataXJobContext taskContext, RpcServiceReference statusRpc,
//            IDataxProcessor processor, Pair<String, IDataXBatchPost.LifeCycleHook> lifeCycleHookInfo, String tabName
//    );

    /**
     * 创建dataX任务
     *
     * @param taskContext
     * @param tabDataXEntity
     * @return
     */
    public final IRemoteTaskTrigger createDataXJob(IDataXJobContext taskContext, RpcServiceReference statusRpc,
                                                   IDataxProcessor processor, TableDataXEntity tabDataXEntity //,
                                                   // List<String> dependencyTasks
    ) {
        final DataXJobInfo jobName = getDataXJobInfo(tabDataXEntity, taskContext, processor);
        if (this.getType() == InstanceType.DISTRIBUTE) {
            //TODO: 获取DataXProcess 相关元数据 用于远程分布式执行任务
            RobustReflectionConverter2.PluginMetas pluginMetas =
                    RobustReflectionConverter2.PluginMetas.collectMetas((metas) -> {

                    });
        }

        CuratorDataXTaskMessage dataXJobDTO = getDataXJobDTO(taskContext, jobName, processor);

        return createDataXJob(taskContext, statusRpc, jobName, processor, dataXJobDTO);
    }

    public abstract IRemoteTaskTrigger createDataXJob(IDataXJobContext taskContext, RpcServiceReference statusRpc,
                                                      DataXJobInfo jobName, IDataxProcessor dataxProcessor,
                                                      CuratorDataXTaskMessage dataXJobDTO);


    private DataXJobInfo getDataXJobInfo(final TableDataXEntity tabDataXEntity, IDataXJobContext taskContext,
                                         IDataxProcessor dataxProcessor) {

        List<IDataxReader> readers = taskContext.getTaskContext().getAttribute(KEY_DATAX_READERS,
                () -> dataxProcessor.getReaders(null));

        if (CollectionUtils.isEmpty(readers)) {
            throw new IllegalStateException("readers can not be empty");
        }

        return getDataXJobInfo(tabDataXEntity, (p) -> {
            TableInDB tabsInDB = p.getLeft();
            DataXJobInfo jobName = tabsInDB.createDataXJobInfo(tabDataXEntity, false);
            return jobName;
        }, readers);
    }

    public static <T> T getDataXJobInfo(DBIdentity targetDBId, Function<Pair<TableInDB, IDataxReader>, T> convert,
                                        List<IDataxReader> readers) {

        for (IDataxReader reader : readers) {
            TableInDB tabsInDB = reader.getTablesInDB();
            if (tabsInDB.isMatch(targetDBId)) {
                return convert.apply(Pair.of(tabsInDB, reader));
            }
        }

        throw new IllegalStateException(targetDBId.toString());
    }


    public static class TableDataXEntity implements DBIdentity {
        public static final String TEST_JDBC_URL = "jdbc_url_test";
        public final DBDataXChildTask fileName;
        private final ISelectedTab selectedTab;
        //  public final boolean prefixWildcardStyle;

        @Override
        public String identityValue() {
            return fileName.getDbFactoryId();
        }

        public static DataXJobSubmit.TableDataXEntity createTableEntity4Test(String dataXCfgFileName, String tabName) {
            return createTableEntity(dataXCfgFileName, TEST_JDBC_URL, tabName);
        }


        /**
         * @param dataXCfgFileName
         * @param dbIdenetity
         * @param tabName
         * @return
         */
        public static DataXJobSubmit.TableDataXEntity createTableEntity(
                String dataXCfgFileName, String dbIdenetity,
                String tabName) {

            ISelectedTab selTab = new ISelectedTab() {
                @Override
                public String getName() {
                    return tabName;
                }

                @Override
                public List<IColMetaGetter> overwriteCols(IMessageHandler pluginCtx, boolean includeContextParams) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public List<CMeta> getCols() {
                    throw new UnsupportedOperationException();
                }
            };
            return new DataXJobSubmit.TableDataXEntity(new DBDataXChildTask(dbIdenetity, null,
                    dataXCfgFileName), selTab);
        }

        public TableDataXEntity(DBDataXChildTask fileName, ISelectedTab selectedTab) {
            this.fileName = fileName;
            this.selectedTab = selectedTab;
        }

        public String getFileName() {
            return this.fileName.getDataXCfgFileNameWithSuffix();
        }

        public String getDbIdenetity() {
            return this.fileName.getDbIdenetity();
        }

        public ISelectedTab getSelectedTab() {
            return this.selectedTab;
        }

        public String getSourceTableName() {
            return this.selectedTab.getName();
        }

        @Override
        public String toString() {
            return "{" + fileName + ", selectedTab=" + selectedTab.getName() + '}';
        }
    }

    public abstract IDataXJobContext createJobContext(IExecChainContext parentContext);


    public interface IDataXJobContext extends IDataXTaskRelevant {
        // public <T> T getContextInstance();

        public static IDataXJobContext create(IExecChainContext parentContext) {
            if (parentContext == null) {
                throw new IllegalArgumentException("param parentContext can not be null");
            }
            return new DataXJobSubmit.IDataXJobContext() {
                @Override
                public IJoinTaskContext getTaskContext() {
                    return parentContext;
                }

                @Override
                public boolean isDisableGrpcRemoteServerConnect() {
                    return parentContext.isDisableGrpcRemoteServerConnect();
                }

                @Override
                public File getSpecifiedLocalLoggerPath() {
                    return parentContext.getSpecifiedLocalLoggerPath();
                }

                @Override
                public <T> void setAttr(Class<T> key, Object val) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public <T> T getAttr(Class<T> key) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Integer getTaskId() {
                    return parentContext.getTaskId();
                }

                @Override
                public String getJobName() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public String getDataXName() {
                    return parentContext.getIndexName();
                }

                @Override
                public long getExecEpochMilli() {
                    return parentContext.getPartitionTimestampWithMillis();
                }

                @Override
                public void destroy() {
                }
            };
        }

        IJoinTaskContext getTaskContext();

        AtomicInteger order = new AtomicInteger();

        /**
         * 保证一个批次执行的DataX任务的每个子任务都有一个唯一的序列号，例如在ODPS数据导入的场景中
         * ，MySQL中有多个分库的表需要导入到ODPS中采用pt+pmod（该值通过唯一序列号）的分区组合来避免不同分库数据导入相同分区的冲突
         *
         * @return
         */
        public default int getTaskSerializeNum() {
            return order.getAndIncrement();
        }

        public default String getFormatTime(TimeFormat format) {
            return format.format(getTaskContext().getPartitionTimestampWithMillis());
        }

        /**
         * 任务执行完成之后回收
         */
        void destroy();
    }

}
