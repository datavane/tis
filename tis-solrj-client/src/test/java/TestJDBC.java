/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.apache.solr.client.solrj.io.sql.DriverImpl;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestJDBC extends TestCase {

    public void testSQL() throws Exception {

        System.out.println(  URLDecoder.decode("%7B!terms%20f%3Demp_no%7D23436%2C23446%2C23451%2C23487%2C23496%2C23517%2C23771"));

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
