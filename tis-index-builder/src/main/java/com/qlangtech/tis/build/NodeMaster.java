/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.build;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.build.log.AppnameAwareFlumeLogstashV1Appender;
import com.qlangtech.tis.build.task.IServerTask;
import com.qlangtech.tis.build.yarn.IndexBuildNodeMaster;
import com.qlangtech.tis.build.yarn.TableDumpNodeMaster;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BuildSharedPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.indexbuilder.HdfsIndexBuilder;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.IndexGetConfig;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.order.dump.task.ITableDumpConstant;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.solrextend.cloud.TISPluginClassLoader;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * build索引的master节点类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月31日
 */
public abstract class NodeMaster {

    public static final String ENVIRONMENT_EXEC_INDEXS = "exec_indexs";

    private static final Logger logger = LoggerFactory.getLogger(NodeMaster.class);

    boolean isShutdown = false;

    private final IServerTask serverTask;

    protected static AtomicReference<StatusRpcClient.AssembleSvcCompsite> statusRpc;

    protected static TisZkClient zkClient;

    public NodeMaster(IServerTask propsGetter) {
        this.serverTask = propsGetter;
    }

    public static void main(String[] args) throws Exception {
        zkClient = new TisZkClient(Config.getZKHost(), 60000);
        statusRpc = StatusRpcClient.getService(zkClient);
        CommandLine commandLine = parseCommandLine(args);
        String val = null;
        logger.info("param:##############################");
        for (String key : IndexBuildParam.getAllFieldName()) {
            val = commandLine.getOptionValue(key);
            if (StringUtils.isEmpty(val)) {
                continue;
            }
            logger.info(key + ":" + val);
        }
        logger.info("param:##############################");
        String jobType = commandLine.getOptionValue(IndexBuildParam.JOB_TYPE);
        // boolean invalidJobType = false;
        boolean sameNodeWithConsole = isSameNodeWithTISConsole();
        if (sameNodeWithConsole) {
            CenterResource.setNotFetchFromCenterRepository();
            logger.info("task get local Repository content");
        } else {
            /**
             * ==================================================================
             * 下载插件代码和配置到本地
             * ==================================================================
             */
            ComponentMeta dumpAndIndexBuilderComponent = null;
            if (IndexBuildParam.JOB_TYPE_DUMP.equals(jobType)) {
                // dump import data
                dumpAndIndexBuilderComponent = TIS.getDumpAndIndexBuilderComponent(getDataSourceFactoryPluginStore(commandLine));
            } else {
                // index builder
                dumpAndIndexBuilderComponent = TIS.getDumpAndIndexBuilderComponent(
                        TISPluginClassLoader.getSchemaRelevantResource(commandLine.getOptionValue(IndexBuildParam.INDEXING_SERVICE_NAME)));
            }
            dumpAndIndexBuilderComponent.synchronizePluginsFromRemoteRepository();
            logger.info("synchronizePluginsFromRemoteRepository success");
        }
        try {
            // 
            if (IndexBuildParam.JOB_TYPE_DUMP.equals(jobType)) {
                final String tabDumpFactory = commandLine.getOptionValue(ITableDumpConstant.DUMP_TABLE_DUMP_FACTORY_NAME);
                DataSourceFactoryPluginStore dbPlugin = getDataSourceFactoryPluginStore(commandLine);

                PluginStore<TableDumpFactory> tableDumpFactoryStore = TIS.getPluginStore(TableDumpFactory.class);
                TableDumpFactory factory = tableDumpFactoryStore.find(tabDumpFactory);
                TableDumpNodeMaster master = new TableDumpNodeMaster(factory, dbPlugin.getPlugin());
                master.run(commandLine);
            } else if (IndexBuildParam.JOB_TYPE_INDEX_BUILD.equals(jobType)) {
                final String builderTriggerFactory = commandLine.getOptionValue(IndexBuildParam.INDEXING_BUILDER_TRIGGER_FACTORY);
                PluginStore<IndexBuilderTriggerFactory> builderTriggerFactoryStore = TIS.getPluginStore(IndexBuilderTriggerFactory.class);
                IndexBuilderTriggerFactory factory = builderTriggerFactoryStore.find(builderTriggerFactory);
                HdfsIndexBuilder.setMdcAppName(commandLine.getOptionValue(IndexBuildParam.INDEXING_SERVICE_NAME));
                NodeMaster master = new IndexBuildNodeMaster(factory);
                master.run(commandLine);
            } else {
                // invalidJobType = true;
                throw new IllegalStateException("jobType:" + jobType + " is illegal");
                // logger.error("jobType:{} is illegal", jobType);
            }
            // 最终要向中心节点报告状态
            finalReportTaskStatus(commandLine, false);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            finalReportTaskStatus(commandLine, true);
            throw new RuntimeException(e);
        } finally {
            try {
                zkClient.getZK().close();
            } catch (Throwable e) {
            }
            try {
                statusRpc.get().close();
            } catch (Throwable e) {
            }
        }
        System.exit(0);
    }

    private static DataSourceFactoryPluginStore getDataSourceFactoryPluginStore(CommandLine commandLine) {
        final String dbName = commandLine.getOptionValue(ITableDumpConstant.DUMP_DBNAME);
        if (StringUtils.isEmpty(dbName)) {
            throw new IllegalStateException("param 'dbName' can not be null");
        }
        return TIS.getDataBasePluginStore(new PostedDSProp(dbName));
    }

    private static void finalReportTaskStatus(CommandLine commandLine, boolean faild) {
        try {
            String jobType = commandLine.getOptionValue(IndexBuildParam.JOB_TYPE);
            TaskContext tskCtx = getTaskContext(commandLine);
            if (IndexBuildParam.JOB_TYPE_DUMP.equals(jobType)) {
                DumpPhaseStatus.TableDumpStatus dumpStatus = new DumpPhaseStatus.TableDumpStatus(String.valueOf(tskCtx.parseDumpTable()), tskCtx.getTaskId());
                dumpStatus.setFaild(faild);
                dumpStatus.setComplete(true);
                statusRpc.get().reportDumpTableStatus(dumpStatus);
            } else if (IndexBuildParam.JOB_TYPE_INDEX_BUILD.equals(jobType)) {
                BuildSharedPhaseStatus buildStatus = new BuildSharedPhaseStatus();
                buildStatus.setTaskid(tskCtx.getTaskId());
                buildStatus.setFaild(faild);
                buildStatus.setComplete(true);
                IndexConf indexConf = IndexGetConfig.getIndexConf(tskCtx);
                buildStatus.setSharedName(indexConf.getCoreName());
                statusRpc.get().reportBuildIndexStatus(buildStatus);
            }
        } catch (Throwable ee) {
            logger.error(ee.getMessage(), ee);
        }
    }

    /**
     * 确认执行构建的和console在相同节点
     *
     * @return
     * @throws MalformedURLException
     */
    private static boolean isSameNodeWithTISConsole() throws MalformedURLException {
        URL url = new URL("http://127.0.0.1:8080" + Config.CONTEXT_TIS + "/check_health");
        try {
            return HttpUtils.get(url, new StreamProcess<Boolean>() {

                @Override
                public Boolean p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                    try {
                        return "ok".equals(IOUtils.toString(stream, TisUTF8.get()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            logger.warn("please check wether ok", e);
            return false;
        }
    }

    public static CommandLine parseCommandLine(String[] args) {
        return processCommandLineArgs("index-build", getClientOptions(), args);
    }

    public static CommandLine processCommandLineArgs(String app, Option[] clientOptions, String[] args) {
        Options options = getOptions(clientOptions);
        CommandLine cli = null;
        try {
            cli = (new GnuParser()).parse(options, args);
        } catch (ParseException exp) {
            boolean hasHelpArg = false;
            if (args != null && args.length > 0) {
                for (int z = 0; z < args.length; z++) {
                    if ("-h".equals(args[z]) || "-help".equals(args[z])) {
                        hasHelpArg = true;
                        break;
                    }
                }
            }
            if (!hasHelpArg) {
                System.err.println("Failed to parse command-line arguments due to: " + exp.getMessage());
            }
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(app, options);
            System.exit(1);
        }
        if (cli.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(app, options);
            System.exit(0);
        }
        return cli;
    }

    private static Options getOptions(Option[] clientOptions) {
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message");
        options.addOption("v", "verbose", false, "Generate verbose log messages");
        // ;
        Option[] opts = clientOptions;
        for (int i = 0; i < opts.length; i++) {
            options.addOption(opts[i]);
        }
        return options;
    }

    // private HdfsIndexBuilder indexBuilder = null;
    // protected TaskContext taskContext = null;
    // /**
    // * 取得任务执行百分比，只能近似正確
    // */
    // @Override
    // public float getProgress() {
    // if (indexBuilder == null || taskContext == null) {
    // return 0;
    // }
    // final long allRowCount = taskContext.getAllRowCount();
    // long indexMakeCounter = taskContext.getIndexMakerComplete();
    // // logger.info("complete:" + indexMakeCounter + ",all:" + allRowCount);
    // float mainProgress = (float) (((double) indexMakeCounter) / allRowCount);
    // return (float) (((mainProgress > 1.0) ? 1.0 : mainProgress));
    // // + ((indexBuilder.getMergeOver() ? 0.02f : 0.0f));
    // }
    @SuppressWarnings("all")
    private static Option[] getClientOptions() {
        List<String> fields = IndexBuildParam.getAllFieldName();
        List<Option> opts = new ArrayList<>();
        for (String f : fields) {
            opts.add(OptionBuilder.withArgName(f).hasArg().isRequired(false).withDescription(f).create(f));
        }
        return opts.toArray(new Option[fields.size()]);
    }

    // private YarnConfiguration conf;
    // private Configuration getConfiguration() {
    // return this.conf;
    // }
    public final void run(CommandLine commandLine) throws Exception {
        try {
            TaskContext taskContext = getTaskContext(commandLine);
            serverTask.startTask((context) -> {
                this.startExecute(context);
            }, taskContext);
        } finally {
            AppnameAwareFlumeLogstashV1Appender.closeAllFlume();
        }
    }

    protected static TaskContext getTaskContext(CommandLine commandLine) {
        return TaskContext.create((key) -> commandLine.getOptionValue(key));
    }

    protected abstract void startExecute(TaskContext context);
}
