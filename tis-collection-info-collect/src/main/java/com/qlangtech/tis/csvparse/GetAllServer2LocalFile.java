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
