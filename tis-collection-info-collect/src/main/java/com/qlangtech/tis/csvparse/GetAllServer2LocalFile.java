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
package com.qlangtech.tis.csvparse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class GetAllServer2LocalFile {

    private static final String ZK_SERVER_NODES_PATH = "/terminator/terminator-nodes";

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        ZooKeeper zookeeper = new ZooKeeper("terminatorzk048080.cm4:2181", 30000, null);
        // ZooKeeper zookeeper = new ZooKeeper("10.232.15.46:2181", 30000,
        // null);
        List<String> servers = zookeeper.getChildren(ZK_SERVER_NODES_PATH, false);
        System.out.println("all server in zk count:" + servers.size());
        PrintStream serversFile = null;
        try {
            File localfile = null;
            // new File(TerminatorConcentrateReport.getWorkDir(),
            // "ips.txt");
            serversFile = new PrintStream(new FileOutputStream(localfile, false));
            for (String server : servers) {
                if (zookeeper.getChildren(ZK_SERVER_NODES_PATH + "/" + server, false).size() > 0) {
                    serversFile.println(server);
                }
            }
            System.out.println("successfull save servers info to file :" + localfile.getAbsolutePath());
        } finally {
            try {
                serversFile.flush();
            } catch (Exception e) {
            }
            IOUtils.closeQuietly(serversFile);
        }
    }
}
