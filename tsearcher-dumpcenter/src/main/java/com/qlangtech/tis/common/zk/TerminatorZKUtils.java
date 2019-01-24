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
package com.qlangtech.tis.common.zk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.StringTokenizer;

/*
 * Zookeeper工具类
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TerminatorZKUtils {

    /**
     * Znode的分隔符
     */
    public static final String SEPARATOR = "/";

    public static final String MUTEXLOCK_ROOT = "terminator-lock/mutex";

    public static final String LOCK_OWNER = "owner";

    /**
     * Terminator的Nodes信息在ZK上的配置的根节点
     */
    public static final String NODES_ROOT_PATH = "terminator-nodes";

    /**
     * 总控树的根路径
     */
    public static final String MAIN_TREE_ROOT_PATH = "main-tree";

    /**
     * leaderAlive目录节点
     */
    public static final String LEADER_ALIVE_CORES_PATH = "leader-alive-cores";

    /**
     * 时间信息的节点鲁锦
     */
    public static final String TIME_ROOT_PATH = "times";

    /**
     * 分布式时间任务的dump控制节点
     */
    public static final String DUMPER_CONTROLLER = "dump-controller";

    /**
     * Terminator整个配置的Root
     */
    public static final String TERMINATOR_ROOT_PATH = "terminator";

    /**
     * 本机文件系统中代表一个Folder的value的文件名，该文件对应的是含有Children的Znode的值
     */
    public static final String NODE_VALUE_FILE_NAME = "value";

    /**
     * terminator/terminator-nodes/192.168.211.22/isAlive节点存在表示该机器可用
     */
    public static final String NODE_STATUS_PATH = "isAlive";

    /**
     * 格式化znode 的path
     *
     * @param path
     * @return
     */
    public static String normalizePath(String path) {
        String _path = path;
        if (!path.startsWith(SEPARATOR)) {
            _path = SEPARATOR + path;
        }
        if (path.endsWith(SEPARATOR)) {
            _path = _path.substring(0, _path.length() - 1);
            return normalizePath(_path);
        } else {
            return _path;
        }
    }

    public static byte[] toBytes(String data) {
        if (data == null || data.trim().equals(""))
            return null;
        return data.getBytes();
    }

    public static String toString(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return null;
        return new String(bytes);
    }

    /**
     * 可序列化对象转换成byte[]
     *
     * @param obj
     * @return
     * @throws IOException
     */
    public static byte[] toBytes(Serializable obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objout = new ObjectOutputStream(out);
        objout.writeObject(obj);
        return out.toByteArray();
    }

    /**
     * byte数组的形式转换成Object对象
     *
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream objin = new ObjectInputStream(new ByteArrayInputStream(bytes));
        return objin.readObject();
    }

    /**
     * 本地文件系统的Path转换成znode的path形式
     *
     * @param fsPath
     * @return
     */
    public static String toZnodePath(String fsPath) {
        StringTokenizer tokenizer = new StringTokenizer(fsPath, File.separator);
        StringBuilder sb = new StringBuilder();
        sb.append(TERMINATOR_ROOT_PATH).append(TerminatorZKUtils.SEPARATOR);
        while (tokenizer.hasMoreTokens()) {
            sb.append(tokenizer.nextToken()).append(TerminatorZKUtils.SEPARATOR);
        }
        return sb.toString();
    }

    /**
     * znode的path转换成本地文件系统的Path
     *
     * @param znodePath
     * @return
     */
    public static String toFsPath(String znodePath) {
        StringTokenizer tokenizer = new StringTokenizer(znodePath, SEPARATOR);
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            sb.append(tokenizer.nextToken()).append(File.separator);
        }
        return sb.toString();
    }

    /**
     * 连接两个path
     *
     * @param path1
     * @param path2
     * @return
     */
    public static String contactZnodePaths(String path1, String path2) {
        if (path2.startsWith(SEPARATOR)) {
            path2 = path2.substring(1);
        }
        if (path1.endsWith(SEPARATOR)) {
            return normalizePath(path1 + path2);
        } else {
            return normalizePath(path1 + SEPARATOR + path2);
        }
    }

    /**
     * 获取机器节点 ==> /terminator/terminator-nodes/[node_ip]
     * @param nodeIp
     * @return
     */
    public static String getNodePath(String nodeIp) {
        String p = contactZnodePaths(TERMINATOR_ROOT_PATH, NODES_ROOT_PATH);
        return contactZnodePaths(p, nodeIp);
    }

    /**
     * 获取表示机器可用状态的节点  ==> /terminator/terminator-nodes/[node_ip]/isAlive
     *
     * @param nodeIp
     * @return
     */
    public static String getNodeStatusPath(String nodeIp) {
        String nodePath = getNodePath(nodeIp);
        return contactZnodePaths(nodePath, NODE_STATUS_PATH);
    }

    /**
     * 获取争相配置信息的Service节点  ==> /terminator/main-tree/[service_name]
     *
     * @param serviceName
     * @return
     */
    public static String getMainPath(String serviceName) {
        return contactZnodePaths(contactZnodePaths(TERMINATOR_ROOT_PATH, MAIN_TREE_ROOT_PATH), serviceName);
    }

    /**
     * 获取争相配置信息的Service节点  ==> /terminator/main-tree/[service_name]
     *
     * @param serviceName
     * @return
     */
    public static String getLeaderAliveCorePath(String coreName) {
        return contactZnodePaths(contactZnodePaths(TERMINATOR_ROOT_PATH, LEADER_ALIVE_CORES_PATH), coreName);
    }
}
