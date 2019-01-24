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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.solr.client.solrj.io.sql.DriverImpl;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestJDBC extends TestCase {

    public void testSQL() throws Exception {
        DriverImpl driver = new DriverImpl();
        // &amp;aggregationMode=map_reduce
        Connection conn = driver.connect("jdbc:solr://10.1.6.65:2181,10.1.6.67:2181,10.1.6.80:2181/tis/cloud?collection=search4totalpay");
        Statement statement = conn.createStatement();
        ResultSet result = statement.executeQuery("select entity_id,count(*) from search4totalpay where entity_id='99000842' group by entity_id");
        while (result.next()) {
            System.out.print(result.getString(1) + ",");
            System.out.println(result.getString(2));
        }
        System.out.println("successful");
        result.close();
        statement.close();
        conn.close();
    }
}
