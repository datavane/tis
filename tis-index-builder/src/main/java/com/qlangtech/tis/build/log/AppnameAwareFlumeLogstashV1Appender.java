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
package com.qlangtech.tis.build.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.gilt.logback.flume.FlumeLogstashV1Appender;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.TISCollectionUtils;
import org.apache.commons.lang.StringUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 发送日志的时候会将当前上下文MDC“app”参数发送
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年8月20日
 */
public class AppnameAwareFlumeLogstashV1Appender extends FlumeLogstashV1Appender {

    private static final Set<AppnameAwareFlumeLogstashV1Appender> flumeAppenderSet = new HashSet<>();

    private boolean closed = false;

    public static void closeAllFlume() {
        int closeCount = 0;
        for (FlumeLogstashV1Appender appender : flumeAppenderSet) {
            try {
                appender.stop();
                closeCount++;
            } catch (Throwable e) {
            }
        }
        System.out.println("closeFlumeClientCount:" + closeCount);
    }

    public AppnameAwareFlumeLogstashV1Appender() {
        super();
        super.setFlumeAgents(Config.getAssembleHost() + ":" + Config.LogFlumeAddressPORT);
        // super.setFlumeAgents("10.1.21.48:41414");
        flumeAppenderSet.add(this);
    }

    @Override
    public void stop() {
        this.closed = true;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (closed) {
            return;
        }
        super.append(eventObject);
    }

    public void setFlumeAgents(String flumeAgents) {
    // super.setFlumeAgents(flumeAgents);
    }

    @Override
    protected Map<String, String> extractHeaders(ILoggingEvent eventObject) {
        Map<String, String> result = super.extractHeaders(eventObject);
        final Map<String, String> mdc = eventObject.getMDCPropertyMap();
        String collection = StringUtils.defaultIfEmpty(mdc.get(TISCollectionUtils.KEY_COLLECTION), "unknown");
        result.put(TISCollectionUtils.KEY_COLLECTION, collection);
        return result;
    }
}
