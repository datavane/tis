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
import com.qlangtech.tis.indexbuilder.IndexBuilderTask;
import com.qlangtech.tis.indexbuilder.map.IndexConf;
import com.qlangtech.tis.indexbuilder.map.IndexGetConfig;
import com.qlangtech.tis.manage.common.*;
import com.qlangtech.tis.manage.common.ConfigFileContext.StreamProcess;
import com.qlangtech.tis.offline.IndexBuilderTriggerFactory;
import com.qlangtech.tis.offline.TableDumpFactory;
import com.qlangtech.tis.order.dump.task.ITableDumpConstant;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.ds.DataSourceFactoryPluginStore;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.solrextend.cloud.TISPluginClassLoader;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.apache.commons.cli.Option;
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


    // protected static TisZkClient zkClient;

    public NodeMaster(IServerTask propsGetter) {
        this.serverTask = propsGetter;
    }

    public static void main(String[] args) throws Exception {
        TisZkClient zkClient = new TisZkClient(Config.getZKHost(), 60000);
        RpcServiceReference statusRpc = StatusRpcClient.getService(zkClient);
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

                IPluginStore<TableDumpFactory> tableDumpFactoryStore = TIS.getPluginStore(TableDumpFactory.class);
                TableDumpFactory factory = tableDumpFactoryStore.find(tabDumpFactory);
                TableDumpNodeMaster master = new TableDumpNodeMaster(factory, dbPlugin.getPlugin());
                master.run(commandLine, zkClient, statusRpc);
            } else if (IndexBuildParam.JOB_TYPE_INDEX_BUILD.equals(jobType)) {
                final String builderTriggerFactory = commandLine.getOptionValue(IndexBuildParam.INDEXING_BUILDER_TRIGGER_FACTORY);
                IPluginStore<IndexBuilderTriggerFactory> builderTriggerFactoryStore = TIS.getPluginStore(IndexBuilderTriggerFactory.class);
                IndexBuilderTriggerFactory factory = builderTriggerFactoryStore.find(builderTriggerFactory);
                IndexBuilderTask.setMdcAppName(commandLine.getOptionValue(IndexBuildParam.INDEXING_SERVICE_NAME));
                NodeMaster master = new IndexBuildNodeMaster(factory);
                master.run(commandLine, zkClient, statusRpc);
            } else {
                // invalidJobType = true;
                throw new IllegalStateException("jobType:" + jobType + " is illegal");
                // logger.error("jobType:{} is illegal", jobType);
            }
            // 最终要向中心节点报告状态
            finalReportTaskStatus(commandLine, statusRpc, false);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            finalReportTaskStatus(commandLine, statusRpc, true);
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

    private static void finalReportTaskStatus(CommandLine commandLine, RpcServiceReference statusRpc, boolean faild) {
        try {
            String jobType = commandLine.getOptionValue(IndexBuildParam.JOB_TYPE);
            TaskContext tskCtx = getTaskContext(commandLine);
            StatusRpcClient.AssembleSvcCompsite rpc = statusRpc.get();
            if (IndexBuildParam.JOB_TYPE_DUMP.equals(jobType)) {
                DumpPhaseStatus.TableDumpStatus dumpStatus = new DumpPhaseStatus.TableDumpStatus(String.valueOf(tskCtx.parseDumpTable()), tskCtx.getTaskId());
                dumpStatus.setFaild(faild);
                dumpStatus.setComplete(true);
                rpc.reportDumpTableStatus(dumpStatus);
            } else if (IndexBuildParam.JOB_TYPE_INDEX_BUILD.equals(jobType)) {
                BuildSharedPhaseStatus buildStatus = new BuildSharedPhaseStatus();
                buildStatus.setTaskid(tskCtx.getTaskId());
                buildStatus.setFaild(faild);
                buildStatus.setComplete(true);
                IndexConf indexConf = IndexGetConfig.getIndexConf(tskCtx);
                buildStatus.setSharedName(indexConf.getCoreName());
                rpc.reportBuildIndexStatus(buildStatus);
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

    private static Options getOptions(org.apache.commons.cli.Option[] clientOptions) {
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message");
        options.addOption("v", "verbose", false, "Generate verbose log messages");
        // ;
        org.apache.commons.cli.Option[] opts = clientOptions;
        for (int i = 0; i < opts.length; i++) {
            options.addOption(opts[i]);
        }
        return options;
    }


    @SuppressWarnings("all")
    private static Option[] getClientOptions() {
        List<String> fields = IndexBuildParam.getAllFieldName();
        List<Option> opts = new ArrayList<>();
        for (String f : fields) {
            opts.add(OptionBuilder.withArgName(f).hasArg().isRequired(false).withDescription(f).create(f));
        }
        return opts.toArray(new Option[fields.size()]);
    }

    public final void run(CommandLine commandLine, TisZkClient zkClient, final RpcServiceReference statusRpc) throws Exception {

        try {
            TaskContext taskContext = getTaskContext(commandLine);
            taskContext.setCoordinator(zkClient);

            serverTask.startTask((context) -> {
                this.startExecute(context, statusRpc);
            }, taskContext);
        } finally {
            AppnameAwareFlumeLogstashV1Appender.closeAllFlume();
        }
    }

    protected static TaskContext getTaskContext(CommandLine commandLine) {
        return TaskContext.create((key) -> commandLine.getOptionValue(key));
    }

    protected abstract void startExecute(TaskContext context, RpcServiceReference statusRpc);
}
