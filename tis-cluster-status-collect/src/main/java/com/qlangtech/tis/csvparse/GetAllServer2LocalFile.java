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
package com.qlangtech.tis.csvparse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.zookeeper.ZooKeeper;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
