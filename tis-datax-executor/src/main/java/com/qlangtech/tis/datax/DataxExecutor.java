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
package com.qlangtech.tis.datax;


import com.alibaba.datax.common.exception.DataXException;
import com.alibaba.datax.common.statistics.PerfTrace;
import com.alibaba.datax.common.statistics.VMInfo;
import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.Engine;
import com.alibaba.datax.core.util.ConfigParser;
import com.alibaba.datax.core.util.ConfigurationValidate;
import com.alibaba.datax.core.util.FrameworkErrorCode;
import com.alibaba.datax.core.util.container.JarLoader;
import com.alibaba.datax.core.util.container.LoadUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.impl.DataXCfgGenerator;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.impl.IOUtils;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.plugin.ComponentMeta;
import com.qlangtech.tis.plugin.IRepositoryResource;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.tis.hadoop.rpc.RpcServiceReference;
import com.tis.hadoop.rpc.StatusRpcClient;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 执行DataX任务入口
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-20 12:38
 */
public final class DataxExecutor {
    public static final Field jarLoaderCenterField;

    public static void synchronizeDataXPluginsFromRemoteRepository(String dataxName, String jobName) {
        TIS.permitInitialize = false;
        try {
            if (StringUtils.isBlank(dataxName)) {
                throw new IllegalArgumentException("param dataXName can not be null");
            }
            if (StringUtils.isBlank(jobName)) {
                throw new IllegalArgumentException("param jobName can not be null");
            }
            KeyedPluginStore<DataxProcessor> processStore = IAppSource.getPluginStore(null, dataxName);
            List<IRepositoryResource> keyedPluginStores = Lists.newArrayList();// Lists.newArrayList(DataxReader.getPluginStore(dataxName), DataxWriter.getPluginStore(dataxName));
            keyedPluginStores.add(processStore);
            keyedPluginStores.add(DataxReader.getPluginStore(null, dataxName));
            keyedPluginStores.add(DataxWriter.getPluginStore(null, dataxName));
            ComponentMeta dataxComponentMeta = new ComponentMeta(keyedPluginStores);
            dataxComponentMeta.synchronizePluginsFromRemoteRepository();

            CenterResource.copyFromRemote2Local(
                    TIS.KEY_TIS_PLUGIN_CONFIG + "/" + processStore.key.getSubDirPath()
                            + "/" + DataxProcessor.DATAX_CFG_DIR_NAME + "/" + jobName, true);
        } finally {
            TIS.permitInitialize = true;
        }
    }

    static {
        try {
            jarLoaderCenterField = LoadUtil.class.getDeclaredField("jarLoaderCenter");
            jarLoaderCenterField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("can not get field 'jarLoaderCenter' of LoadUtil", e);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(DataxExecutor.class);
    private static final MessageFormat FormatKeyPluginReader = new MessageFormat("plugin.reader.{0}");
    private static final MessageFormat FormatKeyPluginWriter = new MessageFormat("plugin.writer.{0}");

    private IDataXPluginMeta.DataXMeta readerMeta;
    private IDataXPluginMeta.DataXMeta writerMeta;


    private final RpcServiceReference statusRpc;
    private final JarLoader uberClassLoader;

    public DataxExecutor(RpcServiceReference statusRpc, JarLoader uberClassLoader) {
        this.statusRpc = statusRpc;
        this.uberClassLoader = uberClassLoader;
    }


    /**
     * 开始执行数据同步任务
     *
     * @param dataxName
     * @param jobPath
     * @throws IOException
     * @throws Exception
     */
    public void startWork(String dataxName, Integer jobId, String jobName, String jobPath) throws IOException, Exception {
        // TaskConfig config = TaskConfig.getInstance();
        String[] args = new String[]{"-mode", "standalone", "-jobid", String.valueOf(jobId), "-job", jobPath};

        KeyedPluginStore<DataxReader> readerStore = DataxReader.getPluginStore(null, dataxName);
        KeyedPluginStore<DataxWriter> writerStore = DataxWriter.getPluginStore(null, dataxName);
//        TIS.permitInitialize = false;
//        try {
//            List<IRepositoryResource> keyedPluginStores = Lists.newArrayList();// Lists.newArrayList(DataxReader.getPluginStore(dataxName), DataxWriter.getPluginStore(dataxName));
//            keyedPluginStores.add(readerStore = DataxReader.getPluginStore(dataxName));
//            keyedPluginStores.add(writerStore = DataxWriter.getPluginStore(dataxName));
//            ComponentMeta dataxComponentMeta = new ComponentMeta(keyedPluginStores);
//            dataxComponentMeta.synchronizePluginsFromRemoteRepository();
//        } finally {
//            TIS.permitInitialize = true;
//        }

        try {
            DataxReader reader = readerStore.getPlugin();
            Objects.requireNonNull(reader, "dataxName:" + dataxName + " relevant reader can not be null");
            DataxWriter writer = writerStore.getPlugin();
            Objects.requireNonNull(writer, "dataxName:" + dataxName + " relevant writer can not be null");
            this.readerMeta = reader.getDataxMeta();
            this.writerMeta = writer.getDataxMeta();
            Objects.requireNonNull(readerMeta, "readerMeta can not be null");
            Objects.requireNonNull(writerMeta, "writerMeta can not be null");

            initializeClassLoader(Sets.newHashSet(this.getPluginReaderKey(), this.getPluginWriterKey()), this.uberClassLoader);

            try {
                entry(args);
                this.reportDataXJobStatus(false, jobId, jobName);
            } catch (Throwable e) {
                this.reportDataXJobStatus(true, jobId, jobName);
                throw new Exception(e);
            } finally {
                cleanPerfTrace();
            }
        } finally {
            //  TIS.cleanTIS();
        }
    }

    public static void initializeClassLoader(Set<String> pluginKeys, JarLoader classLoader) throws IllegalAccessException {
        Map<String, JarLoader> jarLoaderCenter = (Map<String, JarLoader>) jarLoaderCenterField.get(null);
        jarLoaderCenter.clear();
//            final JarLoader uberClassLoader = new JarLoader(new String[]{"."}) {
//                @Override
//                protected Class<?> findClass(String name) throws ClassNotFoundException {
//                    return TIS.get().getPluginManager().uberClassLoader.findClass(name);
//                }
//            };
        for (String pluginKey : pluginKeys) {
            jarLoaderCenter.put(pluginKey, classLoader);
        }
//        jarLoaderCenter.put(this.getPluginReaderKey(), classLoader);
//        jarLoaderCenter.put(this.getPluginWriterKey(), classLoader);
        // jarLoaderCenter;
        Objects.requireNonNull(jarLoaderCenter, "jarLoaderCenter can not be null");
    }

    private void reportDataXJobStatus(boolean faild, Integer taskId, String jobName) {
        StatusRpcClient.AssembleSvcCompsite svc = statusRpc.get();
        DumpPhaseStatus.TableDumpStatus dumpStatus = new DumpPhaseStatus.TableDumpStatus(jobName, taskId);
        dumpStatus.setFaild(faild);
        dumpStatus.setComplete(true);
        svc.reportDumpTableStatus(dumpStatus);
    }


    public void entry(String[] args) throws Throwable {
        Options options = new Options();
        options.addOption("job", true, "Job config.");
        options.addOption("jobid", true, "Job unique id.");
        options.addOption("mode", true, "Job runtime mode.");
        BasicParser parser = new BasicParser();
        CommandLine cl = parser.parse(options, args);
        String jobPath = cl.getOptionValue("job");
        String jobIdString = cl.getOptionValue("jobid");
        String RUNTIME_MODE = cl.getOptionValue("mode");
        Configuration configuration = parse(jobPath);
        Objects.requireNonNull(configuration, "configuration can not be null");
        long jobId = 0;
        if (!"-1".equalsIgnoreCase(jobIdString)) {
            jobId = Long.parseLong(jobIdString);
        }

        boolean isStandAloneMode = "standalone".equalsIgnoreCase(RUNTIME_MODE);
        if (!isStandAloneMode && jobId == -1L) {
            throw DataXException.asDataXException(FrameworkErrorCode.CONFIG_ERROR, "非 standalone 模式必须在 URL 中提供有效的 jobId.");
        }

        VMInfo vmInfo = VMInfo.getVmInfo();
        if (vmInfo != null) {
            logger.info(vmInfo.toString());
        }

        logger.info("\n" + filterJobConfiguration(configuration) + "\n");
        logger.debug(configuration.toJSON());
        ConfigurationValidate.doValidate(configuration);
        Engine engine = new Engine();
        engine.start(configuration);

    }

    /**
     * 指定Job配置路径，ConfigParser会解析Job、Plugin、Core全部信息，并以Configuration返回
     */
    public Configuration parse(final String jobPath) {
        Configuration configuration = ConfigParser.parseJobConfig(jobPath);
        Configuration readerCfg = Configuration.newDefault();
        readerCfg.set("class", this.readerMeta.getImplClass());
        Configuration writerCfg = Configuration.newDefault();
        writerCfg.set("class", this.writerMeta.getImplClass());
        configuration.set(getPluginReaderKey(), readerCfg);
        configuration.set(getPluginWriterKey(), writerCfg);

        String readerPluginName = configuration.getString("job.content[0].reader.name");
        String writerPluginName = configuration.getString("job.content[0].writer.name");

        DataXCfgGenerator.validatePluginName(writerMeta, readerMeta, writerPluginName, readerPluginName);

        configuration.merge(Configuration.from(IOUtils.loadResourceFromClasspath(DataxExecutor.class, "core.json")),
                //ConfigParser.parseCoreConfig(CoreConstant.DATAX_CONF_PATH),
                false);


        Objects.requireNonNull(configuration.get(getPluginReaderKey()), FormatKeyPluginReader + " can not be null");
        Objects.requireNonNull(configuration.get(getPluginWriterKey()), FormatKeyPluginWriter + " can not be null");
        return configuration;
        // todo config优化，只捕获需要的plugin
    }

    private String getPluginReaderKey() {
        Objects.requireNonNull(readerMeta, "readerMeta can not be null");
        return FormatKeyPluginReader.format(new String[]{readerMeta.getName()});
    }


    private String getPluginWriterKey() {
        Objects.requireNonNull(writerMeta, "writerMeta can not be null");
        return FormatKeyPluginWriter.format(new String[]{writerMeta.getName()});
    }


    // 注意屏蔽敏感信息
    public static String filterJobConfiguration(final Configuration configuration) {
        Configuration job = configuration.getConfiguration("job");
        if (job == null) {
            throw new IllegalStateException("job relevant info can not be null,\n" + configuration.toJSON());
        }
        Configuration jobConfWithSetting = job.clone();

        Configuration jobContent = jobConfWithSetting.getConfiguration("content");

        filterSensitiveConfiguration(jobContent);

        jobConfWithSetting.set("content", jobContent);

        return jobConfWithSetting.beautify();
    }

    public static Configuration filterSensitiveConfiguration(Configuration configuration) {
        Set<String> keys = configuration.getKeys();
        for (final String key : keys) {
            boolean isSensitive = StringUtils.endsWithIgnoreCase(key, "password")
                    || StringUtils.endsWithIgnoreCase(key, "accessKey");
            if (isSensitive && configuration.get(key) instanceof String) {
                configuration.set(key, configuration.getString(key).replaceAll(".", "*"));
            }
        }
        return configuration;
    }

    private void cleanPerfTrace() {
        try {
            Field istField = PerfTrace.class.getDeclaredField("instance");
            istField.setAccessible(true);

            istField.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
