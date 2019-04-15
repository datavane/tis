package com.qlangtech.tis.web.start;

import com.qlangtech.tis.web.start.JettyTISRunner.IWebAppContextSetter;

public class TisApp {

	private final String servletContext;
	private final int port;
	private final IWebAppContextSetter contextSetter;

	public TisApp(String servletContext, int port, IWebAppContextSetter contextSetter) {
		super();
		this.contextSetter = contextSetter;
		this.servletContext = servletContext;
		this.port = port;
	}

	public TisApp(String servletContext, int port) {
		this(servletContext, port, (r) -> {
		});
	}

	public void start(String[] args) throws Exception {

		if (TriggerStop.isStopCommand(args)) {
			int stopPort = Integer.parseInt(System.getProperty("STOP.PORT"));
			final String key = System.getProperty("STOP.KEY");
			TriggerStop.stop("127.0.0.1", stopPort, key, 5);
			return;
		}

		JettyTISRunner.start(servletContext, this.port, contextSetter);

	}
}
