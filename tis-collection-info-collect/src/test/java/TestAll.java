import com.qlangtech.tis.collectinfo.TestCoreStatisticsReport;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author: baisui 百岁
 * @create: 2020-10-04 19:05
 **/
public class TestAll extends TestCase {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestCoreStatisticsReport.class);
        return suite;
    }
}
