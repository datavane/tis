/**
 * 
 */
package com.qlangtech.tis.build.yarn;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.yarn.common.YarnConstant;


public class DeployIndexBuildJars extends Configured implements Tool {

	private static final String APP_NAME = "deployBuildDir";

	@Override
	@SuppressWarnings("all")
	public int run(String[] args) throws Exception {



		return 1;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new DeployIndexBuildJars(), args);
		System.exit(res);
	}

	private static Options getOptions(Option[] clientOptions) {
		Options options = new Options();
		options.addOption("h", "help", false, "Print this message");
		options.addOption("v", "verbose", false, "Generate verbose log messages");
		Option[] opts = clientOptions;// ;
		for (int i = 0; i < opts.length; i++) {
			options.addOption(opts[i]);
		}
		return options;
	}

	@SuppressWarnings("all")

	private static Option[] getClientOptions() {
		return new Option[] {
				OptionBuilder.withArgName(RunEnvironment.KEY_RUNTIME).hasArg().isRequired(true)
						.withDescription(RunEnvironment.KEY_RUNTIME).create(RunEnvironment.KEY_RUNTIME),
				OptionBuilder.withArgName("libjars").hasArg().isRequired(false).create("libjars"),
				OptionBuilder.withArgName(YarnConstant.PARAM_OPTION_LOCAL_JAR_DIR).hasArg().isRequired(true)
						.withDescription(YarnConstant.PARAM_OPTION_LOCAL_JAR_DIR)
						.create(YarnConstant.PARAM_OPTION_LOCAL_JAR_DIR) };
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
}
