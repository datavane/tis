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
package com.qlangtech.tis.fullbuild.jmx.impl;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import com.qlangtech.tis.fullbuild.jmx.IRemoteIncrControl;
import com.qlangtech.tis.realtime.yarn.rpc.TopicInfo;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class DefaultRemoteIncrControl implements IRemoteIncrControl {

    public static void main(String[] args) throws Exception {
        final String hostName = "10.1.7.43";
        final int portNum = 9998;
        DefaultRemoteIncrControl control = new DefaultRemoteIncrControl();
        try (JMXConnector connector = createConnector(hostName, portNum)) {
            control.pauseIncrFlow(connector, "");
        }
    }

    public static JMXConnector createConnector(String hostName, int port) throws Exception {
        // JMX客户端远程连接服务器端MBeanServer
        JMXServiceURL u = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + hostName + ":" + port + "/jmxrmi");
        JMXConnector c = JMXConnectorFactory.connect(u);
        return c;
    }

    @Override
    public void pauseIncrFlow(JMXConnector conn, String collectionName) {
        // http://blog.csdn.net/DryKillLogic/article/details/38412913
        executeJMXMethod(conn, collectionName, "pauseConsume");
    }

    /**
     * @param conn
     */
    protected Object executeJMXMethod(JMXConnector conn, String indexName, String method) {
        try {
            MBeanServerConnection mbsc = conn.getMBeanServerConnection();
            // String oname = "tis:type=increase,name=" + index;
            ObjectName mbeanName = new ObjectName("tis:type=increase,name=" + indexName);
            return mbsc.invoke(mbeanName, method, null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resumeIncrFlow(JMXConnector conn, String collectionName) {
        executeJMXMethod(conn, collectionName, "resumeConsume");
    }

    @Override
    public boolean incrLaunching(JMXConnector conn, String collectionName) {
        return false;
    }

    @Override
    public TopicInfo getIncrTopicInfo(JMXConnector conn, String collectionName) {
        return null;
    }
}
