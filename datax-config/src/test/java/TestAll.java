import com.qlangtech.tis.plugin.ds.TestDataType;
import com.qlangtech.tis.plugin.ds.TestDataTypeMeta;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2022-02-19 10:21
 **/

@RunWith(Suite.class)
@Suite.SuiteClasses({TestDataType.class, TestDataTypeMeta.class})
public class TestAll extends TestCase {
    //    public static Test suite() {
    //        TestSuite suite = new TestSuite();
    //        suite.addTestSuite(TestDataType.class);
    //        return suite;
    //    }
}
