package com.qlangtech.tis.build.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class TestAppnameAwareFlumeLogstashV1Appender extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(TestAppnameAwareFlumeLogstashV1Appender.class);

	public void testLog() throws Exception {

		for (int i = 0; i < 10; i++) {
			logger.info("hello_" + i);
			System.out.println("hello_" + i);
			
			Thread.sleep(2000);
		}

		AppnameAwareFlumeLogstashV1Appender.closeAllFlume();
		System.out.println("closed....");

		for (int i = 0; i < 10; i++) {
			logger.info("xxxx_" + i);
			System.out.println("hello_" + i);

			Thread.sleep(1000);
		}

		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {

		}
	}
}
