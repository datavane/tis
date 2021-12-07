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
package com.qlangtech.tis.order.dump.task;

import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.manage.common.HttpUtils.PostParam;
import com.qlangtech.tis.manage.common.PostFormStreamProcess;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年3月4日
 */
public class TestCommonTableDumpTask extends TestCase {

    private static final String DS_CONFIG = "db.member.enum=10.1.6.101\n" + "db.member.dbname=member\n" + "db.member.username=order\n" + "db.member.password=xxxxxx";

    private static final Pattern DB_HOST_ENUM = Pattern.compile("db\\.(.+?)\\.enum");

    public void testDumpTask() {
        CommonTableDumpTask.main(null);
    }

    public void test3() {
    // TisZkClient zkClient = new TisZkClient(TSearcherConfigFetcher.get().getZkAddress(), 60000);
    // Stat stat = new Stat();
    // byte[] bytes = new byte[0];
    // try {
    // bytes = zkClient.getData("/tis-lock/dumpindex/search4opinfo/dumper/nodes0000000199", null, stat, true);
    // System.out.println(new String(bytes));
    // } catch (KeeperException e) {
    // System.out.println("node not exist");
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    }

    public void test4() {
        TisZkClient zkClient = new TisZkClient(Config.getZKHost(), 60000);
        Stat stat = new Stat();
        byte[] bytes = new byte[0];
        String dbName = "scmdb";
        String tableName = "warehouse";
        String prefix = "/tis/table_dump/";
        try {
            zkClient.create(prefix + dbName + "_" + tableName, "2017".getBytes(), CreateMode.EPHEMERAL, false);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void test5() {
        String HOST = "http://10.1.4.208:8080";
        MessageFormat WORKFLOW_CONFIG_URL_FORMAT = new MessageFormat(HOST + "/config/config.ajax?" + "action={0}" + "&event_submit_{1}=true" + "&resulthandler=advance_query_result" + "{2}");
        String url = WORKFLOW_CONFIG_URL_FORMAT.format(new Object[] { "fullbuild_workflow_action", "do_add_table_dump_record", "" });
        List<PostParam> postParams = new LinkedList<>();
        postParams.add(new HttpUtils.PostParam("datasource_table_id", Integer.toString(1)));
        postParams.add(new PostParam("hive_table_name", "hivetable"));
        postParams.add(new PostParam("state", Integer.toString(1)));
        postParams.add(new PostParam("info", "{\"pt\": \"20170420201950\"}"));
        postParams.add(new PostParam("create_time", "201704201111111"));
        try {
            String result = HttpUtils.post(new URL(url), postParams, new PostFormStreamProcess<String>() {

                @Override
                public ContentType getContentType() {
                    return ContentType.JSON;
                }

                @Override
                public String p(int status, InputStream stream, Map<String, List<String>> headerFields) {
                    try {
                        return IOUtils.toString(stream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "";
                }
            });
            System.out.println(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
