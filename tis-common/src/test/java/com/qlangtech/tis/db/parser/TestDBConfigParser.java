/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.db.parser;

import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.db.parser.domain.DBConfig;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年9月7日
 */
public class TestDBConfigParser extends TestCase {

    public void testMulti2() throws Exception {
        DBConfigParser parser = getDBParser("order_db_config.txt");
        DBConfig db = parser.startParser();
        assertEquals("10.1.6.99[1-32],10.1.6.101[01-32],10.1.6.102[33-64],10.1.6.103[65-96],10.1.6.104[97-128],127.0.0.3", db.getHostDesc().toString());
        final String orderName = "order";
        Assert.assertEquals("mysql", db.getDbType());
        Assert.assertEquals(orderName, db.getName());
        Map<String, List<String>> dbEnum = db.getDbEnum();
        DecimalFormat format = new DecimalFormat("00");
        List<String> dbs = dbEnum.get("10.1.6.101");
        Assert.assertNotNull(dbs);
        Assert.assertEquals(32, dbs.size());
        for (int i = 1; i <= 32; i++) {
            assertTrue("dbIndex:" + orderName + format.format(i), dbs.contains(orderName + (format.format(i))));
        }
        dbs = dbEnum.get("127.0.0.3");
        Assert.assertNotNull(dbs);
        Assert.assertEquals(1, dbs.size());
        for (String dbName : dbs) {
            Assert.assertEquals(orderName, dbName);
        }
        dbs = dbEnum.get("10.1.6.99");
        for (int i = 1; i <= 32; i++) {
            assertTrue("dbIndex:" + orderName + i, dbs.contains(orderName + (i)));
        }
    }

    public void testSingle() throws Exception {
        DBConfigParser parser = getDBParser("host_desc_single.txt");
        Assert.assertTrue(parser.parseHostDesc());
        assertEquals("127.0.0.3", parser.hostDesc.toString());
        DBConfig db = parser.dbConfigResult;
        assertEquals(1, db.getDbEnum().entrySet().size());
        // StringBuffer dbdesc = new StringBuffer();
        for (Map.Entry<String, List<String>> e : db.getDbEnum().entrySet()) {
            // dbdesc.append(e.getKey()).append(":");
            // 
            // dbdesc.append("\n");
            assertEquals("127.0.0.3", e.getKey());
            assertEquals(1, e.getValue().size());
            for (String dbName : e.getValue()) {
                Assert.assertNull(dbName);
            }
        }
    // System.out.println(dbdesc.toString());
    }

    // public void testMulti() throws Exception {
    // File f = new File("./host_desc.txt");
    // String content = FileUtils.readFileToString(f, "utf8");
    // // System.out.println(content);
    // 
    // DBTokenizer tokenizer = new DBTokenizer(content);
    // tokenizer.parse();
    // // for (Token t : ) {
    // // System.out.println(t.getContent() + " "
    // // + t.getToken());
    // // }
    // 
    // TokenBuffer buffer = tokenizer.getTokenBuffer();
    // Token t = null;
    // while ((t = buffer.nextToken()) != null) {
    // System.out.println(t.getToken() + "-" + t.getContent());
    // buffer.popToken();
    // }
    // 
    // DBConfigParser parser = new DBConfigParser(tokenizer.getTokenBuffer());
    // 
    // parser.dbConfigResult.setName("order");
    // 
    // parser.parseHostDesc();
    // 
    // DBConfig db = parser.dbConfigResult;
    // 
    // // System.out.println("type:" + db.getDbType());
    // // System.out.println("name:" + db.getName());
    // // System.out.println("getPassword:" + db.getPassword());
    // // System.out.println("getPort:" + db.getPort());
    // // System.out.println("UserName:" + db.getUserName());
    // 
    // StringBuffer dbdesc = new StringBuffer();
    // for (Map.Entry<String, List<String>> e : db.getDbEnum().entrySet()) {
    // dbdesc.append(e.getKey()).append(":");
    // for (String dbName : e.getValue()) {
    // dbdesc.append(dbName).append(",");
    // }
    // dbdesc.append("\n");
    // }
    // 
    // System.out.println(dbdesc.toString());
    // }
    private DBConfigParser getDBParser(String resourceName) throws IOException {
        try {
            DBTokenizer tokenizer = null;
            try (InputStream reader = this.getClass().getResourceAsStream(resourceName)) {
                tokenizer = new DBTokenizer(IOUtils.toString(reader, "utf8"));
            }
            tokenizer.parse();
            return new DBConfigParser(tokenizer.getTokenBuffer());
        } catch (Exception e) {
            throw new RuntimeException(resourceName, e);
        }
    }
}
