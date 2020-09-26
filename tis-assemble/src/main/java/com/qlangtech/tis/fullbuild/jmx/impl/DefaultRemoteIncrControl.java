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
package com.qlangtech.tis.fullbuild.jmx.impl;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import com.qlangtech.tis.fullbuild.jmx.IRemoteIncrControl;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2015年11月10日 下午2:30:34
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
    protected void executeJMXMethod(JMXConnector conn, String indexName, String method) {
        try {
            MBeanServerConnection mbsc = conn.getMBeanServerConnection();
            // String oname = "tis:type=increase,name=" + index;
            ObjectName mbeanName = new ObjectName("tis:type=increase,name=" + indexName);
            mbsc.invoke(mbeanName, method, null, null);
        // mbsc.invoke(mbeanName, "resumeConsume", null, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resumeIncrFlow(JMXConnector conn, String collectionName) {
        executeJMXMethod(conn, collectionName, "resumeConsume");
    }
}
