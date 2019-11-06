import com.qlangtech.tis.indexbuilder.doc.TestSolrDocPack;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestAll extends TestCase {

	/**
	 * Test的总入口
	 * 
	 * @return
	 */
	public static Test suite() {

		TestSuite suite = new TestSuite();

		suite.addTestSuite(TestSolrDocPack.class);

		return suite;
	}

}
