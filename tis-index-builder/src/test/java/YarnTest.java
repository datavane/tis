
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
import org.apache.commons.cli.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.ApplicationConstants.Environment;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
// import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrException;
import org.noggit.JSONParser;
import org.noggit.ObjectBuilder;
import java.io.*;
import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.*;

/*
 * Client for submitting the SolrCloud application to YARN.
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class YarnTest {

	public static Logger log = Logger.getLogger(YarnTest.class);

	public static YarnConfiguration globalYarnConf = null;

	protected YarnConfiguration conf;

	public void run(CommandLine cli, Configuration hadoopConf) throws Exception {
		logCli(cli);
		YarnConfiguration conf = (hadoopConf != null) ? new YarnConfiguration(hadoopConf) : new YarnConfiguration();
		this.conf = conf;
		YarnClient yarnClient = YarnClient.createYarnClient();
		yarnClient.init(conf);
		yarnClient.start();
		logYarnDiagnostics(yarnClient);
		YarnClientApplication app = yarnClient.createApplication();
		String hdfsHome = "";
		String hdfsHomeOption = cli.getOptionValue("hdfs_home");
		if (hdfsHomeOption != null)
			hdfsHome = " -hdfs_home=" + hdfsHomeOption;
		File extYarnConfXml = null;
		String confRes = "";
		if (cli.hasOption("extclasspath")) {
			extYarnConfXml = new File("ext-yarn-conf.xml");
			confRes = " -conf=" + extYarnConfXml.getAbsolutePath();
		}
		String zkHost = cli.getOptionValue("zkHost", "localhost:2181");
		// Set up the container launch context for the application master
		ContainerLaunchContext amContainer = Records.newRecord(ContainerLaunchContext.class);
		amContainer.setCommands(Collections.singletonList("$JAVA_HOME/bin/java" + " -Xmx128M"
				+ " org.apache.solr.cloud.yarn.SolrMaster" + " -port=" + cli.getOptionValue("port", "8983")
				+ " -zkHost=" + zkHost + " -nodes=" + Integer.parseInt(cli.getOptionValue("nodes", "1")) + " -memory="
				+ cli.getOptionValue("memory", "512") + " -virtualCores=" + cli.getOptionValue("virtualCores", "2")
				+ " -solr=" + cli.getOptionValue("solr") + confRes + hdfsHome + " 1>"
				+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" + " 2>"
				+ ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));
		// Setup jar for ApplicationMaster
		Map<String, LocalResource> localResourcesMap = new HashMap<String, LocalResource>();
		LocalResource solrAppJarLocalResource = Records.newRecord(LocalResource.class);
		setupSolrAppJar(new Path(cli.getOptionValue("jar")), solrAppJarLocalResource);
		localResourcesMap.put("app.jar", solrAppJarLocalResource);
		amContainer.setLocalResources(localResourcesMap);
		// Setup CLASSPATH for ApplicationMaster
		Map<String, String> appMasterEnv = new HashMap<String, String>();
		setupAppMasterEnv(cli, appMasterEnv);
		amContainer.setEnvironment(appMasterEnv);
		// Set up resource type requirements for ApplicationMaster
		Resource capability = Records.newRecord(Resource.class);
		capability.setMemory(128);
		capability.setVirtualCores(1);
		// Finally, set-up ApplicationSubmissionContext for the application
		ApplicationSubmissionContext appContext = app.getApplicationSubmissionContext();
		appContext.setApplicationName(cli.getOptionValue("name", "SolrCloud"));
		appContext.setAMContainerSpec(amContainer);
		appContext.setResource(capability);
		appContext.setQueue(cli.getOptionValue("queue", "default"));
		// Submit application
		ApplicationId appId = appContext.getApplicationId();
		log.info("Submitting application " + appId);
		if (extYarnConfXml != null) {
			FileWriter writer = new FileWriter(extYarnConfXml);
			this.conf.writeXml(writer);
			writer.close();
		}
		globalYarnConf = conf;
		yarnClient.submitApplication(appContext);
		// Poll status untile we're running or failed
		ApplicationReport appReport = yarnClient.getApplicationReport(appId);
		YarnApplicationState appState = appReport.getYarnApplicationState();
		while (appState != YarnApplicationState.RUNNING && appState != YarnApplicationState.KILLED
				&& appState != YarnApplicationState.FAILED) {
			Thread.sleep(10000);
			appReport = yarnClient.getApplicationReport(appId);
			appState = appReport.getYarnApplicationState();
		}
		log.info("\n\nSolr (" + appId + ") is " + appState + "\n\n");
		if (appState == YarnApplicationState.RUNNING) {
			Thread.sleep(5000);
			try {
				pingSolrCluster(zkHost, 3);
			} catch (Exception exc) {
				log.error("Failed to ping Solr cluster due to: " + exc, exc);
			}
		}
	}

	public static void pingSolrCluster(String zkHost, int maxRetries) throws Exception {
		// CloudSolrServer cloudSolrServer = null;
		// try {
		// cloudSolrServer = new CloudSolrServer(zkHost);
		// cloudSolrServer.connect();
		//
		// Set<String> liveNodes = cloudSolrServer.getZkStateReader()
		// .getClusterState().getLiveNodes();
		// if (liveNodes.isEmpty()) {
		// throw new IllegalStateException("No live nodes found!");
		// }
		// String firstLiveNode = liveNodes.iterator().next();
		// String solrUrl = cloudSolrServer.getZkStateReader()
		// .getBaseUrlForNodeName(firstLiveNode);
		// if (!solrUrl.endsWith("/"))
		// solrUrl += "/";
		//
		// // getJson(solrUrl + "admin/info/system", maxRetries);
		// } finally {
		// if (cloudSolrServer != null) {
		// try {
		// cloudSolrServer.shutdown();
		// } catch (Exception ignore) {
		// }
		// }
		// }
	}

	/**
	 * Determine if a request to Solr failed due to a communication error, which
	 * is generally retry-able.
	 */
	public static boolean checkCommunicationError(Exception exc) {
		Throwable rootCause = SolrException.getRootCause(exc);
		boolean wasCommError = (rootCause instanceof ConnectException || rootCause instanceof ConnectTimeoutException
				|| rootCause instanceof NoHttpResponseException || rootCause instanceof SocketException);
		return wasCommError;
	}

	private static class SolrResponseHandler implements ResponseHandler<Map<String, Object>> {

		public Map<String, Object> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				Object resp = ObjectBuilder.getVal(new JSONParser(EntityUtils.toString(entity)));
				if (resp != null && resp instanceof Map) {
					return (Map<String, Object>) resp;
				} else {
					throw new ClientProtocolException("Expected JSON object in response but received " + resp);
				}
			} else {
				StatusLine statusLine = response.getStatusLine();
				throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
			}
		}
	}

	protected void setupSolrAppJar(Path jarPath, LocalResource solrMasterJar) throws IOException {
		FileStatus jarStat = FileSystem.get(conf).getFileStatus(jarPath);
		solrMasterJar.setResource(ConverterUtils.getYarnUrlFromPath(jarPath));
		solrMasterJar.setSize(jarStat.getLen());
		solrMasterJar.setTimestamp(jarStat.getModificationTime());
		solrMasterJar.setType(LocalResourceType.FILE);
		solrMasterJar.setVisibility(LocalResourceVisibility.APPLICATION);
	}

	protected void setupAppMasterEnv(CommandLine cli, Map<String, String> appMasterEnv) throws IOException {
		for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
				YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
			Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(), c.trim());
		}
		Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(), Environment.PWD.$() + File.separator + "*");
		if (cli.hasOption("extclasspath")) {
			String extclasspathArg = cli.getOptionValue("extclasspath");
			File extclasspathFile = new File(extclasspathArg);
			StringBuilder sb = new StringBuilder();
			BufferedReader reader = null;
			String line = null;
			try {
				reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(extclasspathFile), Charset.forName("UTF-8")));
				while ((line = reader.readLine()) != null)
					sb.append(line.trim()).append(":");
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
			for (String part : sb.toString().split(":")) {
				if (part.length() > 0)
					Apps.addToEnvironment(appMasterEnv, Environment.CLASSPATH.name(), part);
			}
		}
	}

	protected void logYarnDiagnostics(YarnClient yarnClient) throws IOException, YarnException {
		YarnClusterMetrics clusterMetrics = yarnClient.getYarnClusterMetrics();
		log.info("Got Cluster metric info from ASM" + ", numNodeManagers=" + clusterMetrics.getNumNodeManagers());
		List<NodeReport> clusterNodeReports = yarnClient.getNodeReports();
		log.info("Got Cluster node info from ASM");
		for (NodeReport node : clusterNodeReports) {
			log.info("Got node report from ASM for" + ", nodeId=" + node.getNodeId() + ", nodeAddress"
					+ node.getHttpAddress() + ", nodeRackName" + node.getRackName() + ", nodeNumContainers"
					+ node.getNumContainers());
		}
		QueueInfo queueInfo = yarnClient.getQueueInfo("default");
		log.info("Queue info" + ", queueName=" + queueInfo.getQueueName() + ", queueCurrentCapacity="
				+ queueInfo.getCurrentCapacity() + ", queueMaxCapacity=" + queueInfo.getMaximumCapacity()
				+ ", queueApplicationCount=" + queueInfo.getApplications().size() + ", queueChildQueueCount="
				+ queueInfo.getChildQueues().size());
		List<QueueUserACLInfo> listAclInfo = yarnClient.getQueueAclsInfo();
		for (QueueUserACLInfo aclInfo : listAclInfo) {
			for (QueueACL userAcl : aclInfo.getUserAcls()) {
				log.info("User ACL Info for Queue" + ", queueName=" + aclInfo.getQueueName() + ", userAcl="
						+ userAcl.name());
			}
		}
	}

	protected void logCli(CommandLine cli) {
		StringBuilder optsDbg = new StringBuilder();
		for (Option opt : cli.getOptions()) {
			if (opt.hasArg()) {
				optsDbg.append(opt.getOpt()).append("=").append(opt.getValue());
			} else {
				optsDbg.append(opt.getOpt());
			}
			optsDbg.append("\n");
		}
		log.info("Starting " + this.getClass().getSimpleName() + " with args: " + optsDbg);
	}

	public static void main(String[] args) throws Exception {
		main(args, null);
	}

	public static void main(String[] args, Configuration conf) throws Exception {
		if (args == null || args.length == 0 || args[0] == null || args[0].trim().length() == 0) {
			System.err.println("Invalid command-line args!");
			displayUsage(System.err);
			System.exit(1);
		}
		CommandLine cli = processCommandLineArgs(YarnTest.class.getName(), getOptions(), args);
		YarnTest c = new YarnTest();
		c.run(cli, conf);
	}

	public static void displayUsage(PrintStream out) throws Exception {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(YarnTest.class.getName(), getOptions());
	}

	private static Options getOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "Print this message");
		options.addOption("v", "verbose", false, "Generate verbose log messages");
		Option[] opts = getSolrClientOptions();
		for (int i = 0; i < opts.length; i++)
			options.addOption(opts[i]);
		return options;
	}

	public static Option[] getSolrClientOptions() {
		return new Option[] {
				OptionBuilder.withArgName("NAME").hasArg().isRequired(false)
						.withDescription("Application name; defaults to: SolrCloud").create("name"),
				OptionBuilder.withArgName("QUEUE").hasArg().isRequired(false)
						.withDescription("YARN queue; default is default").create("queue"),
				OptionBuilder.withArgName("HOST").hasArg().isRequired(false)
						.withDescription("Address of the Zookeeper ensemble; defaults to: localhost:2181")
						.create("zkHost"),
				OptionBuilder.withArgName("PORT").hasArg().isRequired(false)
						.withDescription("Solr port; default is 8983").create("port"),
				OptionBuilder.withArgName("PATH").hasArg().isRequired(false)
						.withDescription("Solr HDFS home directory; if provided, Solr will store indexes in HDFS")
						.create("hdfs_home"),
				OptionBuilder.withArgName("JAR").hasArg().isRequired(true)
						.withDescription("JAR file containing the SolrCloud YARN Application Master").create("jar"),
				OptionBuilder.withArgName("ARCHIVE").hasArg().isRequired(true)
						.withDescription("tgz file containing a Solr distribution.").create("solr"),
				OptionBuilder.withArgName("INT").hasArg().isRequired(false)
						.withDescription("Number of Solr nodes to deploy; default is 1").create("nodes"),
				OptionBuilder.withArgName("INT").hasArg().isRequired(false)
						.withDescription("Memory (mb) to allocate to each Solr node; default is 512M").create("memory"),
				OptionBuilder.withArgName("INT").hasArg().isRequired(false)
						.withDescription("Virtual cores to allocate to each Solr node; default is 1")
						.create("virtualCores"),
				OptionBuilder.withArgName("FILE").hasArg().isRequired(false)
						.withDescription("Path to file containing additional classpath entries")
						.create("extclasspath") };
	}

	/**
	 * Parses the command-line arguments passed by the user.
	 */
	public static CommandLine processCommandLineArgs(String app, Options options, String[] args) {
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
}
