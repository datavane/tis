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
package com.qlangtech.tis;

import static java.sql.Types.BIGINT;
import static java.sql.Types.BIT;
import static java.sql.Types.DECIMAL;
import static java.sql.Types.DOUBLE;
import static java.sql.Types.FLOAT;
import static java.sql.Types.INTEGER;
import static java.sql.Types.NUMERIC;
import static java.sql.Types.REAL;
import static java.sql.Types.SMALLINT;
import static java.sql.Types.TINYINT;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.hdfs.client.bean.HdfsRealTimeTerminatorBean;
import com.qlangtech.tis.hdfs.client.context.impl.TSearcherDumpContextImpl;
import com.qlangtech.tis.hdfs.util.Assert;

/*
 * 添加partition
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestBuildHiveTable extends TestCase {

    private final String[] collection = new String[] { "search4dfireOrderInfo", "search4totalpay", "search4pay", "search4instance" };

    String timestamp = "20151102195951";

    String user = "hadoop";

    static FileSystem fileSystem;

    static {
        HdfsRealTimeTerminatorBean dumpbean = (HdfsRealTimeTerminatorBean) TestOrderinfoDump.context.getBean("search4dfireOrderInfo");
        TSearcherDumpContextImpl dumpContext = dumpbean.getDumpContext();
        fileSystem = dumpContext.getDistributeFileSystem();
    }

    /**
     * 创建表
     *
     * @throws Exception
     */
    public void testCreateHiveTable() throws Exception {
        for (String c : collection) {
            buildTableDDL(c, timestamp, StringUtils.substringAfter(c, "search4"));
        }
    }

    public void testBindHdfs() throws Exception {
        String tableName = null;
        for (String c : collection) {
            // buildTableDDL(c, timestamp,
            // StringUtils.substringAfter(c, "search4"));
            tableName = StringUtils.substringAfter(c, "search4");
            int index = 0;
            while (fileSystem.exists(new Path("/user/" + user + "/" + c + "/all/" + index + "/" + (timestamp)))) {
                System.out.println("alter table " + tableName + " add if not exists partition(pt='" + timestamp + "',pmod='" + index + "') location 'hdfs://hadoop6:9000/user/" + user + "/" + c + "/all/" + index + "/" + timestamp + "';");
                index++;
            }
        }
    }

    /**
     * @param collection
     * @param timestamp
     * @param tableName
     * @param dumpbean
     * @throws IOException
     */
    private void buildTableDDL(String collection, String timestamp, String tableName) throws IOException {
        InputStream input = null;
        try {
            Path path = new Path("/user/" + user + "/" + collection + "/all/" + timestamp + "/cols-metadata");
            input = fileSystem.open(path);
            String content = IOUtils.toString(input);
            // System.out.println(content);
            JSONArray array = (JSONArray) JSON.parse(content);
            JSONObject o = null;
            int maxColLength = 0;
            int tmpLength = 0;
            for (int i = 0; i < array.size(); i++) {
                o = (JSONObject) array.get(i);
                tmpLength = o.getString("key").length();
                if (tmpLength > maxColLength) {
                    maxColLength = tmpLength;
                }
            }
            String colformat = "%-" + (++maxColLength) + "s";
            StringBuffer hiveSQl = new StringBuffer();
            hiveSQl.append("CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n");
            for (int i = 0; i < array.size(); i++) {
                o = (JSONObject) array.get(i);
                Assert.assertEquals(i, o.getIntValue("index"));
                hiveSQl.append("  ").append("`").append(String.format(colformat, o.getString("key") + '`')).append(" ").append(getHiveType(o.getIntValue("type")));
                // System.out.print("tp."+o.getString("key")+",");
                if ((i + 1) < array.size()) {
                    hiveSQl.append(",");
                }
                hiveSQl.append("\n");
            }
            hiveSQl.append(") COMMENT 'hive_tmp_" + tableName + "' PARTITIONED BY(pt string,pmod string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '\\t' LINES TERMINATED BY '\\n' STORED AS TEXTFILE;");
            System.out.println(hiveSQl);
            System.out.println();
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    public String getHiveType(int type) {
        switch(type) {
            case BIT:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
                return "INT";
            case BIGINT:
                return "BIGINT";
            case FLOAT:
            case REAL:
            case DOUBLE:
            case NUMERIC:
            case DECIMAL:
                return "DOUBLE";
            default:
                return "STRING";
        }
    // for (Field f : Types.class.getDeclaredFields()) {
    // System.out.println("case " + f.getName() + ":");
    // }
    }
}
