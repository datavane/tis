/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.flume;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.SinkRunner;
import org.apache.flume.SourceRunner;
import org.apache.flume.instrumentation.MonitorService;
import org.apache.flume.instrumentation.MonitoringType;
import org.apache.flume.lifecycle.LifecycleAware;
import org.apache.flume.lifecycle.LifecycleState;
import org.apache.flume.lifecycle.LifecycleSupervisor;
import org.apache.flume.lifecycle.LifecycleSupervisor.SupervisorPolicy;
import org.apache.flume.node.MaterializedConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Throwables;
import com.google.common.eventbus.Subscribe;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class FlumeApplication {

    private static final Logger logger = LoggerFactory.getLogger(FlumeApplication.class);

    public static final String CONF_MONITOR_CLASS = "flume.monitoring.type";

    public static final String CONF_MONITOR_PREFIX = "flume.monitoring.";

    private final List<LifecycleAware> components;

    private final LifecycleSupervisor supervisor;

    private MaterializedConfiguration materializedConfiguration;

    private MonitorService monitorServer;

    public FlumeApplication() {
        this(new ArrayList<LifecycleAware>(0));
    }

    public FlumeApplication(List<LifecycleAware> components) {
        this.components = components;
        supervisor = new LifecycleSupervisor();
    }

    public synchronized void start() {
        for (LifecycleAware component : components) {
            supervisor.supervise(component, new SupervisorPolicy.AlwaysRestartPolicy(), LifecycleState.START);
        }
    }

    @Subscribe
    public synchronized void handleConfigurationEvent(MaterializedConfiguration conf) {
        stopAllComponents();
        startAllComponents(conf);
    }

    public synchronized void stop() {
        supervisor.stop();
        if (monitorServer != null) {
            monitorServer.stop();
        }
    }

    private void stopAllComponents() {
        if (this.materializedConfiguration != null) {
            logger.info("Shutting down configuration: {}", this.materializedConfiguration);
            for (Entry<String, SourceRunner> entry : this.materializedConfiguration.getSourceRunners().entrySet()) {
                try {
                    logger.info("Stopping Source " + entry.getKey());
                    supervisor.unsupervise(entry.getValue());
                } catch (Exception e) {
                    logger.error("Error while stopping {}", entry.getValue(), e);
                }
            }
            for (Entry<String, SinkRunner> entry : this.materializedConfiguration.getSinkRunners().entrySet()) {
                try {
                    logger.info("Stopping Sink " + entry.getKey());
                    supervisor.unsupervise(entry.getValue());
                } catch (Exception e) {
                    logger.error("Error while stopping {}", entry.getValue(), e);
                }
            }
            for (Entry<String, Channel> entry : this.materializedConfiguration.getChannels().entrySet()) {
                try {
                    logger.info("Stopping Channel " + entry.getKey());
                    supervisor.unsupervise(entry.getValue());
                } catch (Exception e) {
                    logger.error("Error while stopping {}", entry.getValue(), e);
                }
            }
        }
        if (monitorServer != null) {
            monitorServer.stop();
        }
    }

    private void startAllComponents(MaterializedConfiguration materializedConfiguration) {
        logger.info("Starting new configuration:{}", materializedConfiguration);
        this.materializedConfiguration = materializedConfiguration;
        for (Entry<String, Channel> entry : materializedConfiguration.getChannels().entrySet()) {
            try {
                logger.info("Starting Channel " + entry.getKey());
                supervisor.supervise(entry.getValue(), new SupervisorPolicy.AlwaysRestartPolicy(), LifecycleState.START);
            } catch (Exception e) {
                logger.error("Error while starting {}", entry.getValue(), e);
            }
        }
        /*
		 * Wait for all channels to start.
		 */
        for (Channel ch : materializedConfiguration.getChannels().values()) {
            while (ch.getLifecycleState() != LifecycleState.START && !supervisor.isComponentInErrorState(ch)) {
                try {
                    logger.info("Waiting for channel: " + ch.getName() + " to start. Sleeping for 500 ms");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    logger.error("Interrupted while waiting for channel to start.", e);
                    Throwables.propagate(e);
                }
            }
        }
        for (Entry<String, SinkRunner> entry : materializedConfiguration.getSinkRunners().entrySet()) {
            try {
                logger.info("Starting Sink " + entry.getKey());
                supervisor.supervise(entry.getValue(), new SupervisorPolicy.AlwaysRestartPolicy(), LifecycleState.START);
            } catch (Exception e) {
                logger.error("Error while starting {}", entry.getValue(), e);
            }
        }
        for (Entry<String, SourceRunner> entry : materializedConfiguration.getSourceRunners().entrySet()) {
            try {
                logger.info("Starting Source " + entry.getKey());
                supervisor.supervise(entry.getValue(), new SupervisorPolicy.AlwaysRestartPolicy(), LifecycleState.START);
            } catch (Exception e) {
                logger.error("Error while starting {}", entry.getValue(), e);
            }
        }
        this.loadMonitoring();
    }

    @SuppressWarnings("unchecked")
    private void loadMonitoring() {
        Properties systemProps = System.getProperties();
        Set<String> keys = systemProps.stringPropertyNames();
        try {
            if (keys.contains(CONF_MONITOR_CLASS)) {
                String monitorType = systemProps.getProperty(CONF_MONITOR_CLASS);
                Class<? extends MonitorService> klass;
                try {
                    // Is it a known type?
                    klass = MonitoringType.valueOf(monitorType.toUpperCase(Locale.ENGLISH)).getMonitorClass();
                } catch (Exception e) {
                    // Not a known type, use FQCN
                    klass = (Class<? extends MonitorService>) Class.forName(monitorType);
                }
                this.monitorServer = klass.newInstance();
                Context context = new Context();
                for (String key : keys) {
                    if (key.startsWith(CONF_MONITOR_PREFIX)) {
                        context.put(key.substring(CONF_MONITOR_PREFIX.length()), systemProps.getProperty(key));
                    }
                }
                monitorServer.configure(context);
                monitorServer.start();
            }
        } catch (Exception e) {
            logger.warn("Error starting monitoring. " + "Monitoring might not be available.", e);
        }
    }

    public static void startFlume() {
        try {
            // boolean isZkConfigured = false;
            // Options options = new Options();
            // Option option = new Option("n", "name", true,
            // "the name of this agent");
            // option.setRequired(true);
            // options.addOption(option);
            // 
            // option = new Option("f", "conf-file", true,
            // "specify a config file (required if -z missing)");
            // option.setRequired(false);
            // options.addOption(option);
            // 
            // option = new Option(null, "no-reload-conf", false,
            // "do not reload config file if changed");
            // options.addOption(option);
            // 
            // // Options for Zookeeper
            // option = new Option("z", "zkConnString", true,
            // "specify the ZooKeeper connection to use (required if -f
            // missing)");
            // option.setRequired(false);
            // options.addOption(option);
            // 
            // option = new Option("p", "zkBasePath", true,
            // "specify the base path in ZooKeeper for agent configs");
            // option.setRequired(false);
            // options.addOption(option);
            // 
            // option = new Option("h", "help", false, "display help text");
            // options.addOption(option);
            // 
            // CommandLineParser parser = new GnuParser();
            // CommandLine commandLine = parser.parse(options, args);
            // 
            // if (commandLine.hasOption('h')) {
            // new HelpFormatter().printHelp("flume-ng agent", options, true);
            // return;
            // }
            // commandLine.getOptionValue('n');
            final String agentName = "tis-agent1";
            // boolean reload =
            // false;//!commandLine.hasOption("no-reload-conf");
            // if (commandLine.hasOption('z')
            // || commandLine.hasOption("zkConnString")) {
            // isZkConfigured = true;
            // }
            FlumeApplication application = null;
            // if (isZkConfigured) {
            // // get options
            // String zkConnectionStr = commandLine.getOptionValue('z');
            // String baseZkPath = commandLine.getOptionValue('p');
            // 
            // if (reload) {
            // EventBus eventBus = new EventBus(agentName + "-event-bus");
            // List<LifecycleAware> components = Lists.newArrayList();
            // PollingZooKeeperConfigurationProvider
            // zookeeperConfigurationProvider = new
            // PollingZooKeeperConfigurationProvider(
            // agentName, zkConnectionStr, baseZkPath, eventBus);
            // components.add(zookeeperConfigurationProvider);
            // application = new Application(components);
            // eventBus.register(application);
            // } else {
            // StaticZooKeeperConfigurationProvider
            // zookeeperConfigurationProvider = new
            // StaticZooKeeperConfigurationProvider(
            // agentName, zkConnectionStr, baseZkPath);
            // application = new Application();
            // application.handleConfigurationEvent(
            // zookeeperConfigurationProvider.getConfiguration());
            // }
            // } else {
            // File configurationFile = new
            // File(commandLine.getOptionValue('f'));
            /*
			 * The following is to ensure that by default the agent will fail on
			 * startup if the file does not exist.
			 */
            // if (!configurationFile.exists()) {
            // // If command line invocation, then need to fail fast
            // if (System.getProperty(
            // Constants.SYSPROP_CALLED_FROM_SERVICE) == null) {
            // String path = configurationFile.getPath();
            // try {
            // path = configurationFile.getCanonicalPath();
            // } catch (IOException ex) {
            // logger.error("Failed to read canonical path for file: "
            // + path, ex);
            // }
            // throw new ParseException(
            // "The specified configuration file does not exist: "
            // + path);
            // }
            // }
            // List<LifecycleAware> components = Lists.newArrayList();
            // if (reload) {
            // EventBus eventBus = new EventBus(agentName + "-event-bus");
            // PollingPropertiesFileConfigurationProvider
            // configurationProvider = new
            // PollingPropertiesFileConfigurationProvider(
            // agentName, configurationFile, eventBus, 30);
            // components.add(configurationProvider);
            // application = new Application(components);
            // eventBus.register(application);
            // } else {
            TisPropertiesFileConfigurationProvider configurationProvider = new TisPropertiesFileConfigurationProvider(agentName);
            application = new FlumeApplication();
            application.handleConfigurationEvent(configurationProvider.getConfiguration());
            // }
            // }
            application.start();
            final FlumeApplication appReference = application;
            Runtime.getRuntime().addShutdownHook(new Thread("agent-shutdown-hook") {

                @Override
                public void run() {
                    appReference.stop();
                }
            });
        } catch (Exception e) {
            logger.error("A fatal error occurred while running. Exception follows.", e);
        }
    }
}
