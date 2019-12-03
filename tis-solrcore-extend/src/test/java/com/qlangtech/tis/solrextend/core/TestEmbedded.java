package com.qlangtech.tis.solrextend.core;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestEmbedded extends TestCase {

	static{
		
		System.setProperty("config.load.local", "true");
		
	}
	/**
	 * Test的总入口
	 * 
	 * @return
	 */
	public static Test suite() {

		TestSuite suite = new TestSuite();

		suite.addTest(new TestEmbeddedSolrServer("testHight"));

		return suite;
	}

}
