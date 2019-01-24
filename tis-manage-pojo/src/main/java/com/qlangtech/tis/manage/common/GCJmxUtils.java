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
package com.qlangtech.tis.manage.common;

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
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
