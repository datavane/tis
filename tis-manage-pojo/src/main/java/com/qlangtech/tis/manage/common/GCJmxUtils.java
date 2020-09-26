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
package com.qlangtech.tis.manage.common;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年2月23日
 */
public class GCJmxUtils {

    private static final MBeanServer DEFAULT_MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();

    public static long getYongGC() throws Exception {
        return getYoungGC(DEFAULT_MBEAN_SERVER);
    }

    public static long getFullGC() throws Exception {
        return getFullGC(DEFAULT_MBEAN_SERVER);
    }

    private static ObjectName youngGCObjectName = null;

    private static ObjectName fullGCObjectName = null;

    private static long getYoungGC(MBeanServerConnection mbeanServer) throws Exception {
        // ObjectName objectName;
        if (youngGCObjectName == null) {
            if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=ParNew"))) {
                youngGCObjectName = new ObjectName("java.lang:type=GarbageCollector,name=ParNew");
            } else if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=Copy"))) {
                youngGCObjectName = new ObjectName("java.lang:type=GarbageCollector,name=Copy");
            } else {
                youngGCObjectName = new ObjectName("java.lang:type=GarbageCollector,name=PS Scavenge");
            }
        }
        return (Long) mbeanServer.getAttribute(youngGCObjectName, "CollectionCount");
    }

    private static long getFullGC(MBeanServerConnection mbeanServer) throws Exception {
        if (fullGCObjectName == null) {
            if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep"))) {
                fullGCObjectName = new ObjectName("java.lang:type=GarbageCollector,name=ConcurrentMarkSweep");
            } else if (mbeanServer.isRegistered(new ObjectName("java.lang:type=GarbageCollector,name=MarkSweepCompact"))) {
                fullGCObjectName = new ObjectName("java.lang:type=GarbageCollector,name=MarkSweepCompact");
            } else {
                fullGCObjectName = new ObjectName("java.lang:type=GarbageCollector,name=PS MarkSweep");
            }
        }
        return (Long) mbeanServer.getAttribute(fullGCObjectName, "CollectionCount");
    }

    public static void main(String[] args) throws Exception {
        long curr = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            GCJmxUtils.getFullGC();
            getYongGC();
        }
        System.out.println(System.currentTimeMillis() - curr);
    }
}
